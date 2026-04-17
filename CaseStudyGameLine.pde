import com.ata.charts.*;
import processing.data.Table;
import processing.data.TableRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.TreeSet;

LineChartFinalDemo g;
boolean showFPS = true;

String DATA_FILE = "vgsales.csv";
int TOP_N_PLATFORMS = 6;

class PlatformSales {
  String platform;
  float totalSales;

  PlatformSales(String platform, float totalSales) {
    this.platform = platform;
    this.totalSales = totalSales;
  }
}

void setup() {
  size(1380, 820, P2D);

  Table raw = loadTable(DATA_FILE, "header");
  if (raw == null) {
    println(DATA_FILE + " not found.");
    exit();
  }

  TreeSet<Integer> yearSet = new TreeSet<Integer>();
  LinkedHashMap<String, LinkedHashMap<Integer, Float>> salesByPlatform =
    new LinkedHashMap<String, LinkedHashMap<Integer, Float>>();
  LinkedHashMap<String, Float> totalByPlatform =
    new LinkedHashMap<String, Float>();

  for (TableRow row : raw.rows()) {
    String platform = row.getString("Platform");

    if (platform == null || trim(platform).length() == 0) continue;

    int year;
    float sales;

    try {
      String yearText = trim(row.getString("Year"));
      if (yearText == null || yearText.length() == 0 || yearText.equals("N/A")) continue;

      year = round(Float.parseFloat(yearText));
      sales = row.getFloat("Global_Sales");
    } catch (Exception e) {
      continue;
    }

    if (Float.isNaN(sales)) continue;

    yearSet.add(year);

    if (!salesByPlatform.containsKey(platform)) {
      salesByPlatform.put(platform, new LinkedHashMap<Integer, Float>());
    }

    LinkedHashMap<Integer, Float> yearly = salesByPlatform.get(platform);
    if (!yearly.containsKey(year)) {
      yearly.put(year, 0.0);
    }
    yearly.put(year, yearly.get(year) + sales);

    if (!totalByPlatform.containsKey(platform)) {
      totalByPlatform.put(platform, 0.0);
    }
    totalByPlatform.put(platform, totalByPlatform.get(platform) + sales);
  }

  ArrayList<PlatformSales> ranking = new ArrayList<PlatformSales>();
  for (String platform : totalByPlatform.keySet()) {
    ranking.add(new PlatformSales(platform, totalByPlatform.get(platform)));
  }

  Collections.sort(ranking, new Comparator<PlatformSales>() {
    public int compare(PlatformSales a, PlatformSales b) {
      return Float.compare(b.totalSales, a.totalSales);
    }
  });

  int selectedCount = min(TOP_N_PLATFORMS, ranking.size());
  String[] selectedPlatforms = new String[selectedCount];
  for (int i = 0; i < selectedCount; i++) {
    selectedPlatforms[i] = ranking.get(i).platform;
  }

  // Build wide table:
  // Year | Wii | DS | PS2 | ...
  Table pivot = new Table();
  pivot.addColumn("Year");
  for (int i = 0; i < selectedPlatforms.length; i++) {
    pivot.addColumn(selectedPlatforms[i], Table.FLOAT);
  }

  float refSum = 0;
  int refCount = 0;

  for (Integer year : yearSet) {
    TableRow newRow = pivot.addRow();
    newRow.setString("Year", str(year));

    float yearTotal = 0;
    int yearSeriesCount = 0;

    for (int i = 0; i < selectedPlatforms.length; i++) {
      String platform = selectedPlatforms[i];
      LinkedHashMap<Integer, Float> yearly = salesByPlatform.get(platform);

      float value = Float.NaN;
      if (yearly != null && yearly.containsKey(year)) {
        value = yearly.get(year);
      }

      newRow.setFloat(platform, value);

      if (!Float.isNaN(value)) {
        yearTotal += value;
        yearSeriesCount++;
      }
    }

    if (yearSeriesCount > 0) {
      refSum += yearTotal / yearSeriesCount;
      refCount++;
    }
  }

  float referenceValue = refCount > 0 ? refSum / refCount : 0;

  g = new LineChartFinalDemo(this)
    .setDataFromTable(pivot, "Year", selectedPlatforms)
    .setTitle("Global Video Game Sales Trends by Platform")
    .setXLabel("Year")
    .setYLabel("Global Sales (Millions)")
    .setAxisInterpretation(LineChartFinalDemo.AxisInterpretation.CATEGORIES_ON_X)
    .setPageSize(30)
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .setReferenceLine(referenceValue, "Avg")
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
