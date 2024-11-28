package Client.src;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import Server.src.TaskFilter;

public class Filter extends JFrame {

    @SuppressWarnings("unused")
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private JTextField imageFileField;
    private JTextField intensityField;
    private JComboBox<String> filterComboBox;
    private JButton chooseImageButton;
    private JButton applyFilterButton;
    private JLabel originalImageLabel;
    private JLabel filteredImageLabel;

    public Filter(Socket socket) {
        this.socket = socket;
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Image Filter");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));
        add(mainPanel);

        JPanel originalImagePanel = new JPanel(new BorderLayout());
        mainPanel.add(originalImagePanel);
        originalImageLabel = new JLabel();
        originalImagePanel.add(originalImageLabel, BorderLayout.CENTER);
        originalImagePanel.setBorder(BorderFactory.createTitledBorder("Original Image"));

        JPanel filteredImagePanel = new JPanel(new BorderLayout());
        mainPanel.add(filteredImagePanel);
        filteredImageLabel = new JLabel();
        filteredImagePanel.add(filteredImageLabel, BorderLayout.CENTER);
        filteredImagePanel.setBorder(BorderFactory.createTitledBorder("Filtered Image"));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        add(inputPanel, BorderLayout.SOUTH);

        inputPanel.add(new JLabel("Image File:"));

        imageFileField = new JTextField(20);
        inputPanel.add(imageFileField);

        chooseImageButton = new JButton("Choose Image");
        chooseImageButton.addActionListener(new ChooseImageListener());
        inputPanel.add(chooseImageButton);

        // Adding field for intensity input
        inputPanel.add(new JLabel("Intensity (0-100):"));
        intensityField = new JTextField(5);
        inputPanel.add(intensityField);

        // Adding JComboBox for filter selection
        filterComboBox = new JComboBox<>(new String[] { "RGB to Grayscale", "Contour", "Mosaic", "Gaussian Noise" });
        inputPanel.add(filterComboBox);

        applyFilterButton = new JButton("Apply Filter");
        applyFilterButton.addActionListener(new ApplyFilterListener());
        inputPanel.add(applyFilterButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class ChooseImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Images", "jpg", "jpeg", "png", "gif");
            fileChooser.setFileFilter(filter);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                imageFileField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private class ApplyFilterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String imageFileName = imageFileField.getText();

                if (imageFileName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please choose an image file.");
                    return;
                }

                File imageFile = new File(imageFileName);
                if (!imageFile.exists()) {
                    JOptionPane.showMessageDialog(null, "File not found: " + imageFileName);
                    return;
                }

                BufferedImage originalImage = ImageIO.read(imageFile);
                displayOriginalImage(originalImage); // Display original image

                // Retrieve the intensity entered by the user
                int intensity;
                String selectedFilter = (String) filterComboBox.getSelectedItem();

                if (selectedFilter.equals("RGB to Grayscale") && intensityField.getText().isEmpty()) {
                    intensity = 100; // Default intensity value for RGB to Grayscale (full intensity)
                } else {
                    try {
                        intensity = Integer.parseInt(intensityField.getText());
                        if (intensity < 0 || intensity > 100) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Intensity must be a number between 0 and 100.");
                        return;
                    }
                }

                TaskFilter filterTask;

                switch (selectedFilter) {
                    case "RGB to Grayscale":
                        filterTask = new TaskFilter(imageFileName, "RGB_TO_GRAYSCALE", intensity);
                        break;
                    case "Contour":
                        filterTask = new TaskFilter(imageFileName, "CONTOUR", intensity);
                        break;
                    case "Mosaic":
                        filterTask = new TaskFilter(imageFileName, "MOSAIC", intensity);
                        break;
                    case "Gaussian Noise":
                        filterTask = new TaskFilter(imageFileName, "GAUSSIAN_NOISE", intensity);
                        break;
                    default:
                        // Handle default case
                        return;
                }

                oos.writeObject(filterTask);

                Object receivedObject = ois.readObject();
                if (receivedObject instanceof byte[]) {
                    byte[] imageBytes = (byte[]) receivedObject;
                    displayFilteredImage(imageBytes); // Display filtered image
                    saveFilteredImage(imageBytes, selectedFilter); // Save the filtered image to file
                } else if (receivedObject instanceof String) {
                    String errorMessage = (String) receivedObject;
                    JOptionPane.showMessageDialog(null, errorMessage);
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void displayOriginalImage(BufferedImage originalImage) {
        try {
            // Resize the original image to fit the label
            originalImage = resizeImage(originalImage, originalImageLabel.getWidth(), originalImageLabel.getHeight());
            originalImageLabel.setIcon(new ImageIcon(originalImage));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error displaying original image: " + e.getMessage());
        }
    }

    private void displayFilteredImage(byte[] imageBytes) {
        try {
            BufferedImage filteredImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            // Resize the filtered image to fit the label
            filteredImage = resizeImage(filteredImage, filteredImageLabel.getWidth(), filteredImageLabel.getHeight());
            filteredImageLabel.setIcon(new ImageIcon(filteredImage));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, width, height, null);
        g2.dispose();
        return resizedImage;
    }

    private void saveFilteredImage(byte[] imageBytes, String filterName) {
        try {
            // Generate a random number
            int randomNumber = (int) (Math.random() * 1000);

            // Construct the file name with the filter name and random number
            String fileName = filterName.replace(" ", "_") + "_" + randomNumber + ".jpg";

            // Construct the full path to the output file
            String outputPath = "C:\\Users\\hp\\Downloads\\ProjetJava\\Client\\src\\results\\" + fileName;

            File outputFile = new File(outputPath);

            // Write the image as a JPG file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(imageBytes);
                // Display success message in a frame
                JOptionPane.showMessageDialog(null,
                        "The filtered image has been saved successfully.\nImage saved at: " + outputPath);
            } catch (IOException e) {
                System.err.println("Error saving filtered image: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

}
