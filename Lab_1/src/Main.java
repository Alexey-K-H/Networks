import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class.getName());

    public static long ipToLong(InetAddress ip){
        byte[] octets = ip.getAddress();
        long result = 0;
        for(byte octet : octets){
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isValidRange(String ipStart, String ipEnd, String ipToCheck) {
        try {
            long ipLo = ipToLong(InetAddress.getByName(ipStart));
            long ipHi = ipToLong(InetAddress.getByName(ipEnd));
            long ipToTest = ipToLong(InetAddress.getByName(ipToCheck));
            return (ipToTest >= ipLo && ipToTest <= ipHi);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args){
        LogManager logManager = LogManager.getLogManager();
        try{
            logManager.readConfiguration(new FileInputStream("log.properties"));
        }catch (IOException ex){
            logger.log(Level.SEVERE, "Cannot get log configuration!");
        }

        if(args.length == 1){
            CopySearcher searcher = new CopySearcher(args[0]);
            if(isValidRange("224.0.0.0", "239.255.255.255", args[0])){
                logger.log(Level.INFO, "Begin of the searcher work...");
                searcher.run();
            }
            else {
                logger.log(Level.SEVERE, "Not a valid address!");
            }

        }
        else{
            logger.log(Level.SEVERE, "ERROR! Please enter argument multicast address!");
        }
    }
}
