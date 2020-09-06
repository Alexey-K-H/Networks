import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CopySearcher {
    private final static Logger logger = Logger.getLogger(CopySearcher.class.getName());
    private final int port = 2048;
    private final int receive_timeout = 1000;
    private final Map<String, Long> currentIpTable = new HashMap<>();
    private InetAddress group;
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;


    public CopySearcher(String multicastIpAddr){
        try{
            group = InetAddress.getByName(multicastIpAddr);
            multicastSocket = new MulticastSocket(port);
            datagramSocket = new DatagramSocket();
            multicastSocket.joinGroup(group);
            multicastSocket.setSoTimeout(receive_timeout);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void run(){
        logger.log(Level.INFO, "New host join group");
        while (true){
            try{
                datagramSocket.send(new DatagramPacket("Packet Hello".getBytes(), "Packet Hello".getBytes().length, group, port));
                long end = System.currentTimeMillis() + receive_timeout;
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
                ex.printStackTrace();
                break;
            }
        }
    }

    private void checkCurrentIpTable(){
        for(Iterator<Map.Entry<String, Long>> iterator = currentIpTable.entrySet().iterator(); iterator.hasNext();){
            Map.Entry<String, Long> entry = iterator.next();
            int table_timeout = 5000;
            if(System.currentTimeMillis() - entry.getValue() > table_timeout){
                logger.log(Level.INFO, "Lost connection with: " + entry.getKey());
                iterator.remove();
                printCurrentIPTable();
            }
        }
    }

    private void checkIpState(String ip_plus_port){
        if(!currentIpTable.containsKey(ip_plus_port)){
            currentIpTable.put(ip_plus_port, System.currentTimeMillis());
            printCurrentIPTable();
        }
        else {
            currentIpTable.put(ip_plus_port, System.currentTimeMillis());
        }
    }

    private void printCurrentIPTable(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        System.out.println("Table update:" + formatter.format(date));
        for(Map.Entry<String, Long> entry : currentIpTable.entrySet()){
            System.out.println("IP:" + entry.getKey());
        }
        System.out.println();
    }
}
