package Client.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static String serverHost;
    private static int serverPort;

    public static void main(String[] args) throws InterruptedException {

        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader("E:\\Master\\M1\\ProjetJavaFinal\\ProjetJava\\Client\\src\\config.txtgit init"));
            serverHost = reader.readLine();
            serverPort = Integer.parseInt(reader.readLine());
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        Socket socket = null;

        try {
            socket = new Socket(serverHost, serverPort);
            Matrix matrixChoice = new Matrix(socket);

            matrixChoice.MenuTask();

        } catch (IOException | ClassNotFoundException e) {

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void handleImageFiltering() {
        try {
            Socket socket = new Socket(serverHost, serverPort);
            @SuppressWarnings("unused")
            Filter filter = new Filter(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}