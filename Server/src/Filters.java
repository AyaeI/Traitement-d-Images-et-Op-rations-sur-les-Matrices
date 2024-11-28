package Server.src;

import java.awt.Color;
import java.util.Random;

public class Filters {

    public int[][] GaussianNoise(int[][] inputMatrix, float intensity) {
        int width = inputMatrix.length;
        int height = inputMatrix[0].length;
        int[][] outputMatrix = new int[width][height];

        Random random = new Random();

        // Apply Gaussian noise to each pixel of the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(inputMatrix[x][y]);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Add Gaussian noise to each channel
                int newRed = (int) (red + random.nextGaussian() * intensity);
                int newGreen = (int) (green + random.nextGaussian() * intensity);
                int newBlue = (int) (blue + random.nextGaussian() * intensity);

                // Limit values to 0-255 range
                newRed = Math.min(255, Math.max(0, newRed));
                newGreen = Math.min(255, Math.max(0, newGreen));
                newBlue = Math.min(255, Math.max(0, newBlue));

                // Combine channels into a single pixel
                outputMatrix[x][y] = new Color(newRed, newGreen, newBlue).getRGB();
            }
        }

        return outputMatrix;
    }

    private int applyKernel(int[][] imageMatrix, int[][] kernel, int x, int y, char channel) {
        int result = 0;
        int kernelSize = kernel.length;

        for (int i = 0; i < kernelSize; i++) {
            for (int j = 0; j < kernelSize; j++) {
                int pixel = imageMatrix[x - 1 + i][y - 1 + j];
                int value;

                // Extract the specified channel from the pixel
                if (channel == 'R') {
                    value = (pixel >> 16) & 0xFF; // Red channel
                } else if (channel == 'G') {
                    value = (pixel >> 8) & 0xFF; // Green channel
                } else {
                    value = pixel & 0xFF; // Blue channel
                }

                result += kernel[i][j] * value;
            }
        }

        return result;
    }

    public int[][] SepiaFilter(int[][] imageMatrix) {
        int width = imageMatrix.length;
        int height = imageMatrix[0].length;
        int[][] outputMatrix = new int[width][height];

        // Coefficients pour le filtre sépia
        final double sepiaRedCoefficient = 0.393;
        final double sepiaGreenCoefficient = 0.769;
        final double sepiaBlueCoefficient = 0.189;

        // Appliquer le filtre sépia à chaque pixel de l'image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(imageMatrix[x][y]);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                int newRed = (int) (sepiaRedCoefficient * red + sepiaGreenCoefficient * green
                        + sepiaBlueCoefficient * blue);
                int newGreen = (int) (sepiaRedCoefficient * red + sepiaGreenCoefficient * green
                        + sepiaBlueCoefficient * blue);
                int newBlue = (int) (sepiaRedCoefficient * red + sepiaGreenCoefficient * green
                        + sepiaBlueCoefficient * blue);

                // Limiter les valeurs de chaque composante de couleur entre 0 et 255
                newRed = Math.min(newRed, 255);
                newGreen = Math.min(newGreen, 255);
                newBlue = Math.min(newBlue, 255);

                outputMatrix[x][y] = new Color(newRed, newGreen, newBlue).getRGB();
            }
        }

        return outputMatrix;
    }

    public int[][] RGBToGrayscale(int[][] inputMatrix) {
        int width = inputMatrix.length;
        int height = inputMatrix[0].length;
        int[][] outputMatrix = new int[width][height];

        // Convert each pixel to grayscale
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(inputMatrix[x][y]);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Convert RGB to grayscale using luminosity method
                int gray = (int) (0.21 * red + 0.72 * green + 0.07 * blue);

                // Create grayscale pixel
                outputMatrix[x][y] = new Color(gray, gray, gray).getRGB();
            }
        }

        return outputMatrix;
    }

    public int[][] MosaicFilter(int[][] imageMatrix, float intensity) {
        int width = imageMatrix.length;
        int height = imageMatrix[0].length;
        int[][] outputMatrix = new int[width][height];

        // Appliquer le filtre de mosaïque en moyennant les couleurs dans des blocs de
        // pixels
        for (int x = 0; x < width; x += intensity) {
            for (int y = 0; y < height; y += intensity) {
                int sumRed = 0, sumGreen = 0, sumBlue = 0;

                // Calculer la somme des couleurs dans le bloc actuel
                for (int i = x; i < x + intensity && i < width; i++) {
                    for (int j = y; j < y + intensity && j < height; j++) {
                        Color color = new Color(imageMatrix[i][j]);
                        sumRed += color.getRed();
                        sumGreen += color.getGreen();
                        sumBlue += color.getBlue();
                    }
                }

                // Calculer la couleur moyenne du bloc
                int averageRed = sumRed / (int) (intensity * intensity);
                int averageGreen = sumGreen / (int) (intensity * intensity);
                int averageBlue = sumBlue / (int) (intensity * intensity);

                // Appliquer la couleur moyenne à tous les pixels du bloc
                for (int i = x; i < x + intensity && i < width; i++) {
                    for (int j = y; j < y + intensity && j < height; j++) {
                        outputMatrix[i][j] = new Color(averageRed, averageGreen, averageBlue).getRGB();
                    }
                }
            }
        }

        return outputMatrix;
    }

    //////////////////////////////// contour///////////////////
    public int[][] ContourFilter(int[][] imageMatrix, float intensity) {
        int width = imageMatrix.length;
        int height = imageMatrix[0].length;
        int[][] filteredImage = new int[width][height];

        // Sobel operator kernels for edge detection
        int[][] sobelX = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
        int[][] sobelY = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Apply the Sobel operator to each channel separately
                int gradientXRed = applyKernel(imageMatrix, sobelX, i, j, 'R');
                int gradientYRed = applyKernel(imageMatrix, sobelY, i, j, 'R');
                int gradientXGreen = applyKernel(imageMatrix, sobelX, i, j, 'G');
                int gradientYGreen = applyKernel(imageMatrix, sobelY, i, j, 'G');
                int gradientXBlue = applyKernel(imageMatrix, sobelX, i, j, 'B');
                int gradientYBlue = applyKernel(imageMatrix, sobelY, i, j, 'B');

                // Calculate the magnitude of the gradient for each channel
                int magnitudeRed = (int) Math.sqrt(gradientXRed * gradientXRed + gradientYRed * gradientYRed);
                int magnitudeGreen = (int) Math.sqrt(gradientXGreen * gradientXGreen + gradientYGreen * gradientYGreen);
                int magnitudeBlue = (int) Math.sqrt(gradientXBlue * gradientXBlue + gradientYBlue * gradientYBlue);

                // Combine magnitudes and scale based on intensity
                int magnitude = (magnitudeRed + magnitudeGreen + magnitudeBlue) / 3;
                magnitude = (int) (magnitude * intensity / 100.0);

                // Ensure the magnitude stays within the valid range (0-255)
                magnitude = Math.min(255, Math.max(0, magnitude));

                // Set the magnitude for the output pixel in each channel
                filteredImage[i][j] = (magnitude << 16) | (magnitude << 8) | magnitude
                        | (imageMatrix[i][j] & 0xFF000000);
            }
        }

        return filteredImage;
    }

}