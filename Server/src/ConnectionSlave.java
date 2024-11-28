package Server.src;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionSlave implements Runnable {
    private ConcurrentHashMap<Socket, ObjectOutputStream> listConnectedSlave;

    private ConcurrentHashMap<Socket, Boolean> unavailableSlaves;
    private ConcurrentHashMap<TaskResult, UUID> taskResult;

    private int port;

    public ConnectionSlave(int port) {
        this.port = port;
        this.listConnectedSlave = new ConcurrentHashMap<>();
        this.unavailableSlaves = new ConcurrentHashMap<>();
        this.taskResult = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("\nThe server is waiting for a slave to connect.");

                while (true) {
                    socket = serverSocket.accept();
                    System.out.println("\nNew slave is connected with the server successfully!");

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    listConnectedSlave.put(socket, oos);
                    HandlerSl slavetHandler = new HandlerSl(socket, unavailableSlaves, taskResult,
                            listConnectedSlave);
                    new Thread(slavetHandler).start();
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

    public ConcurrentHashMap<TaskResult, UUID> getTaskResult() {
        return taskResult;
    }

    public ConcurrentHashMap<Socket, ObjectOutputStream> getListSlaveConnected() {
        return listConnectedSlave;
    }

    public ConcurrentHashMap<Socket, Boolean> getUnavailableSlaves() {
        return unavailableSlaves;
    }

}
