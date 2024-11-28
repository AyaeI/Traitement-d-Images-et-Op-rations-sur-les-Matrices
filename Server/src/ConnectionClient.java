package Server.src;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionClient implements Runnable {

    private ConcurrentHashMap<String, ObjectOutputStream> clientMap;
    private int port;
    private TaskQueue taskQueue;

    public ConnectionClient(int port) {
        this.port = port;
        this.clientMap = new ConcurrentHashMap<>();
        this.taskQueue = new TaskQueue();
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("\nThe server is waiting for a client to connect.");

                while (true) {
                    socket = serverSocket.accept();
                    System.out.println("\nNew client is connected with the server successfully!");

                    String clientId = "" + socket.getPort(); // Implement this method
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    clientMap.put(clientId, oos);

                    HandlerCl clientHandler = new HandlerCl(socket, clientId, clientMap, taskQueue);
                    new Thread(clientHandler).start();

                }
            }

        } catch (IOException e) {

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    public int getPort() {
        return port;
    }

    public ConcurrentHashMap<String, ObjectOutputStream> getClientMap() {
        return clientMap;
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

}
