package com.ata.charts;

import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LineChartFinalDemo {

    public enum SortOrder { ASCENDING, DESCENDING }
    public enum AxisInterpretation { CATEGORIES_ON_X, SERIES_ON_X }

    private static class NiceAxis {
        float min;
        float max;
        float step;
        int tickCount;
    }

    private final PApplet p;

    private String title = "";
    private String xLabel = "";
    private String yLabel = "";

    // Raw input data from table
    private String[] rowNames;          // e.g. fruit categories
    private String[] columnNames;       // e.g. 2023, 2024, 2025
    private float[][] sourceValues;     // [column][row]

    // Current plotted data after interpretation + paging
    private String[] plotXLabels;
    private String[] plotSeriesNames;
    private float[][] plotValues;       // [series][x]

    private final HashMap<String, Boolean> visibilityBySeriesName = new HashMap<String, Boolean>();
    private boolean[] seriesVisible;

    private int hoveredSeries = -1;
    private int hoveredIndex = -1;
    private int selectedSeries = -1;
    private int selectedIndex = -1;

    private boolean gridVisible = true;
    private boolean helpVisible = true;
    private boolean statusVisible = true;
    private boolean detailsVisible = true;
    private boolean pointLabelsVisible = false;
    private boolean averageLineVisible = false;
    private boolean legendInteractive = true;
    private boolean animationEnabled = true;
    private boolean preserveCustomPaletteAcrossModeChange = true;
    private boolean darkMode = false;
    private boolean customColorsActive = false;

    private AxisInterpretation axisInterpretation = AxisInterpretation.CATEGORIES_ON_X;

    private int pageSize = Integer.MAX_VALUE;
    private int currentPage = 0;

    private int[] palette;
    private int[] currentSeriesColors;
    private int[] currentHoverColors;
    private int[] currentSelectedColors;
    private int[] savedPaletteBeforeCustom;

    private int backgroundColor;
    private int axisColor;
    private int gridColor;
    private int textColor;
    private int titleColor;
    private int tooltipFill;
    private int tooltipStroke;
    private int overlayFill;
    private int overlayStroke;
    private int legendFill;
    private int legendStroke;
    private int averageLineColor;
    private int referenceLineColor;
    private int selectedMarkerColor;

    private float leftPad = 90;
    private float rightPad = 170;
    private float topPad = 70;
    private float bottomPad = 95;
    private float pointRadius = 5f;
    private float hitRadius = 10f;

    private long animationStartMs = 0;
    private int animationDurationMs = 900;

    private float referenceLineValue = Float.NaN;
    private String referenceLineLabel = "";

    private final LinkedHashMap<String, Integer> pendingNamedSeriesColors = new LinkedHashMap<String, Integer>();

    private int[] pointScreenX;
    private int[] pointScreenY;
    private int[] pointScreenSeries;
    private int[] pointScreenCategory;
    private int pointCount = 0;
    private boolean geometryValid = false;

    public LineChartFinalDemo(PApplet p) {
        this.p = p;
        applyLightThemeColors();
        palette = defaultLightPalette();
    }

    public LineChartFinalDemo setTitle(String t) {
        this.title = (t == null) ? "" : t;
        return this;
    }

    public LineChartFinalDemo setXLabel(String t) {
        this.xLabel = (t == null) ? "" : t;
        return this;
    }

    public LineChartFinalDemo setYLabel(String t) {
        this.yLabel = (t == null) ? "" : t;
        return this;
    }

    public LineChartFinalDemo setLegendInteractive(boolean enabled) {
        this.legendInteractive = enabled;
        return this;
    }

    public LineChartFinalDemo setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
        return this;
    }

    public LineChartFinalDemo setAnimationDurationMs(int ms) {
        if (ms > 0) this.animationDurationMs = ms;
        return this;
    }

    public LineChartFinalDemo setPreserveCustomPaletteAcrossModeChange(boolean preserve) {
        this.preserveCustomPaletteAcrossModeChange = preserve;
        return this;
    }

    public LineChartFinalDemo setReferenceLine(float value, String label) {
        this.referenceLineValue = value;
        this.referenceLineLabel = (label == null) ? "" : label;
        return this;
    }

    public LineChartFinalDemo setPageSize(int n) {
        if (n > 0) this.pageSize = n;
        clampCurrentPage();
        rebuildPlotData();
        return this;
    }

    public LineChartFinalDemo setAxisInterpretation(AxisInterpretation interpretation) {
        if (interpretation != null) {
            clearPointBuffers();
            selectedSeries = -1;
            selectedIndex = -1;
            this.axisInterpretation = interpretation;
            currentPage = 0;
            rebuildPlotData();
            clearPointBuffers();
            restartAnimation();
        }
        return this;
    }

    public LineChartFinalDemo setTranspose(boolean transpose) {
        return setAxisInterpretation(
            transpose ? AxisInterpretation.SERIES_ON_X : AxisInterpretation.CATEGORIES_ON_X
        );
    }

    public boolean isTransposed() {
        return axisInterpretation == AxisInterpretation.SERIES_ON_X;
    }

    public LineChartFinalDemo toggleAxisInterpretation() {
        clearPointBuffers();
        selectedSeries = -1;
        selectedIndex = -1;
        axisInterpretation = isTransposed()
            ? AxisInterpretation.CATEGORIES_ON_X
            : AxisInterpretation.SERIES_ON_X;
        rebuildPlotData();
        clearPointBuffers();
        restartAnimation();
        return this;
    }

    public LineChartFinalDemo enableDefaultAnalyticalUI() {
        helpVisible = true;
        statusVisible = true;
        detailsVisible = true;
        gridVisible = true;
        return this;
    }

    public LineChartFinalDemo restartAnimation() {
        animationStartMs = p.millis();
        return this;
    }

    public LineChartFinalDemo setDataFromTable(Table t, String categoryCol, String[] valueCols) {
        if (t == null || categoryCol == null || valueCols == null || valueCols.length == 0) return this;

        int n = t.getRowCount();
        rowNames = new String[n];
        float[][] values = new float[valueCols.length][n];

        int rowIndex = 0;
        for (TableRow row : t.rows()) {
            rowNames[rowIndex] = row.getString(categoryCol);
            for (int s = 0; s < valueCols.length; s++) {
                try {
                    values[s][rowIndex] = row.getFloat(valueCols[s]);
                } catch (Exception e) {
                    values[s][rowIndex] = Float.NaN;
                }
            }
            rowIndex++;
        }

        columnNames = Arrays.copyOf(valueCols, valueCols.length);
        sourceValues = values;
        visibilityBySeriesName.clear();
        rebuildPlotData();
        restartAnimation();
        return this;
    }

    public LineChartFinalDemo setPalette(int[] colors) {
        if (colors != null && colors.length > 0) {
            palette = Arrays.copyOf(colors, colors.length);
            customColorsActive = false;
            ensurePaletteCapacity();
            applyPendingNamedColors();
        }
        return this;
    }

    public LineChartFinalDemo setPaletteByName(String paletteName) {
        if (paletteName == null) return this;
        String key = paletteName.trim().toLowerCase();

        if ("default".equals(key)) {
            palette = darkMode ? defaultDarkPalette() : defaultLightPalette();
        } else if ("colorblind".equals(key)) {
            palette = new int[] {
                0xff0072B2, 0xffE69F00, 0xff009E73, 0xffD55E00,
                0xffCC79A7, 0xff56B4E9, 0xffF0E442, 0xff000000
            };
        } else if ("pastel".equals(key)) {
            palette = new int[] {
                0xff8DD3C7, 0xffFFFFB3, 0xffBEBADA, 0xffFB8072,
                0xff80B1D3, 0xffFDB462, 0xffB3DE69, 0xffFCCDE5
            };
        } else if ("highcontrast".equals(key)) {
            palette = new int[] {
                0xff1F77B4, 0xffD62728, 0xff2CA02C, 0xff9467BD,
                0xffFF7F0E, 0xff17BECF, 0xff8C564B, 0xffE377C2
            };
        }

        customColorsActive = false;
        ensurePaletteCapacity();
        applyPendingNamedColors();
        restartAnimation();
        return this;
    }

    public LineChartFinalDemo setSeriesColor(String seriesName, int color) {
        if (seriesName == null) return this;
        pendingNamedSeriesColors.put(seriesName, color);
        applyPendingNamedColors();
        return this;
    }

    public LineChartFinalDemo setCustomSeriesColors(String[] seriesNames, int[] colors) {
        if (seriesNames == null || colors == null || seriesNames.length == 0 || colors.length == 0) return this;
        int n = Math.min(seriesNames.length, colors.length);
        pendingNamedSeriesColors.clear();
        for (int i = 0; i < n; i++) {
            pendingNamedSeriesColors.put(seriesNames[i], colors[i]);
        }
        applyPendingNamedColors();
        return this;
    }

    public LineChartFinalDemo toggleCustomColors() {
        if (plotSeriesNames == null) return this;

        if (customColorsActive) {
            customColorsActive = false;
            if (savedPaletteBeforeCustom != null) {
                palette = Arrays.copyOf(savedPaletteBeforeCustom, savedPaletteBeforeCustom.length);
            }
            ensurePaletteCapacity();
            applyPendingNamedColors();
        } else {
            savedPaletteBeforeCustom = (palette == null) ? null : Arrays.copyOf(palette, palette.length);
            pendingNamedSeriesColors.clear();
            for (int i = 0; i < plotSeriesNames.length; i++) {
                pendingNamedSeriesColors.put(plotSeriesNames[i], vividPresetColor(i, plotSeriesNames.length));
            }
            customColorsActive = true;
            ensurePaletteCapacity();
            applyPendingNamedColors();
        }

        restartAnimation();
        return this;
    }

    public LineChartFinalDemo toggleMode() {
        if (darkMode) setLightMode(); else setDarkMode();
        return this;
    }

    public LineChartFinalDemo setLightMode() {
        darkMode = false;
        applyLightThemeColors();
        if (!preserveCustomPaletteAcrossModeChange) palette = defaultLightPalette();
        ensurePaletteCapacity();
        applyPendingNamedColors();
        return this;
    }

    public LineChartFinalDemo setDarkMode() {
        darkMode = true;
        applyDarkThemeColors();
        if (!preserveCustomPaletteAcrossModeChange) palette = defaultDarkPalette();
        ensurePaletteCapacity();
        applyPendingNamedColors();
        return this;
    }

    public void updateHover(int mouseX, int mouseY) {
        hoveredSeries = -1;
        hoveredIndex = -1;

        if (!geometryValid) return;
        if (pointScreenX == null || pointScreenY == null) return;
        if (pointScreenSeries == null || pointScreenCategory == null) return;

        int n = Math.min(
            Math.min(pointCount, Math.min(pointScreenX.length, pointScreenY.length)),
            Math.min(pointScreenSeries.length, pointScreenCategory.length)
        );

        for (int i = 0; i < n; i++) {
            int sx = pointScreenX[i];
            int sy = pointScreenY[i];

            float dx = mouseX - sx;
            float dy = mouseY - sy;
            float rr = pointRadius + 4f;

            if (dx * dx + dy * dy <= rr * rr) {
                int s = pointScreenSeries[i];
                int c = pointScreenCategory[i];

                if (s >= 0 && c >= 0) {
                    hoveredSeries = s;
                    hoveredIndex = c;
                    return;
                }
            }
        }
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (hitLegend(mouseX, mouseY)) return;

        int foundSeries = -1;
        int foundIndex = -1;
        float bestDist = Float.MAX_VALUE;

        if (pointScreenX != null && pointScreenY != null &&
            pointScreenSeries != null && pointScreenCategory != null) {

            int n = Math.min(
                Math.min(pointCount, Math.min(pointScreenX.length, pointScreenY.length)),
                Math.min(pointScreenSeries.length, pointScreenCategory.length)
            );

            for (int i = 0; i < n; i++) {
                float dx = mouseX - pointScreenX[i];
                float dy = mouseY - pointScreenY[i];
                float d2 = dx * dx + dy * dy;

                if (d2 < bestDist && d2 <= hitRadius * hitRadius) {
                    bestDist = d2;
                    foundSeries = pointScreenSeries[i];
                    foundIndex = pointScreenCategory[i];
                }
            }
        }

        if (foundSeries >= 0 && foundIndex >= 0) {
            selectedSeries = foundSeries;
            selectedIndex = foundIndex;
        } else {
            selectedSeries = -1;
            selectedIndex = -1;
        }
    }

    public void handleKeyPressed(char key, int keyCode) {
        if (key == 'h' || key == 'H') helpVisible = !helpVisible;
        else if (key == 'o' || key == 'O') statusVisible = !statusVisible;
        else if (key == 'p' || key == 'P') detailsVisible = !detailsVisible;
        else if (key == 'g' || key == 'G') gridVisible = !gridVisible;
        else if (key == 'i' || key == 'I') pointLabelsVisible = !pointLabelsVisible;
        else if (key == 'a' || key == 'A') averageLineVisible = !averageLineVisible;
        else if (key == 'm' || key == 'M') toggleMode();
        else if (key == 'b' || key == 'B') setPaletteByName("colorblind");
        else if (key == 'l' || key == 'L') setPaletteByName("pastel");
        else if (key == 'k' || key == 'K') setPaletteByName("highcontrast");
        else if (key == 'u' || key == 'U') setPaletteByName("default");
        else if (key == 'y' || key == 'Y') toggleCustomColors();
        else if (key == 'q' || key == 'Q') {
            toggleAxisInterpretation();
            return;
        } else if (key == 'r' || key == 'R') restartAnimation();
        else if (key == 'x' || key == 'X') resetView();
        else if (keyCode == PApplet.LEFT) {
            if (currentPage > 0) {
                currentPage--;
                rebuildPlotData();
                restartAnimation();
            }
        } else if (keyCode == PApplet.RIGHT) {
            if (canGoToNextPage()) {
                currentPage++;
                rebuildPlotData();
                restartAnimation();
            }
        }
    }

    public void draw(int x, int y, int w, int h) {
        geometryValid = false;
        clearPointBuffers();

        if (plotXLabels == null || plotValues == null) {
            drawMessage("No line data set", x, y, w, h);
            return;
        }

        p.pushStyle();
        p.fill(backgroundColor);
        p.noStroke();
        p.rect(x, y, w, h);

        float plotX = x + leftPad;
        float plotY = y + topPad;
        float plotW = w - leftPad - rightPad;
        float plotH = h - topPad - bottomPad;
        if (plotW <= 10 || plotH <= 10) {
            p.popStyle();
            return;
        }

        float yMin = Float.POSITIVE_INFINITY;
        float yMax = Float.NEGATIVE_INFINITY;
        for (int s = 0; s < plotValues.length; s++) {
            if (!seriesVisible[s]) continue;
            for (int i = 0; i < plotValues[s].length; i++) {
                float v = plotValues[s][i];
                if (Float.isNaN(v)) continue;
                yMin = Math.min(yMin, v);
                yMax = Math.max(yMax, v);
            }
        }

        if (!Float.isNaN(referenceLineValue)) {
            yMin = Math.min(yMin, referenceLineValue);
            yMax = Math.max(yMax, referenceLineValue);
        }

        NiceAxis axis = computeNiceAxis(yMin, yMax, 6);
        yMin = axis.min;
        yMax = axis.max;

        if (gridVisible) drawGrid(plotX, plotY, plotW, plotH, axis);
        drawAxes(plotX, plotY, plotW, plotH, axis);

        if (!Float.isNaN(referenceLineValue)) drawReferenceLine(plotX, plotY, plotW, plotH, yMin, yMax);
        if (averageLineVisible) drawAverageLine(plotX, plotY, plotW, plotH, yMin, yMax);

        float anim = animationEnabled
            ? PApplet.constrain((p.millis() - animationStartMs) / (float) animationDurationMs, 0, 1)
            : 1;
        float eased = 1 - (float) Math.pow(1 - anim, 3);

        int requiredPointCount = countDrawablePoints();
        pointScreenX = new int[requiredPointCount];
        pointScreenY = new int[requiredPointCount];
        pointScreenSeries = new int[requiredPointCount];
        pointScreenCategory = new int[requiredPointCount];
        pointCount = 0;

        int visibleCount = Math.max(1, plotXLabels.length - 1);
        for (int s = 0; s < plotValues.length; s++) {
            if (!seriesVisible[s]) continue;

            p.noFill();
            p.stroke(currentSeriesColors[s]);
            p.strokeWeight((selectedSeries == s) ? 3f : 2.2f);
            p.beginShape();
            for (int i = 0; i < plotXLabels.length; i++) {
                float v = plotValues[s][i];
                if (Float.isNaN(v)) continue;
                float px = (plotXLabels.length == 1)
                    ? plotX + plotW * 0.5f
                    : plotX + (i / (float) visibleCount) * plotW;
                float finalPy = mapValueToY(v, plotY, plotH, yMin, yMax);
                float py = plotY + plotH - (plotY + plotH - finalPy) * eased;
                p.vertex(px, py);
            }
            p.endShape();

            for (int i = 0; i < plotXLabels.length; i++) {
                float v = plotValues[s][i];
                if (Float.isNaN(v)) continue;

                float px = (plotXLabels.length == 1)
                    ? plotX + plotW * 0.5f
                    : plotX + (i / (float) visibleCount) * plotW;
                float finalPy = mapValueToY(v, plotY, plotH, yMin, yMax);
                float py = plotY + plotH - (plotY + plotH - finalPy) * eased;

                if (pointCount < requiredPointCount) {
                    pointScreenX[pointCount] = Math.round(px);
                    pointScreenY[pointCount] = Math.round(py);
                    pointScreenSeries[pointCount] = s;
                    pointScreenCategory[pointCount] = i;
                    pointCount++;
                }

                int fillColor = currentSeriesColors[s];
                if (hoveredSeries == s && hoveredIndex == i) fillColor = currentHoverColors[s];
                if (selectedSeries == s && selectedIndex == i) fillColor = currentSelectedColors[s];

                p.noStroke();
                p.fill(fillColor);
                float r = pointRadius
                    + ((hoveredSeries == s && hoveredIndex == i) ? 2.5f : 0f)
                    + ((selectedSeries == s && selectedIndex == i) ? 1.5f : 0f);
                p.ellipse(px, py, r * 2, r * 2);

                if (selectedSeries == s && selectedIndex == i) {
                    p.noFill();
                    p.stroke(selectedMarkerColor);
                    p.strokeWeight(1.5f);
                    p.ellipse(px, py, r * 2 + 6, r * 2 + 6);
                }

                if (pointLabelsVisible) {
                    p.fill(textColor);
                    p.textAlign(PApplet.CENTER, PApplet.BOTTOM);
                    p.text(formatNumber(plotValues[s][i]), px, py - 8);
                }
            }
        }

        geometryValid = true;

        drawLegend(x + w - (int) rightPad + 18, y + 86);
        drawTitleAndLabels(x, y, w, h);

        if (hoveredSeries >= 0 && hoveredIndex >= 0) {
            drawTooltip();
        } else if (selectedSeries >= 0 && selectedIndex >= 0) {
            drawTooltipForSelected();
        }

        if (statusVisible) drawStatusOverlay(x + 8, y + 8);
        if (detailsVisible) drawDetailsPanel(x + w - 220, y + h - 145);
        if (helpVisible) drawHelpOverlay(x + 8, y + h - 224);

        p.popStyle();
    }

    private void drawGrid(float plotX, float plotY, float plotW, float plotH, NiceAxis axis) {
        p.stroke(gridColor);
        p.strokeWeight(1);

        for (int i = 0; i < axis.tickCount; i++) {
            float v = axis.min + i * axis.step;
            float gy = mapValueToY(v, plotY, plotH, axis.min, axis.max);
            p.line(plotX, gy, plotX + plotW, gy);
        }

        int count = plotXLabels.length;
        for (int i = 0; i < count; i++) {
            float gx = (count == 1)
                ? plotX + plotW * 0.5f
                : plotX + (i / (float) Math.max(1, count - 1)) * plotW;
            p.line(gx, plotY, gx, plotY + plotH);
        }
    }

    private void drawAxes(float plotX, float plotY, float plotW, float plotH, NiceAxis axis) {
        p.stroke(axisColor);
        p.strokeWeight(1.3f);
        p.line(plotX, plotY, plotX, plotY + plotH);
        p.line(plotX, plotY + plotH, plotX + plotW, plotY + plotH);

        p.fill(textColor);
        p.textAlign(PApplet.RIGHT, PApplet.CENTER);
        for (int i = 0; i < axis.tickCount; i++) {
            float v = axis.min + i * axis.step;
            float gy = mapValueToY(v, plotY, plotH, axis.min, axis.max);
            p.text(formatAxisNumber(v), plotX - 8, gy);
        }

        p.textAlign(PApplet.CENTER, PApplet.TOP);
        int count = plotXLabels.length;
        for (int i = 0; i < count; i++) {
            float gx = (count == 1)
                ? plotX + plotW * 0.5f
                : plotX + (i / (float) Math.max(1, count - 1)) * plotW;
            p.pushMatrix();
            p.translate(gx, plotY + plotH + 10);
            p.rotate(-0.45f);
            p.fill(textColor);
            p.text(plotXLabels[i], 0, 0);
            p.popMatrix();
        }
    }

    private void drawTitleAndLabels(int x, int y, int w, int h) {
        p.fill(titleColor);
        p.textAlign(PApplet.CENTER, PApplet.TOP);
        p.textSize(18);
        p.text(title, x + w * 0.5f, y + 12);

        p.fill(textColor);
        p.textSize(12);
        p.textAlign(PApplet.CENTER, PApplet.BOTTOM);
        p.text(xLabel, x + (w - rightPad + leftPad) * 0.5f, y + h - 8);

        p.pushMatrix();
        p.translate(x + 18, y + h * 0.5f);
        p.rotate(-PApplet.HALF_PI);
        p.textAlign(PApplet.CENTER, PApplet.TOP);
        p.text(yLabel, 0, 0);
        p.popMatrix();
    }

    private void drawLegend(int x, int y) {
        if (plotSeriesNames == null) return;
        int rowH = 22;
        int boxW = 145;
        int boxH = plotSeriesNames.length * rowH + 12;

        p.noStroke();
        p.fill(legendFill);
        p.rect(x, y, boxW, boxH, 8);

        p.stroke(legendStroke);
        p.noFill();
        p.rect(x, y, boxW, boxH, 8);

        for (int i = 0; i < plotSeriesNames.length; i++) {
            int yy = y + 8 + i * rowH;
            p.noStroke();
            p.fill(seriesVisible[i] ? currentSeriesColors[i] : 0xff999999);
            p.rect(x + 8, yy + 4, 12, 12, 3);
            p.fill(textColor);
            p.textAlign(PApplet.LEFT, PApplet.TOP);
            p.text(plotSeriesNames[i], x + 28, yy + 2);
        }
    }

    private boolean hitLegend(int mouseX, int mouseY) {
        if (!legendInteractive || plotSeriesNames == null) return false;

        int x = (int) (p.width - rightPad + 18);
        int y = 86;
        int rowH = 22;
        int boxW = 145;
        int boxH = plotSeriesNames.length * rowH + 12;

        if (mouseX < x || mouseX > x + boxW || mouseY < y || mouseY > y + boxH) return false;

        int row = (mouseY - (y + 8)) / rowH;
        if (row >= 0 && row < plotSeriesNames.length) {
            seriesVisible[row] = !seriesVisible[row];
            visibilityBySeriesName.put(plotSeriesNames[row], seriesVisible[row]);
            restartAnimation();
            return true;
        }

        return false;
    }

    private void drawTooltip() {
        String sName = plotSeriesNames[hoveredSeries];
        String cat = plotXLabels[hoveredIndex];
        float v = plotValues[hoveredSeries][hoveredIndex];

        String line1 = cat;
        String line2 = sName + ": " + formatNumber(v);

        float tw = Math.max(p.textWidth(line1), p.textWidth(line2)) + 18;
        float th = 36;
        float tx = pointScreenPositionX(hoveredSeries, hoveredIndex) + 12;
        float ty = pointScreenPositionY(hoveredSeries, hoveredIndex) - 42;

        p.noStroke();
        p.fill(tooltipFill);
        p.rect(tx, ty, tw, th, 6);

        p.stroke(tooltipStroke);
        p.noFill();
        p.rect(tx, ty, tw, th, 6);

        p.fill(textColor);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.text(line1, tx + 8, ty + 6);
        p.text(line2, tx + 8, ty + 20);
    }

    private void drawTooltipForSelected() {
        String sName = plotSeriesNames[selectedSeries];
        String cat = plotXLabels[selectedIndex];
        float v = plotValues[selectedSeries][selectedIndex];

        String line1 = cat;
        String line2 = sName + ": " + formatNumber(v);

        float tw = Math.max(p.textWidth(line1), p.textWidth(line2)) + 18;
        float th = 36;
        float tx = pointScreenPositionX(selectedSeries, selectedIndex) + 12;
        float ty = pointScreenPositionY(selectedSeries, selectedIndex) - 42;

        p.noStroke();
        p.fill(tooltipFill);
        p.rect(tx, ty, tw, th, 6);

        p.stroke(tooltipStroke);
        p.noFill();
        p.rect(tx, ty, tw, th, 6);

        p.fill(textColor);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.text(line1, tx + 8, ty + 6);
        p.text(line2, tx + 8, ty + 20);
    }

    private void drawStatusOverlay(int x, int y) {
        String mode = darkMode ? "Dark" : "Light";
        String pageText = (pageSize >= Integer.MAX_VALUE) ? "All" : (currentPage + 1) + "/" + totalPages();
        String interp = isTransposed() ? "Series-on-X" : "Categories-on-X";

        String txt =
            "Mode: " + mode +
            "\nInterpretation: " + interp +
            "\nPalette keys: B/L/K/U" +
            "\nPage: " + pageText +
            "\nCustom colors: " + (customColorsActive ? "On" : "Off");

        drawOverlayBox(txt, x, y);
    }

    private void drawDetailsPanel(int x, int y) {
        String txt;
        if (selectedSeries >= 0 && selectedIndex >= 0) {
            txt = "Selected Point\nSeries: " + plotSeriesNames[selectedSeries] +
                  "\nX: " + plotXLabels[selectedIndex] +
                  "\nValue: " + formatNumber(plotValues[selectedSeries][selectedIndex]);
        } else {
            txt = "Selected Point\nNone";
        }
        drawOverlayBox(txt, x, y);
    }

    private void drawHelpOverlay(int x, int y) {
        String txt =
            "Controls\n" +
            "Click point: select\n" +
            "Click legend: toggle series\n" +
            "Left/Right: pages\n" +
            "Q: transpose x/series\n" +
            "M: theme   B/L/K/U: palettes\n" +
            "Y: custom colors\n" +
            "A: average line  G: grid\n" +
            "I: point labels  R: animate\n" +
            "H/O/P: overlays  X: reset";
        drawOverlayBox(txt, x, y);
    }

    private void drawOverlayBox(String txt, int x, int y) {
        String[] lines = txt.split("\\n");
        float maxW = 0;
        for (String line : lines) {
            maxW = Math.max(maxW, p.textWidth(line));
        }

        float bw = maxW + 18;
        float bh = lines.length * 14 + 12;

        p.noStroke();
        p.fill(overlayFill);
        p.rect(x, y, bw, bh, 8);

        p.stroke(overlayStroke);
        p.noFill();
        p.rect(x, y, bw, bh, 8);

        p.fill(textColor);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        float ty = y + 6;
        for (String line : lines) {
            p.text(line, x + 8, ty);
            ty += 14;
        }
    }

    private void drawAverageLine(float plotX, float plotY, float plotW, float plotH, float yMin, float yMax) {
        float sum = 0;
        int count = 0;
        for (int s = 0; s < plotValues.length; s++) {
            if (!seriesVisible[s]) continue;
            for (float v : plotValues[s]) {
                if (!Float.isNaN(v)) {
                    sum += v;
                    count++;
                }
            }
        }

        if (count == 0) return;

        float avg = sum / count;
        float py = mapValueToY(avg, plotY, plotH, yMin, yMax);

        p.stroke(averageLineColor);
        p.strokeWeight(1.5f);
        p.line(plotX, py, plotX + plotW, py);

        p.fill(averageLineColor);
        p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
        p.text("Avg " + formatNumber(avg), plotX + 4, py - 2);
    }

    private void drawReferenceLine(float plotX, float plotY, float plotW, float plotH, float yMin, float yMax) {
        float py = mapValueToY(referenceLineValue, plotY, plotH, yMin, yMax);

        p.stroke(referenceLineColor);
        p.strokeWeight(1.5f);
        p.line(plotX, py, plotX + plotW, py);

        p.fill(referenceLineColor);
        p.textAlign(PApplet.LEFT, PApplet.BOTTOM);

        String label = referenceLineLabel.length() == 0
            ? "Ref " + formatNumber(referenceLineValue)
            : referenceLineLabel + " " + formatNumber(referenceLineValue);

        p.text(label, plotX + 4, py - 2);
    }

    private float mapValueToY(float v, float plotY, float plotH, float yMin, float yMax) {
        return plotY + plotH - ((v - yMin) / (yMax - yMin)) * plotH;
    }

    private void drawMessage(String msg, int x, int y, int w, int h) {
        p.pushStyle();
        p.fill(245);
        p.rect(x, y, w, h);
        p.fill(40);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.text(msg, x + w * 0.5f, y + h * 0.5f);
        p.popStyle();
    }

    private void resetView() {
        visibilityBySeriesName.clear();
        clearPointBuffers();
        selectedSeries = -1;
        selectedIndex = -1;
        currentPage = 0;
        rebuildPlotData();
        restartAnimation();
    }

    private void rebuildPlotData() {
        if (rowNames == null || columnNames == null || sourceValues == null) return;

        selectedSeries = -1;
        selectedIndex = -1;
        hoveredSeries = -1;
        hoveredIndex = -1;
        clearPointBuffers();

        if (axisInterpretation == AxisInterpretation.CATEGORIES_ON_X) {
            int total = rowNames.length;
            int start = 0;
            int end = total;

            if (pageSize < Integer.MAX_VALUE) {
                start = currentPage * pageSize;
                end = Math.min(total, start + pageSize);
                if (start >= total) {
                    currentPage = 0;
                    start = 0;
                    end = Math.min(total, pageSize);
                }
            }

            plotXLabels = Arrays.copyOfRange(rowNames, start, end);
            plotSeriesNames = Arrays.copyOf(columnNames, columnNames.length);
            plotValues = new float[plotSeriesNames.length][plotXLabels.length];

            for (int s = 0; s < plotSeriesNames.length; s++) {
                for (int i = start; i < end; i++) {
                    plotValues[s][i - start] = sourceValues[s][i];
                }
            }
        } else {
            int total = rowNames.length;
            int start = 0;
            int end = total;

            if (pageSize < Integer.MAX_VALUE) {
                start = currentPage * pageSize;
                end = Math.min(total, start + pageSize);
                if (start >= total) {
                    currentPage = 0;
                    start = 0;
                    end = Math.min(total, pageSize);
                }
            }

            plotXLabels = Arrays.copyOf(columnNames, columnNames.length);
            plotSeriesNames = Arrays.copyOfRange(rowNames, start, end);
            plotValues = new float[plotSeriesNames.length][plotXLabels.length];

            for (int row = start; row < end; row++) {
                for (int col = 0; col < columnNames.length; col++) {
                    plotValues[row - start][col] = sourceValues[col][row];
                }
            }
        }

        seriesVisible = new boolean[plotSeriesNames.length];
        for (int i = 0; i < plotSeriesNames.length; i++) {
            Boolean v = visibilityBySeriesName.get(plotSeriesNames[i]);
            seriesVisible[i] = (v == null) ? true : v.booleanValue();
        }

        ensurePaletteCapacity();
        applyPendingNamedColors();
        clearPointBuffers();
    }

    private void clampCurrentPage() {
        int last = Math.max(0, totalPages() - 1);
        currentPage = Math.max(0, Math.min(currentPage, last));
    }

    private boolean canGoToNextPage() {
        return currentPage + 1 < totalPages();
    }

    private int totalPages() {
        if (pageSize >= Integer.MAX_VALUE) return 1;
        int count = (rowNames == null) ? 0 : rowNames.length;
        return Math.max(1, (int) Math.ceil(count / (float) pageSize));
    }

    private void ensurePaletteCapacity() {
        if (plotSeriesNames == null) return;

        int n = plotSeriesNames.length;
        currentSeriesColors = new int[n];
        currentHoverColors = new int[n];
        currentSelectedColors = new int[n];

        for (int i = 0; i < n; i++) {
            int c = (palette != null && i < palette.length) ? palette[i] : autoColor(i, n);
            currentSeriesColors[i] = c;
            currentHoverColors[i] = lighten(c, 0.20f);
            currentSelectedColors[i] = lighten(c, 0.35f);
        }
    }

    private void applyPendingNamedColors() {
        if (plotSeriesNames == null || currentSeriesColors == null) return;

        for (Map.Entry<String, Integer> e : pendingNamedSeriesColors.entrySet()) {
            int idx = seriesIndex(e.getKey());
            if (idx >= 0) {
                currentSeriesColors[idx] = e.getValue();
                currentHoverColors[idx] = lighten(e.getValue(), 0.20f);
                currentSelectedColors[idx] = lighten(e.getValue(), 0.35f);
            }
        }
    }

    private int seriesIndex(String name) {
        if (name == null || plotSeriesNames == null) return -1;
        for (int i = 0; i < plotSeriesNames.length; i++) {
            if (name.equals(plotSeriesNames[i])) return i;
        }
        return -1;
    }

    private int[] defaultLightPalette() {
        return new int[] {
            0xff1f77b4, 0xffff7f0e, 0xff2ca02c, 0xffd62728,
            0xff9467bd, 0xff8c564b, 0xffe377c2, 0xff7f7f7f
        };
    }

    private int[] defaultDarkPalette() {
        return new int[] {
            0xff4db6ff, 0xffff8a65, 0xff81c784, 0xfff06292,
            0xffba68c8, 0xffffd54f, 0xff90a4ae, 0xffaed581
        };
    }

    private int autoColor(int i, int total) {
        float hue = (i / (float) Math.max(1, total)) % 1f;
        return 0xff000000 | (Color.HSBtoRGB(hue, 0.65f, darkMode ? 0.95f : 0.82f) & 0x00ffffff);
    }

    private int vividPresetColor(int i, int total) {
        float hue = (i / (float) Math.max(1, total)) % 1f;
        return 0xff000000 | (Color.HSBtoRGB(hue, 0.80f, 0.90f) & 0x00ffffff);
    }

    private int lighten(int c, float amt) {
        int a = (c >> 24) & 0xff;
        int r = (c >> 16) & 0xff;
        int g = (c >> 8) & 0xff;
        int b = c & 0xff;
        r = (int) (r + (255 - r) * amt);
        g = (int) (g + (255 - g) * amt);
        b = (int) (b + (255 - b) * amt);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void applyLightThemeColors() {
        backgroundColor = 0xffffffff;
        axisColor = 0xff111111;
        gridColor = 0xffdddddd;
        textColor = 0xff111111;
        titleColor = 0xff111111;
        tooltipFill = 0xffffffff;
        tooltipStroke = 0xff222222;
        overlayFill = 0xf5ffffff;
        overlayStroke = 0xffcccccc;
        legendFill = 0xf5ffffff;
        legendStroke = 0xffcccccc;
        averageLineColor = 0xff3366cc;
        referenceLineColor = 0xffcc3333;
        selectedMarkerColor = 0xff111111;
    }

    private void applyDarkThemeColors() {
        backgroundColor = 0xff111111;
        axisColor = 0xffeeeeee;
        gridColor = 0xff333333;
        textColor = 0xfff2f2f2;
        titleColor = 0xffffffff;
        tooltipFill = 0xff1f1f1f;
        tooltipStroke = 0xffeeeeee;
        overlayFill = 0xf0222222;
        overlayStroke = 0xff666666;
        legendFill = 0xf0222222;
        legendStroke = 0xff666666;
        averageLineColor = 0xff66b3ff;
        referenceLineColor = 0xffff6b6b;
        selectedMarkerColor = 0xffffffff;
    }

    private String formatNumber(float v) {
        if (Math.abs(v - Math.round(v)) < 0.0001f) return Integer.toString(Math.round(v));
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private String formatAxisNumber(float v) {
        if (Math.abs(v - Math.round(v)) < 0.0001f) {
            return Integer.toString(Math.round(v));
        }
        return String.format(java.util.Locale.US, "%.1f", v);
    }

    private void clearPointBuffers() {
        pointScreenX = null;
        pointScreenY = null;
        pointScreenSeries = null;
        pointScreenCategory = null;
        pointCount = 0;
        hoveredSeries = -1;
        hoveredIndex = -1;
        geometryValid = false;
    }

    private int countDrawablePoints() {
        if (plotValues == null || seriesVisible == null) return 0;

        int count = 0;
        for (int s = 0; s < plotValues.length; s++) {
            if (!seriesVisible[s]) continue;
            for (int i = 0; i < plotValues[s].length; i++) {
                if (!Float.isNaN(plotValues[s][i])) count++;
            }
        }
        return count;
    }

    private float pointScreenPositionX(int series, int category) {
        if (pointScreenX == null || pointScreenSeries == null || pointScreenCategory == null) return 0;
        for (int i = 0; i < pointCount; i++) {
            if (pointScreenSeries[i] == series && pointScreenCategory[i] == category) {
                return pointScreenX[i];
            }
        }
        return 0;
    }

    private float pointScreenPositionY(int series, int category) {
        if (pointScreenY == null || pointScreenSeries == null || pointScreenCategory == null) return 0;
        for (int i = 0; i < pointCount; i++) {
            if (pointScreenSeries[i] == series && pointScreenCategory[i] == category) {
                return pointScreenY[i];
            }
        }
        return 0;
    }

    private NiceAxis computeNiceAxis(float dataMin, float dataMax, int targetTicks) {
        NiceAxis axis = new NiceAxis();

        if (!Float.isFinite(dataMin) || !Float.isFinite(dataMax)) {
            axis.min = 0;
            axis.max = 10;
            axis.step = 2;
            axis.tickCount = 6;
            return axis;
        }

        if (dataMin == dataMax) {
            dataMin -= 1;
            dataMax += 1;
        }

        float range = niceNum(dataMax - dataMin, false);
        float step = niceNum(range / (targetTicks - 1), true);

        float niceMin = (float) Math.floor(dataMin / step) * step;
        float niceMax = (float) Math.ceil(dataMax / step) * step;

        axis.min = niceMin;
        axis.max = niceMax;
        axis.step = step;
        axis.tickCount = Math.round((niceMax - niceMin) / step) + 1;

        return axis;
    }

    private float niceNum(float range, boolean round) {
        float exponent = (float) Math.floor(Math.log10(range));
        float fraction = range / (float) Math.pow(10, exponent);
        float niceFraction;

        if (round) {
            if (fraction < 1.5f) niceFraction = 1f;
            else if (fraction < 3f) niceFraction = 2f;
            else if (fraction < 7f) niceFraction = 5f;
            else niceFraction = 10f;
        } else {
            if (fraction <= 1f) niceFraction = 1f;
            else if (fraction <= 2f) niceFraction = 2f;
            else if (fraction <= 5f) niceFraction = 5f;
            else niceFraction = 10f;
        }

        return niceFraction * (float) Math.pow(10, exponent);
    }
}