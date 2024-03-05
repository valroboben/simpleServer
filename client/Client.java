package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void menu(boolean isClientActive, DataInputStream input, DataOutputStream output) throws IOException {

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
        output.writeUTF("exit exit exit");
        System.out.println("The request was sent.");
        isClientActive = false;
    }

    static void putRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();
        System.out.println("Enter file content:");
        String text = Main.scanner.nextLine();
        output.writeUTF("2 " + fileName + " " + text);
        System.out.println("The request was sent.");
        String inputString = input.readUTF();
        System.out.println(inputString.startsWith("200") ? "The response says that file was created!" :
                "The response says that creating the file was forbidden!");
    }

    static void deleteRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();
        output.writeUTF("3 " + fileName + " please delete this file ))))");
        System.out.println("The request was sent.");
        String inputString = input.readUTF();
        System.out.println(inputString.startsWith("200") ? "The response says that the file was successfully deleted!" :
                "The response says that the file was not found!");
    }

    static void getRequest(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Enter filename:");
        String fileName = Main.scanner.nextLine();
        output.writeUTF("1 " + fileName + " please read this file )))");
        System.out.println("The request was sent.");
        String inputString = input.readUTF();
        if (inputString.equals("404")) {
            System.out.println("The response says that the file was not found!");
        } else if (inputString.startsWith("200")) {
            System.out.println("The content of the file is: " + inputString.substring(4));
        }
    }

}
