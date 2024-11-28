package Server.src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskResult implements Serializable {
    private String IDc;
    private List<Integer> IDt;
    private String resultType;
    private int TaskIndex;
    private int[][] resultF;
    private List<List<Integer>> result = new ArrayList<>();
    private UUID taskId;

    public TaskResult(String IDc, List<List<Integer>> result, List<Integer> IDt, UUID taskId) {
        this.IDt = IDt;
        this.taskId = taskId;
        this.IDc = IDc;
        this.result = result;
    }
    public TaskResult(String clientId, UUID taskId, String resultType, int[][] result, int TaskIndex) {
        this.TaskIndex = TaskIndex;
        this.taskId = taskId;
        this.resultType = resultType;
        this.IDc = clientId;
        this.resultF = result;
    }
    public int[][] getResultF() {
        return resultF;
    }
    public int getTaskIndex() {
        return TaskIndex;
    }
    public String getResultType() {
        return resultType;
    }

    public List<List<Integer>> getResult() {
        return result;
    }

    public String getClientId() {
        return IDc;
    }

    public List<Integer> getIdtask() {
        return IDt;
    }

    public UUID getTaskId() {
        return taskId;
    }

}


   
   

    



