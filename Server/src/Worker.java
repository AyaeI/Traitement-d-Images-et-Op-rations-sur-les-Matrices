package Server.src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

public class Worker implements Runnable {

    private ConcurrentHashMap<Socket, ObjectOutputStream> slavesList;
    private TaskQueue taskQueue;
    private ConcurrentHashMap<TaskResult, UUID> taskResult;
    private ConcurrentHashMap<String, ObjectOutputStream> clientMap;
    private ConcurrentHashMap<Socket, Boolean> unavailableSlaves;

    public Worker(ConcurrentHashMap<Socket, ObjectOutputStream> slavesList,
            ConcurrentHashMap<Socket, Boolean> unavailableSlaves,
            ConcurrentHashMap<TaskResult, UUID> taskResult,
            ConcurrentHashMap<String, ObjectOutputStream> clientMap) {
        this.slavesList = slavesList;
        this.unavailableSlaves = unavailableSlaves;
        this.taskResult = taskResult;
        this.clientMap = clientMap;
        this.taskQueue = new TaskQueue();
    }

    public Worker(TaskQueue taskQueue,
            ConcurrentHashMap<Socket, ObjectOutputStream> slavesList,
            ConcurrentHashMap<Socket, Boolean> unavailableSlaves,
            ConcurrentHashMap<TaskResult, UUID> taskResult,
            ConcurrentHashMap<String, ObjectOutputStream> clientMap) {
        this.taskQueue = taskQueue;
        this.slavesList = slavesList;
        this.unavailableSlaves = unavailableSlaves;
        this.taskResult = taskResult;
        this.clientMap = clientMap;
    }

    @SuppressWarnings("unused")
    @Override
    public void run() {
        while (true) {
            OrderingClientTask task = taskQueue.dequeueTask();
            if (task != null) {
                System.out.println("\n The worker is working on a task...");
                if (task.getTask() instanceof TaskMatrix) {
                    TaskMatrix matrixTask = (TaskMatrix) task.getTask();
                    distributeTaskToSlaves(task);
                } else if (task.getTaskf() instanceof TaskFilter) {
                    TaskFilter filterTask = (TaskFilter) task.getTaskf();
                    String imageFileName = filterTask.getImageFileName();

                    int[][] imageMatrix = loadImageAndConvertToMatrix(imageFileName);
                    // if the file in not found
                    if (imageMatrix == null) {
                        ObjectOutputStream oos = clientMap.get(task.getClientId());

                        String errorMessage = "\n The file is not found. Please check the path for your image!";

                        try {
                            oos.writeObject(errorMessage);
                            oos.reset();
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {

                        filterTask.setImageMatrix(imageMatrix);
                        distributeTaskToSlaves(task.getClientId(), filterTask);
                    }

                }
            }
        }

    }

    private void distributeTaskToSlaves(OrderingClientTask clientTask) {
        try {
            List<Integer> tasksPerSlave = calculateTasksPerSlave(clientTask);
            int tasksToDistribute = tasksPerSlave.size();
            int taskIndex = 0;
            synchronized (unavailableSlaves) {

                for (Map.Entry<Socket, ObjectOutputStream> entry : slavesList.entrySet()) {
                    Socket slaveSocket = entry.getKey();
                    ObjectOutputStream oos = entry.getValue();

                    if (unavailableSlaves.containsKey(slaveSocket))
                        continue;
                    unavailableSlaves.put(slaveSocket, true);

                    TaskDistributedMatrix matrixTask = createMiniTaskForSlave(clientTask, tasksPerSlave.get(0),
                            taskIndex);
                    taskIndex += tasksPerSlave.remove(0);

                    oos.writeObject(matrixTask);
                    oos.reset();

                    if (tasksPerSlave.isEmpty())
                        break;
                }
            }

            ResultMatrix checkResult = new ResultMatrix(taskResult, clientMap,
                    clientTask.getTask().getTaskId(),
                    tasksToDistribute);

            new Thread(checkResult).start();
        } catch (Exception e) {
            System.err.println("\n Error in distribution of the task to the slaves: " + e.getMessage());
        }
    }

    private List<Integer> calculateTasksPerSlave(OrderingClientTask clientTask) {
        int[][] matrixA = clientTask.getTask().getMatrixA();
        int numberTasks = matrixA.length;

        int numberSlavesAvailable = slavesList.size() - unavailableSlaves.size();
        while (!checkAvailableSlaves(numberTasks, numberSlavesAvailable)) {

            System.out.println("\n The worker waits for the slaves to be available");
            numberSlavesAvailable = slavesList.size() - unavailableSlaves.size();
        }

        return calculateTasksForEachSlave(numberSlavesAvailable, numberTasks);
    }

    public boolean checkAvailableSlaves(int numberTask, int slavesAvailable) {
        return numberTask <= slavesAvailable || slavesAvailable == slavesList.size();

    }

    private int checkAvailableSlaves(int numberTasks) {

        int numberSlavesAvailable = slavesList.size() - unavailableSlaves.size();
        while (!(numberTasks <= numberSlavesAvailable || numberSlavesAvailable == slavesList.size())) {

            System.out.println("\n The worker waits for the slaves to be available");
            numberSlavesAvailable = slavesList.size() - unavailableSlaves.size();
        }

        return numberSlavesAvailable;
    }

    private TaskDistributedMatrix createMiniTaskForSlave(OrderingClientTask clientTask, int tasksCount, int startTask) {
        List<List<Integer>> listRowsSlave = new ArrayList<>();
        List<List<Integer>> listColumnsSlave = new ArrayList<>();
        List<Integer> Idtask = new ArrayList<>();

        int[][] matrixA = clientTask.getTask().getMatrixA();
        int[][] matrixB = clientTask.getTask().getMatrixB();
        String operation = clientTask.getTask().getOperation();

        if (operation.equals("multiplication")) {
            for (int i = 0; i < matrixB[0].length; i++) {
                List<Integer> listColumns = new ArrayList<>();
                for (int j = 0; j < matrixB.length; j++) {
                    listColumns.add(matrixB[j][i]);
                }
                listColumnsSlave.add(listColumns);
            }

            for (int j = 0; j < tasksCount; j++) {
                List<Integer> listRows = new ArrayList<>();
                for (int j2 = 0; j2 < matrixA[0].length; j2++) {
                    listRows.add(matrixA[startTask][j2]);
                }
                Idtask.add(startTask);
                startTask++;
                listRowsSlave.add(listRows);
            }

            return new TaskDistributedMatrix(clientTask.getClientId(), listRowsSlave, listColumnsSlave, Idtask,
                    clientTask.getTask().getTaskId(), clientTask.getTask().getOperation());

        } else {
            if (operation.equals("addition") || operation.equals("subtraction")) {
                for (int j = 0; j < tasksCount; j++) {
                    List<Integer> listRows = new ArrayList<>();
                    List<Integer> listColumns = new ArrayList<>();

                    for (int j2 = 0; j2 < matrixA[0].length; j2++) {
                        listRows.add(matrixA[startTask][j2]);
                        listColumns.add(matrixB[startTask][j2]);
                    }

                    Idtask.add(startTask);
                    listRowsSlave.add(listRows);
                    listColumnsSlave.add(listColumns);

                    startTask++;
                }
            }

            return new TaskDistributedMatrix(clientTask.getClientId(), listRowsSlave, listColumnsSlave, Idtask,
                    clientTask.getTask().getTaskId(), clientTask.getTask().getOperation());

        }
    }

    private List<Integer> calculateTasksForEachSlave(int availableSlaves, int totalTasks) {
        ArrayList<Integer> nbrTaskList = new ArrayList<>();
        if (totalTasks >= availableSlaves) {

            int tasksPerSlave = totalTasks / availableSlaves;
            int remainingTasks = totalTasks % availableSlaves;

            for (int i = 1; i <= availableSlaves; i++) {
                int tasksForThisSlave = tasksPerSlave + (i <= remainingTasks ? 1 : 0);
                nbrTaskList.add(tasksForThisSlave);
            }

        } else {

            for (int i = 1; i <= totalTasks; i++) {
                nbrTaskList.add(1);
            }
        }

        return nbrTaskList;
    }

    private void distributeTaskToSlaves(String clientID, TaskFilter filterTask) {

        try {

            int numberTasks = filterTask.getImageMatrix().length;
            int numberSlavesAvailable = checkAvailableSlaves(numberTasks);

            List<Integer> tasksPerSlave = calculateTasksForEachSlave(numberSlavesAvailable, numberTasks);
            int tasksToDistribute = tasksPerSlave.size();
            int currentSlaveIndex = 0;
            int taskIndex = 0;

            for (Map.Entry<Socket, ObjectOutputStream> entry : slavesList.entrySet()) {
                Socket slaveSocket = entry.getKey();
                ObjectOutputStream oos = entry.getValue();

                if (unavailableSlaves.containsKey(slaveSocket))
                    continue;
                unavailableSlaves.put(slaveSocket, true);

                TaskDistributedFilter subFilterTask = createSubFilterTask(clientID, filterTask, tasksPerSlave.get(0),
                        currentSlaveIndex, taskIndex);
                currentSlaveIndex += tasksPerSlave.remove(0);
                taskIndex++;

                oos.writeObject(subFilterTask);
                oos.reset();

                if (tasksPerSlave.isEmpty())
                    break;

            }

            ResultFilter checkResult = new ResultFilter(taskResult, clientMap, filterTask.getTaskId(),
                    tasksToDistribute);
            new Thread(checkResult).start();

        } catch (Exception e) {
            System.err.println("\n Error in distributeFilterTaskToSlaves: " + e.getMessage());
        }

    }

    private TaskDistributedFilter createSubFilterTask(String clientID, TaskFilter filterTask, int tasksCount,
            int currentSlaveIndex, int taskIndex) {

        int[][] imageMatrix = filterTask.getImageMatrix();

        int[][] subMatrix = Arrays.copyOfRange(imageMatrix, currentSlaveIndex, currentSlaveIndex + tasksCount);

        return new TaskDistributedFilter(clientID, filterTask.getTaskId(), filterTask.getFilterType(),
                filterTask.getIntensityValue(), subMatrix, taskIndex);
    }

    public static int[][] loadImageAndConvertToMatrix(String imageFileName) {
        try {
            System.out.println("\n Attempting to load image from: " + imageFileName + "\n");

            // Check if the file exists
            File imageFile = new File(imageFileName);
            if (!imageFile.exists()) {
                System.err.println("\n Error: Image file not found.");
                return null;
            }

            // Read the image
            BufferedImage image = ImageIO.read(imageFile);

            // Get the dimensions of the image
            int width = image.getWidth();
            int height = image.getHeight();

            // Convert the image to a bidimensional array
            int[][] imageMatrix = new int[width][height];

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    imageMatrix[i][j] = image.getRGB(i, j);
                }
            }

            return imageMatrix;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
