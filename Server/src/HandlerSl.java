package Server.src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerSl implements Runnable {

    private Socket socket;
    private ConcurrentHashMap<TaskResult, UUID> taskResult;
    private ConcurrentHashMap<Socket, Boolean> unavailableSlaves;
    private ObjectInputStream ois;
    private ConcurrentHashMap<Socket, ObjectOutputStream> listConnectedSlave;

    public HandlerSl(Socket socket, ConcurrentHashMap<Socket, Boolean> unavailableSlaves,
            ConcurrentHashMap<TaskResult, UUID> taskResult,
            ConcurrentHashMap<Socket, ObjectOutputStream> listConnectedSlave) throws IOException {
        this.unavailableSlaves = unavailableSlaves;
        this.socket = socket;
        this.taskResult = taskResult;
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.listConnectedSlave = listConnectedSlave;
    }

    @Override
    public void run() {
        try {
            TaskResult result;
            while (true) {
                if ((result = (TaskResult) ois.readObject()) != null) {

                    unavailableSlaves.remove(socket);
                    taskResult.put(result, result.getTaskId());
                }
            }

        } catch (Exception e) {
            closeClientConnection();
        }
    }

    public void closeClientConnection() {
        try {
            if (listConnectedSlave.get(socket) != null) {
                listConnectedSlave.get(socket).close();
            }
            if (ois != null) {
                ois.close();
            }
            if (socket != null) {
                socket.close();
            }

            listConnectedSlave.remove(socket);

            System.out.println("The slave has disconnected from the server successfully ");
        } catch (IOException e) {
            System.err.println("Error closing : " + e.getMessage());
        }
    }

}
