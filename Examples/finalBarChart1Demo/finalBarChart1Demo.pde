import com.ata.charts.*; // Imports all chart classes from custom chart library package
import processing.data.Table; // Imports Processing's Table class so CSV data can be loaded

finalBarChart1 g; // Declares a variable called g that will store the bar chart object

void setup() { // Runs once at the start of the sketch to set everything up
  size(1380, 820, P2D); // Creates the window; P2D uses Processing's 2D renderer for smoother graphics

  Table t = loadTable("data2.csv", "header"); // Loads the CSV file called data2.csv and treats the first row as column headers
  if (t == null) { // Safety check in case the file is missing or fails to load
    println("data2.csv not found."); // Prints an error message to the console
    exit(); // Stops the program so the chart does not run with missing data
  }

  String[] cols = {"2023", "2024", "2025"}; // These are the value columns from the CSV that will become the bar chart series

  g = new finalBarChart1(this) // Creates a new bar chart object and passes this sketch into it so the library can draw on the canvas
    .setDataFromTable(t, "Category", cols) // Loads the chart data from the table: Category becomes labels, and the chosen year columns become values
    .setTitle("Sales by Year") // Sets the chart title shown at the top
    .setXLabel("Category") // Sets the x-axis label
    .setYLabel("Units") // Sets the y-axis label
    .setPageSize(6) // Shows 6 categories per page, useful if the dataset has many rows
    .setDefaultTopN(8) // Stores 8 as the default Top-N amount if Top-N mode is later turned on
    .setTopNEnabledByDefault(false) // Starts with Top-N filtering turned off, so the full paged dataset is shown
    .setSortBySeries("2025", finalBarChart1.SortOrder.DESCENDING) // Sorts categories using the 2025 values from highest to lowest
    .setReferenceLine(18, "Target") // Draws a horizontal target/reference line at value 18
    .setPreserveCustomPaletteAcrossModeChange(true) // Keeps the chosen colours even if the user switches between light and dark mode
    .setPaletteByName("colorblind") // Applies a colourblind-friendly palette first
    .setCustomSeriesColors( // Defines explicit colours for each series, overriding the palette when custom colours are toggled/applied
      new String[] {"2023", "2024", "2025"}, // Names of the series that will receive custom colours
      new int[] { // Colour values for those three series
        color(52, 73, 94), // Dark blue-grey colour for 2023
        color(230, 126, 34), // Orange colour for 2024
        color(26, 188, 156) // Teal colour for 2025
      }
    )
    .setBarMode(finalBarChart1.BarMode.GROUPED) // Displays the bars side by side within each category instead of stacked
    .setOrientation(finalBarChart1.Orientation.VERTICAL) // Uses the normal vertical bar chart layout
    .enableDefaultAnalyticalUI() // Turns on the built-in UI features such as legend, overlays, help, and details panel
    .setAnimationDurationMs(900) // Makes chart animations last 900 milliseconds
    .restartAnimation(); // Starts the initial animation when the chart first appears
}

void draw() { // Runs continuously every frame to keep the display updated
  background(255); // Clears the screen to white before redrawing the chart
  g.updateHover(mouseX, mouseY); // Tells the chart where the mouse is, so hover effects and tooltips work
  g.draw(0, 0, width, height); // Draws the chart so it fills the whole sketch window
}

void mousePressed() { // Runs whenever the mouse is clicked
  g.handleMousePressed(mouseX, mouseY); // Passes the click to the chart so legend toggles and bar selection work
}

void keyPressed() { // Runs whenever a keyboard key is pressed
  if (keyCode == ESC) { // Checks if the Escape key was pressed
    key = 0; // Prevents Processing's default behaviour of closing the sketch when ESC is pressed
  }
  g.handleKeyPressed(key, keyCode); // Passes the keyboard input to the chart so shortcuts like sorting, paging, or mode changes work
}
