package Server.src;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderingWorkerTask {
    private static final int NUM_WORKER_THREADS = 8;

    private ExecutorService threadPool;
    private ConcurrentHashMap<Socket, ObjectOutputStream> listSlaveConnected;
    private ConcurrentHashMap<Socket, Boolean> unavailableSlaves;
    private ConcurrentHashMap<TaskResult, UUID> taskResult;
    private ConcurrentHashMap<String, ObjectOutputStream> clientMap;
    private TaskQueue taskQueue;

    public OrderingWorkerTask(TaskQueue taskQueue,
            ConcurrentHashMap<Socket, ObjectOutputStream> listSlaveConnected,
            ConcurrentHashMap<Socket, Boolean> unavailableSlaves,
            ConcurrentHashMap<TaskResult, UUID> taskResult,
            ConcurrentHashMap<String, ObjectOutputStream> clientMap) {
        this.threadPool = Executors.newFixedThreadPool(NUM_WORKER_THREADS);
        this.taskQueue = taskQueue;
        this.listSlaveConnected = listSlaveConnected;
        this.unavailableSlaves = unavailableSlaves;
        this.clientMap = clientMap;
        this.taskResult = taskResult;
    }

    public void start() {
        for (int i = 0; i < NUM_WORKER_THREADS; i++) {
            threadPool.submit(new Worker(taskQueue, listSlaveConnected, unavailableSlaves, taskResult, clientMap));

        }
    }

}
