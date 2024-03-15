package client;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Client {

    private String address;
    private int port;
    boolean isClientActive;
    private String fileDir;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        this.isClientActive = true;
        String separator = File.separator;
        this.fileDir = "C:" + separator + "Users" + separator + "user" + separator + "IdeaProjects" + separator + "File Server"
                + separator + "File Server" + separator + "task" + separator + "src" + separator + "client" + separator + "data" + separator;
        System.out.println("Client started!");
    }

    public void start() throws InterruptedException {

        try {
            TimeUnit.MILLISECONDS.sleep(333);
        } catch (InterruptedException ignored) {
        }

        try (Socket socket = new Socket(InetAddress.getByName(address), port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
//             ObjectInputStream objectInputStream = new ObjectInputStream(input);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            while (isClientActive) {
                menu(isClientActive, input, output);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    void menu(boolean isClientActive, DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {

        System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file):");
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
        this.isClientActive = false;
    }

    void putRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {
        System.out.println("Enter name of the file:");
        String fileName = Main.scanner.nextLine();
        System.out.println("Enter name of the file to be saved on server:");
        String fileNameToBeSavedOnServer = Main.scanner.nextLine();

        byte[] dataFromFile = getByteArrayFromFile(fileName);

        output.write(messageToByteArrayForSending(new Message(HttpMethods.PUT, null, fileNameToBeSavedOnServer, dataFromFile)));
        System.out.println("The request was sent.");

        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();

        if (response.getHttpStatusCode() == HttpStatusCodes.OK) {
            System.out.printf("Response says that file is saved! ID = %d\n", response.getID());
        } else {
            System.out.println("The response says that creating the file was forbidden!");
        }

    }

    void deleteRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {

        Message message = new Message(HttpMethods.DELETE, null, null, null);
        System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id):");
        String choice = Main.scanner.nextLine();
        if (choice.equals("1")) { // by NAME
            System.out.println("Enter filename:");
            String fileName = Main.scanner.nextLine();
            message.setFileName(fileName);
        } else { // by ID
            System.out.println("Enter id:");
            message.setID(Integer.valueOf(Main.scanner.nextLine()));
        }

        output.write(messageToByteArrayForSending(message));
        System.out.println("The request was sent.");


        try {
            TimeUnit.MILLISECONDS.sleep(333);
        } catch (InterruptedException ignored) {
        }



        ObjectInputStream objectInputStream = new ObjectInputStream(input); // TODO тут ошибка
        Message response = (Message) objectInputStream.readObject();

        System.out.println(response.getHttpStatusCode() == HttpStatusCodes.OK ? "The response says that the file was successfully deleted!" :
                "The response says that the file was not found!");
    }

    void getRequest(DataInputStream input, DataOutputStream output) throws IOException, ClassNotFoundException {

        System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id):");
        String choice = Main.scanner.nextLine();
        Message message;
        if (choice.equals("1")) {
            System.out.println("Enter file name on server that you want to download");
            String fileName = Main.scanner.nextLine();
            message = new Message(HttpMethods.GET, null, fileName, null);
        } else {
            System.out.println("Enter id:");
            int id = Integer.parseInt(Main.scanner.nextLine());
            message = new Message(HttpMethods.GET, null, null, null);
            message.setID(id);
        }

        output.write(messageToByteArrayForSending(message));
        System.out.println("The request was sent.");

        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        Message response = (Message) objectInputStream.readObject();
//        objectInputStream.close();

        if (response.getHttpStatusCode() == HttpStatusCodes.NOT_FOUND) {
            System.out.println("The response says that this file is not found!");
        } else if (response.getHttpStatusCode() == HttpStatusCodes.OK) {
            System.out.println("The file was downloaded! Specify a name for it:");
            String fileNameToBeSavedOnClient = Main.scanner.nextLine();
            saveFileToHardDrive(response, fileNameToBeSavedOnClient);
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

    private byte[] getByteArrayFromFile(String fileName) {

        String filePath = "C:\\Users\\user\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data\\" + fileName;
        try {
            // сюда доходит
            byte[] dataFromFile = Files.readAllBytes(Paths.get(filePath));
            // сюда НЕ доходит
            return dataFromFile;
        } catch (IOException e) {
            // сюда попадает...
            e.printStackTrace();
        }

        System.out.println("polni pizdez !"); // TODO remove this line ! // ну и вот это получаем...
        return null;
    }

    private void saveFileToHardDrive(Message message, String fileNameToBeSavedOnClient) {
        String filePath = fileDir + fileNameToBeSavedOnClient;
        byte[] data = message.getData();
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.close();
            System.out.println("File saved on the hard drive!");
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

}