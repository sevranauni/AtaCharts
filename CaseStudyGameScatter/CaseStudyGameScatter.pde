import com.ata.charts.*;
import processing.data.Table;
import processing.data.TableRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

ScatterPlotFinal1 s;
boolean showFPS = true;

String DATA_FILE = "vgsales.csv";
int MAX_POINTS = 500;   // keep only top-selling games for readability

class GameRecord {
  String name;
  String genre;
  float naSales;
  float euSales;
  float globalSales;

  GameRecord(String name, String genre, float naSales, float euSales, float globalSales) {
    this.name = name;
    this.genre = genre;
    this.naSales = naSales;
    this.euSales = euSales;
    this.globalSales = globalSales;
  }
}

void setup() {
  size(1380, 820, P2D);

  Table raw = loadTable(DATA_FILE, "header");
  if (raw == null) {
    println(DATA_FILE + " not found.");
    exit();
  }

  ArrayList<GameRecord> records = new ArrayList<GameRecord>();

  for (TableRow r : raw.rows()) {
    String name = r.getString("Name");
    String genre = r.getString("Genre");

    float naSales, euSales, globalSales;
    try {
      naSales = r.getFloat("NA_Sales");
      euSales = r.getFloat("EU_Sales");
      globalSales = r.getFloat("Global_Sales");
    } catch (Exception e) {
      continue;
    }

    if (Float.isNaN(naSales) || Float.isNaN(euSales) || Float.isNaN(globalSales)) continue;
    if (name == null || trim(name).length() == 0) continue;
    if (genre == null || trim(genre).length() == 0) genre = "Unknown";

    records.add(new GameRecord(name, genre, naSales, euSales, globalSales));
  }

  // Keep the top-selling games so the scatter plot stays interpretable
  Collections.sort(records, new Comparator<GameRecord>() {
    public int compare(GameRecord a, GameRecord b) {
      return Float.compare(b.globalSales, a.globalSales);
    }
  });

  int n = min(MAX_POINTS, records.size());

  // Build a clean table with constant point size
  Table t = new Table();
  t.addColumn("Name");
  t.addColumn("NA_Sales", Table.FLOAT);
  t.addColumn("EU_Sales", Table.FLOAT);
  t.addColumn("PointSize", Table.FLOAT);
  t.addColumn("Genre");

  for (int i = 0; i < n; i++) {
    GameRecord rec = records.get(i);

    TableRow nr = t.addRow();
    nr.setString("Name", rec.name);
    nr.setFloat("NA_Sales", rec.naSales);
    nr.setFloat("EU_Sales", rec.euSales);
    nr.setFloat("PointSize", 1.0);
    nr.setString("Genre", rec.genre);
  }

  s = new ScatterPlotFinal1(this)
    .setDataFromTable(
      t,
      "Name",       // label
      "NA_Sales",   // x
      "EU_Sales",   // y
      "PointSize",  // constant size
      "Genre"       // group
    )
    .setTitle("Video Game Sales Scatter Plot")
    .setXLabel("North America Sales (Millions)")
    .setYLabel("Europe Sales (Millions)")
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
