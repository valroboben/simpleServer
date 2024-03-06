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


    static void menu(boolean isClientActive, DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {

//        if (!isClientActive) {
//            System.exit(0); // МОЖЕТ ВОТ НЕ НУЖНО ЭТОГО ?????!!!
//        }

        System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
        String httpMethod = Main.scanner.nextLine();

        switch (httpMethod) {
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


    static void exit(DataInputStream input, DataOutputStream output, boolean isClientActive) throws IOException {

        Message request = new Message(null, null, null, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        byte [] messageByteArray = byteArrayOutputStream.toByteArray();

        output.write(messageByteArray);

        objectOutputStream.close();     // закрываем в обратном порядке
        byteArrayOutputStream.close();

        System.out.println("The request was sent.");
        isClientActive = false;
    }

    static void putRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();
        System.out.println("Enter file content:");
        String text = Main.scanner.nextLine();

        Message request = new Message(HttpMethods.PUT, null, fileName, text.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        byte[] requestByteArray = byteArrayOutputStream.toByteArray();

        output.write(requestByteArray);

        objectOutputStream.close();
        byteArrayOutputStream.close();
        System.out.println("The request was sent.");

        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();

        System.out.println(response.getHttpStatusCode() == HttpStatusCodes.OK ? "The response says that file was created!" :
                "The response says that creating the file was forbidden!");
    }

    static void deleteRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();

        Message request = new Message(HttpMethods.DELETE, null, fileName, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        byte [] messageByteArray = byteArrayOutputStream.toByteArray();

        output.write(messageByteArray); //         output.writeUTF("3 " + fileName + " please delete this file ))))");

        objectOutputStream.close();
        byteArrayOutputStream.close();

        System.out.println("The request was sent.");
        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();

        System.out.println(response.getHttpStatusCode() == HttpStatusCodes.OK ? "The response says that the file was successfully deleted!" :
                "The response says that the file was not found!");
    }

    static void getRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();

        Message request = new Message(HttpMethods.GET, null, fileName, null);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        byte[] requestByteArray = byteArrayOutputStream.toByteArray();

        output.write(requestByteArray);

        objectOutputStream.close();
        byteArrayOutputStream.close();

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


    /* TODO: заменить Message request на отдельный метод, который возвращает сразу byte[]
        то есть byte[] toBeSent = getByteArrayOfMessage(message); */

    // TODO: получение ответа сделать через отдельный метод "showResponse()" ?????

}
