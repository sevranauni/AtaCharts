import com.ata.charts.*;
import processing.data.Table;

ScatterPlotFinal1 g;

void setup() {
  size(1380, 820, P2D);

  Table t = loadTable("gapminder_scatter.csv", "header");
  if (t == null) {
    println("gapminder_scatter.csv not found.");
    exit();
  }

  g = new ScatterPlotFinal1(this)
    .setDataFromTable(
      t,
      "Country",
      "GDPPerCapita",
      "LifeExpectancy",
      null,
      "Continent"
    )
    .setTitle("GDP per Capita vs Life Expectancy")
    .setXLabel("GDP per Capita")
    .setYLabel("Life Expectancy")
    .setUseBubbleSizes(false)
    .setPointSizeRange(10, 10)
    .setPreserveCustomPaletteAcrossModeChange(true)
    .setPaletteByName("colorblind")
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
  if (keyCode == ESC) {
    key = 0;
  }
  g.handleKeyPressed(key, keyCode);
}
