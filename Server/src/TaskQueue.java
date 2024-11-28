package Server.src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class TaskQueue implements Serializable {
    private Queue<OrderingClientTask> taskQueue = new LinkedList<>();
    private final Object lock = new Object();
    private static final long serialVersionUID = 1L;
    private final ReentrantLock lock1 = new ReentrantLock();

    public void enqueueTask(OrderingClientTask clientTask) {
        synchronized (lock) {
            taskQueue.add(clientTask);
            lock.notify(); // Notify any waiting threads that a task has been added
        }
    }

    public OrderingClientTask dequeueTask() {
        synchronized (lock) {
            while (taskQueue.isEmpty()) {
                try {
                    lock.wait(); // Wait for a task to be added if the queue is empty
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error waiting for task: " + e.getMessage());
                }
            }
            return taskQueue.poll();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return taskQueue.isEmpty();
        }
    }

    // Serialization methods
    private void writeObject(ObjectOutputStream out) throws IOException {
        lock1.lock();
        try {
            out.defaultWriteObject();
        } finally {
            lock1.unlock();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        lock1.lock();
        try {
            in.defaultReadObject();
        } finally {
            lock1.unlock();
        }
    }
}
