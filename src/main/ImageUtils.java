package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class ImageUtils {

    /**
     * Prevent this class from being instantiated.
     */
    private ImageUtils() {}
    
    /**
     * Saves the given image to a PNG file, if it doesn't already exist.
     * 
     * @param image
     * @param filename
     * @throws IOException
     */
    public static void saveImage(BufferedImage image, String filename)
            throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            ImageIO.write(image, "PNG", file);
        }
    }

    /**
     * Crops the given image based on the given background colour.
     * 
     * @param image
     * @param backgroundColour
     * @return
     */
    public static BufferedImage crop(BufferedImage image,
            int backgroundColour) {

        int x1 = image.getWidth();
        int y1 = image.getHeight();
        int x2 = 0;
        int y2 = 0;
        
        for (int y = 0; y < image.getHeight(); y++) {
            
            // Find leftmost coloured pixel
            for (int x = 0; x < image.getWidth(); x++) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && x < x1) {
                    x1 = x;
                }
            }

            // Find rightmost coloured pixel
            for (int x = image.getWidth() - 1; x >= x1; x--) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && x > x2) {
                    x2 = x;
                }
            }
        }
        
        for (int x = x1; x <= x2; x++) {
            
            // Find topmost coloured pixel
            for (int y = 0; y < image.getHeight(); y++) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && y < y1) {
                    y1 = y;
                }
            }

            // Find bottom-most coloured pixel
            for (int y = image.getHeight() - 1; y >= y1; y--) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && y > y2) {
                    y2 = y;
                }
            }
        }
        
        // We have to add 1 here - best explained by example: if the leftmost
        // pixel is 0 and rightmost pixel is 10, width is actually 11 pixels!
        int width = x2 - x1 + 1;
        int height = y2 - y1 + 1;

        return image.getSubimage(x1, y1, width, height);
    }

}
