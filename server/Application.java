package server;


import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class Application {

    String ipAddress;
    int port;

    public Application() {
        this.ipAddress = "127.0.0.1";
        this.port = 23456;
    }

    public void run() {
        Server server = new Server(ipAddress, port);
        server.start();

    }
}


