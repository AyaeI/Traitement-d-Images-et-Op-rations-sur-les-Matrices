package Server.src;

import java.io.Serializable;
import java.util.UUID;

public class TaskMatrix implements Serializable {
    private UUID taskId;
    private int[][] matrixA;
    private int[][] matrixB;
    private String operation;

    public TaskMatrix(int[][] matrixA, int[][] matrixB, String operation) {
        this.taskId = UUID.randomUUID();
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.operation = operation;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public int[][] getMatrixA() {
        return matrixA;
    }

    public int[][] getMatrixB() {
        return matrixB;
    }

    public String getOperation() {
        return operation;
    }

}
