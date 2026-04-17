import com.ata.charts.*;
import processing.data.Table;

LineChartFinalDemo g;

void setup() {
  size(1380, 820, P2D);
  Table t = loadTable("data3.csv", "header");
  if (t == null) { println("data3.csv not found."); exit(); }

  String[] cols = {"2019","2020","2021","2022","2023","2024","2025","2026"};

  g = new LineChartFinalDemo(this)
    .setDataFromTable(t, "Category", cols)
    .setTitle("Sales Trend Line Chart")
    .setXLabel("Year / Category")
    .setYLabel("Units")
    .setAxisInterpretation(LineChartFinalDemo.AxisInterpretation.SERIES_ON_X)
    .setPageSize(6)
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
    .setReferenceLine(18, "Target")
    .enableDefaultAnalyticalUI()
    .setAnimationDurationMs(900)
    .restartAnimation();
}

void draw() {
  background(255);
  g.updateHover(mouseX, mouseY);
  g.draw(0, 0, width, height);
}

void mousePressed() {
  g.handleMousePressed(mouseX, mouseY);
}

void keyPressed() {
  if (keyCode == ESC) key = 0;
  g.handleKeyPressed(key, keyCode);
}
