package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final static Logger logger = Logger.getLogger(Server.class.getName());
    private ServerSocket socket;

    public Server(int port){
        try{
            this.socket = new ServerSocket(port);
        }
        catch (IOException ex){
            logger.log(Level.SEVERE, "FAILED to create ServerSocket! " + ex.getMessage());
        }
    }

    public void startServer(){
        try{
            while (true){
                ClientHandler handler = new ClientHandler(socket.accept());
                new Thread(handler).start();
            }
        }
        catch (IOException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
        finally {
            try{
                socket.close();
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }
}
