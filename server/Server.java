package server;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class Server {
    private String address;
    private int port;
    private String fileDir;
    private boolean isServerActive;
    private FilesDatabase filesDatabase;
    private String filesDatabaseAbsolutePath;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
        String separator = File.separator;
        this.fileDir = "C:" + separator + "Users" + separator + "user" + separator + "IdeaProjects" + separator + "File Server"
                + separator + "File Server" + separator + "task" + separator + "src" + separator + "server" + separator + "data" + separator;
        this.isServerActive = true;

        this.filesDatabaseAbsolutePath = "C:" + separator + "Users" + separator + "user" + separator + "IdeaProjects" + separator + "File Server"
                + separator + "File Server" + separator + "task" + separator + "src" + separator + "server" + separator + "database.ser";
        File database = new File(filesDatabaseAbsolutePath);

        if (database.exists()) {

            this.filesDatabase = getFilesDatabaseFromHardDrive();
        } else {
            this.filesDatabase = new FilesDatabase();
        }
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
                        saveDatabaseToHardDrive();
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

        String absolutePathToFile;
        if (message.getFileName() == null) {
            int id = message.getID();              // мы точно попадаем сюда !
            absolutePathToFile = fileDir + filesDatabase.getNameByID(id);
            System.out.println(filesDatabase.getNameByID(id));
        } else {
            absolutePathToFile = fileDir + message.getFileName();
        }
        File file = new File(absolutePathToFile);

        if (file.exists()) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(absolutePathToFile));
                output.write(messageToByteArrayForSending(new Message(null, HttpStatusCodes.OK, null, fileContent)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.NOT_FOUND, output);
        }
    }

    private void putFile(Message message, DataOutputStream output) throws IOException {

        String fileName;
        String absolutePathToFile;
        if (message.getFileName().equals("")) {
            fileName = generateFileName();
        } else {
            fileName = message.getFileName();
        }
        absolutePathToFile = fileDir + fileName;

        File file = new File(absolutePathToFile);
        if (file.createNewFile()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(absolutePathToFile)) {
                fileOutputStream.write(message.getData());

                int id = filesDatabase.saveToFilesDatabase(fileName);
                sendMessageOnlyStatus(HttpStatusCodes.OK, output, id);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.FILE_ALREADY_EXISTS, output);
        }
    }

    private void deleteFile(Message message, DataOutputStream output) throws IOException {

        // @TODO он удаляет файлы по имени, но не удаляет файлы по id даже вручную !!!

        String fileName;
        String absolutePathToFile;

        if (message.getFileName() == null) {

            fileName = filesDatabase.getNameByID(message.getID());
            System.out.println(fileName);
        } else {
            fileName = message.getFileName();
        }
        absolutePathToFile = fileDir + fileName;

        File file = new File(absolutePathToFile);
        if (file.delete()) {
            sendMessageOnlyStatus(HttpStatusCodes.OK, output);
        } else {
            sendMessageOnlyStatus(HttpStatusCodes.NOT_FOUND, output);
        }
        saveDatabaseToHardDrive(); // ???
    }

    private void sendMessageOnlyStatus(HttpStatusCodes code, DataOutputStream output) throws IOException {
        Message message = new Message(null, code, null, null);
        sendMessage(message, output);
    }

    private void sendMessageOnlyStatus(HttpStatusCodes code, DataOutputStream output, int id) throws IOException {
        Message message = new Message(null, code, null, null);
        message.setID(id);
        sendMessage(message, output);
    }

    private void sendMessage(Message message, DataOutputStream output) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        byte[] messageByteArray = byteArrayOutputStream.toByteArray();
        output.write(messageByteArray);

        objectOutputStream.close();
        byteArrayOutputStream.close();
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

    private String generateFileName() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }

    private FilesDatabase getFilesDatabaseFromHardDrive() {
        try {
            FileInputStream fileInputStream = new FileInputStream(filesDatabaseAbsolutePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (FilesDatabase) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("couldn't read database from HD");
        return null;
    }

    private void saveDatabaseToHardDrive() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filesDatabaseAbsolutePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(filesDatabase);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}