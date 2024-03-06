package server;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Server {
    private String address;
    private int port;
    String fileDir;
    private boolean isServerActive;

    public  Server(String address, int port) {
        this.address = address;
        this.port = port;
        String separator = File.separator;
        this.fileDir =  "C:"+separator+"Users"+separator+"user"+separator+"IdeaProjects"+separator+"File Server"
                +separator+"File Server"+separator+"task"+separator+"src"+separator+"server"+separator+"data"+separator;
        this.isServerActive = true;
    }

    public void start() {

        while (isServerActive) {

            try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
//            System.out.println("Server started!");


                try (Socket socket = server.accept();
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     ObjectInputStream objectInputStream = new ObjectInputStream(input);
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                    Message message = (Message) objectInputStream.readObject();

                    if (message.getHttpMethod() == null) {
                        isServerActive = false;
                    } else {
                        HttpMethods httpMethod = message.getHttpMethod();
                        switch (httpMethod) {
                            case GET:
                                getFile(message, output);
                                break;
                            case PUT:
                                putFile(message, output);
                                break;
                            case DELETE:
                                deleteFile(message, output);
                                break;
                            default:
                                System.out.println("UNKNOWN REQUEST FROM CLIENT : " + httpMethod);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void getFile(Message message, DataOutputStream output) throws IOException {
        File file = new File(fileDir + message.getFileName());
        if (file.exists()) {
//            Path path = Paths.get(fileDir + message.getFileName());

            String filePath = fileDir + message.getFileName();
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

                Message response = new Message(null, HttpStatusCodes.OK, message.getFileName(), fileContent);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
                byte[] responseByteArray = byteArrayOutputStream.toByteArray();

                output.write(responseByteArray);

                objectOutputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.NOT_FOUND, output);
        }
    }

    private void putFile(Message message, DataOutputStream output) throws IOException {
        File file = new File(fileDir + message.getFileName());
        if (file.createNewFile()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileDir + message.getFileName())) {
                fileOutputStream.write(message.getData());
                sendMessageOnlyStatus(HttpStatusCodes.OK, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.FILE_ALREADY_EXISTS, output);
        }
    }

    private void deleteFile(Message message, DataOutputStream output) throws IOException {
        File file = new File(fileDir + message.getFileName());
        if (file.delete()) {
            sendMessageOnlyStatus(HttpStatusCodes.OK, output);
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.NOT_FOUND, output);
        }
    }

    private void sendMessageOnlyStatus(HttpStatusCodes code, DataOutputStream output) throws IOException {
        Message message = new Message(null, code, null, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        byte[] messageByteArray = byteArrayOutputStream.toByteArray();
        output.write(messageByteArray);

        objectOutputStream.close();
        byteArrayOutputStream.close();
    }

}