import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

public class CopySearcher {
    private final static Logger logger = Logger.getLogger(CopySearcher.class.getName());
    private final int PORT = 2048;
    private final int RECEIVE_TIMEOUT = 1000;
    private final Map<String, Long> currentIpTable = new HashMap<>();
    private InetAddress group;
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;

    public CopySearcher(String multicastIpAddr){
        try{
            group = InetAddress.getByName(multicastIpAddr);
            multicastSocket = new MulticastSocket(PORT);
            datagramSocket = new DatagramSocket();
            multicastSocket.joinGroup(group);
            multicastSocket.setSoTimeout(RECEIVE_TIMEOUT);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void run(){
        logger.log(Level.INFO, "New host join group");
        while (true){
            try{
                datagramSocket.send(new DatagramPacket("Packet Hello".getBytes(), "Packet Hello".getBytes().length, group, PORT));
                long end = System.currentTimeMillis() + RECEIVE_TIMEOUT;
                while (System.currentTimeMillis() < end){
                    byte[] buffer = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try{
                        multicastSocket.receive(packet);
                        long packetPort = packet.getPort();
                        logger.log(Level.INFO, "Received packet from port:" + packetPort);
                    }
                    catch (SocketTimeoutException ex){
                        logger.log(Level.INFO, "Receive timeout exception");
                        break;
                    }
                    checkIpState(packet.getAddress().getHostAddress() + " port:" + packet.getPort());
                }
                checkCurrentIpTable();
            }
            catch (IOException ex){
                multicastSocket.close();
                datagramSocket.close();
                ex.printStackTrace();
                break;
            }
        }

        multicastSocket.close();
        datagramSocket.close();
    }

    private void checkCurrentIpTable(){
        int table_timeout = 5000;
        for(Map.Entry<String, Long> ip_plus_port : currentIpTable.entrySet()){
            if(System.currentTimeMillis() - ip_plus_port.getValue() > table_timeout){
                logger.log(Level.INFO, "Lost connection with: " + ip_plus_port.getKey());
                currentIpTable.remove(ip_plus_port.getKey());
                printCurrentIPTable();
            }
        }
    }

    private void checkIpState(String ip_plus_port){
        if(currentIpTable.put(ip_plus_port, System.currentTimeMillis()) == null){
            printCurrentIPTable();
        }
    }

    private void printCurrentIPTable(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        System.out.println("Table update:" + formatter.format(date));
        currentIpTable.forEach((key, value) -> System.out.println("IP:" + key));
        System.out.println();
    }
}
