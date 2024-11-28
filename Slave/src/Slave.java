package Slave.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

public class Slave {

    public static void main(String[] args) throws IOException {
        String serverHost;
        int port;

        try (BufferedReader br = new BufferedReader(
                new FileReader("E:\\Master\\M1\\ProjetJavaFinal\\ProjetJava\\Slave\\src\\config.txt"))) {
            serverHost = br.readLine();
            port = Integer.parseInt(br.readLine());
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        Socket socket = null;
        try {
            socket = new Socket(serverHost, port);

            System.out.println("\n You are successfully connected to the server");
            SlaveTask taskProcessor = new SlaveTask(socket);
            taskProcessor.connectToRMI();
            taskProcessor.listenForTasks();
        } catch (Exception e) {

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }

}
