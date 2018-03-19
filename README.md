# Stray Pixel Cleaner

A simple program to remove stray pixels from an image.

![Example](docs/example.png)

## Compile

From the `src` directory:

    javac main/*.java

## Run

From the `src` directory:

    java main.StrayPixelCleaner SOURCE_FOLDER MIN_THRESHOLD MAX_THRESHOLD

Images are saved to an `out` directory.

### Parameters

#### SOURCE_FOLDER

Name of the directory containing the images to be processed.

#### MIN_THRESHOLD

Minimum number of pixels that must be connected before they are considered "stray". If set to 1, a single pixel is considered stray. If set to 3, areas of only 1 or 2 connected pixels are preserved.

Pixels are considered to be "connected" if they are orthogonally or diagonally adjacent.

#### MAX_THRESHOLD

Minimum number of pixels that must be connected in order for them to no longer be considered "stray". For example, if set to 10, areas of 10 or more connected pixels are preserved.

Pixels are considered to be "connected" if they are orthogonally or diagonally adjacent.

### Example

    java main.StrayPixelCleaner images 1 10
