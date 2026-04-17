import com.ata.charts.*;
import processing.data.Table;
import processing.data.TableRow;
import java.util.ArrayList;
import java.util.LinkedHashMap;

finalBarChart1 g;
boolean showFPS = true;

String DATA_FILE = "vgsales.csv";

// Regional sales series for grouped comparison
String[] SALES_COLS = {"NA_Sales", "EU_Sales", "JP_Sales", "Other_Sales"};

void setup() {
  size(1380, 820, P2D);

  Table t = loadTable(DATA_FILE, "header");
  if (t == null) {
    println(DATA_FILE + " not found.");
    exit();
  }

  // Aggregate regional sales by genre
  LinkedHashMap<String, float[]> byGenre = new LinkedHashMap<String, float[]>();

  for (TableRow row : t.rows()) {
    String genre = row.getString("Genre");
    if (genre == null || trim(genre).length() == 0) continue;

    if (!byGenre.containsKey(genre)) {
      byGenre.put(genre, new float[SALES_COLS.length]);
    }

    float[] sums = byGenre.get(genre);

    for (int i = 0; i < SALES_COLS.length; i++) {
      float v = 0;
      try {
        v = row.getFloat(SALES_COLS[i]);
      } catch (Exception e) {
        v = 0;
      }

      if (!Float.isNaN(v)) {
        sums[i] += v;
      }
    }
  }

  ArrayList<String> categoriesList = new ArrayList<String>();
  ArrayList<float[]> valuesList = new ArrayList<float[]>();

  float totalOfAllBars = 0;
  int totalBarCount = 0;

  for (String genre : byGenre.keySet()) {
    float[] vals = byGenre.get(genre);

    categoriesList.add(genre);
    valuesList.add(vals);

    for (int i = 0; i < vals.length; i++) {
      totalOfAllBars += vals[i];
      totalBarCount++;
    }
  }

  String[] categories = new String[categoriesList.size()];
  float[][] values = new float[categoriesList.size()][SALES_COLS.length];

  for (int i = 0; i < categoriesList.size(); i++) {
    categories[i] = categoriesList.get(i);
    values[i] = valuesList.get(i);
  }

  float referenceValue = totalBarCount > 0 ? totalOfAllBars / totalBarCount : 0;

  g = new finalBarChart1(this)
    .setData(categories, SALES_COLS, values)
    .setTitle("Video Game Sales by Genre")
    .setXLabel("Genre")
    .setYLabel("Sales (Millions)")
    .setPageSize(8)
    .setDefaultTopN(8)
    .setTopNEnabledByDefault(false)
    .setSortBySeries("NA_Sales", finalBarChart1.SortOrder.DESCENDING)
    .setReferenceLine(referenceValue, "Avg")
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .setCustomSeriesColors(
      new String[] {"NA_Sales", "EU_Sales", "JP_Sales", "Other_Sales"},
      new int[] {
        color(52, 152, 219),
        color(46, 204, 113),
        color(241, 196, 15),
        color(231, 76, 60)
      }
    )
    .setBarMode(finalBarChart1.BarMode.GROUPED)
    .setOrientation(finalBarChart1.Orientation.VERTICAL)
    .enableDefaultAnalyticalUI()
    .setAnimationDurationMs(900)
    .restartAnimation();
}

void draw() {
  background(255);
  g.updateHover(mouseX, mouseY);
  g.draw(0, 0, width, height);
  drawFPSOverlay();
}

void drawFPSOverlay() {
  if (!showFPS) return;

  fill(0);
  textAlign(LEFT, TOP);
  textSize(12);
  text("FPS: " + nf(frameRate, 0, 2), 8, 8);
}

void mousePressed() {
  g.handleMousePressed(mouseX, mouseY);
}

void keyPressed() {
  if (keyCode == ESC) {
    key = 0;
  }

  if (key == 'f' || key == 'F') {
    showFPS = !showFPS;
  }

  g.handleKeyPressed(key, keyCode);
}
