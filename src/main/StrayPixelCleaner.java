package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class StrayPixelCleaner {

    /**
     * Regex used to match image filenames.
     */
    private static final String IMAGE_FILENAME_REGEX = 
            ".+\\.(?i)(bmp|jpg|gif|png)";

    /**
     * Minimum number of connected pixels that are considered "stray".
     */
    private int minThreshold;

    /**
     * Minimum number of pixels that must be connected in order for them to no
     * longer be considered "stray".
     */
    private int maxThreshold;

    /**
     * Constructs a StrayPixelCleaner with the given threshold.
     * 
     * @param minThreshold
     * @param maxThreshold 
     */
    public StrayPixelCleaner(int minThreshold, int maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    /**
     * Removes stray pixels from the given image and crops it based on the
     * background colour.
     * 
     * @param image
     * @return
     */
    private BufferedImage process(BufferedImage image) {

        // The top-left colour is assumed to be the background colour
        int background = image.getRGB(0, 0);

        Set<ImagePixel> processedPixels = new HashSet<>();
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                
                ImagePixel thisPixel = new ImagePixel(x, y);
                
                if (image.getRGB(x, y) == background || 
                        processedPixels.contains(thisPixel)) {
                    // Pixel matches the background, or has already been processed
                    continue;
                }
                
                // Find connected non-background pixels
                Set<ImagePixel> connectedPixels = new HashSet<>();
                connectedPixels.add(thisPixel);
                findConnectedPixels(image, background, x, y, connectedPixels);
                
                // Remember pixels we have just processed
                processedPixels.addAll(connectedPixels);
                
                if (connectedPixels.size() < minThreshold ||
                        connectedPixels.size() >= maxThreshold) {
                    // This area is too small or too large to be removed
                    continue;
                }
                
                System.out.println(
                        "Removing " + connectedPixels.size() + " stray pixels");
                
                // Remove all connected pixels
                for (ImagePixel px : connectedPixels) {
                    image.setRGB(px.x, px.y, background);
                }
            }
        }
        
        return ImageUtils.crop(image, background);
    }
    
    /**
     * Recursively find connected pixels in each direction.
     * 
     * <p>This is essentially a flood-fill algorithm, but instead of changing
     * the pixel colours immediately we are just remembering which pixels do not
     * match the background colour.
     * 
     * @param image
     * @param background
     * @param x
     * @param y
     * @param connectedPixels
     */
    private void findConnectedPixels(
            BufferedImage image,
            int background,
            int x,
            int y,
            Set<ImagePixel> connectedPixels) {
        
        // Top-left
        if (x > 0 && y > 0 &&
                image.getRGB(x - 1, y - 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x - 1, y - 1));
            if (added) {
                findConnectedPixels(image, background, x - 1, y - 1, connectedPixels);                
            }
        }

        // Top-middle
        if (y > 0 &&
                image.getRGB(x, y - 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x, y - 1));
            if (added) {
                findConnectedPixels(image, background, x, y - 1, connectedPixels);                
            }
        }

        // Top-right
        if (x < image.getWidth() - 1 && y > 0 &&
                image.getRGB(x + 1, y - 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x + 1, y - 1));
            if (added) {
                findConnectedPixels(image, background, x + 1, y - 1, connectedPixels);                
            }
        }

        // Middle-left
        if (x > 0 &&
                image.getRGB(x - 1, y) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x - 1, y));
            if (added) {
                findConnectedPixels(image, background, x - 1, y, connectedPixels);                
            }
        }
        
        // Middle-right
        if (x < image.getWidth() - 1 &&
                image.getRGB(x + 1, y) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x + 1, y));
            if (added) {
                findConnectedPixels(image, background, x + 1, y, connectedPixels);                
            }
        }

        // Bottom-left
        if (x > 0 && y < image.getHeight() - 1 &&
                image.getRGB(x - 1, y + 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x - 1, y + 1));
            if (added) {
                findConnectedPixels(image, background, x - 1, y + 1, connectedPixels);                
            }
        }

        // Bottom-middle
        if (y < image.getHeight() - 1 &&
                image.getRGB(x, y + 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x, y + 1));
            if (added) {
                findConnectedPixels(image, background, x, y + 1, connectedPixels);                
            }
        }

        // Bottom-right
        if (x < image.getWidth() - 1 && y < image.getHeight() - 1 &&
                image.getRGB(x + 1, y + 1) != background) {
            boolean added = connectedPixels.add(new ImagePixel(x + 1, y + 1));
            if (added) {
                findConnectedPixels(image, background, x + 1, y + 1, connectedPixels);                
            }
        }

    }

    /**
     * Entry point for the application.
     * 
     * @param args
     */
    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println(
                    "Expected: SOURCE_FOLDER MIN_THRESHOLD MAX_THRESHOLD");
            System.exit(-1);
        }

        StrayPixelCleaner pixCleaner = null;
        
        String imageDir = args[0];

        try {
            
            int minThreshold = Integer.parseInt(args[1]);
            int maxThreshold = Integer.parseInt(args[2]);
            
            pixCleaner = new StrayPixelCleaner(minThreshold, maxThreshold);

        } catch (NumberFormatException ex) {
            System.err.println("Argument is not a valid integer!");
            System.exit(1);
        }
        
        // Find all images in directory
        System.out.println("Finding files");
        File dir = new File(imageDir);
        File[] imageFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(IMAGE_FILENAME_REGEX);
            }
        });

        if (imageFiles.length == 0) {
            System.err.println("No image files found in directory: " + 
                    dir.getAbsolutePath());
            System.exit(1);
        }

        // Remove stray pixels from each image
        for (File file : imageFiles) {
            
            BufferedImage image = null;

            // Read image
            try {
                System.out.println("Reading image: " + file);
                image = ImageIO.read(file);
            } catch (IOException ex) {
                System.err.println("Unable to read image");
                ex.printStackTrace();
                continue;
            }
            
            // Remove the stray pixels
            System.out.println("Processing...");
            image = pixCleaner.process(image);
            
            // Ensure "out" directory exists
            new File("out").mkdir();
            
            // Save modified image
            String filename = file.getName();
                
            // Remove extension
            int pos = filename.lastIndexOf(".");
            if (pos > 0) {
                filename = filename.substring(0, pos);
            }
                
            filename = "out/" + filename + ".png";
                
            try {
                ImageUtils.saveImage(image, filename);
            } catch (IOException e) {
                System.err.println("Unable to save image");
                e.printStackTrace();
            }
        }
        
        System.out.println("Success!");
    }

}
