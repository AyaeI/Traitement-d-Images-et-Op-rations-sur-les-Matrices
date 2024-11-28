package Server.src;
import java.io.Serializable;
import java.util.UUID;

public class TaskDistributedFilter implements Serializable {
    private String clientID; // The identifier for the client
    private final UUID taskID; // This is the unique ID for each Task instance
    private String filterType;
    private float intensityValue;
    private int indexTask;
    private int[][] subMatrix;

    public TaskDistributedFilter(String clientID, UUID taskID, String filterType, float intensityValue, int[][] subMatrix,
            int indexTask) {
        this.clientID = clientID;
        this.taskID = taskID;
        this.filterType = filterType;
        this.intensityValue = intensityValue;
        this.indexTask = indexTask;
        this.subMatrix = subMatrix;
    }

    public String getClientID() {
        return clientID;
    }

    public UUID getTaskID() {
        return taskID;
    }

    public String getfilterType() {
        return filterType;
    }

    public int[][] getSubMatrix() {
        return subMatrix;
    }

    public int getIndexTask() {
        return indexTask;
    }

    public String getFilterType() {
        return filterType;
    }

    public float getIntensityValue() {
        return intensityValue;
    }

}
