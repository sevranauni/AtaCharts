import com.ata.charts.*;
import processing.data.Table;
import processing.data.TableRow;
import java.util.ArrayList;
import java.util.LinkedHashMap;

finalBarChart1 g;
boolean showFPS = true;

String DATA_FILE = "World-happiness-report-updated_2024.csv";
String METRIC_COLUMN = "Life Ladder";

int[] YEARS = {2013, 2018, 2023};

void setup() {
  size(1380, 820, P2D);

  Table t = loadTable(DATA_FILE, "header");
  if (t == null) {
    println(DATA_FILE + " not found.");
    exit();
  }

  LinkedHashMap<String, float[]> byCountry = new LinkedHashMap<String, float[]>();

  for (TableRow row : t.rows()) {
    String country = row.getString("Country name");
    int year = row.getInt("year");
    float value = row.getFloat(METRIC_COLUMN);

    int yearIndex = indexOfYear(year);
    if (yearIndex == -1) continue;

    if (!byCountry.containsKey(country)) {
      float[] arr = new float[YEARS.length];
      for (int i = 0; i < arr.length; i++) arr[i] = Float.NaN;
      byCountry.put(country, arr);
    }

    byCountry.get(country)[yearIndex] = value;
  }

  ArrayList<String> categoriesList = new ArrayList<String>();
  ArrayList<float[]> valuesList = new ArrayList<float[]>();

  float refSum = 0;
  int refCount = 0;

  for (String country : byCountry.keySet()) {
    float[] vals = byCountry.get(country);

    boolean complete = true;
    for (int i = 0; i < vals.length; i++) {
      if (Float.isNaN(vals[i])) {
        complete = false;
        break;
      }
    }

    if (!complete) continue;

    categoriesList.add(country);
    valuesList.add(vals);

    refSum += vals[YEARS.length - 1];
    refCount++;
  }

  String[] categories = new String[categoriesList.size()];
  float[][] values = new float[categoriesList.size()][YEARS.length];
  for (int i = 0; i < categoriesList.size(); i++) {
    categories[i] = categoriesList.get(i);
    values[i] = valuesList.get(i);
  }

  String[] seriesNames = new String[YEARS.length];
  for (int i = 0; i < YEARS.length; i++) {
    seriesNames[i] = str(YEARS[i]);
  }

  float referenceValue = refCount > 0 ? refSum / refCount : 0;

  g = new finalBarChart1(this)
    .setData(categories, seriesNames, values)
    .setTitle("World Happiness by Country")
    .setXLabel("Country")
    .setYLabel("Life Ladder")
    .setPageSize(8)
    .setDefaultTopN(10)
    .setTopNEnabledByDefault(false)
    .setSortBySeries("2023", finalBarChart1.SortOrder.DESCENDING)
    .setReferenceLine(referenceValue, "2023 Avg")
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .setCustomSeriesColors(
      new String[] {"2013", "2018", "2023"},
      new int[] {
        color(52, 73, 94),
        color(230, 126, 34),
        color(26, 188, 156)
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

int indexOfYear(int year) {
  for (int i = 0; i < YEARS.length; i++) {
    if (YEARS[i] == year) return i;
  }
  return -1;
}
