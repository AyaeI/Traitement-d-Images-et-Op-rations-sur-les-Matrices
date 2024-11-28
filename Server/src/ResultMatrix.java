package Server.src;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ResultMatrix implements Runnable {

    private ConcurrentHashMap<TaskResult, UUID> taskResult;
    private ConcurrentHashMap<String, ObjectOutputStream> clientMap;
    private UUID IDt;
    private int nbrTasks;

    public ResultMatrix(ConcurrentHashMap<TaskResult, UUID> taskResult,
            ConcurrentHashMap<String, ObjectOutputStream> clientMap,
            UUID IDt, int nbrTasks) {
        this.clientMap = clientMap;
        this.taskResult = taskResult;
        this.IDt = IDt;
        this.nbrTasks = nbrTasks;

    }

    @Override
    public void run() {
        try {
            while (true) {
                List<TaskResult> taskResults = taskResult.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(IDt))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                if (taskResults.size() == nbrTasks) {
                    String IDc = taskResults.get(0).getClientId();
                    System.out.println(
                            "\n All matrix tasks" + IDt + " with Client ID:  " + IDc + " are completed.");
                    int numberResult = 0;
                    for (TaskResult result : taskResults) {
                        numberResult += result.getIdtask().size();
                        taskResult.remove(result);

                    }
                    organizeResults(taskResults, numberResult, IDc);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // Prépare les résultats des tâches dans une structure de données appropriée
    private void organizeResults(List<TaskResult> taskResults, int numberResult, String IDc) {
        List<List<Integer>> resultsList = new ArrayList<>(Collections.nCopies(numberResult, null));

        for (TaskResult r : taskResults) {
            for (int i = 0; i < r.getIdtask().size(); i++) {
                int index = r.getIdtask().get(i);
                resultsList.set(index, r.getResult().get(i));
            }
        }

        broadcastTaskToClient(resultsList, IDc);
    }

    // utilise cette structure pour les envoyer au client associé
    public void broadcastTaskToClient(List<List<Integer>> listResult, String IDc) {
        ObjectOutputStream oos = clientMap.get(IDc);

        if (oos != null) {
            try {
                // Convertir la liste de listes d'entiers en tableau bidimensionnel
                int[][] resultArray = new int[listResult.size()][];
                for (int i = 0; i < listResult.size(); i++) {
                    List<Integer> row = listResult.get(i);
                    resultArray[i] = row.stream().mapToInt(Integer::intValue).toArray();
                }

                // Envoyer le tableau bidimensionnel au client
                oos.writeObject(resultArray);
                oos.reset();

            } catch (IOException e) {
                System.err.println("Error sending the result to client: " + e.getMessage());
            }
        }
    }

}
