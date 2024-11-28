package Server.src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.imageio.ImageIO;

public class FilterServer extends UnicastRemoteObject implements FilterProcessor {
    public FilterServer() throws RemoteException {
        super();
    }

    @Override
    public int[][] AddFilter(int[][] imageMatrix, String filterType, float intensity) throws RemoteException {
        Filters imageProcessor = new Filters();
        switch (filterType) {

            case "GAUSSIAN_NOISE":
                return imageProcessor.GaussianNoise(imageMatrix, intensity);
            case "RGB_TO_GRAYSCALE":
                return imageProcessor.RGBToGrayscale(imageMatrix);
            case "CONTOUR":
                return imageProcessor.ContourFilter(imageMatrix, intensity);
            case "MOSAIC":
                return imageProcessor.MosaicFilter(imageMatrix, intensity);
            default:
                throw new IllegalArgumentException("Filter type not supported: " + filterType);
        }
    }

    @Override
    public void saveFilteredImage(int[][] filteredMatrix, String outputFilePath) {
        int width = filteredMatrix.length;
        int height = filteredMatrix[0].length;

        BufferedImage image;

        // Create a BufferedImage based on the ARGB values
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = filteredMatrix[x][y];
                image.setRGB(x, y, argb);
            }
        }

        try {
            // Split the outputFilePath into folderName and outputFileName
            String[] parts = outputFilePath.split("\\\\");
            String folderName = parts[0];
            String outputFileName = parts[1];

            String folderPath = "C:\\Users\\hp\\Downloads\\ProjetJava\\Server\\src\\tasks\\"
                    + folderName;

            File folder = new File(folderPath);

            // Create the folder if it doesn't exist
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String outputPath = folderPath + "/" + outputFileName + ".jpg";
            File outputImage = new File(outputPath);

            // Write the image as a PNG file
            ImageIO.write(image, "png", outputImage);
            System.out.println(" The new filtered image saved successfully: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error saving the filtered image: " + e.getMessage());
        }
    }

}
