package com.ata.charts;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.Table;
import processing.data.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScatterPlotFinal1 {

    public enum ThemeMode {
        LIGHT,
        DARK
    }

    private enum PaletteName {
        DEFAULT,
        COLORBLIND,
        PASTEL,
        HIGHCONTRAST
    }

    private static class NiceScale {
        float niceMin;
        float niceMax;
        float tickSpacing;
        int tickCount;
    }

    private final PApplet p;

    // Data
    private String[] labels = new String[0];
    private String[] groups = new String[0];
    private float[] xValues = new float[0];
    private float[] yValues = new float[0];
    private float[] sizeValues = new float[0];

    private String[] uniqueGroups = new String[0];
    private boolean[] groupVisible = new boolean[0];

    // Titles
    private String title = "";
    private String xLabel = "";
    private String yLabel = "";
    private String sizeLabel = "";

    // Layout
    private int marginLeft = 90;
    private int marginRight = 40;
    private int marginTop = 70;
    private int marginBottom = 90;

    // Styling and overlays
    private boolean showGrid = true;
    private boolean showHelpOverlay = true;
    private boolean showStatusOverlay = true;
    private boolean showDetailsPanel = true;
    private boolean showAverageLines = true;
    private boolean showPointLabels = false;
    private boolean showLegend = true;
    private boolean legendInteractive = true;
    private boolean selectionEnabled = true;
    private boolean animationEnabled = true;
    private int animationDurationMs = 1000;

    private int detailsPanelWidth = 260;

    // Plot behavior
    private boolean useNiceAxis = true;
    private boolean useBubbleSizes = true;
    private float pointSizeMin = 8f;
    private float pointSizeMax = 26f;
    private float zoomFactor = 1.0f;
    private float panX = 0f;
    private float panY = 0f;

    // Initial state snapshot
    private boolean initialShowAverageLines = true;
    private boolean initialShowPointLabels = false;
    private boolean initialShowHelpOverlay = true;
    private boolean initialShowStatusOverlay = true;
    private boolean initialShowDetailsPanel = true;
    private float initialZoomFactor = 1.0f;
    private float initialPanX = 0f;
    private float initialPanY = 0f;

    // Selection / hover
    private int hoverIndex = -1;
    private int selectedIndex = -1;

    // Animation
    private int animationStartMs = -1;

    // Cached geometry
    private float[] pointScreenX = new float[0];
    private float[] pointScreenY = new float[0];
    private float[] pointScreenR = new float[0];
    private boolean geometryValid = false;

    // Legend geometry
    private int legendBoxX, legendBoxY, legendBoxW, legendBoxH;
    private int[] legendRowY;
    private int legendRowH = 18;
    private boolean legendGeometryValid = false;

    // Theme
    private ThemeMode themeMode = ThemeMode.LIGHT;
    private boolean preserveCustomPaletteAcrossModeChange = true;
    private boolean customColorsActive = false;

    private int background = 0xffffffff;
    private int axisColor = 0xff000000;
    private int gridColor = 0xffdddddd;
    private int textColor = 0xff000000;
    private int tooltipFill = 0xffffffff;
    private int tooltipStroke = 0xff000000;
    private int averageLineColor = 0xff3366cc;
    private int detailsPanelFill = 0xfff7f7f7;
    private int detailsPanelStroke = 0xffbbbbbb;
    private int statusOverlayFill = 0xf2ffffff;
    private int statusOverlayStroke = 0xffbbbbbb;
    private int helpOverlayFill = 0xf7ffffff;
    private int helpOverlayStroke = 0xffbbbbbb;
    private int selectionStroke = 0xff000000;

    private int[] palette = defaultLightPalette();
    private PaletteName activePaletteName = PaletteName.DEFAULT;

    private final LinkedHashMap<String, Integer> groupColorOverrides = new LinkedHashMap<String, Integer>();
    private String[] customGroupNames = new String[0];
    private int[] customGroupColors = new int[0];

    public ScatterPlotFinal1(PApplet p) {
        this.p = p;
    }

    // ---------------------------
    // Fluent configuration
    // ---------------------------

    public ScatterPlotFinal1 setTitle(String t) {
        this.title = (t == null) ? "" : t;
        return this;
    }

    public ScatterPlotFinal1 setXLabel(String t) {
        this.xLabel = (t == null) ? "" : t;
        return this;
    }

    public ScatterPlotFinal1 setYLabel(String t) {
        this.yLabel = (t == null) ? "" : t;
        return this;
    }

    public ScatterPlotFinal1 setSizeLabel(String t) {
        this.sizeLabel = (t == null) ? "" : t;
        return this;
    }

    public ScatterPlotFinal1 setMargins(int left, int right, int top, int bottom) {
        this.marginLeft = left;
        this.marginRight = right;
        this.marginTop = top;
        this.marginBottom = bottom;
        invalidateGeometry();
        return this;
    }

    public ScatterPlotFinal1 setShowGrid(boolean show) {
        this.showGrid = show;
        return this;
    }

    public ScatterPlotFinal1 setShowHelpOverlay(boolean show) {
        this.showHelpOverlay = show;
        return this;
    }

    public ScatterPlotFinal1 setShowStatusOverlay(boolean show) {
        this.showStatusOverlay = show;
        return this;
    }

    public ScatterPlotFinal1 setShowDetailsPanel(boolean show) {
        this.showDetailsPanel = show;
        return this;
    }

    public ScatterPlotFinal1 setShowAverageLines(boolean show) {
        this.showAverageLines = show;
        return this;
    }

    public ScatterPlotFinal1 setShowPointLabels(boolean show) {
        this.showPointLabels = show;
        return this;
    }

    public ScatterPlotFinal1 setShowLegend(boolean show) {
        this.showLegend = show;
        return this;
    }

    public ScatterPlotFinal1 setLegendInteractive(boolean enabled) {
        this.legendInteractive = enabled;
        return this;
    }

    public ScatterPlotFinal1 setSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
        return this;
    }

    public ScatterPlotFinal1 setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
        return this;
    }

    public ScatterPlotFinal1 setAnimationDurationMs(int ms) {
        if (ms >= 0) this.animationDurationMs = ms;
        return this;
    }

    public ScatterPlotFinal1 setUseNiceAxis(boolean useNiceAxis) {
        this.useNiceAxis = useNiceAxis;
        return this;
    }

    public ScatterPlotFinal1 setUseBubbleSizes(boolean useBubbleSizes) {
        this.useBubbleSizes = useBubbleSizes;
        invalidateGeometry();
        return this;
    }

    public ScatterPlotFinal1 setPointSizeRange(float minSize, float maxSize) {
        this.pointSizeMin = Math.max(2f, minSize);
        this.pointSizeMax = Math.max(this.pointSizeMin, maxSize);
        invalidateGeometry();
        return this;
    }

    public ScatterPlotFinal1 setPreserveCustomPaletteAcrossModeChange(boolean preserve) {
        this.preserveCustomPaletteAcrossModeChange = preserve;
        return this;
    }

    public ScatterPlotFinal1 setPalette(int[] colors) {
        if (colors != null && colors.length > 0) {
            this.palette = Arrays.copyOf(colors, colors.length);
            this.activePaletteName = null;
        }
        return this;
    }

    public ScatterPlotFinal1 setPaletteByName(String paletteName) {
        if (paletteName == null) return this;
        String key = paletteName.trim().toLowerCase();

        if (key.equals("default")) {
            activePaletteName = PaletteName.DEFAULT;
            this.palette = (themeMode == ThemeMode.DARK) ? defaultDarkPalette() : defaultLightPalette();
        } else if (key.equals("colorblind")) {
            activePaletteName = PaletteName.COLORBLIND;
            this.palette = new int[] {
                0xff0072B2, 0xffE69F00, 0xff009E73, 0xffD55E00,
                0xffCC79A7, 0xff56B4E9, 0xffF0E442, 0xff000000
            };
        } else if (key.equals("pastel")) {
            activePaletteName = PaletteName.PASTEL;
            this.palette = new int[] {
                0xff8DD3C7, 0xffFFFFB3, 0xffBEBADA, 0xffFB8072,
                0xff80B1D3, 0xffFDB462, 0xffB3DE69, 0xffFCCDE5
            };
        } else if (key.equals("highcontrast")) {
            activePaletteName = PaletteName.HIGHCONTRAST;
            this.palette = new int[] {
                0xff1F77B4, 0xffD62728, 0xff2CA02C, 0xff9467BD,
                0xffFF7F0E, 0xff17BECF, 0xff8C564B, 0xffE377C2
            };
        }
        return this;
    }

    public ScatterPlotFinal1 setGroupColor(String groupName, int color) {
        if (groupName != null && groupName.trim().length() > 0) {
            groupColorOverrides.put(groupName.trim(), color);
        }
        return this;
    }

    public ScatterPlotFinal1 clearGroupColors() {
        groupColorOverrides.clear();
        customColorsActive = false;
        return this;
    }

    public ScatterPlotFinal1 setCustomGroupColors(String[] groupNames, int[] colors) {
        if (groupNames == null || colors == null || groupNames.length == 0 || colors.length == 0) {
            customGroupNames = new String[0];
            customGroupColors = new int[0];
            return this;
        }

        int n = Math.min(groupNames.length, colors.length);
        customGroupNames = new String[n];
        customGroupColors = new int[n];
        for (int i = 0; i < n; i++) {
            customGroupNames[i] = groupNames[i];
            customGroupColors[i] = colors[i];
        }
        return this;
    }

    public ScatterPlotFinal1 applyCustomGroupColors() {
        if (customGroupNames == null || customGroupColors == null) return this;
        clearGroupColors();
        for (int i = 0; i < customGroupNames.length; i++) {
            if (customGroupNames[i] != null) {
                groupColorOverrides.put(customGroupNames[i], customGroupColors[i]);
            }
        }
        customColorsActive = true;
        return this;
    }

    public ScatterPlotFinal1 toggleCustomColors() {
        if (customColorsActive) {
            clearGroupColors();
        } else {
            applyCustomGroupColors();
        }
        restartAnimation();
        return this;
    }

    public ScatterPlotFinal1 toggleMode() {
        if (themeMode == ThemeMode.DARK) {
            setLightMode();
        } else {
            setDarkMode();
        }
        return this;
    }

    public ScatterPlotFinal1 setLightMode() {
        themeMode = ThemeMode.LIGHT;
        background = 0xffffffff;
        axisColor = 0xff000000;
        gridColor = 0xffdddddd;
        textColor = 0xff000000;
        tooltipFill = 0xffffffff;
        tooltipStroke = 0xff000000;
        averageLineColor = 0xff3366cc;
        detailsPanelFill = 0xfff7f7f7;
        detailsPanelStroke = 0xffbbbbbb;
        statusOverlayFill = 0xf2ffffff;
        statusOverlayStroke = 0xffbbbbbb;
        helpOverlayFill = 0xf7ffffff;
        helpOverlayStroke = 0xffbbbbbb;
        selectionStroke = 0xff000000;
        if (!preserveCustomPaletteAcrossModeChange || activePaletteName == PaletteName.DEFAULT) {
            palette = defaultLightPalette();
            activePaletteName = PaletteName.DEFAULT;
        }
        return this;
    }

    public ScatterPlotFinal1 setDarkMode() {
        themeMode = ThemeMode.DARK;
        background = 0xff111111;
        axisColor = 0xffeeeeee;
        gridColor = 0xff333333;
        textColor = 0xfff2f2f2;
        tooltipFill = 0xff1f1f1f;
        tooltipStroke = 0xffeeeeee;
        averageLineColor = 0xff66b3ff;
        detailsPanelFill = 0xff1b1b1b;
        detailsPanelStroke = 0xff555555;
        statusOverlayFill = 0xf0222222;
        statusOverlayStroke = 0xff666666;
        helpOverlayFill = 0xf0222222;
        helpOverlayStroke = 0xff666666;
        selectionStroke = 0xffffffff;
        if (!preserveCustomPaletteAcrossModeChange || activePaletteName == PaletteName.DEFAULT) {
            palette = defaultDarkPalette();
            activePaletteName = PaletteName.DEFAULT;
        }
        return this;
    }

    public ScatterPlotFinal1 useDefaultPaletteForCurrentMode() {
        activePaletteName = PaletteName.DEFAULT;
        palette = (themeMode == ThemeMode.DARK) ? defaultDarkPalette() : defaultLightPalette();
        return this;
    }

    public ScatterPlotFinal1 enableDefaultAnalyticalUI() {
        this.showGrid = true;
        this.showHelpOverlay = true;
        this.showStatusOverlay = true;
        this.showDetailsPanel = true;
        this.showAverageLines = true;
        this.showPointLabels = false;
        this.showLegend = true;
        this.legendInteractive = true;
        this.selectionEnabled = true;
        this.animationEnabled = true;
        return this;
    }

    public ScatterPlotFinal1 restartAnimation() {
        this.animationStartMs = p.millis();
        return this;
    }

    // ---------------------------
    // Data loading
    // ---------------------------

    public ScatterPlotFinal1 setData(
        String[] labels,
        float[] xValues,
        float[] yValues,
        float[] sizeValues,
        String[] groups
    ) {
        if (labels == null || xValues == null || yValues == null) {
            this.labels = new String[0];
            this.xValues = new float[0];
            this.yValues = new float[0];
            this.sizeValues = new float[0];
            this.groups = new String[0];
            this.uniqueGroups = new String[0];
            this.groupVisible = new boolean[0];
            invalidateGeometry();
            clearSelection();
            return this;
        }

        int n = labels.length;
        if (xValues.length != n || yValues.length != n) {
            throw new IllegalArgumentException("labels, xValues, yValues must have the same length");
        }

        this.labels = Arrays.copyOf(labels, n);
        this.xValues = Arrays.copyOf(xValues, n);
        this.yValues = Arrays.copyOf(yValues, n);

        if (sizeValues != null && sizeValues.length == n) {
            this.sizeValues = Arrays.copyOf(sizeValues, n);
        } else {
            this.sizeValues = new float[n];
            Arrays.fill(this.sizeValues, 1f);
        }

        if (groups != null && groups.length == n) {
            this.groups = Arrays.copyOf(groups, n);
        } else {
            this.groups = new String[n];
            Arrays.fill(this.groups, "Default");
        }

        rebuildGroups();
        captureInitialState();
        invalidateGeometry();
        clearSelection();
        restartAnimation();
        return this;
    }

    public ScatterPlotFinal1 setDataFromTable(
        Table t,
        String labelCol,
        String xCol,
        String yCol,
        String sizeCol,
        String groupCol
    ) {
        if (t == null) {
            return setData(null, null, null, null, null);
        }

        ArrayList<String> labelList = new ArrayList<String>();
        ArrayList<Float> xList = new ArrayList<Float>();
        ArrayList<Float> yList = new ArrayList<Float>();
        ArrayList<Float> sizeList = new ArrayList<Float>();
        ArrayList<String> groupList = new ArrayList<String>();

        for (TableRow row : t.rows()) {
            String label = safeString(row.getString(labelCol));
            Float xv = parseFloatSafe(row.getString(xCol));
            Float yv = parseFloatSafe(row.getString(yCol));
            Float sv = (sizeCol != null && sizeCol.length() > 0)
                ? parseFloatSafe(row.getString(sizeCol))
                : 1f;
            String group = (groupCol != null && groupCol.length() > 0)
                ? safeString(row.getString(groupCol))
                : "Default";

            if (xv == null || yv == null) continue;
            if (sv == null) sv = 1f;
            if (group.length() == 0) group = "Default";

            labelList.add(label);
            xList.add(xv);
            yList.add(yv);
            sizeList.add(sv);
            groupList.add(group);
        }

        int n = labelList.size();
        String[] labelsArr = new String[n];
        float[] xArr = new float[n];
        float[] yArr = new float[n];
        float[] sArr = new float[n];
        String[] gArr = new String[n];

        for (int i = 0; i < n; i++) {
            labelsArr[i] = labelList.get(i);
            xArr[i] = xList.get(i);
            yArr[i] = yList.get(i);
            sArr[i] = sizeList.get(i);
            gArr[i] = groupList.get(i);
        }

        return setData(labelsArr, xArr, yArr, sArr, gArr);
    }

    private void rebuildGroups() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (String g : groups) {
            if (!map.containsKey(g)) {
                map.put(g, map.size());
            }
        }

        uniqueGroups = new String[map.size()];
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            uniqueGroups[e.getValue()] = e.getKey();
        }

        groupVisible = new boolean[uniqueGroups.length];
        Arrays.fill(groupVisible, true);
    }

    private void captureInitialState() {
        initialShowAverageLines = showAverageLines;
        initialShowPointLabels = showPointLabels;
        initialShowHelpOverlay = showHelpOverlay;
        initialShowStatusOverlay = showStatusOverlay;
        initialShowDetailsPanel = showDetailsPanel;
        initialZoomFactor = zoomFactor;
        initialPanX = panX;
        initialPanY = panY;
    }

    // ---------------------------
    // State / reset
    // ---------------------------

    public void clearSelection() {
        selectedIndex = -1;
    }

    public ScatterPlotFinal1 showAllGroups() {
        for (int i = 0; i < groupVisible.length; i++) groupVisible[i] = true;
        invalidateGeometry();
        restartAnimation();
        return this;
    }

    public ScatterPlotFinal1 resetToInitialView() {
        clearSelection();
        hoverIndex = -1;
        zoomFactor = initialZoomFactor;
        panX = initialPanX;
        panY = initialPanY;
        showAverageLines = initialShowAverageLines;
        showPointLabels = initialShowPointLabels;
        showHelpOverlay = initialShowHelpOverlay;
        showStatusOverlay = initialShowStatusOverlay;
        showDetailsPanel = initialShowDetailsPanel;
        showAllGroups();
        restartAnimation();
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedLabel() {
        return (selectedIndex >= 0 && selectedIndex < labels.length) ? labels[selectedIndex] : null;
    }

    public String getSelectedGroup() {
        return (selectedIndex >= 0 && selectedIndex < groups.length) ? groups[selectedIndex] : null;
    }

    public Float getSelectedX() {
        return (selectedIndex >= 0 && selectedIndex < xValues.length) ? xValues[selectedIndex] : null;
    }

    public Float getSelectedY() {
        return (selectedIndex >= 0 && selectedIndex < yValues.length) ? yValues[selectedIndex] : null;
    }

    public Float getSelectedSize() {
        return (selectedIndex >= 0 && selectedIndex < sizeValues.length) ? sizeValues[selectedIndex] : null;
    }

    // ---------------------------
    // Interaction
    // ---------------------------

    public void updateHover(int mouseX, int mouseY) {
        if (!geometryValid || pointScreenX == null) {
            hoverIndex = -1;
            return;
        }

        hoverIndex = -1;
        for (int i = 0; i < pointScreenX.length; i++) {
            if (!isPointVisible(i)) continue;

            float dx = mouseX - pointScreenX[i];
            float dy = mouseY - pointScreenY[i];
            float rr = pointScreenR[i] * 0.5f;
            if (dx * dx + dy * dy <= rr * rr) {
                hoverIndex = i;
                return;
            }
        }
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (legendInteractive && showLegend && legendGeometryValid) {
            if (mouseX >= legendBoxX && mouseX <= legendBoxX + legendBoxW &&
                mouseY >= legendBoxY && mouseY <= legendBoxY + legendBoxH) {

                for (int i = 0; i < uniqueGroups.length; i++) {
                    int yCenter = legendRowY[i];
                    int yTop = yCenter - legendRowH / 2;
                    int yBot = yCenter + legendRowH / 2;

                    if (mouseY >= yTop && mouseY <= yBot) {
                        groupVisible[i] = !groupVisible[i];
                        if (selectedIndex >= 0 && !isPointVisible(selectedIndex)) clearSelection();
                        invalidateGeometry();
                        restartAnimation();
                        return;
                    }
                }
            }
        }

        if (!selectionEnabled || !geometryValid) return;

        for (int i = 0; i < pointScreenX.length; i++) {
            if (!isPointVisible(i)) continue;

            float dx = mouseX - pointScreenX[i];
            float dy = mouseY - pointScreenY[i];
            float rr = pointScreenR[i] * 0.5f;
            if (dx * dx + dy * dy <= rr * rr) {
                selectedIndex = i;
                return;
            }
        }

        clearSelection();
    }

    public void handleKeyPressed(char key, int keyCode) {
        if (labels == null || labels.length == 0) return;

        if (key == 'x' || key == 'X') {
            resetToInitialView();
            return;
        }

        if (key == 's' || key == 'S') {
            showAllGroups();
            return;
        }

        if (key == 'c' || key == 'C') {
            clearSelection();
            return;
        }

        if (key == 'r' || key == 'R') {
            restartAnimation();
            return;
        }

        if (key == 'h' || key == 'H') {
            showHelpOverlay = !showHelpOverlay;
            return;
        }

        if (key == 'o' || key == 'O') {
            showStatusOverlay = !showStatusOverlay;
            return;
        }

        if (key == 'p' || key == 'P') {
            showDetailsPanel = !showDetailsPanel;
            return;
        }

        if (key == 'i' || key == 'I') {
            showPointLabels = !showPointLabels;
            return;
        }

        if (key == 'a' || key == 'A') {
            showAverageLines = !showAverageLines;
            return;
        }

        if (key == 'g' || key == 'G') {
            showGrid = !showGrid;
            return;
        }

        if (key == '+' || key == '=') {
            zoomFactor *= 1.15f;
            invalidateGeometry();
            return;
        }

        if (key == '-' || key == '_') {
            zoomFactor /= 1.15f;
            zoomFactor = Math.max(0.5f, zoomFactor);
            invalidateGeometry();
            return;
        }

        if (key == 'm' || key == 'M') {
            toggleMode();
            return;
        }

        if (key == 'b' || key == 'B') {
            setPaletteByName("colorblind");
            restartAnimation();
            return;
        }

        if (key == 'l' || key == 'L') {
            setPaletteByName("pastel");
            restartAnimation();
            return;
        }

        if (key == 'k' || key == 'K') {
            setPaletteByName("highcontrast");
            restartAnimation();
            return;
        }

        if (key == 'u' || key == 'U') {
            useDefaultPaletteForCurrentMode();
            restartAnimation();
            return;
        }

        if (key == 'y' || key == 'Y') {
            toggleCustomColors();
            return;
        }

        if (keyCode == PConstants.LEFT) {
            panX -= 0.08f / zoomFactor;
            invalidateGeometry();
            return;
        }

        if (keyCode == PConstants.RIGHT) {
            panX += 0.08f / zoomFactor;
            invalidateGeometry();
            return;
        }

        if (keyCode == PConstants.UP) {
            panY += 0.08f / zoomFactor;
            invalidateGeometry();
            return;
        }

        if (keyCode == PConstants.DOWN) {
            panY -= 0.08f / zoomFactor;
            invalidateGeometry();
        }
    }

    // ---------------------------
    // Draw
    // ---------------------------

    public void draw(int x, int y, int w, int h) {
        if (labels == null || labels.length == 0) {
            drawMessage("No scatter data set", x, y);
            return;
        }

        int panelW = showDetailsPanel ? detailsPanelWidth : 0;
        int plotX = x + marginLeft;
        int plotY = y + marginTop;
        int plotW = w - marginLeft - marginRight - panelW;
        int plotH = h - marginTop - marginBottom;

        if (plotW <= 0 || plotH <= 0) {
            drawMessage("Chart area too small", x, y);
            return;
        }

        p.noStroke();
        p.fill(background);
        p.rect(x, y, w, h);

        float dataMinX = getVisibleMin(xValues);
        float dataMaxX = getVisibleMax(xValues);
        float dataMinY = getVisibleMin(yValues);
        float dataMaxY = getVisibleMax(yValues);

        if (Float.isNaN(dataMinX) || Float.isNaN(dataMaxX) || Float.isNaN(dataMinY) || Float.isNaN(dataMaxY)) {
            drawMessage("All groups hidden", x, y);
            drawTitles(x, y, w, h, panelW);
            if (showLegend) drawLegend(plotX + plotW - 10, plotY + 10);
            if (showDetailsPanel) drawDetailsPanel(x + w - panelW + 10, plotY, panelW - 20, plotH);
            if (showStatusOverlay) drawStatusOverlay(plotX, plotY);
            if (showHelpOverlay) drawHelpOverlay(plotX, plotY + 76, plotW);
            return;
        }

        float[] zoomedX = applyZoomAndPan(dataMinX, dataMaxX, panX, zoomFactor);
        float[] zoomedY = applyZoomAndPan(dataMinY, dataMaxY, panY, zoomFactor);

        NiceScale xScale = computeScale(zoomedX[0], zoomedX[1], 5);
        NiceScale yScale = computeScale(zoomedY[0], zoomedY[1], 5);

        if (showGrid) {
            drawGridAndTicks(plotX, plotY, plotW, plotH, xScale, yScale);
        } else {
            drawAxesOnly(plotX, plotY, plotW, plotH);
            drawTicks(plotX, plotY, plotW, plotH, xScale, yScale);
        }

        float avgX = getVisibleAverage(xValues);
        float avgY = getVisibleAverage(yValues);

        if (showAverageLines && !Float.isNaN(avgX) && !Float.isNaN(avgY)) {
            float sx = valueToScreenX(avgX, xScale.niceMin, xScale.niceMax, plotX, plotW);
            float sy = valueToScreenY(avgY, yScale.niceMin, yScale.niceMax, plotY, plotH);

            p.stroke(averageLineColor);
            p.strokeWeight(1.2f);
            p.line(sx, plotY, sx, plotY + plotH);
            p.line(plotX, sy, plotX + plotW, sy);

            p.noStroke();
            p.fill(averageLineColor);
            p.textAlign(PConstants.LEFT, PConstants.BOTTOM);
            p.text("Avg X", sx + 4, plotY + 14);
            p.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
            p.text("Avg Y", plotX + plotW - 4, sy - 3);
        }

        computePointGeometry(plotX, plotY, plotW, plotH, xScale.niceMin, xScale.niceMax, yScale.niceMin, yScale.niceMax);

        float anim = 1.0f;
        if (animationEnabled && animationDurationMs > 0) {
            if (animationStartMs < 0) animationStartMs = p.millis();
            float t = (p.millis() - animationStartMs) / (float) animationDurationMs;
            t = PApplet.constrain(t, 0, 1);
            anim = easeOutCubic(t);
        }

        for (int i = 0; i < labels.length; i++) {
            if (!isPointVisible(i)) continue;

            int fillColor = resolveGroupColor(groups[i], getGroupIndex(groups[i]));
            if (i == selectedIndex) fillColor = lighten(fillColor, 35);
            else if (i == hoverIndex) fillColor = lighten(fillColor, 18);

            float r = pointScreenR[i] * anim;
            float px = pointScreenX[i];
            float py = pointScreenY[i];

            p.noStroke();
            p.fill(fillColor);
            p.ellipse(px, py, r, r);

            p.stroke(i == selectedIndex ? selectionStroke : axisColor);
            p.strokeWeight(i == selectedIndex ? 2f : 1f);
            p.noFill();
            p.ellipse(px, py, r, r);

            if (showPointLabels) {
                p.noStroke();
                p.fill(textColor);
                p.textAlign(PConstants.LEFT, PConstants.BOTTOM);
                p.textSize(11);
                p.text(labels[i], px + r * 0.5f + 3, py - 3);
            }
        }

        drawTitles(x, y, w, h, panelW);
        if (showLegend) drawLegend(plotX + plotW - 10, plotY + 10);
        if (showDetailsPanel) drawDetailsPanel(x + w - panelW + 10, plotY, panelW - 20, plotH);
        if (showStatusOverlay) drawStatusOverlay(plotX, plotY);
        if (showHelpOverlay) drawHelpOverlay(plotX, plotY + 76, plotW);

        if (hoverIndex >= 0 && hoverIndex < labels.length) {
            drawTooltip(buildTooltipText(hoverIndex), p.mouseX, p.mouseY);
        }
    }

    private void computePointGeometry(float plotX, float plotY, float plotW, float plotH,
                                      float minX, float maxX, float minY, float maxY) {
        pointScreenX = new float[labels.length];
        pointScreenY = new float[labels.length];
        pointScreenR = new float[labels.length];

        float minSize = getVisibleMin(sizeValues);
        float maxSize = getVisibleMax(sizeValues);

        if (Float.isNaN(minSize) || Float.isNaN(maxSize) || Math.abs(maxSize - minSize) < 0.000001f) {
            minSize = 1f;
            maxSize = 1f;
        }

        for (int i = 0; i < labels.length; i++) {
            pointScreenX[i] = valueToScreenX(xValues[i], minX, maxX, plotX, plotW);
            pointScreenY[i] = valueToScreenY(yValues[i], minY, maxY, plotY, plotH);

            if (useBubbleSizes) {
                float t = (sizeValues[i] - minSize) / (maxSize - minSize);
                if (Float.isNaN(t)) t = 0;
                t = PApplet.constrain(t, 0, 1);
                pointScreenR[i] = PApplet.lerp(pointSizeMin, pointSizeMax, t);
            } else {
                pointScreenR[i] = pointSizeMin;
            }
        }

        geometryValid = true;
    }

    private void drawGridAndTicks(float plotX, float plotY, float plotW, float plotH, NiceScale xScale, NiceScale yScale) {
        p.stroke(gridColor);
        p.strokeWeight(1);

        for (int i = 0; i < xScale.tickCount; i++) {
            float value = xScale.niceMin + i * xScale.tickSpacing;
            float sx = valueToScreenX(value, xScale.niceMin, xScale.niceMax, plotX, plotW);
            p.line(sx, plotY, sx, plotY + plotH);
        }

        for (int i = 0; i < yScale.tickCount; i++) {
            float value = yScale.niceMin + i * yScale.tickSpacing;
            float sy = valueToScreenY(value, yScale.niceMin, yScale.niceMax, plotY, plotH);
            p.line(plotX, sy, plotX + plotW, sy);
        }

        drawAxesOnly(plotX, plotY, plotW, plotH);
        drawTicks(plotX, plotY, plotW, plotH, xScale, yScale);
    }

    private void drawAxesOnly(float plotX, float plotY, float plotW, float plotH) {
        p.stroke(axisColor);
        p.strokeWeight(1.2f);
        p.line(plotX, plotY, plotX, plotY + plotH);
        p.line(plotX, plotY + plotH, plotX + plotW, plotY + plotH);
    }

    private void drawTicks(float plotX, float plotY, float plotW, float plotH, NiceScale xScale, NiceScale yScale) {
        p.noStroke();
        p.fill(textColor);
        p.textSize(12);

        p.textAlign(PConstants.CENTER, PConstants.TOP);
        for (int i = 0; i < xScale.tickCount; i++) {
            float value = xScale.niceMin + i * xScale.tickSpacing;
            float sx = valueToScreenX(value, xScale.niceMin, xScale.niceMax, plotX, plotW);
            p.text(formatTick(value), sx, plotY + plotH + 8);
        }

        p.textAlign(PConstants.RIGHT, PConstants.CENTER);
        for (int i = 0; i < yScale.tickCount; i++) {
            float value = yScale.niceMin + i * yScale.tickSpacing;
            float sy = valueToScreenY(value, yScale.niceMin, yScale.niceMax, plotY, plotH);
            p.text(formatTick(value), plotX - 8, sy);
        }
    }

    private void drawTitles(int x, int y, int w, int h, int panelW) {
        p.fill(textColor);
        float centerX = x + (w - panelW) / 2.0f;

        if (!title.isEmpty()) {
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(18);
            p.text(title, centerX, y + marginTop / 2.0f);
        }

        if (!xLabel.isEmpty()) {
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(14);
            p.text(xLabel, centerX, y + h - marginBottom / 2.0f);
        }

        if (!yLabel.isEmpty()) {
            p.pushMatrix();
            p.translate(x + marginLeft / 2.0f, y + h / 2.0f);
            p.rotate(-PConstants.HALF_PI);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(14);
            p.text(yLabel, 0, 0);
            p.popMatrix();
        }
    }

    private void drawLegend(int rightX, int topY) {
        if (uniqueGroups == null || uniqueGroups.length == 0) return;

        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.textSize(12);

        int box = 12;
        int gap = 8;
        int lineH = legendRowH;

        float maxW = 0;
        for (String name : uniqueGroups) maxW = Math.max(maxW, p.textWidth(name));

        int bw = Math.round(maxW) + box + gap + 16;
        int bh = uniqueGroups.length * lineH + 12;

        int bx = rightX - bw;
        int by = topY;

        legendBoxX = bx;
        legendBoxY = by;
        legendBoxW = bw;
        legendBoxH = bh;

        if (legendRowY == null || legendRowY.length != uniqueGroups.length) {
            legendRowY = new int[uniqueGroups.length];
        }

        p.noStroke();
        p.fill(background);
        p.rect(bx, by, bw, bh);

        p.stroke(axisColor);
        p.strokeWeight(1);
        p.noFill();
        p.rect(bx, by, bw, bh);

        for (int j = 0; j < uniqueGroups.length; j++) {
            int yy = by + 10 + j * lineH + 6;
            legendRowY[j] = yy;

            int fill = resolveGroupColor(uniqueGroups[j], j);
            boolean vis = groupVisible[j];
            int swatchFill = vis ? fill : (themeMode == ThemeMode.DARK ? 0xff555555 : 0xffcccccc);

            p.noStroke();
            p.fill(swatchFill);
            p.rect(bx + 8, yy - box / 2, box, box);

            p.stroke(axisColor);
            p.strokeWeight(1);
            p.noFill();
            p.rect(bx + 8, yy - box / 2, box, box);

            p.noStroke();
            p.fill(vis ? textColor : (themeMode == ThemeMode.DARK ? 0xffaaaaaa : 0xff777777));
            p.text(uniqueGroups[j], bx + 8 + box + gap, yy);
        }

        legendGeometryValid = true;
    }

    private void drawDetailsPanel(int x, int y, int w, int h) {
        p.noStroke();
        p.fill(detailsPanelFill);
        p.rect(x, y, w, h);

        p.stroke(detailsPanelStroke);
        p.strokeWeight(1);
        p.noFill();
        p.rect(x, y, w, h);

        int tx = x + 12;
        int ty = y + 14;
        int lineH = 18;

        p.noStroke();
        p.fill(textColor);
        p.textAlign(PConstants.LEFT, PConstants.TOP);

        p.textSize(14);
        p.text("Analysis", tx, ty);
        ty += 28;

        p.textSize(12);

        if (selectedIndex >= 0) {
            p.text("Label:", tx, ty); ty += lineH;
            p.text(labels[selectedIndex], tx, ty); ty += lineH + 4;

            p.text("Group:", tx, ty); ty += lineH;
            p.text(groups[selectedIndex], tx, ty); ty += lineH + 4;

            p.text("X:", tx, ty); ty += lineH;
            p.text(formatTick(xValues[selectedIndex]), tx, ty); ty += lineH + 4;

            p.text("Y:", tx, ty); ty += lineH;
            p.text(formatTick(yValues[selectedIndex]), tx, ty); ty += lineH + 4;

            if (sizeLabel != null && sizeLabel.length() > 0) {
                p.text(sizeLabel + ":", tx, ty); ty += lineH;
                p.text(formatTick(sizeValues[selectedIndex]), tx, ty); ty += lineH + 10;
            }
        } else {
            p.text("No point selected.", tx, ty);
            ty += lineH + 10;
        }

        p.text("Visible data", tx, ty); ty += 24;
        p.text("Visible points: " + getVisiblePointCount(), tx, ty); ty += 16;
        p.text("Visible groups: " + getVisibleGroupCount() + " / " + uniqueGroups.length, tx, ty); ty += 16;
        p.text("Zoom: " + String.format("%.2fx", zoomFactor), tx, ty); ty += 16;

        float avgX = getVisibleAverage(xValues);
        float avgY = getVisibleAverage(yValues);
        if (!Float.isNaN(avgX) && !Float.isNaN(avgY)) {
            p.text("Avg X: " + formatTick(avgX), tx, ty); ty += 16;
            p.text("Avg Y: " + formatTick(avgY), tx, ty); ty += 16;
        }

        int[] q = getQuadrantCounts(avgX, avgY);
        if (q != null) {
            ty += 6;
            p.text("Quadrants vs averages", tx, ty); ty += 20;
            p.text("Q1 (+X,+Y): " + q[0], tx, ty); ty += 16;
            p.text("Q2 (-X,+Y): " + q[1], tx, ty); ty += 16;
            p.text("Q3 (-X,-Y): " + q[2], tx, ty); ty += 16;
            p.text("Q4 (+X,-Y): " + q[3], tx, ty); ty += 16;
        }
    }

    private void drawStatusOverlay(int plotX, int plotY) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Visible points: " + getVisiblePointCount());
        lines.add("Zoom: " + String.format("%.2fx", zoomFactor));
        lines.add("Avg lines: " + (showAverageLines ? "on" : "off"));
        lines.add("Labels: " + (showPointLabels ? "on" : "off"));
        lines.add("Theme: " + (themeMode == ThemeMode.DARK ? "dark" : "light"));
        lines.add("Palette: " + getActivePaletteNameLabel());
        lines.add("Custom colors: " + (customColorsActive ? "on" : "off"));

        p.textSize(11);
        float maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, p.textWidth(line));

        int bw = (int) maxW + 16;
        int bh = lines.size() * 15 + 12;
        int bx = plotX + 8;
        int by = plotY + 8;

        p.noStroke();
        p.fill(statusOverlayFill);
        p.rect(bx, by, bw, bh);

        p.stroke(statusOverlayStroke);
        p.strokeWeight(1);
        p.noFill();
        p.rect(bx, by, bw, bh);

        p.noStroke();
        p.fill(textColor);
        p.textAlign(PConstants.LEFT, PConstants.TOP);

        int ty = by + 6;
        for (String line : lines) {
            p.text(line, bx + 8, ty);
            ty += 15;
        }
    }

    private void drawHelpOverlay(int x, int y, int plotW) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Controls");
        lines.add("Arrow keys pan");
        lines.add("+ / - zoom in/out");
        lines.add("S show all groups");
        lines.add("C clear selection");
        lines.add("X reset initial view");
        lines.add("A average lines");
        lines.add("I point labels");
        lines.add("G grid");
        lines.add("P details panel");
        lines.add("O status overlay");
        lines.add("H help overlay");
        lines.add("R restart animation");
        lines.add("M toggle light/dark");
        lines.add("B colorblind palette");
        lines.add("L pastel palette");
        lines.add("K high contrast palette");
        lines.add("U default palette");
        lines.add("Y toggle custom colors");

        p.textSize(11);
        float maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, p.textWidth(line));

        int bw = (int) maxW + 18;
        int bh = lines.size() * 14 + 12;
        int bx = x + 8;
        int by = y;

        if (bx + bw > x + plotW) bx = x + plotW - bw - 8;

        p.noStroke();
        p.fill(helpOverlayFill);
        p.rect(bx, by, bw, bh);

        p.stroke(helpOverlayStroke);
        p.strokeWeight(1);
        p.noFill();
        p.rect(bx, by, bw, bh);

        p.noStroke();
        p.fill(textColor);
        p.textAlign(PConstants.LEFT, PConstants.TOP);

        int ty = by + 6;
        for (String line : lines) {
            p.text(line, bx + 8, ty);
            ty += 14;
        }
    }

    private void drawTooltip(String text, int mx, int my) {
        p.textSize(12);
        float tw = p.textWidth(text);
        int padding = 8;

        int bx = mx + 12;
        int by = my - 18;
        int bw = Math.round(tw) + padding * 2;
        int bh = 22;

        if (bx + bw > p.width) bx = p.width - bw - 5;
        if (by < 5) by = 5;

        p.noStroke();
        p.fill(tooltipFill);
        p.rect(bx, by, bw, bh);

        p.stroke(tooltipStroke);
        p.strokeWeight(1);
        p.noFill();
        p.rect(bx, by, bw, bh);

        p.noStroke();
        p.fill(textColor);
        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.text(text, bx + padding, by + bh / 2.0f);
    }

    private void drawMessage(String msg, int x, int y) {
        p.fill(textColor);
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        p.textSize(12);
        p.text(msg, x + 20, y + 20);
    }

    // ---------------------------
    // Analytics / helpers
    // ---------------------------

    private boolean isPointVisible(int i) {
        int g = getGroupIndex(groups[i]);
        return g >= 0 && g < groupVisible.length && groupVisible[g];
    }

    private int getGroupIndex(String group) {
        for (int i = 0; i < uniqueGroups.length; i++) {
            if (uniqueGroups[i].equals(group)) return i;
        }
        return 0;
    }

    private int getVisiblePointCount() {
        int count = 0;
        for (int i = 0; i < labels.length; i++) {
            if (isPointVisible(i)) count++;
        }
        return count;
    }

    private int getVisibleGroupCount() {
        int count = 0;
        for (boolean v : groupVisible) if (v) count++;
        return count;
    }

    private float getVisibleMin(float[] arr) {
        float min = Float.NaN;
        for (int i = 0; i < arr.length; i++) {
            if (!isPointVisible(i)) continue;
            if (Float.isNaN(min) || arr[i] < min) min = arr[i];
        }
        return min;
    }

    private float getVisibleMax(float[] arr) {
        float max = Float.NaN;
        for (int i = 0; i < arr.length; i++) {
            if (!isPointVisible(i)) continue;
            if (Float.isNaN(max) || arr[i] > max) max = arr[i];
        }
        return max;
    }

    private float getVisibleAverage(float[] arr) {
        float sum = 0;
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!isPointVisible(i)) continue;
            sum += arr[i];
            count++;
        }
        return count > 0 ? sum / count : Float.NaN;
    }

    private int[] getQuadrantCounts(float avgX, float avgY) {
        if (Float.isNaN(avgX) || Float.isNaN(avgY)) return null;

        int[] q = new int[4];
        for (int i = 0; i < labels.length; i++) {
            if (!isPointVisible(i)) continue;

            boolean right = xValues[i] >= avgX;
            boolean top = yValues[i] >= avgY;

            if (right && top) q[0]++;
            else if (!right && top) q[1]++;
            else if (!right) q[2]++;
            else q[3]++;
        }
        return q;
    }

    private String buildTooltipText(int i) {
        String txt = labels[i] + " | " + xLabel + ": " + formatTick(xValues[i]) + ", " + yLabel + ": " + formatTick(yValues[i]);
        if (sizeLabel != null && sizeLabel.length() > 0) {
            txt += ", " + sizeLabel + ": " + formatTick(sizeValues[i]);
        }
        txt += ", Group: " + groups[i];
        return txt;
    }

    private float[] applyZoomAndPan(float minVal, float maxVal, float pan, float zoom) {
        float center = (minVal + maxVal) * 0.5f;
        float range = (maxVal - minVal);
        if (Math.abs(range) < 0.000001f) range = 1f;
        range /= zoom;
        center += pan * range;
        return new float[] { center - range * 0.5f, center + range * 0.5f };
    }

    private NiceScale computeScale(float minVal, float maxVal, int desiredTicks) {
        NiceScale scale = new NiceScale();

        if (!useNiceAxis) {
            scale.niceMin = minVal;
            scale.niceMax = maxVal;
            scale.tickCount = desiredTicks + 1;
            scale.tickSpacing = (maxVal - minVal) / desiredTicks;
            return scale;
        }

        if (Math.abs(maxVal - minVal) < 0.000001f) {
            maxVal += 1;
            minVal -= 1;
        }

        float range = niceNum(maxVal - minVal, false);
        float tickSpacing = niceNum(range / desiredTicks, true);
        float niceMin = PApplet.floor(minVal / tickSpacing) * tickSpacing;
        float niceMax = PApplet.ceil(maxVal / tickSpacing) * tickSpacing;
        int tickCount = Math.max(2, Math.round((niceMax - niceMin) / tickSpacing) + 1);

        scale.niceMin = niceMin;
        scale.niceMax = niceMax;
        scale.tickSpacing = tickSpacing;
        scale.tickCount = tickCount;
        return scale;
    }

    private float niceNum(float range, boolean round) {
        if (range <= 0) return 1;

        double exponent = Math.floor(Math.log10(range));
        double fraction = range / Math.pow(10, exponent);
        double niceFraction;

        if (round) {
            if (fraction < 1.5) niceFraction = 1;
            else if (fraction < 3) niceFraction = 2;
            else if (fraction < 7) niceFraction = 5;
            else niceFraction = 10;
        } else {
            if (fraction <= 1) niceFraction = 1;
            else if (fraction <= 2) niceFraction = 2;
            else if (fraction <= 5) niceFraction = 5;
            else niceFraction = 10;
        }

        return (float) (niceFraction * Math.pow(10, exponent));
    }

    private float valueToScreenX(float value, float minVal, float maxVal, float plotX, float plotW) {
        float t = (value - minVal) / (maxVal - minVal);
        t = PApplet.constrain(t, 0, 1);
        return plotX + t * plotW;
    }

    private float valueToScreenY(float value, float minVal, float maxVal, float plotY, float plotH) {
        float t = (value - minVal) / (maxVal - minVal);
        t = PApplet.constrain(t, 0, 1);
        return plotY + plotH - t * plotH;
    }

    private float easeOutCubic(float t) {
        float u = 1 - t;
        return 1 - u * u * u;
    }

    private int resolveGroupColor(String groupName, int groupIndex) {
        Integer override = groupColorOverrides.get(groupName);
        if (override != null) return override;
        return autoColor(groupIndex, uniqueGroups.length);
    }

    private int autoColor(int index, int totalGroups) {
        if (palette != null && index >= 0 && index < palette.length) {
            return palette[index];
        }

        if (palette != null && palette.length > 0 && totalGroups <= palette.length) {
            return palette[index % palette.length];
        }

        float hue = (index * (360.0f / Math.max(1, totalGroups))) % 360.0f;
        float sat = (themeMode == ThemeMode.DARK) ? 70f : 75f;
        float bri = (themeMode == ThemeMode.DARK) ? 92f : 82f;
        return hsbToArgb(hue, sat, bri);
    }

    private int hsbToArgb(float h, float s, float v) {
        float hh = h / 360.0f;
        float ss = s / 100.0f;
        float vv = v / 100.0f;

        int i = (int) Math.floor(hh * 6.0f);
        float f = hh * 6.0f - i;
        float pVal = vv * (1.0f - ss);
        float qVal = vv * (1.0f - f * ss);
        float tVal = vv * (1.0f - (1.0f - f) * ss);

        float r = 0, g = 0, b = 0;
        switch (i % 6) {
            case 0: r = vv; g = tVal; b = pVal; break;
            case 1: r = qVal; g = vv; b = pVal; break;
            case 2: r = pVal; g = vv; b = tVal; break;
            case 3: r = pVal; g = qVal; b = vv; break;
            case 4: r = tVal; g = pVal; b = vv; break;
            case 5: r = vv; g = pVal; b = qVal; break;
        }

        int ri = clamp255(Math.round(r * 255.0f));
        int gi = clamp255(Math.round(g * 255.0f));
        int bi = clamp255(Math.round(b * 255.0f));
        return (0xff << 24) | (ri << 16) | (gi << 8) | bi;
    }

    private String getActivePaletteNameLabel() {
        if (activePaletteName == null) return "custom";
        switch (activePaletteName) {
            case COLORBLIND: return "colorblind";
            case PASTEL: return "pastel";
            case HIGHCONTRAST: return "highcontrast";
            default: return "default";
        }
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

    private String safeString(String s) {
        return s == null ? "" : s.trim();
    }

    private Float parseFloatSafe(String raw) {
        if (raw == null) return null;
        String cleaned = raw.trim().replace(",", "");
        if (cleaned.length() == 0) return null;
        try {
            return Float.parseFloat(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatTick(float v) {
        float av = Math.abs(v);
        if (av >= 1000000) return String.format("%.1fM", v / 1000000f);
        if (av >= 1000) return String.format("%.1fk", v / 1000f);
        if (av >= 10) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }

    private int lighten(int argb, int amount) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = (argb) & 0xff;

        r = clamp255(r + amount);
        g = clamp255(g + amount);
        b = clamp255(b + amount);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    private void invalidateGeometry() {
        geometryValid = false;
        legendGeometryValid = false;
        hoverIndex = -1;
    }
}
