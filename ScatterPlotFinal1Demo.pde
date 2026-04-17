import com.ata.charts.*; // Imports your custom chart library so ScatterPlotFinal1 can be used
import processing.data.Table; // Imports Processing's Table class for loading CSV data

ScatterPlotFinal1 s; // Declares the scatter plot object

void setup() { // Runs once at the start to initialise everything
  size(1380, 820, P2D); // Creates the window using Processing's 2D renderer

  Table t = loadTable("gapminder_scatter.csv", "header"); // Loads dataset (Gapminder-style) with column headers
  if (t == null) { // Checks if file failed to load
    println("gapminder_scatter.csv not found."); // Prints error message
    exit(); // Stops the program to prevent further errors
  }

  s = new ScatterPlotFinal1(this) // Creates scatter plot object and links it to the sketch for rendering
    .setDataFromTable( // Loads and maps data from the table into the chart
      t,
      "Country", // Each point represents a country (label)
      "GDPPerCapita", // X-axis values (economic indicator)
      "LifeExpectancy", // Y-axis values (health indicator)
      "PopulationMillions", // Controls bubble size (third variable)
      "Continent" // Groups data by continent (used for colours and legend)
    )
    .setTitle("Gapminder-Style Scatter Plot") // Sets chart title
    .setXLabel("GDP per Capita") // Labels x-axis
    .setYLabel("Life Expectancy") // Labels y-axis
    .setSizeLabel("Population (Millions)") // Labels the bubble size dimension (shown in tooltip/details)
    .setPreserveCustomPaletteAcrossModeChange(true) // Keeps colours consistent when switching between light/dark mode
    .setPaletteByName("colorblind") // Applies a colourblind-friendly palette for accessibility
    .setCustomGroupColors( // Defines specific colours for each continent
      new String[] {"Asia", "Europe", "Africa", "Americas", "Oceania"}, // Group names
      new int[] {
        color(52, 152, 219), // Blue for Asia
        color(155, 89, 182), // Purple for Europe
        color(46, 204, 113), // Green for Africa
        color(230, 126, 34), // Orange for Americas
        color(231, 76, 60) // Red for Oceania
      }
    )
    .enableDefaultAnalyticalUI() // Enables legend, overlays, help panel, and interaction features
    .setAnimationDurationMs(900) // Sets animation duration for transitions
    .restartAnimation(); // Starts the animation when the chart is first displayed
}

void draw() { // Runs continuously (frame loop)
  background(255); // Clears the screen to white before drawing each frame
  s.updateHover(mouseX, mouseY); // Updates which point is being hovered (for highlighting and tooltip)
  s.draw(0, 0, width, height); // Draws the scatter plot across the entire window
}

void mousePressed() { // Runs when the user clicks the mouse
  s.handleMousePressed(mouseX, mouseY); // Handles selection and legend interaction
}

void keyPressed() { // Runs when any key is pressed
  if (keyCode == ESC) { // Checks if escape key is pressed
    key = 0; // Prevents Processing from closing the sketch automatically
  }
  s.handleKeyPressed(key, keyCode); // Passes keyboard input to chart (zoom, pan, toggles, etc.)
}
