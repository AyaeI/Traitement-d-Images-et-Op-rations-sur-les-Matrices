package Server.src;
import java.io.Serializable;
import java.util.UUID;

public class TaskFilter implements Serializable {
    private final UUID taskId; // This is the unique ID for each FilterTask instance
    private String imageFileName; // Nom du fichier image
    private String filterType; // Type de filtre à appliquer
    private float intensityValue;
    private int[][] imageMatrix = null;

    public TaskFilter(String imageFileName, String filterType, float intensityValue) {
        this.taskId = UUID.randomUUID(); // Générer un UUID unique
        this.imageFileName = imageFileName;
        this.filterType = filterType;
        this.intensityValue = intensityValue;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getFilterType() {
        return filterType;
    }

    public int[][] getImageMatrix() {
        return imageMatrix;
    }

    public void setImageMatrix(int[][] imageMatrix) {
        this.imageMatrix = imageMatrix;
    }

    public float getIntensityValue() {
        return intensityValue;
    }

}
