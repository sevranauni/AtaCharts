import com.ata.charts.*;
import processing.data.Table;
import processing.data.TableRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

ScatterPlotFinal1 s;
boolean showFPS = true;

class ScatterRecord {
  String country;
  float x;
  float y;
  String year;

  ScatterRecord(String country, float x, float y, String year) {
    this.country = country;
    this.x = x;
    this.y = y;
    this.year = year;
  }
}

void setup() {
  size(1380, 820, P2D);

  Table raw = loadTable("World-happiness-report-updated_2024.csv", "header");
  if (raw == null) {
    println("World-happiness-report-updated_2024.csv not found.");
    exit();
  }

  ArrayList<ScatterRecord> records = new ArrayList<ScatterRecord>();

  for (TableRow r : raw.rows()) {
    String country = r.getString("Country name");
    String year = r.getString("year");

    float x, y;
    try {
      x = r.getFloat("Log GDP per capita");
      y = r.getFloat("Life Ladder");
    } catch (Exception e) {
      continue;
    }

    if (Float.isNaN(x) || Float.isNaN(y)) continue;

    records.add(new ScatterRecord(country, x, y, year));
  }

  Collections.sort(records, new Comparator<ScatterRecord>() {
    public int compare(ScatterRecord a, ScatterRecord b) {
      int ya = int(a.year);
      int yb = int(b.year);
      return ya - yb;
    }
  });

  Table t = new Table();
  t.addColumn("Country name");
  t.addColumn("Log GDP per capita", Table.FLOAT);
  t.addColumn("Life Ladder", Table.FLOAT);
  t.addColumn("PointSize", Table.FLOAT);
  t.addColumn("year");

  for (ScatterRecord rec : records) {
    TableRow nr = t.addRow();
    nr.setString("Country name", rec.country);
    nr.setFloat("Log GDP per capita", rec.x);
    nr.setFloat("Life Ladder", rec.y);
    nr.setFloat("PointSize", 1.0);
    nr.setString("year", rec.year);
  }

  s = new ScatterPlotFinal1(this)
    .setDataFromTable(
      t,
      "Country name",
      "Log GDP per capita",
      "Life Ladder",
      "PointSize",
      "year"
    )
    .setTitle("World Happiness Scatter Plot")
    .setXLabel("Log GDP per Capita")
    .setYLabel("Life Ladder")
    .setSizeLabel("")
    .setUseBubbleSizes(false)
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .enableDefaultAnalyticalUI()
    .setAnimationDurationMs(900)
    .restartAnimation();
}

void draw() {
  background(255);
  s.updateHover(mouseX, mouseY);
  s.draw(0, 0, width, height);
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
  s.handleMousePressed(mouseX, mouseY);
}

void keyPressed() {
  if (keyCode == ESC) {
    key = 0;
  }

  if (key == 'f' || key == 'F') {
    showFPS = !showFPS;
  }

  s.handleKeyPressed(key, keyCode);
}
