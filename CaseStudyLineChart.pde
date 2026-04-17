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

String DATA_FILE = "World-happiness-report-updated_2024.csv";
String METRIC_COLUMN = "Life Ladder";
int TOP_N_COUNTRIES = 8;

class CountryScore {
  String country;
  float value;

  CountryScore(String country, float value) {
    this.country = country;
    this.value = value;
  }
}

void setup() {
  size(1380, 820, P2D);

  Table raw = loadTable(DATA_FILE, "header");
  if (raw == null) {
    println(DATA_FILE + " not found.");
    exit();
  }

  // Collect all years and store values by country/year
  TreeSet<Integer> yearSet = new TreeSet<Integer>();
  LinkedHashMap<String, LinkedHashMap<Integer, Float>> valuesByCountry =
    new LinkedHashMap<String, LinkedHashMap<Integer, Float>>();

  int latestYear = -1;

  for (TableRow row : raw.rows()) {
    String country = row.getString("Country name");

    int year;
    float value;

    try {
      year = row.getInt("year");
      value = row.getFloat(METRIC_COLUMN);
    } catch (Exception e) {
      continue;
    }

    if (Float.isNaN(value)) continue;

    yearSet.add(year);
    latestYear = max(latestYear, year);

    if (!valuesByCountry.containsKey(country)) {
      valuesByCountry.put(country, new LinkedHashMap<Integer, Float>());
    }
    valuesByCountry.get(country).put(year, value);
  }

  // Rank countries by latest available year
  ArrayList<CountryScore> ranking = new ArrayList<CountryScore>();

  for (String country : valuesByCountry.keySet()) {
    LinkedHashMap<Integer, Float> yearly = valuesByCountry.get(country);
    if (yearly.containsKey(latestYear)) {
      ranking.add(new CountryScore(country, yearly.get(latestYear)));
    }
  }

  Collections.sort(ranking, new Comparator<CountryScore>() {
    public int compare(CountryScore a, CountryScore b) {
      return Float.compare(b.value, a.value);
    }
  });

  int chosenCount = min(TOP_N_COUNTRIES, ranking.size());
  String[] selectedCountries = new String[chosenCount];
  for (int i = 0; i < chosenCount; i++) {
    selectedCountries[i] = ranking.get(i).country;
  }

  // Build a wide table:
  // year | Finland | Denmark | ... etc.
  Table pivot = new Table();
  pivot.addColumn("year");
  for (int i = 0; i < selectedCountries.length; i++) {
    pivot.addColumn(selectedCountries[i], Table.FLOAT);
  }

  for (Integer year : yearSet) {
    TableRow newRow = pivot.addRow();
    newRow.setString("year", str(year));

    for (int i = 0; i < selectedCountries.length; i++) {
      String country = selectedCountries[i];
      LinkedHashMap<Integer, Float> yearly = valuesByCountry.get(country);

      if (yearly != null && yearly.containsKey(year)) {
        newRow.setFloat(country, yearly.get(year));
      } else {
        newRow.setFloat(country, Float.NaN);
      }
    }
  }

  g = new LineChartFinalDemo(this)
    .setDataFromTable(pivot, "year", selectedCountries)
    .setTitle("World Happiness Trends: Top Countries")
    .setXLabel("Year")
    .setYLabel("Life Ladder")
    .setAxisInterpretation(LineChartFinalDemo.AxisInterpretation.CATEGORIES_ON_X)
    .setPageSize(20)
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .setReferenceLine(5.5, "Global Midpoint")
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
