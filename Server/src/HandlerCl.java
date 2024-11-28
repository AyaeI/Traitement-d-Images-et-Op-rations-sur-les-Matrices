package Server.src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerCl implements Runnable {

    private Socket socket;
    private String IDc;
    private ObjectInputStream ois;
    private ConcurrentHashMap<String, ObjectOutputStream> clientMap;
    private TaskQueue taskStack;

    public HandlerCl(Socket socket, String IDc, ConcurrentHashMap<String, ObjectOutputStream> clientMap)
            throws IOException {
        this.socket = socket;
        this.IDc = IDc;
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.clientMap = clientMap;
        this.taskStack = new TaskQueue();
    }

    public HandlerCl(Socket socket, String clientId, ConcurrentHashMap<String, ObjectOutputStream> clientMap,
            TaskQueue taskQueue)
            throws IOException {
        this.socket = socket;
        this.IDc = clientId;
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.clientMap = clientMap;
        this.taskStack = taskQueue;
    }

    @Override
    public void run() {
        try {
            Object task;
            // Read the task from the client
            while ((task = ois.readObject()) != null) {
                // Add the task to the task stack
                if (task instanceof TaskMatrix) {
                    TaskMatrix matrixTask = (TaskMatrix) task;
                    OrderingClientTask clientTask = new OrderingClientTask(matrixTask, IDc);
                    taskStack.enqueueTask(clientTask);
                    System.out.println("\nThe Matrix task added to the queue with the client ID.");

                } else if (task instanceof TaskFilter) {
                    TaskFilter filterTask = (TaskFilter) task;
                    OrderingClientTask clientTask = new OrderingClientTask(filterTask, IDc);
                    taskStack.enqueueTask(clientTask);
                    System.out.println("\nThe Filter task added to the queue with the client ID.");

                }
            }

            closeClientConnection(IDc);
        } catch (IOException | ClassNotFoundException e) {
            // Handle exceptions
            closeClientConnection(IDc);
        }
    }

    public void closeClientConnection(String IDc) {
        try {
            if (clientMap.get(IDc) != null) {
                clientMap.get(IDc).close();
            }
            if (ois != null) {
                ois.close();
            }
            if (socket != null) {
                socket.close();
            }
            clientMap.remove(IDc);
            System.out.println("\nThe Client " + IDc + " is disconnected from the server successfully");
        } catch (IOException e) {
            System.err.println("Error closing : " + e.getMessage());
        }
    }

}
