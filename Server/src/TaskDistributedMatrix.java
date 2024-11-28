package Server.src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskDistributedMatrix implements Serializable {
    private String IDc;
    private final UUID taskId;
    private List<List<Integer>> listRows;
    private List<List<Integer>> listColumns;
    private List<List<Integer>> taskResult;
    private List<Integer> IDt;
    private String operation;

    public TaskDistributedMatrix(String IDc, List<List<Integer>> listRows, List<List<Integer>> listColumns,
            List<Integer> IDt, UUID taskId, String operation) {
        this.IDc = IDc;
        this.taskId = taskId;
        this.listRows = listRows;
        this.listColumns = listColumns;
        this.IDt = IDt;
        this.taskResult = new ArrayList<>();
        this.operation = operation;
    }

    public void viewTasks() {
        System.out.println("+++ My current task +++");
        System.out.println("\n List the elements of rows from Matrice A");
        for (List<Integer> row : listRows) {
            System.out.print("[ ");
            for (Integer r : row) {
                System.out.print(r + " ");
            }
            System.out.print("]");
            System.out.println();
        }

        if (operation.equalsIgnoreCase("multiplication")) {
            System.out.println("\n List the elements of columns from Matrice B");
            for (List<Integer> column : listColumns) {
                System.out.print("[ ");
                for (Integer c : column) {
                    System.out.print(c + " ");
                }
                System.out.print("]");
                System.out.println();
            }
        } else if (operation.equalsIgnoreCase("addition") || operation.equalsIgnoreCase("subtraction")) {
            System.out.println("\n List the elements of rows from Matrice B");
            for (List<Integer> column : listColumns) {
                System.out.print("[ ");
                for (Integer c : column) {
                    System.out.print(c + " ");
                }
                System.out.print("]");
                System.out.println();
            }
        }
    }

    public void viewResult() {
        System.out.println("\n--- The results ---");
        for (List<Integer> result : taskResult) {
            System.out.print("[ ");
            for (Integer r : result) {
                System.out.print(r + " ");
            }
            System.out.print("]");
            System.out.println();
        }
    }

    public List<List<Integer>> getListRows() {
        return listRows;
    }

    public List<List<Integer>> getListColumns() {
        return listColumns;
    }

    public List<List<Integer>> getTaskResult() {
        return taskResult;
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

    public String getOperation() {
        return operation;
    }

    public void setTaskResult(List<List<Integer>> taskResult) {
        this.taskResult = taskResult;
    }

}