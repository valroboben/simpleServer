package server;

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
        this.fileDir =  "C:\\Users\\user\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";
        this.isServerActive = true;
    }

    public void start() {

        while (isServerActive) {

            try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
//            System.out.println("Server started!");

                try (Socket socket = server.accept();
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                    String msg = input.readUTF();

                    if (msg.startsWith("exit")) {
                        isServerActive = false;
                    } else {
                        String[] msgArr = msg.split(" ");
                        String httpMethod = msgArr[0];
                        String fileName = msgArr[1];
                        String text = msg.substring(httpMethod.length() + fileName.length() + 2);
                        switch (httpMethod) {
                            case "1":
                                getFile(fileName, output);
                                break;
                            case "2":
                                putFile(fileName, text, output);
                                break;
                            case "3":
                                deleteFile(fileName, output);
                                break;
                            case "exit":
                                isServerActive = false;
                                break;
                            default:
                                System.out.println("UNKNOWN REQUEST FROM CLIENT : " + httpMethod);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void getFile(String filename, DataOutputStream output) throws IOException {
        File file = new File(fileDir + filename);
        if (file.exists()) {
            Path path = Paths.get(fileDir + filename);
            String content = Files.readString(path);
            output.writeUTF("200 " + content);
        } else {
            output.writeUTF("404");
        }
    }

    private void putFile(String filename, String text, DataOutputStream output) throws IOException {
        File file = new File(fileDir + filename);
        if (file.createNewFile()) {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileDir + filename));
            out.write(text);
            out.close();
            output.writeUTF("200");
        } else {
            output.writeUTF("403");
        }
    }

    private void deleteFile(String fileName, DataOutputStream output) throws IOException {
        File file = new File(fileDir + fileName);
        if (file.delete()) {
            output.writeUTF("200");
        } else {
            output.writeUTF("404");
        }
    }

}