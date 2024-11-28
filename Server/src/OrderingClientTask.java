package Server.src;

import java.io.Serializable;

public class OrderingClientTask implements Serializable {
    private TaskMatrix task;
    private Object taskf;
    private String IDc;

    public OrderingClientTask(TaskMatrix task, String IDc) {
        this.task = task;
        this.IDc = IDc;
    }
    public OrderingClientTask(Object task, String clientId) {
        this.taskf = task;
        this.IDc = clientId;
    }
    public Object getTaskf() {
        return taskf;
    }

    public TaskMatrix getTask() {
        return task;
    }

    public String getClientId() {
        return IDc;
    }
}
