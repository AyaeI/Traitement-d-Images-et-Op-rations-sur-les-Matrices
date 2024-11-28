package Client.src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

import Server.src.TaskMatrix;

public class Matrix {

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream objectInputStream;
    private int[][] matrixA;
    private int[][] matrixB;

    public Matrix(Socket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.matrixA = null;
        this.matrixB = null;

    }

    public void menu() {
        System.out.println("===== Choose an operation: =====");
        System.out.println(" 1. Matrices calculation operations.");
        System.out.println(" 2. Apply filter to an image.");
        System.out.println(" 0. Exit");
        System.out.print("-----Enter your choice: ");
    }

    public void MenuTask() throws IOException, ClassNotFoundException {
        int taskChoise;

        boolean exit = false;
        try (Scanner scanner = new Scanner(System.in)) {
            while (!exit) {
                try {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } catch (InterruptedException e) {
                    // Handle InterruptedException
                    System.err.println("Error waiting for process to complete: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
                menu();
                taskChoise = scanner.nextInt();

                switch (taskChoise) {
                    case 0:
                        exit = true;
                        close();
                        System.out.println("You have exited from the program");
                        return;

                    case 1:
                        matrixOperationMenu();
                        break;

                    case 2:
                        Client.handleImageFiltering();
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

                // Ask user if they want to choose another option
                System.out.print("\nDo you want to choose another option? (yes/no): ");
                String response = scanner.next().toLowerCase();
                exit = response.equals("no") ? true : false;
                close();

            }
        }
    }

    public void matrixOperationMenu() throws IOException, ClassNotFoundException {
        int inputMethod;
        boolean exit = false;
        try (Scanner scanner = new Scanner(System.in)) {
            while (!exit) {
                clearConsole();
                System.out.println("===== Choose the Matrix Task =====");
                System.out.println(" 1. Matrix Addition");
                System.out.println(" 2. Matrix Subtraction");
                System.out.println(" 3. Matrix Multiplication");
                System.out.println("\n 0. Back to main menu");
                System.out.print("\n-----Enter your choice: ");
                inputMethod = scanner.nextInt();
                switch (inputMethod) {
                    case 0:
                        MenuTask();
                        break;
                    case 1:
                        matrixTask(inputMethod, scanner);
                        break;
                    case 2:
                        matrixTask(inputMethod, scanner);
                        break;
                    case 3:
                        matrixTask(inputMethod, scanner);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
                scanner.nextLine();
                // Ask user if they want to choose another option
                System.out.print("\n Do you want to choose another option? (yes/no): ");
                String response = scanner.nextLine().toLowerCase();
                exit = response.equals("n") ? true : false;
                // performTask();
            }
        }
    }

    private void matrixTask(int operation, Scanner scanner) throws IOException, ClassNotFoundException {
        boolean matricesCompatible;

        do {
            System.out.print("\nEnter dimensions of the row for Matrix A : ");
            int rowsA = scanner.nextInt();
            System.out.print("Enter dimensions of the column for Matrix A : ");
            int colsA = scanner.nextInt();
            matrixA = generateRandomMatrix(rowsA, colsA);

            System.out.print("\nEnter dimensions of the row for Matrix B : ");
            int rowsB = scanner.nextInt();
            System.out.print("Enter dimensions of the column for Matrix B : ");
            int colsB = scanner.nextInt();
            matrixB = generateRandomMatrix(rowsB, colsB);

            switch (getOperationType(operation)) {
                case "addition":
                    matricesCompatible = (matrixA.length == matrixB.length)
                            && (matrixA[0].length == matrixB[0].length);
                    break;
                case "subtraction":
                    matricesCompatible = (matrixA.length == matrixB.length)
                            && (matrixA[0].length == matrixB[0].length);
                    break;
                case "multiplication":
                    matricesCompatible = matrixA[0].length == matrixB.length;
                    break;
                default:
                    System.out.println("Invalid operation.");
                    matricesCompatible = false;
                    break;
            }

            if (!matricesCompatible) {
                if (getOperationType(operation).equals("addition"))
                    System.out.println(
                            "\nThe number of rows and columns of both the matrices A and B must be the same for addition!!");
                else {
                    if (getOperationType(operation).equals("subtraction")) {
                        System.out.println(
                                "\nThe number of rows and columns of both the matrices A and B must be  the same for subtraction!!");
                    } else {
                        if (getOperationType(operation).equals("multiplication")) {
                            System.out.println(
                                    "\nThe number of columns in Matrix A must be equal to the number of rows in Matrix B for multiplication!!");
                        }
                    }

                }

            }
        } while (!matricesCompatible);
        createAndSendTask(operation);
    }

    private int[][] generateRandomMatrix(int rows, int columns) {
        Random random = new Random();
        int[][] matrix = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = random.nextInt(10);
            }
        }
        return matrix;
    }

    private void createAndSendTask(int operation) throws IOException, ClassNotFoundException {
        TaskMatrix task = new TaskMatrix(matrixA, matrixB, getOperationType(operation));
        oos.writeObject(task);
        listenForMessage();
    }

    private void listenForMessage() throws ClassNotFoundException {
        try {
            Object resultMatrix;
            if ((resultMatrix = objectInputStream.readObject()) != null) {
                if (resultMatrix instanceof int[][]) {
                    int[][] matrixResult = (int[][]) resultMatrix;
                    System.out.println("\nTask successfully processed:");
                    displayMatrix("Matrix A", matrixA);
                    displayMatrix("Matrix B", matrixB);
                    displayMatrix("Result", matrixResult);
                } else {
                    System.err.println("Invalid result received.");
                }
            }
        } catch (IOException e) {
            System.out.println("Server is Closed!");
            close();
        }
    }

    private void displayMatrix(String label, int[][] matrix) {
        System.out.println("\n---- " + label + " ----");
        for (int[] row : matrix) {
            for (int element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    private String getOperationType(int operation) {
        switch (operation) {
            case 1:
                return "addition";
            case 2:
                return "subtraction";
            case 3:
                return "multiplication";
            default:
                return "unknown";
        }
    }

    public void close() {
        try {

            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    private static void clearConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
