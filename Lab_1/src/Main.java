import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args){
        LogManager logManager = LogManager.getLogManager();
        try{
            logManager.readConfiguration(new FileInputStream("log.properties"));
        }catch (IOException ex){
            logger.log(Level.SEVERE, "Cannot get log configuration!");
        }

        if(args.length == 1){
            CopySearcher searcher = new CopySearcher(args[0]);
            logger.log(Level.INFO, "Begin of the searcher work...");
            searcher.run();
        }
        else{
            logger.log(Level.SEVERE, "ERROR! Please enter argument multicast address!");
        }
    }
}
