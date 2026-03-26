import com.ata.charts.*; // Imports your custom chart library (needed to use LineChartFinal2)
import processing.data.Table; // Allows loading CSV data into a table structure

LineChartFinal2 l; // Declares the line chart object

void setup() { // Runs once at the start to initialise the sketch
  size(1380, 820, P2D); // Creates the window using Processing's 2D renderer

  Table t = loadTable("data2.csv", "header"); // Loads dataset from CSV file, using first row as column headers
  if (t == null) { // Checks if the file failed to load
    println("data2.csv not found."); // Prints error message
    exit(); // Stops the program to avoid running with missing data
  }

  String[] cols = {"2023", "2024", "2025"}; // Defines which columns from the dataset will be used as series

  l = new LineChartFinal2(this) // Creates the line chart object and links it to this sketch for drawing
    .setDataFromTable(t, "Category", cols) // Loads data: Category becomes labels, and selected columns become line series
    .setTranspose(true) // Transposes the data: years become the x-axis, and categories become separate lines
    .setTitle("Sales Trend by Year") // Sets chart title
    .setXLabel("Year") // Labels x-axis (important after transpose, since years are now on x-axis)
    .setYLabel("Units") // Labels y-axis
    .setPageSize(8) // Limits number of categories (lines) shown per page
    .setReferenceLine(18, "Target") // Adds a horizontal reference line (e.g., target value)
    .setPreserveCustomPaletteAcrossModeChange(true) // Keeps colours consistent when switching light/dark mode
    .setPaletteByName("colorblind") // Applies a colourblind-friendly palette
    .enableDefaultAnalyticalUI() // Enables UI features (legend, overlays, interaction)
    .setAnimationDurationMs(900) // Sets animation duration
    .restartAnimation(); // Starts animation when chart first appears
}

void draw() { // Runs continuously (frame loop)
  background(255); // Clears screen before redrawing
  l.updateHover(mouseX, mouseY); // Updates which point is being hovered (for tooltip and highlight)
  l.draw(0, 0, width, height); // Draws the chart across the full window
}

void mousePressed() { // Runs when mouse is clicked
  l.handleMousePressed(mouseX, mouseY); // Handles selection and legend interaction
}

void keyPressed() { // Runs when a key is pressed
  if (keyCode == ESC) { // Checks for escape key
    key = 0; // Prevents Processing from closing the sketch
  }
  l.handleKeyPressed(key, keyCode); // Passes keyboard input to chart (zoom, toggles, paging, etc.)

  if (key == 'q' || key == 'Q') { // Custom interaction: pressing Q changes axis label dynamically
    if (l.isTransposed()) { // If chart is currently transposed
      l.setXLabel("Category"); // Update x-axis label to reflect category-based view
    } else {
      l.setXLabel("Year"); // Otherwise keep x-axis label as "Year"
    }
  }
}
