package Server.src;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FilterProcessor extends Remote {
    int[][] AddFilter(int[][] imageMatrix, String filterType, float intensity) throws RemoteException;

    public void saveFilteredImage(int[][] FilteredMatrix, String outputPath)
            throws RemoteException;

}
