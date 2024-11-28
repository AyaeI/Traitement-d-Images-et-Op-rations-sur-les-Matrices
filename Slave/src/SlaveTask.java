package Slave.src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

import Server.src.FilterProcessor;
import Server.src.TaskDistributedFilter;
import Server.src.TaskDistributedMatrix;
import Server.src.TaskResult;

public class SlaveTask {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private FilterProcessor filterServer;

    public SlaveTask(Socket socket) throws IOException {
        this.socket = socket;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void connectToRMI() {
        try {
            filterServer = (FilterProcessor) Naming.lookup("//localhost/FilterServer");
            System.out.println("Successfully established connection with the RMI server.");
        } catch (Exception e) {
            System.err.println("Error connecting to the RMI server: " + e.getMessage());
        }
    }

    public void listenForTasks() {
        try {
            while (true) {
                Object taskObject = objectInputStream.readObject();
                if (taskObject instanceof TaskDistributedMatrix) {
                    handleMatrixTask((TaskDistributedMatrix) taskObject);
                } else if (taskObject instanceof TaskDistributedFilter) {
                    handleFilterTask((TaskDistributedFilter) taskObject);
                }
            }
        } catch (Exception e) {
            close();
            System.out.println("\nServer is Closed!");
        }
    }

    private void handleMatrixTask(TaskDistributedMatrix task) {
        try {
            System.out.println("\n The Slave is processing Matrix for Client ID: " + task.getClientId()
                    + ", Operation Type: " + task.getOperation() + ", Task arrangement: "
                    + task.getTaskId());

            task.viewTasks();
            switch (task.getOperation()) {
                case "addition":
                    executeAdd(task);
                    break;
                case "subtraction":
                    executeSubtract(task);
                    break;
                case "multiplication":
                    executeMultiply(task);
                    break;
                default:
                    System.err.println("Invalid operation specified: " + task.getOperation());
            }

            task.viewResult();
            TaskResult taskResult = new TaskResult(task.getClientId(), task.getTaskResult(), task.getIdtask(),
                    task.getTaskId());

            objectOutputStream.writeObject(taskResult);
            objectOutputStream.reset(); // Reset the stream after each write
        } catch (Exception e) {
            close();
            System.out.println("\nServer is Closed!");
        }
    }

    private void handleFilterTask(TaskDistributedFilter subFilterTask) {
        try {
            System.out.println("\nThe Slave is processing filter image for Client ID: " + subFilterTask.getClientID()
                    + ", Filter Type: " + subFilterTask.getFilterType() + ", Task arrangement: "
                    + subFilterTask.getIndexTask());
            int[][] result = filterServer.AddFilter(subFilterTask.getSubMatrix(), subFilterTask.getFilterType(),
                    subFilterTask.getIntensityValue());
            String fileName = "Client_" + subFilterTask.getClientID() + "\\Image_" + subFilterTask.getFilterType()
                    + "_slave_" + subFilterTask.getIndexTask();
            filterServer.saveFilteredImage(result, fileName);
            String outputPath = "C:\\Users\\hp\\Downloads\\ProjetJava\\Server\\src\\tasks"
                    + fileName;
            System.out.println("\n Filtered image saved successfully with Client ID: " + subFilterTask.getClientID());
            System.out.println("» Click on the image path: \"" + outputPath + "\"");
            TaskResult taskResult = new TaskResult(subFilterTask.getClientID(), subFilterTask.getTaskID(),
                    "IMAGE_MATRIX", result, subFilterTask.getIndexTask());
            objectOutputStream.writeObject(taskResult);
            objectOutputStream.reset();
        } catch (IOException e) {
            System.err.println("Error handling filter task: " + e.getMessage());
        }
    }

    private void executeAdd(TaskDistributedMatrix task) {
        for (int i = 0; i < task.getListRows().size(); i++) {
            List<Integer> row1 = task.getListRows().get(i);
            List<Integer> row2 = task.getListColumns().get(i);
            List<Integer> result = new ArrayList<>();
            for (int j = 0; j < row1.size(); j++) {
                result.add(row1.get(j) + row2.get(j));
            }
            task.getTaskResult().add(result);
        }
    }

    private void executeSubtract(TaskDistributedMatrix task) {
        for (int i = 0; i < task.getListRows().size(); i++) {
            List<Integer> row1 = task.getListRows().get(i);
            List<Integer> row2 = task.getListColumns().get(i);
            List<Integer> result = new ArrayList<>();
            for (int j = 0; j < row1.size(); j++) {
                result.add(row1.get(j) - row2.get(j));
            }
            task.getTaskResult().add(result);
        }
    }

    private void executeMultiply(TaskDistributedMatrix task) {
        for (List<Integer> row : task.getListRows()) {
            List<Integer> result = new ArrayList<>();
            for (List<Integer> col : task.getListColumns()) {
                result.add(multiplyRowAndColumn(row, col));
            }
            task.getTaskResult().add(result);
        }
    }

    public static int multiplyRowAndColumn(List<Integer> row, List<Integer> column) {
        int sum = 0;
        for (int i = 0; i < row.size(); i++) {
            sum += row.get(i) * column.get(i);
        }
        return sum;
    }

    public void close() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
