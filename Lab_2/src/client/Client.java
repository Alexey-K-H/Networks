package client;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private final static Logger logger = Logger.getLogger(Client.class.getName());
    private Socket clientSocket;
    private Sender sender;
    private Receiver receiver;

    public Client(String filePath, String ipAddress, int port){
        try{
            clientSocket = new Socket(ipAddress, port);
            this.sender = new Sender(filePath);
            this.receiver = new Receiver();
        }
        catch (IOException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    void start_Send_Receive(){
        try{
            sender.start();
            if(sender.isAlive()){
                sender.join();
            }
            receiver.join();
        }
        catch (InterruptedException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
        finally {
            try{
                clientSocket.close();
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    //Sender class
    private class Sender extends Thread{
        private final int SIZE = 4096;
        private String filePath;
        private DataOutputStream outputStream;

        public Sender(String filePath){
            try{
                this.filePath = filePath;
                this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }

        @Override
        public void run() {
            File file = new File(filePath);

            try(FileInputStream inputStream = new FileInputStream(file)){
                receiver.start();
                //Name length
                outputStream.writeLong(file.getName().getBytes().length);
                //Name
                outputStream.writeUTF(file.getName());
                //File size
                outputStream.writeLong(file.length());
                //Content
                byte[] buf = new byte[SIZE];
                int sentBytes;
                while (((sentBytes = inputStream.read(buf)) != -1) && !isInterrupted()){
                    outputStream.write(buf, 0, sentBytes);
                }

                outputStream.flush();
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    //Receiver class
    private class Receiver extends Thread{
        private DataInputStream inputStream;

        public Receiver(){
            try{
                this.inputStream = new DataInputStream(clientSocket.getInputStream());
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }

        @Override
        public void run() {
            try{
                String message = inputStream.readUTF();
                if(message.equals("SUCCESS")){
                    System.out.println("File was sent successfully");
                }
                else if(message.equals("FAILED")){
                    System.out.println("Failed to send file");
                    sender.interrupt();
                }
            }
            catch (IOException ex){
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }


}
