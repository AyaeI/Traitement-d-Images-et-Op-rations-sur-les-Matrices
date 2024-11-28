package Server.src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        int portClient;
        int portSlave;

        try (BufferedReader br = new BufferedReader(
                new FileReader("E:\\Master\\M1\\ProjetJavaFinal\\ProjetJava\\Server\\src\\config.txt"))) {
            portClient = Integer.parseInt(br.readLine());
            portSlave = Integer.parseInt(br.readLine());
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        try {
            LocateRegistry.createRegistry(1099); // Port RMI par défaut

            // Créez et enregistrez le serveur RMI des filtres
            FilterProcessor filterServer = new FilterServer();
            Naming.rebind("//localhost/FilterServer", filterServer);

            System.out.println("The FilterServer is added to the RMI registry.");

            ConnectionClient serverClient = new ConnectionClient(portClient);
            ConnectionSlave serverSlave = new ConnectionSlave(portSlave);

            new Thread(serverClient).start();
            new Thread(serverSlave).start();

            new OrderingWorkerTask(serverClient.getTaskQueue(), serverSlave.getListSlaveConnected(),
                    serverSlave.getUnavailableSlaves(),
                    serverSlave.getTaskResult(), serverClient.getClientMap()).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
