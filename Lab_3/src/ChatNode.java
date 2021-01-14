import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import messages.*;
import serialization.Serializer;

public class ChatNode {
    private final int TABLE_TIMEOUT = 5000;
    private final int ALIVE_DELAY = 100;
    private final int NEIGHBOUR_DELAY = 1000;
    private final int DELIVERY_DELAY = 10000;

    private DatagramSocket nodeSocket;

    private String nodeName;
    private int lossPercent;
    private int port;
    private int neighbourPort;
    private String neighbourIp;

    private Neighbour ownAlternateNeighbour;

    private final List<Neighbour> neighboursList = new CopyOnWriteArrayList<>();
    private final List<String> confirmedMessages = new ArrayList<>();
    private final Map<String, DatagramPacket> sentMessages = new HashMap<>();
    private final Map<String, Long> receivedMessages = new HashMap<>();

    private DataSender dataSender = new DataSender();
    private DataReceiver dataReceiver = new DataReceiver();

    public ChatNode(String nodeName, int lossPercent, int port) {
        this.nodeName = nodeName;
        this.lossPercent = lossPercent;
        this.port = port;
        this.neighbourPort = -1;
        this.neighbourIp = null;
    }

    public ChatNode(String nodeName, int lossPercent, int port, String neighbourIp, int neighbourPort) {
        this.nodeName = nodeName;
        this.lossPercent = lossPercent;
        this.port = port;
        this.neighbourPort = neighbourPort;
        this.neighbourIp = neighbourIp;
    }

    public void startChatting() {
        try {
            nodeSocket = new DatagramSocket(this.port);

            if (neighbourPort != -1 && neighbourIp != null) {
                neighboursList.add(new Neighbour(InetAddress.getByName(neighbourIp), neighbourPort, System.currentTimeMillis()));
                dataSender.sendConnectMessage(UUID.randomUUID().toString(), InetAddress.getByName(neighbourIp), neighbourPort);

                dataSender.sendAlternateNeighbour(
                        UUID.randomUUID().toString(),
                        InetAddress.getByName(neighbourIp), neighbourPort,
                        InetAddress.getByName(neighbourIp), neighbourPort
                );
                ownAlternateNeighbour = new Neighbour(InetAddress.getByName(neighbourIp), neighbourPort, System.currentTimeMillis());
            }

            startSendKeepAliveMessages();
            startCheckReceivedMessagesAge();
            startCheckNeighboursAliveStatus();

            dataSender.start();
            dataReceiver.start();

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if (scanner.hasNextLine()) {
                        String text = scanner.nextLine();
                        dataSender.sendTextToAllNeighbours(text);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(nodeSocket != null){
                nodeSocket.close();
            }
        }

    }

    private void startSendKeepAliveMessages() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    dataSender.sendAliveToAllNeighbours();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(ALIVE_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startCheckReceivedMessagesAge() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(DELIVERY_DELAY);
                    synchronized (receivedMessages) {
                        receivedMessages.entrySet().removeIf(msg -> System.currentTimeMillis() - msg.getValue() > DELIVERY_DELAY * 6);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startCheckNeighboursAliveStatus() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    checkNeighboursStatus();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(NEIGHBOUR_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    private void checkNeighboursStatus() throws IOException {
        boolean neighbourWasChanged = false;

        for (Neighbour neighbour : neighboursList) {
            if (System.currentTimeMillis() - neighbour.getLastTime() > TABLE_TIMEOUT) {

                if (neighbour.equals(ownAlternateNeighbour)) {
                    neighbourWasChanged = true;
                }

                Neighbour newNeighbour = neighbour.getAlternateNeighbour();

                if (newNeighbour != null) {
                    newNeighbour.setLastTime(System.currentTimeMillis());
                    neighboursList.add(newNeighbour);
                    dataSender.sendConnectMessage(UUID.randomUUID().toString(), newNeighbour.getIp(), newNeighbour.getPort());
                }

                neighboursList.remove(neighbour);
            }
        }

        if (neighbourWasChanged) {
            if (neighboursList.size() > 0) {
                ownAlternateNeighbour = neighboursList.get(0);
                dataSender.sendAlternateToAllNeighbours(ownAlternateNeighbour.getIp(), ownAlternateNeighbour.getPort());
            } else {
                ownAlternateNeighbour = null;
            }
        }

    }

    private class DataReceiver extends Thread {
        private final int PACKET_SIZE = 1024;

        @Override
        public void run() {
            try {
                Random rnd = new Random(System.currentTimeMillis());

                while (!isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

                    nodeSocket.receive(packet);

                    if (rnd.nextInt(100) > lossPercent) {
                        Message message = Serializer.deserialize(packet.getData());

                        switch (message.getType()) {
                            case CONFIRM:
                                checkConfirmDeliveryStatus(message);
                                break;

                            case TEXT:
                                checkTextDeliveryStatus(message, packet.getAddress(), packet.getPort());
                                dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                                break;

                            case CONNECT:
                                checkConnectionDeliveryStatus(message, packet.getAddress(), packet.getPort());
                                shareOwnAlternateNeighbour(packet.getAddress(), packet.getPort());
                                dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                                break;

                            case ALIVE:
                                refreshNeighbourLastAliveTime(packet.getAddress(), packet.getPort());
                                dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                                break;

                            case ALTERNATE:
                                AlternateMessage alternateMessage = (AlternateMessage) message;
                                setAlternateNeighbour(
                                        packet.getAddress(), packet.getPort(),
                                        alternateMessage.getIp(), alternateMessage.getPort()
                                );
                                dataSender.sendConfirmMessage(message.getMessageID(), packet.getAddress(), packet.getPort());
                                break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void shareOwnAlternateNeighbour(InetAddress inetAddress, int port) throws IOException {
            if (null == ownAlternateNeighbour) {
                if (neighboursList.size() > 0) {
                    ownAlternateNeighbour = neighboursList.get(0);
                    dataSender.sendAlternateToAllNeighbours(ownAlternateNeighbour.getIp(), ownAlternateNeighbour.getPort());
                }
            } else {
                if (!ownAlternateNeighbour.equals(new Neighbour(inetAddress, port))) {
                    dataSender.sendAlternateNeighbour(
                            UUID.randomUUID().toString(),
                            ownAlternateNeighbour.getIp(), ownAlternateNeighbour.getPort(),
                            inetAddress, port
                    );
                }
            }
        }

        private void checkConfirmDeliveryStatus(Message message) {
            synchronized (confirmedMessages) {
                if (!confirmedMessages.contains(message.getMessageID())) {
                    confirmedMessages.add(message.getMessageID());
                }
            }
        }

        private void checkTextDeliveryStatus(Message message, InetAddress inetAddress, int port) throws IOException {
            synchronized (receivedMessages) {
                if (!receivedMessages.containsKey(message.getMessageID())) {
                    System.out.println(((TextMessage) message).getName() + ":" + ((TextMessage) message).getContent());
                    receivedMessages.put(message.getMessageID(), System.currentTimeMillis());
                    dataSender.sendTextNext((TextMessage) message, inetAddress, port);
                }
            }
        }


        private void checkConnectionDeliveryStatus(Message message, InetAddress inetAddress, int port) {
            synchronized (receivedMessages) {
                if (!receivedMessages.containsKey(message.getMessageID())) {
                    Neighbour neighbour = new Neighbour(inetAddress, port, System.currentTimeMillis());
                    neighboursList.add(neighbour);
                    receivedMessages.put(message.getMessageID(), System.currentTimeMillis());
                }
            }
        }

        private void setAlternateNeighbour(InetAddress senderIp, int senderPort, InetAddress alternateIp, int alternatePort) {
            for (Neighbour neighbour : neighboursList) {
                if (neighbour.getPort() == senderPort && neighbour.getIp().equals(senderIp)) {
                    neighbour.setAlternateNeighbour(new Neighbour(alternateIp, alternatePort, System.currentTimeMillis()));
                }
            }

        }

        private void refreshNeighbourLastAliveTime(InetAddress inetAddress, int port) {
            for (Neighbour neighbour : neighboursList) {
                if (neighbour.getIp().equals(inetAddress) && port == neighbour.getPort()) {
                    neighbour.setLastTime(System.currentTimeMillis());
                    break;
                }
            }
        }
    }

    private class DataSender extends Thread {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (sentMessages) {
                        sentMessages.entrySet().removeIf(msg -> checkDeliveryStatus(msg.getKey()));
                        for (Iterator<Map.Entry<String, DatagramPacket>> iterator = sentMessages.entrySet().iterator(); iterator.hasNext(); ) {
                            Map.Entry<String, DatagramPacket> entry = iterator.next();
                            if (!neighboursList.contains(new Neighbour(entry.getValue().getAddress(), entry.getValue().getPort()))) {
                                iterator.remove();
                            } else {
                                nodeSocket.send(entry.getValue());
                            }
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

        }

        private boolean checkDeliveryStatus(String messageID) {
            synchronized (confirmedMessages) {
                boolean status = confirmedMessages.contains(messageID);

                if (status) {
                    confirmedMessages.remove(messageID);
                }
                return status;
            }
        }

        private void sendMessage(String messageID, DatagramPacket datagramPacket) {
            synchronized (sentMessages) {
                sentMessages.put(messageID, datagramPacket);
            }
        }

        private void sendTextMessage(String messageID, String content, int port, InetAddress ip) throws IOException {
            byte[] confirmAnswer = Serializer.serialize(new TextMessage(content, messageID,  nodeName));
            sendMessage(messageID, new DatagramPacket(confirmAnswer, confirmAnswer.length, ip, port));
        }

        private void sendTextToAllNeighbours(String content) throws IOException {
            for (Neighbour neighbour : neighboursList) {
                sendTextMessage(UUID.randomUUID().toString(), content, neighbour.getPort(), neighbour.getIp());
            }
        }

        private void sendAliveStatusMessage(String messageID, InetAddress ip, int port) throws IOException {
            byte[] aliveMsg = Serializer.serialize(new AliveMessage(messageID));
            sendMessage(messageID, new DatagramPacket(aliveMsg, aliveMsg.length, ip, port));
        }

        private void sendAliveToAllNeighbours() throws IOException {
            for (Neighbour neighbour : neighboursList) {
                sendAliveStatusMessage(UUID.randomUUID().toString(), neighbour.getIp(), neighbour.getPort());
            }
        }

        private void sendAlternateToAllNeighbours(InetAddress ip, int port) throws IOException {
            for (Neighbour neighbour : neighboursList) {
                if (!ownAlternateNeighbour.equals(neighbour)) {
                    sendAlternateNeighbour(UUID.randomUUID().toString(), ip, port, neighbour.getIp(), neighbour.getPort());
                }
            }
        }

        private void sendAlternateNeighbour(String messageID, InetAddress alterIp, int alterPort, InetAddress distIp, int distPort) throws IOException {
            byte[] alternateMsg = Serializer.serialize(new AlternateMessage(messageID, alterIp, alterPort));
            sendMessage(messageID, new DatagramPacket(alternateMsg, alternateMsg.length, distIp, distPort));
        }

        private void sendConnectMessage(String messageID, InetAddress ip, int port) throws IOException {
            byte[] connectMsg = Serializer.serialize(new ConnectMessage(messageID));
            sendMessage(messageID, new DatagramPacket(connectMsg, connectMsg.length, ip, port));
        }

        private void sendConfirmMessage(String messageID, InetAddress ip, int port) throws IOException {
            byte[] confirmAnswer = Serializer.serialize(new ConfirmMessage(messageID));
            nodeSocket.send(new DatagramPacket(confirmAnswer, confirmAnswer.length, ip, port));
        }

        private void sendTextNext(TextMessage oldTextMsg, InetAddress neighbourSenderIp, int neighbourSenderPort) throws IOException {
            for (Neighbour neighbour : neighboursList) {
                if (neighbour.getPort() != neighbourSenderPort && neighbour.getIp() != neighbourSenderIp) {
                    String messageID = UUID.randomUUID().toString();
                    byte[] newTextMsg = Serializer.serialize(new TextMessage(oldTextMsg.getContent(), messageID, oldTextMsg.getName()));

                    DatagramPacket packet = new DatagramPacket(
                            newTextMsg, newTextMsg.length,
                            neighbour.getIp(), neighbour.getPort()
                    );

                    sendMessage(messageID, packet);
                }
            }
        }
    }
}
