package server;

import exceptions.TooLongNameException;
import exceptions.UpLimitFileSizeException;
import exceptions.WrongSizeReceivedDataException;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable{
    private final static Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final int SIZE = 4096;
    private final int TIMEOUT = 3000;

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClientHandler(Socket clientSocket){
        logger.log(Level.INFO, "Try to configure new handler...");
        try {
            this.clientSocket = clientSocket;
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch (IOException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
        logger.log(Level.INFO, "Created new handler for socket port:" + clientSocket.getPort());
    }

    private File createFile(String path){
        File filePath = new File("uploads");
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        try{
            filePath.mkdir();
        }
        catch (SecurityException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
        return new File(filePath + File.separator + fileName);
    }

    private void printInstantSpeed(long time, long receivedBytes){
        if(time < 1000){
            time = 1000;
        }
        System.out.println("Instant speed for[" + clientSocket.getInetAddress() + "]:" + (long)(receivedBytes / (time/(double)1000)) + "(bytes/s)");
    }

    private void printAverageSpeed(long time, long receivedBytes){
        if(time < 1000){
            time = 1000;
        }
        System.out.println("Average speed for[" + clientSocket.getInetAddress() + "]:" + (long)(receivedBytes / (time/(double)1000)) + "(bytes/s)");
    }

    private void sendErrorMessage(){
        try{
            outputStream.writeUTF("FAILED");
        }catch (IOException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void run() {
        try{
            //File name size
            long fileNameSize = inputStream.readLong();
            if(fileNameSize > SIZE){
                throw new TooLongNameException(fileNameSize);
            }
            //File name
            String fileName = inputStream.readUTF();
            //File size
            long fileSize = inputStream.readLong();
            if(fileSize > Math.pow(2, 40)){
                throw new UpLimitFileSizeException(fileSize);
            }

            byte[] buf = new byte[SIZE];
            File file = createFile(fileName);

            try(FileOutputStream outputStream = new FileOutputStream(file)){
                int bytesCurrReceived;
                long timeBorder = System.currentTimeMillis();
                long currentTime;
                long bytesCommonReceived = 0;
                long bytesInstantReceived = 0;
                long startTime = System.currentTimeMillis();

                while((bytesCurrReceived = inputStream.read(buf)) != -1){

                    if((currentTime = System.currentTimeMillis() - timeBorder) > TIMEOUT){
                        printAverageSpeed(System.currentTimeMillis() - startTime, bytesCommonReceived);
                        printInstantSpeed(currentTime, bytesInstantReceived);

                        timeBorder = System.currentTimeMillis();
                        bytesInstantReceived = 0;
                    }

                    bytesInstantReceived += bytesCurrReceived;
                    bytesCommonReceived += bytesCurrReceived;

                    outputStream.write(buf, 0, bytesCurrReceived);

                    if(bytesCommonReceived == fileSize){
                        break;
                    }

                    if(bytesCommonReceived > fileSize){
                        throw new WrongSizeReceivedDataException("ERROR! Received bytes != file size");
                    }
                }

                currentTime = System.currentTimeMillis() - timeBorder;

                printAverageSpeed(System.currentTimeMillis() - startTime, bytesCommonReceived);
                printInstantSpeed(currentTime, bytesInstantReceived);

                outputStream.flush();
            }
            outputStream.writeUTF("SUCCESS");
        }
        catch (WrongSizeReceivedDataException | IOException | TooLongNameException | UpLimitFileSizeException ex){
            logger.log(Level.SEVERE, ex.getMessage());
            sendErrorMessage();
        }
    }
}
