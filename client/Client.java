package client;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Client {

    private String address;
    private int port;
    boolean isClientActive;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        this.isClientActive = true;
    }

    public void start() throws InterruptedException {

        TimeUnit.MILLISECONDS.sleep(666);

        try (Socket socket = new Socket(InetAddress.getByName(address), port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Client started!");
            while (isClientActive) {
                menu(isClientActive, input, output);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    void menu(boolean isClientActive, DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {

        System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
        String action = Main.scanner.nextLine();
        switch (action) {
            case "exit":
                exit(input, output, isClientActive);
                break;
            case "1":
                getRequest(input, output);
                break;
            case "2":
                putRequest(input, output);
                break;
            case "3":
                deleteRequest(input, output);
                break;
            default:
                System.out.println("unknown http method !");
        }
    }


    void exit(DataInputStream input, DataOutputStream output, boolean isClientActive) throws IOException {

        output.write(messageToByteArrayForSending(new Message(null, null, null, null)));
        System.out.println("The request was sent.");
        isClientActive = false;
    }

    void putRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();
        System.out.println("Enter file content:");
        String text = Main.scanner.nextLine();

        output.write(messageToByteArrayForSending(new Message(HttpMethods.PUT, null, fileName, text.getBytes(StandardCharsets.UTF_8))));
        System.out.println("The request was sent.");

        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();

        System.out.println(response.getHttpStatusCode() == HttpStatusCodes.OK ? "The response says that file was created!" :
                "The response says that creating the file was forbidden!");
    }

    void deleteRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();

        output.write(messageToByteArrayForSending(new Message(HttpMethods.DELETE, null, fileName, null)));

        System.out.println("The request was sent.");
        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();

        System.out.println(response.getHttpStatusCode() == HttpStatusCodes.OK ? "The response says that the file was successfully deleted!" :
                "The response says that the file was not found!");
    }

    void getRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();

        output.write(messageToByteArrayForSending(new Message(HttpMethods.GET, null, fileName, null)));

        System.out.println("The request was sent.");

        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();
        objectInputStream.close();

        if (response.getHttpStatusCode() == HttpStatusCodes.NOT_FOUND) {
            System.out.println("The response says that the file was not found!");
        } else if (response.getHttpStatusCode() == HttpStatusCodes.OK) {
            System.out.println("The content of the file is: " + new String(response.getData()));
        }
    }

    private byte[] messageToByteArrayForSending(Message message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArray;
    }

}
