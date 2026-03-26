package com.ata.charts;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.Table;
import processing.data.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class finalBarChart1 {

    public enum SortOrder {
        ASCENDING,
        DESCENDING
    }

    public enum BarMode {
        GROUPED,
        STACKED,
        PERCENT_STACKED
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    private static class RowEntry {
        String category;
        float[] values;

        RowEntry(String category, float[] values) {
            this.category = category;
            this.values = values;
        }
    }

    private final PApplet p;

    private String title = "";
    private String xLabel = "";
    private String yLabel = "";

    private int marginLeft = 90;
    private int marginRight = 40;
    private int marginTop = 70;
    private int marginBottom = 120;

    private int yTickCount = 5;
    private boolean showGrid = true;
    private boolean useNiceAxis = true;

    private String[] sourceCategories;
    private String[] sourceSeriesNames;
    private float[][] sourceValues;

    private String[] categories;
    private String[] seriesNames;
    private float[][] values;

    private boolean[] seriesVisible;
    private boolean legendInteractive = true;

    private int legendBoxX, legendBoxY, legendBoxW, legendBoxH;
    private int[] legendRowY;
    private int legendRowH = 18;
    private boolean legendGeometryValid = false;

    private int hoverI = -1;
    private int hoverJ = -1;
    private int selectedI = -1;
    private int selectedJ = -1;
    private boolean selectionEnabled = true;

    private int[][] barX, barY, barW, barH;
    private int[] visibleDataIndices;
    private boolean geometryValid = false;

    private float groupInnerPadding = 0.25f;
    private float barInnerGap = 0.15f;

    private boolean animationEnabled = true;
    private int animationDurationMs = 1000;
    private int animationStartMs = -1;

    private boolean skipInvalidTableRows = false;
    private float invalidReplacementValue = 0f;

    private boolean useTopN = false;
    private int topN = 8;
    private String sortSeriesName = null;
    private SortOrder sortOrder = SortOrder.DESCENDING;

    private String initialSortSeriesName = null;
    private SortOrder initialSortOrder = SortOrder.DESCENDING;
    private int initialTopN = 8;
    private boolean initialUseTopN = false;
    private int initialPageSize = 6;
    private boolean initialShowReferenceLine = false;
    private float initialReferenceLineValue = 0f;
    private String initialReferenceLineLabel = "";

    private int pageSize = 6;
    private int currentPage = 0;

    private BarMode barMode = BarMode.GROUPED;
    private Orientation orientation = Orientation.VERTICAL;

    private boolean showReferenceLine = false;
    private float referenceLineValue = 0f;
    private String referenceLineLabel = "";

    private boolean showAverageLine = true;

    private boolean showDetailsPanel = true;
    private int detailsPanelWidth = 250;
    private boolean showStatusOverlay = true;
    private boolean showHelpOverlay = true;
    private boolean showValueLabels = false;

    private boolean topNInputMode = false;
    private String topNInputBuffer = "";

    private boolean rotateCategoryLabels = false;
    private float categoryLabelAngle = -PConstants.HALF_PI / 3.0f;
    private boolean clipLongLabels = true;

    // Light/dark mode
    private boolean darkMode = false;

    // Colors
    private int background = 0xffffffff;
    private int axisColor = 0xff202020;
    private int gridColor = 0xffdddddd;
    private int textColor = 0xff111111;
    private int titleColor = 0xff111111;
    private int axisLabelColor = 0xff202020;
    private int tickLabelColor = 0xff404040;
    private int barStroke = 0xff202020;
    private int tooltipFill = 0xffffffff;
    private int tooltipStroke = 0xff202020;
    private int selectionOutline = 0xff000000;
    private int referenceLineColor = 0xffcc3333;
    private int averageLineColor = 0xff3366cc;
    private int detailsPanelFill = 0xfff7f7f7;
    private int detailsPanelStroke = 0xffbbbbbb;
    private int statusOverlayFill = 0xf2ffffff;
    private int statusOverlayStroke = 0xffbbbbbb;
    private int helpOverlayFill = 0xf7ffffff;
    private int helpOverlayStroke = 0xffbbbbbb;
    private int selectedGroupFill = 0x11000000;

    private float strokeWeight = 1.0f;

    private int titleSize = 18;
    private int labelSize = 14;
    private int tickSize = 12;

    private int hoverLightenAmount = 18;
    private int selectedLightenAmount = 35;

    // Base palette for first few series
    private int[] palette = new int[] {
        0xff1f77b4, 0xffff7f0e, 0xff2ca02c, 0xffd62728,
        0xff9467bd, 0xff8c564b, 0xffe377c2, 0xff7f7f7f
    };

    // Explicit colors
    private int[] seriesColors = null;
    private int[] seriesHoverColors = null;
    private int[] seriesSelectedColors = null;
    private boolean useExplicitSeriesColors = false;
    private boolean useExplicitHoverColors = false;
    private boolean useExplicitSelectedColors = false;

    // Automatic overflow colors
    private boolean autoGenerateSeriesColors = true;

    // Novice-friendly color behavior
    private boolean preserveCustomPaletteAcrossModeChange = true;
    private int[] customPalette = null;
    private String[] knownSeriesNames = null;
    private final LinkedHashMap<String, Integer> pendingNamedBaseColors = new LinkedHashMap<String, Integer>();

    // Optional toggleable custom color preset
    private String[] customColorSeriesNames = null;
    private int[] customColorValues = null;
    private boolean customColorMode = false;

    public finalBarChart1(PApplet p) {
        this.p = p;
        applyLightMode();
    }

    public finalBarChart1 setTitle(String t) {
        this.title = (t == null) ? "" : t;
        return this;
    }

    public finalBarChart1 setXLabel(String t) {
        this.xLabel = (t == null) ? "" : t;
        return this;
    }

    public finalBarChart1 setYLabel(String t) {
        this.yLabel = (t == null) ? "" : t;
        return this;
    }

    public finalBarChart1 setBarMode(BarMode mode) {
        if (mode != null) {
            this.barMode = mode;
            invalidateGeometry();
            restartAnimation();
        }
        return this;
    }

    public finalBarChart1 setOrientation(Orientation orientation) {
        if (orientation != null) {
            this.orientation = orientation;
            invalidateGeometry();
            restartAnimation();
        }
        return this;
    }

    public finalBarChart1 setMargins(int left, int right, int top, int bottom) {
        this.marginLeft = left;
        this.marginRight = right;
        this.marginTop = top;
        this.marginBottom = bottom;
        invalidateGeometry();
        return this;
    }

    public finalBarChart1 setYTickCount(int ticks) {
        if (ticks >= 2) this.yTickCount = ticks;
        return this;
    }

    public finalBarChart1 setShowGrid(boolean show) {
        this.showGrid = show;
        return this;
    }

    public finalBarChart1 setUseNiceAxis(boolean useNiceAxis) {
        this.useNiceAxis = useNiceAxis;
        return this;
    }

    public finalBarChart1 setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
        return this;
    }

    public finalBarChart1 setAnimationDurationMs(int ms) {
        if (ms >= 0) this.animationDurationMs = ms;
        return this;
    }

    public finalBarChart1 restartAnimation() {
        this.animationStartMs = p.millis();
        return this;
    }

    public finalBarChart1 setLegendInteractive(boolean enabled) {
        this.legendInteractive = enabled;
        return this;
    }

    public finalBarChart1 setSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
        return this;
    }

    public finalBarChart1 setSkipInvalidTableRows(boolean skip) {
        this.skipInvalidTableRows = skip;
        return this;
    }

    public finalBarChart1 setInvalidReplacementValue(float value) {
        this.invalidReplacementValue = value;
        return this;
    }

    public finalBarChart1 setTopN(int n) {
        this.topN = Math.max(1, n);
        this.useTopN = true;
        rebuildData();
        return this;
    }

    public finalBarChart1 clearTopN() {
        this.useTopN = false;
        rebuildData();
        return this;
    }

    public finalBarChart1 setDefaultTopN(int n) {
        this.topN = Math.max(1, n);
        this.initialTopN = this.topN;
        return this;
    }

    public finalBarChart1 setTopNEnabledByDefault(boolean enabled) {
        this.initialUseTopN = enabled;
        this.useTopN = enabled;
        rebuildData();
        return this;
    }

    public finalBarChart1 setSortBySeries(String seriesName, SortOrder order) {
        this.sortSeriesName = seriesName;
        this.sortOrder = (order == null) ? SortOrder.DESCENDING : order;

        if (initialSortSeriesName == null) {
            initialSortSeriesName = this.sortSeriesName;
            initialSortOrder = this.sortOrder;
        }

        rebuildData();
        return this;
    }

    public finalBarChart1 clearSorting() {
        this.sortSeriesName = null;
        rebuildData();
        return this;
    }

    public finalBarChart1 setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
        this.initialPageSize = this.pageSize;
        clampPage();
        invalidateGeometry();
        return this;
    }

    public finalBarChart1 nextPage() {
        if (currentPage < getPageCount() - 1) {
            currentPage++;
            clearSelection();
            invalidateGeometry();
            restartAnimation();
        }
        return this;
    }

    public finalBarChart1 previousPage() {
        if (currentPage > 0) {
            currentPage--;
            clearSelection();
            invalidateGeometry();
            restartAnimation();
        }
        return this;
    }

    public finalBarChart1 setReferenceLine(float value, String label) {
        this.showReferenceLine = true;
        this.referenceLineValue = value;
        this.referenceLineLabel = (label == null) ? "" : label;
        this.initialShowReferenceLine = true;
        this.initialReferenceLineValue = value;
        this.initialReferenceLineLabel = this.referenceLineLabel;
        return this;
    }

    public finalBarChart1 clearReferenceLine() {
        this.showReferenceLine = false;
        return this;
    }

    public finalBarChart1 setShowAverageLine(boolean show) {
        this.showAverageLine = show;
        return this;
    }

    public finalBarChart1 setShowDetailsPanel(boolean show) {
        this.showDetailsPanel = show;
        return this;
    }

    public finalBarChart1 setShowStatusOverlay(boolean show) {
        this.showStatusOverlay = show;
        return this;
    }

    public finalBarChart1 setShowHelpOverlay(boolean show) {
        this.showHelpOverlay = show;
        return this;
    }

    public finalBarChart1 setShowValueLabels(boolean show) {
        this.showValueLabels = show;
        return this;
    }

    public finalBarChart1 setRotateCategoryLabels(boolean rotate) {
        this.rotateCategoryLabels = rotate;
        return this;
    }

    public finalBarChart1 setCategoryLabelAngle(float angle) {
        this.categoryLabelAngle = angle;
        return this;
    }

    public finalBarChart1 enableDefaultAnalyticalUI() {
        this.showDetailsPanel = true;
        this.showStatusOverlay = true;
        this.showHelpOverlay = true;
        this.showAverageLine = true;
        this.showValueLabels = false;
        this.legendInteractive = true;
        this.selectionEnabled = true;
        return this;
    }

    // ---------------------------
    // Light / dark mode
    // ---------------------------

    public finalBarChart1 setLightMode() {
        int[] paletteBefore = getPaletteCopy();
        applyLightMode();

        if (preserveCustomPaletteAcrossModeChange) {
            if (customPalette != null) {
                this.palette = Arrays.copyOf(customPalette, customPalette.length);
                refreshAutoColorCache();
            } else if (paletteBefore != null) {
                this.palette = Arrays.copyOf(paletteBefore, paletteBefore.length);
                refreshAutoColorCache();
            }
        }

        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 setDarkMode() {
        int[] paletteBefore = getPaletteCopy();
        applyDarkMode();

        if (preserveCustomPaletteAcrossModeChange) {
            if (customPalette != null) {
                this.palette = Arrays.copyOf(customPalette, customPalette.length);
                refreshAutoColorCache();
            } else if (paletteBefore != null) {
                this.palette = Arrays.copyOf(paletteBefore, paletteBefore.length);
                refreshAutoColorCache();
            }
        }

        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 toggleMode() {
        if (darkMode) setLightMode();
        else setDarkMode();
        return this;
    }

    public finalBarChart1 setPreserveCustomPaletteAcrossModeChange(boolean preserve) {
        this.preserveCustomPaletteAcrossModeChange = preserve;
        return this;
    }

    public finalBarChart1 useDefaultPaletteForCurrentMode() {
        this.customPalette = null;
        if (darkMode) applyDarkMode();
        else applyLightMode();
        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 setPaletteByName(String paletteName) {
        if (paletteName == null) return this;
        String key = paletteName.trim().toLowerCase();

        if (key.equals("default")) {
            return useDefaultPaletteForCurrentMode();
        } else if (key.equals("colorblind")) {
            return setPalette(new int[] {
                0xff0072B2, 0xffE69F00, 0xff009E73, 0xffD55E00,
                0xffCC79A7, 0xff56B4E9, 0xffF0E442, 0xff000000
            });
        } else if (key.equals("pastel")) {
            return setPalette(new int[] {
                0xff8DD3C7, 0xffFFFFB3, 0xffBEBADA, 0xffFB8072,
                0xff80B1D3, 0xffFDB462, 0xffB3DE69, 0xffFCCDE5
            });
        } else if (key.equals("highcontrast")) {
            return setPalette(new int[] {
                0xff1F77B4, 0xffD62728, 0xff2CA02C, 0xff9467BD,
                0xffFF7F0E, 0xff17BECF, 0xff8C564B, 0xffE377C2
            });
        }

        return this;
    }

    public finalBarChart1 setCustomSeriesColors(String[] seriesNames, int[] colors) {
        if (seriesNames == null || colors == null || seriesNames.length == 0 || colors.length == 0) {
            this.customColorSeriesNames = null;
            this.customColorValues = null;
            this.customColorMode = false;
            return this;
        }

        int n = Math.min(seriesNames.length, colors.length);
        this.customColorSeriesNames = new String[n];
        this.customColorValues = new int[n];
        for (int i = 0; i < n; i++) {
            this.customColorSeriesNames[i] = seriesNames[i];
            this.customColorValues[i] = colors[i];
        }

        if (customColorMode) {
            refreshAutoColorCache();
            reapplyNamedSeriesColors();
        }
        return this;
    }

    public finalBarChart1 applyCustomSeriesColors() {
        customColorMode = true;
        refreshAutoColorCache();
        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 clearCustomSeriesColors() {
        customColorMode = false;
        refreshAutoColorCache();
        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 toggleCustomSeriesColors() {
        customColorMode = !customColorMode;
        refreshAutoColorCache();
        reapplyNamedSeriesColors();
        return this;
    }

    public boolean isCustomColorMode() {
        return customColorMode;
    }

    public boolean hasCustomSeriesColors() {
        return customColorSeriesNames != null && customColorValues != null && customColorSeriesNames.length > 0;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    private void applyLightMode() {
        darkMode = false;

        background = 0xffffffff;
        axisColor = 0xff202020;
        gridColor = 0xffdddddd;
        textColor = 0xff111111;
        titleColor = 0xff111111;
        axisLabelColor = 0xff202020;
        tickLabelColor = 0xff404040;
        barStroke = 0xff202020;
        tooltipFill = 0xffffffff;
        tooltipStroke = 0xff202020;
        selectionOutline = 0xff000000;
        referenceLineColor = 0xffcc3333;
        averageLineColor = 0xff3366cc;
        detailsPanelFill = 0xfff7f7f7;
        detailsPanelStroke = 0xffbbbbbb;
        statusOverlayFill = 0xf2ffffff;
        statusOverlayStroke = 0xffbbbbbb;
        helpOverlayFill = 0xf7ffffff;
        helpOverlayStroke = 0xffbbbbbb;
        selectedGroupFill = 0x11000000;

        palette = new int[] {
            0xff1f77b4, 0xffff7f0e, 0xff2ca02c, 0xffd62728,
            0xff9467bd, 0xff8c564b, 0xffe377c2, 0xff7f7f7f
        };

        refreshAutoColorCache();
    }

    private void applyDarkMode() {
        darkMode = true;

        background = 0xff1b1f24;
        axisColor = 0xffd7dee7;
        gridColor = 0xff38414c;
        textColor = 0xffedf2f7;
        titleColor = 0xffffffff;
        axisLabelColor = 0xffedf2f7;
        tickLabelColor = 0xffd7dee7;
        barStroke = 0xffd7dee7;
        tooltipFill = 0xff2a3038;
        tooltipStroke = 0xff566170;
        selectionOutline = 0xffffffff;
        referenceLineColor = 0xffff6b6b;
        averageLineColor = 0xff7cc6fe;
        detailsPanelFill = 0xff242a31;
        detailsPanelStroke = 0xff44505c;
        statusOverlayFill = 0xf2242a31;
        statusOverlayStroke = 0xff44505c;
        helpOverlayFill = 0xf7242a31;
        helpOverlayStroke = 0xff44505c;
        selectedGroupFill = 0x22ffffff;

        palette = new int[] {
            0xff4cc9f0, 0xfff72585, 0xff90be6d, 0xfffca311,
            0xff9d4edd, 0xffff6b6b, 0xff43aa8b, 0xff4895ef
        };

        refreshAutoColorCache();
    }

    // ---------------------------
    // Color customization
    // ---------------------------

    public finalBarChart1 setAutoGenerateSeriesColors(boolean enabled) {
        this.autoGenerateSeriesColors = enabled;
        refreshAutoColorCache();
        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 setPalette(int[] colors) {
        if (colors != null && colors.length > 0) {
            this.palette = Arrays.copyOf(colors, colors.length);
            this.customPalette = Arrays.copyOf(colors, colors.length);
            refreshAutoColorCache();
            reapplyNamedSeriesColors();
        }
        return this;
    }

    public finalBarChart1 setBackgroundColor(int c) {
        this.background = c;
        return this;
    }

    public finalBarChart1 setAxisColor(int c) {
        this.axisColor = c;
        return this;
    }

    public finalBarChart1 setGridColor(int c) {
        this.gridColor = c;
        return this;
    }

    public finalBarChart1 setTextColor(int c) {
        this.textColor = c;
        this.titleColor = c;
        this.axisLabelColor = c;
        this.tickLabelColor = c;
        return this;
    }

    public finalBarChart1 setTitleColor(int c) {
        this.titleColor = c;
        return this;
    }

    public finalBarChart1 setAxisLabelColor(int c) {
        this.axisLabelColor = c;
        return this;
    }

    public finalBarChart1 setTickLabelColor(int c) {
        this.tickLabelColor = c;
        return this;
    }

    public finalBarChart1 setBarStrokeColor(int c) {
        this.barStroke = c;
        return this;
    }

    public finalBarChart1 setTooltipFillColor(int c) {
        this.tooltipFill = c;
        return this;
    }

    public finalBarChart1 setTooltipStrokeColor(int c) {
        this.tooltipStroke = c;
        return this;
    }

    public finalBarChart1 setSelectionOutlineColor(int c) {
        this.selectionOutline = c;
        return this;
    }

    public finalBarChart1 setReferenceLineColor(int c) {
        this.referenceLineColor = c;
        return this;
    }

    public finalBarChart1 setAverageLineColor(int c) {
        this.averageLineColor = c;
        return this;
    }

    public finalBarChart1 setDetailsPanelColors(int fill, int stroke) {
        this.detailsPanelFill = fill;
        this.detailsPanelStroke = stroke;
        return this;
    }

    public finalBarChart1 setStatusOverlayColors(int fill, int stroke) {
        this.statusOverlayFill = fill;
        this.statusOverlayStroke = stroke;
        return this;
    }

    public finalBarChart1 setHelpOverlayColors(int fill, int stroke) {
        this.helpOverlayFill = fill;
        this.helpOverlayStroke = stroke;
        return this;
    }

    public finalBarChart1 setSelectedGroupFillColor(int c) {
        this.selectedGroupFill = c;
        return this;
    }

    public finalBarChart1 setStrokeWeight(float w) {
        this.strokeWeight = Math.max(0.5f, w);
        return this;
    }

    public finalBarChart1 setTextSizes(int titleSize, int labelSize, int tickSize) {
        this.titleSize = Math.max(10, titleSize);
        this.labelSize = Math.max(10, labelSize);
        this.tickSize = Math.max(9, tickSize);
        return this;
    }

    public finalBarChart1 setSeriesColor(int seriesIndex, int color) {
        ensureSeriesColorCapacity();
        if (seriesIndex >= 0 && seriesColors != null && seriesIndex < seriesColors.length) {
            seriesColors[seriesIndex] = color;
            useExplicitSeriesColors = true;
        }
        return this;
    }

    public finalBarChart1 setSeriesColor(String seriesName, int color) {
        if (seriesName == null) return this;
        pendingNamedBaseColors.put(seriesName, color);

        int idx = getKnownSeriesIndex(seriesName);
        if (idx >= 0) {
            setSeriesColor(idx, color);
        }
        return this;
    }

    public finalBarChart1 setSeriesColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            ensureSeriesColorCapacity();
            int n = Math.min(colors.length, seriesColors.length);
            for (int i = 0; i < n; i++) seriesColors[i] = colors[i];
            useExplicitSeriesColors = true;
        }
        return this;
    }

    public finalBarChart1 clearSeriesColors() {
        useExplicitSeriesColors = false;
        pendingNamedBaseColors.clear();
        refreshAutoColorCache();
        return this;
    }

    public finalBarChart1 setSeriesHoverColor(int seriesIndex, int color) {
        ensureSeriesColorCapacity();
        if (seriesIndex >= 0 && seriesHoverColors != null && seriesIndex < seriesHoverColors.length) {
            seriesHoverColors[seriesIndex] = color;
            useExplicitHoverColors = true;
        }
        return this;
    }

    public finalBarChart1 setSeriesHoverColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            ensureSeriesColorCapacity();
            int n = Math.min(colors.length, seriesHoverColors.length);
            for (int i = 0; i < n; i++) seriesHoverColors[i] = colors[i];
            useExplicitHoverColors = true;
        }
        return this;
    }

    public finalBarChart1 clearSeriesHoverColors() {
        useExplicitHoverColors = false;
        return this;
    }

    public finalBarChart1 setSeriesSelectedColor(int seriesIndex, int color) {
        ensureSeriesColorCapacity();
        if (seriesIndex >= 0 && seriesSelectedColors != null && seriesIndex < seriesSelectedColors.length) {
            seriesSelectedColors[seriesIndex] = color;
            useExplicitSelectedColors = true;
        }
        return this;
    }

    public finalBarChart1 setSeriesSelectedColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            ensureSeriesColorCapacity();
            int n = Math.min(colors.length, seriesSelectedColors.length);
            for (int i = 0; i < n; i++) seriesSelectedColors[i] = colors[i];
            useExplicitSelectedColors = true;
        }
        return this;
    }

    public finalBarChart1 clearSeriesSelectedColors() {
        useExplicitSelectedColors = false;
        return this;
    }

    public finalBarChart1 resetToInitialView() {
        clearSelection();
        showAllSeries();
        currentPage = 0;
        pageSize = initialPageSize;
        sortSeriesName = initialSortSeriesName;
        sortOrder = initialSortOrder;
        topN = initialTopN;
        useTopN = initialUseTopN;
        showReferenceLine = initialShowReferenceLine;
        referenceLineValue = initialReferenceLineValue;
        referenceLineLabel = initialReferenceLineLabel;
        topNInputMode = false;
        topNInputBuffer = "";
        rebuildData();
        restartAnimation();
        return this;
    }

    public void beginTopNInput() {
        topNInputMode = true;
        topNInputBuffer = "";
    }

    public void cancelTopNInput() {
        topNInputMode = false;
        topNInputBuffer = "";
    }

    public void confirmTopNInput() {
        if (topNInputBuffer != null && topNInputBuffer.length() > 0) {
            try {
                int n = Integer.parseInt(topNInputBuffer);
                if (n > 0) setTopN(n);
            } catch (Exception e) {
            }
        }
        topNInputMode = false;
        topNInputBuffer = "";
    }

    public void clearSelection() {
        selectedI = -1;
        selectedJ = -1;
    }

    public int getPageCount() {
        if (categories == null || categories.length == 0) return 1;
        return (int) Math.ceil(categories.length / (float) pageSize);
    }

    public finalBarChart1 setSeriesVisible(int seriesIndex, boolean visible) {
        ensureSeriesVisible();
        if (seriesVisible == null) return this;
        if (seriesIndex < 0 || seriesIndex >= seriesVisible.length) return this;
        seriesVisible[seriesIndex] = visible;
        invalidateGeometry();
        restartAnimation();
        return this;
    }

    public finalBarChart1 showAllSeries() {
        ensureSeriesVisible();
        if (seriesVisible != null) {
            for (int i = 0; i < seriesVisible.length; i++) seriesVisible[i] = true;
        }
        invalidateGeometry();
        restartAnimation();
        return this;
    }

    public finalBarChart1 setData(String[] categories, String[] seriesNames, float[][] values) {
        this.knownSeriesNames = (seriesNames == null) ? null : Arrays.copyOf(seriesNames, seriesNames.length);
        this.sourceCategories = categories;
        this.sourceSeriesNames = seriesNames;
        this.sourceValues = values;
        ensureSeriesVisible();
        ensureSeriesColorCapacity();
        rebuildData();
        reapplyNamedSeriesColors();
        return this;
    }

    public finalBarChart1 setDataFromTable(Table t, String categoryCol, String[] valueCols) {
        if (t == null || valueCols == null || valueCols.length == 0) {
            this.sourceCategories = null;
            this.sourceSeriesNames = null;
            this.sourceValues = null;
            this.categories = null;
            this.seriesNames = null;
            this.values = null;
            this.seriesVisible = null;
            invalidateGeometry();
            clearSelection();
            return this;
        }

        ArrayList<String> cats = new ArrayList<String>();
        ArrayList<float[]> valsList = new ArrayList<float[]>();

        for (TableRow row : t.rows()) {
            String cat = row.getString(categoryCol);
            if (cat == null) cat = "";

            float[] rowVals = new float[valueCols.length];
            boolean rowValid = true;

            for (int j = 0; j < valueCols.length; j++) {
                String raw = row.getString(valueCols[j]);
                Float parsed = parseFloatSafe(raw);

                if (parsed == null) {
                    if (skipInvalidTableRows) {
                        rowValid = false;
                        break;
                    } else {
                        rowVals[j] = invalidReplacementValue;
                    }
                } else {
                    rowVals[j] = parsed.floatValue();
                }
            }

            if (rowValid) {
                cats.add(cat);
                valsList.add(rowVals);
            }
        }

        String[] finalCats = new String[cats.size()];
        float[][] finalVals = new float[cats.size()][valueCols.length];

        for (int i = 0; i < cats.size(); i++) {
            finalCats[i] = cats.get(i);
            finalVals[i] = valsList.get(i);
        }

        return setData(finalCats, valueCols, finalVals);
    }

    private void rebuildData() {
        if (sourceCategories == null || sourceSeriesNames == null || sourceValues == null) {
            categories = null;
            seriesNames = null;
            values = null;
            invalidateGeometry();
            clearSelection();
            return;
        }

        seriesNames = Arrays.copyOf(sourceSeriesNames, sourceSeriesNames.length);
        ensureSeriesColorCapacity();

        ArrayList<RowEntry> rows = new ArrayList<RowEntry>();
        for (int i = 0; i < sourceCategories.length; i++) {
            rows.add(new RowEntry(sourceCategories[i], Arrays.copyOf(sourceValues[i], sourceValues[i].length)));
        }

        int sortIndex = getSeriesIndex(sortSeriesName);
        if (sortIndex >= 0) {
            final int idx = sortIndex;
            final SortOrder order = sortOrder;
            rows.sort(new Comparator<RowEntry>() {
                @Override
                public int compare(RowEntry a, RowEntry b) {
                    return order == SortOrder.ASCENDING
                        ? Float.compare(a.values[idx], b.values[idx])
                        : Float.compare(b.values[idx], a.values[idx]);
                }
            });
        }

        if (useTopN && rows.size() > topN) {
            rows = new ArrayList<RowEntry>(rows.subList(0, topN));
        }

        categories = new String[rows.size()];
        values = new float[rows.size()][seriesNames.length];

        for (int i = 0; i < rows.size(); i++) {
            categories[i] = rows.get(i).category;
            values[i] = rows.get(i).values;
        }

        currentPage = 0;
        clampPage();
        invalidateGeometry();
        clearSelection();
        restartAnimation();
    }

    private void ensureSeriesVisible() {
        if (sourceSeriesNames == null) {
            seriesVisible = null;
            return;
        }
        int s = sourceSeriesNames.length;
        if (seriesVisible == null || seriesVisible.length != s) {
            seriesVisible = new boolean[s];
            for (int i = 0; i < s; i++) seriesVisible[i] = true;
        }
    }

    private void ensureSeriesColorCapacity() {
        if (sourceSeriesNames == null) return;
        int s = sourceSeriesNames.length;

        if (seriesColors == null || seriesColors.length != s) {
            seriesColors = new int[s];
            seriesHoverColors = new int[s];
            seriesSelectedColors = new int[s];

            for (int i = 0; i < s; i++) {
                int base = defaultAutoColor(i, s);
                seriesColors[i] = base;
                seriesHoverColors[i] = lighten(base, hoverLightenAmount);
                seriesSelectedColors[i] = lighten(base, selectedLightenAmount);
            }
        }
    }

    private void refreshAutoColorCache() {
        if (sourceSeriesNames != null) {
            seriesColors = null;
            seriesHoverColors = null;
            seriesSelectedColors = null;
            ensureSeriesColorCapacity();
        }
    }

    private void clampPage() {
        int pageCount = getPageCount();
        if (currentPage >= pageCount) currentPage = pageCount - 1;
        if (currentPage < 0) currentPage = 0;
    }

    public void updateHover(int mouseX, int mouseY) {
        if (!geometryValid || barX == null || visibleDataIndices == null) {
            hoverI = -1;
            hoverJ = -1;
            return;
        }

        hoverI = -1;
        hoverJ = -1;

        int n = barX.length;
        int s = (n > 0) ? barX[0].length : 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) continue;
                int x = barX[i][j];
                int y = barY[i][j];
                int w = barW[i][j];
                int h = barH[i][j];

                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    hoverI = visibleDataIndices[i];
                    hoverJ = j;
                    return;
                }
            }
        }
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (legendInteractive && legendGeometryValid && seriesNames != null && seriesVisible != null) {
            if (mouseX >= legendBoxX && mouseX <= legendBoxX + legendBoxW &&
                mouseY >= legendBoxY && mouseY <= legendBoxY + legendBoxH) {

                int s = seriesNames.length;
                for (int j = 0; j < s; j++) {
                    int yCenter = legendRowY[j];
                    int yTop = yCenter - legendRowH / 2;
                    int yBot = yCenter + legendRowH / 2;

                    if (mouseY >= yTop && mouseY <= yBot) {
                        seriesVisible[j] = !seriesVisible[j];
                        if (selectedJ == j) clearSelection();
                        invalidateGeometry();
                        restartAnimation();
                        return;
                    }
                }
            }
        }

        if (selectionEnabled && geometryValid && barX != null && visibleDataIndices != null) {
            int n = barX.length;
            int s = (n > 0) ? barX[0].length : 0;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < s; j++) {
                    if (!isSeriesVisible(j)) continue;

                    int x = barX[i][j];
                    int y = barY[i][j];
                    int w = barW[i][j];
                    int h = barH[i][j];

                    if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                        selectedI = visibleDataIndices[i];
                        selectedJ = j;
                        return;
                    }
                }
            }
        }

        clearSelection();
    }

    public void handleKeyPressed(char key, int keyCode) {
        if (seriesNames == null) return;

        if (topNInputMode) {
            if (key >= '0' && key <= '9') {
                topNInputBuffer += key;
                return;
            }
            if (keyCode == PConstants.BACKSPACE) {
                if (topNInputBuffer.length() > 0) {
                    topNInputBuffer = topNInputBuffer.substring(0, topNInputBuffer.length() - 1);
                }
                return;
            }
            if (keyCode == PConstants.ENTER || keyCode == PConstants.RETURN) {
                confirmTopNInput();
                return;
            }
            if (keyCode == PConstants.ESC) {
                cancelTopNInput();
                return;
            }
        }

        if (key >= '1' && key <= '9') {
            int idx = key - '1';
            if (idx >= 0 && idx < seriesNames.length) {
                setSortBySeries(seriesNames[idx], sortOrder);
            }
            return;
        }

        if (key == 'a' || key == 'A') {
            sortOrder = SortOrder.ASCENDING;
            if (sortSeriesName != null) setSortBySeries(sortSeriesName, sortOrder);
            return;
        }

        if (key == 'd' || key == 'D') {
            sortOrder = SortOrder.DESCENDING;
            if (sortSeriesName != null) setSortBySeries(sortSeriesName, sortOrder);
            return;
        }

        if (keyCode == PConstants.RIGHT) {
            nextPage();
            return;
        }

        if (keyCode == PConstants.LEFT) {
            previousPage();
            return;
        }

        if (key == 't' || key == 'T') {
            if (useTopN) clearTopN();
            else setTopN(topN);
            return;
        }

        if (key == 'n' || key == 'N') {
            beginTopNInput();
            return;
        }

        if (key == 'x' || key == 'X') {
            resetToInitialView();
            return;
        }

        if (key == 's' || key == 'S') {
            showAllSeries();
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

        if (key == 'v' || key == 'V') {
            showValueLabels = !showValueLabels;
            return;
        }

        if (key == 'g' || key == 'G') {
            showGrid = !showGrid;
            return;
        }

        if (key == 'm' || key == 'M') {
            toggleMode();
            return;
        }

        if (key == 'b' || key == 'B') {
            setPaletteByName("colorblind");
            return;
        }

        if (key == 'l' || key == 'L') {
            setPaletteByName("pastel");
            return;
        }

        if (key == 'k' || key == 'K') {
            setPaletteByName("highcontrast");
            return;
        }

        if (key == 'u' || key == 'U') {
            useDefaultPaletteForCurrentMode();
            return;
        }

        if (key == 'y' || key == 'Y') {
            toggleCustomSeriesColors();
            return;
        }

        if (key == 'e' || key == 'E') {
            setBarMode(BarMode.GROUPED);
            return;
        }

        if (key == 'f' || key == 'F') {
            setBarMode(BarMode.STACKED);
            return;
        }

        if (key == 'j' || key == 'J') {
            setBarMode(BarMode.PERCENT_STACKED);
            return;
        }

        if (key == 'w' || key == 'W') {
            setOrientation(Orientation.VERTICAL);
            return;
        }

        if (key == 'z' || key == 'Z') {
            setOrientation(Orientation.HORIZONTAL);
            return;
        }

        if (key == 'q' || key == 'Q') {
            rotateCategoryLabels = !rotateCategoryLabels;
        }
    }

    public void draw(int x, int y, int w, int h) {
        if (!dataValid()) {
            drawMessage("No grouped data set", x, y);
            return;
        }

        ensureSeriesVisible();
        clampPage();
        p.textAlign(PConstants.LEFT, PConstants.BASELINE);

        int panelW = showDetailsPanel ? detailsPanelWidth : 0;
        int plotX = x + marginLeft;
        int plotY = y + marginTop;
        int plotW = w - marginLeft - marginRight - panelW;
        int plotH = h - marginTop - marginBottom;

        if (plotW <= 0 || plotH <= 0) {
            drawMessage("Chart area too small", x, y);
            return;
        }

        int totalN = categories.length;
        int s = seriesNames.length;

        int start = currentPage * pageSize;
        int end = Math.min(totalN, start + pageSize);
        int visibleN = Math.max(0, end - start);

        if (visibleN <= 0) {
            drawMessage("No categories on this page", x, y);
            return;
        }

        visibleDataIndices = new int[visibleN];
        for (int i = 0; i < visibleN; i++) visibleDataIndices[i] = start + i;

        boolean anyVisible = false;
        for (int j = 0; j < s; j++) {
            if (isSeriesVisible(j)) {
                anyVisible = true;
                break;
            }
        }

        float maxVal = getVisibleMaxForCurrentMode(start, end);
        if (showReferenceLine) maxVal = Math.max(maxVal, referenceLineValue);

        p.noStroke();
        p.fill(background);
        p.rect(x, y, w, h);

        if (!anyVisible) {
            drawTitles(x, y, w, h, panelW);
            drawLegend(plotX + plotW - 10, plotY + 10);
            if (showDetailsPanel) drawDetailsPanel(x + w - panelW + 10, plotY, panelW - 20, plotH);
            if (showStatusOverlay) drawStatusOverlay(plotX, plotY);
            if (showHelpOverlay) drawHelpOverlay(plotX, plotY + 70, plotW);
            drawMessage("All series hidden (click legend to re-enable)", x, y);
            return;
        }

        if (maxVal == 0) maxVal = 1;
        float axisMax = useNiceAxis && barMode != BarMode.PERCENT_STACKED ? niceCeil(maxVal) : maxVal;
        float tickStep = axisMax / yTickCount;
        float avgVisible = getVisibleAverageForCurrentMode(start, end);

        float anim = 1.0f;
        if (animationEnabled && animationDurationMs > 0) {
            if (animationStartMs < 0) animationStartMs = p.millis();
            float t = (p.millis() - animationStartMs) / (float) animationDurationMs;
            t = clamp01(t);
            anim = easeOutCubic(t);
        }

        p.textSize(tickSize);

        if (orientation == Orientation.VERTICAL) drawVerticalAxisAndGuides(plotX, plotY, plotW, plotH, axisMax, tickStep, avgVisible);
        else drawHorizontalAxisAndGuides(plotX, plotY, plotW, plotH, axisMax, tickStep, avgVisible);

        if (barX == null || barX.length != visibleN || (visibleN > 0 && barX[0].length != s)) {
            barX = new int[visibleN][s];
            barY = new int[visibleN][s];
            barW = new int[visibleN][s];
            barH = new int[visibleN][s];
        }

        if (selectedI >= start && selectedI < end) {
            drawSelectedCategoryBand(selectedI, start, plotX, plotY, plotW, plotH, visibleN);
        }

        if (orientation == Orientation.VERTICAL) {
            if (barMode == BarMode.GROUPED) drawVerticalGroupedBars(start, visibleN, plotX, plotY, plotW, plotH, axisMax, anim, s);
            else drawVerticalStackedBars(start, visibleN, plotX, plotY, plotW, plotH, axisMax, anim, s);
        } else {
            if (barMode == BarMode.GROUPED) drawHorizontalGroupedBars(start, visibleN, plotX, plotY, plotW, plotH, axisMax, anim, s);
            else drawHorizontalStackedBars(start, visibleN, plotX, plotY, plotW, plotH, axisMax, anim, s);
        }
        geometryValid = true;

        for (int localI = 0; localI < visibleN; localI++) {
            int globalI = visibleDataIndices[localI];

            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) continue;

                int bx = barX[localI][j];
                int by2 = barY[localI][j];
                int bw2 = barW[localI][j];
                int bh2 = barH[localI][j];

                int fill = resolveSeriesBaseColor(j);
                if (globalI == selectedI && j == selectedJ) fill = resolveSeriesSelectedColor(j);
                else if (globalI == hoverI && j == hoverJ) fill = resolveSeriesHoverColor(j);

                p.noStroke();
                p.fill(fill);
                p.rect(bx, by2, bw2, bh2);

                p.stroke(barStroke);
                p.strokeWeight(strokeWeight);
                p.noFill();
                p.rect(bx, by2, bw2, bh2);

                if (globalI == selectedI && j == selectedJ) {
                    p.stroke(selectionOutline);
                    p.strokeWeight(2);
                    p.noFill();
                    p.rect(bx - 2, by2 - 2, bw2 + 4, bh2 + 4);
                }

                if (showValueLabels) {
                    float displayVal = getDisplayedValue(globalI, j);
                    if (orientation == Orientation.VERTICAL && bh2 > 12) {
                        p.noStroke();
                        p.fill(textColor);
                        p.textSize(11);
                        drawTextCenterBottom(formatDisplayValue(displayVal), bx + bw2 / 2, by2 - 3);
                    } else if (orientation == Orientation.HORIZONTAL && bw2 > 22) {
                        p.noStroke();
                        p.fill(textColor);
                        p.textSize(11);
                        drawTextLeftCenter(formatDisplayValue(displayVal), bx + bw2 + 4, by2 + bh2 / 2);
                    }
                }
            }

            drawCategoryLabel(categories[globalI], localI, visibleN, plotX, plotY, plotW, plotH);
        }

        if (selectedI >= start && selectedI < end) {
            drawSelectedGroupTotal(selectedI, start, plotX, plotY, plotW, plotH, visibleN);
        }

        drawLegend(plotX + plotW - 10, plotY + 10);
        drawTitles(x, y, w, h, panelW);

        if (showDetailsPanel) drawDetailsPanel(x + w - panelW + 10, plotY, panelW - 20, plotH);
        if (showStatusOverlay) drawStatusOverlay(plotX, plotY);
        if (showHelpOverlay) drawHelpOverlay(plotX, plotY + 70, plotW);
        if (topNInputMode) drawTopNInputOverlay(plotX, plotY, plotW);

        if (hoverI >= 0 && hoverJ >= 0 && isSeriesVisible(hoverJ)) {
            String tt = categories[hoverI] + " / " + seriesNames[hoverJ] + ": " + formatDisplayValue(getDisplayedValue(hoverI, hoverJ));
            drawTooltip(tt, p.mouseX, p.mouseY);
        }
    }

    private float getDisplayedValue(int rowIndex, int seriesIndex) {
        if (barMode != BarMode.PERCENT_STACKED) return values[rowIndex][seriesIndex];
        float total = getCategoryVisibleTotal(rowIndex);
        if (total <= 0) return 0;
        return values[rowIndex][seriesIndex] * 100f / total;
    }

    private String formatDisplayValue(float v) {
        return barMode == BarMode.PERCENT_STACKED ? formatTick(v) + "%" : formatTick(v);
    }

    private float getVisibleMaxForCurrentMode(int start, int end) {
        if (barMode == BarMode.PERCENT_STACKED) return 100f;

        float maxVal = 0;
        if (barMode == BarMode.GROUPED) {
            for (int j = 0; j < seriesNames.length; j++) {
                if (!isSeriesVisible(j)) continue;
                for (int i = start; i < end; i++) {
                    if (values[i][j] > maxVal) maxVal = values[i][j];
                }
            }
        } else {
            for (int i = start; i < end; i++) {
                maxVal = Math.max(maxVal, getCategoryVisibleTotal(i));
            }
        }
        return maxVal;
    }

    private float getVisibleAverageForCurrentMode(int start, int end) {
        if (barMode == BarMode.PERCENT_STACKED) return 0;
        if (barMode == BarMode.GROUPED) return getVisibleAverage(start, end);

        float sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            sum += getCategoryVisibleTotal(i);
            count++;
        }
        return count > 0 ? sum / count : 0;
    }

    private void drawVerticalAxisAndGuides(int plotX, int plotY, int plotW, int plotH, float axisMax, float tickStep, float avgVisible) {
        for (int ti = 0; ti <= yTickCount; ti++) {
            float v = tickStep * ti;
            int iy = Math.round(map(v, 0, axisMax, plotY + plotH, plotY));

            if (showGrid) {
                p.stroke(gridColor);
                p.strokeWeight(strokeWeight);
                p.line(plotX, iy, plotX + plotW, iy);
            }

            p.stroke(axisColor);
            p.strokeWeight(strokeWeight);
            p.line(plotX - 5, iy, plotX, iy);

            p.noStroke();
            p.fill(tickLabelColor);
            drawTextRight(formatDisplayValue(v), plotX - 10, iy);
        }

        p.stroke(axisColor);
        p.strokeWeight(strokeWeight);
        p.line(plotX, plotY, plotX, plotY + plotH);
        p.line(plotX, plotY + plotH, plotX + plotW, plotY + plotH);

        if (showReferenceLine) {
            int refY = Math.round(map(referenceLineValue, 0, axisMax, plotY + plotH, plotY));
            p.stroke(referenceLineColor);
            p.strokeWeight(1.5f);
            p.line(plotX, refY, plotX + plotW, refY);

            if (referenceLineLabel != null && referenceLineLabel.length() > 0) {
                p.noStroke();
                p.fill(referenceLineColor);
                drawTextRightTop(referenceLineLabel, plotX + plotW - 4, refY - 3);
            }
        }

        if (showAverageLine && avgVisible > 0) {
            int avgY = Math.round(map(avgVisible, 0, axisMax, plotY + plotH, plotY));
            p.stroke(averageLineColor);
            p.strokeWeight(1.2f);
            p.line(plotX, avgY, plotX + plotW, avgY);

            p.noStroke();
            p.fill(averageLineColor);
            drawTextRightTop("Avg", plotX + plotW - 4, avgY - 3);
        }
    }

    private void drawHorizontalAxisAndGuides(int plotX, int plotY, int plotW, int plotH, float axisMax, float tickStep, float avgVisible) {
        for (int ti = 0; ti <= yTickCount; ti++) {
            float v = tickStep * ti;
            int ix = Math.round(map(v, 0, axisMax, plotX, plotX + plotW));

            if (showGrid) {
                p.stroke(gridColor);
                p.strokeWeight(strokeWeight);
                p.line(ix, plotY, ix, plotY + plotH);
            }

            p.stroke(axisColor);
            p.strokeWeight(strokeWeight);
            p.line(ix, plotY + plotH, ix, plotY + plotH + 5);

            p.noStroke();
            p.fill(tickLabelColor);
            drawTextCenterTop(formatDisplayValue(v), ix, plotY + plotH + 8);
        }

        p.stroke(axisColor);
        p.strokeWeight(strokeWeight);
        p.line(plotX, plotY, plotX, plotY + plotH);
        p.line(plotX, plotY + plotH, plotX + plotW, plotY + plotH);

        if (showReferenceLine) {
            int refX = Math.round(map(referenceLineValue, 0, axisMax, plotX, plotX + plotW));
            p.stroke(referenceLineColor);
            p.strokeWeight(1.5f);
            p.line(refX, plotY, refX, plotY + plotH);

            if (referenceLineLabel != null && referenceLineLabel.length() > 0) {
                p.noStroke();
                p.fill(referenceLineColor);
                drawTextLeftTop(referenceLineLabel, refX + 4, plotY + 4);
            }
        }

        if (showAverageLine && avgVisible > 0) {
            int avgX = Math.round(map(avgVisible, 0, axisMax, plotX, plotX + plotW));
            p.stroke(averageLineColor);
            p.strokeWeight(1.2f);
            p.line(avgX, plotY, avgX, plotY + plotH);

            p.noStroke();
            p.fill(averageLineColor);
            drawTextLeftTop("Avg", avgX + 4, plotY + 18);
        }
    }

    private void drawSelectedCategoryBand(int globalIndex, int start, int plotX, int plotY, int plotW, int plotH, int visibleN) {
        int localSelected = globalIndex - start;
        if (orientation == Orientation.VERTICAL) {
            float slot = (float) plotW / visibleN;
            int gx = Math.round(plotX + localSelected * slot);
            p.noStroke();
            p.fill(selectedGroupFill);
            p.rect(gx, plotY, Math.round(slot), plotH);
        } else {
            float slot = (float) plotH / visibleN;
            int gy = Math.round(plotY + localSelected * slot);
            p.noStroke();
            p.fill(selectedGroupFill);
            p.rect(plotX, gy, plotW, Math.round(slot));
        }
    }

    private void drawVerticalGroupedBars(int start, int visibleN, int plotX, int plotY, int plotW, int plotH, float axisMax, float anim, int s) {
        float groupSlot = (float) plotW / visibleN;
        float groupPad = groupSlot * groupInnerPadding;
        float groupW = groupSlot - groupPad;
        float barSlot = groupW / s;
        float barGap = barSlot * barInnerGap;
        float barWf = barSlot - barGap;

        for (int localI = 0; localI < visibleN; localI++) {
            int globalI = visibleDataIndices[localI];
            float groupStart = plotX + localI * groupSlot + groupPad * 0.5f;

            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) {
                    barX[localI][j] = barY[localI][j] = barW[localI][j] = barH[localI][j] = 0;
                    continue;
                }

                float v = getDisplayedValue(globalI, j);
                float barHF = map(v, 0, axisMax, 0, plotH) * anim;

                int bx = Math.round(groupStart + j * barSlot + barGap * 0.5f);
                int bw2 = Math.max(1, Math.round(barWf));
                int bh2 = Math.max(0, Math.round(barHF));
                int by2 = plotY + plotH - bh2;

                barX[localI][j] = bx;
                barY[localI][j] = by2;
                barW[localI][j] = bw2;
                barH[localI][j] = bh2;
            }
        }
    }

    private void drawVerticalStackedBars(int start, int visibleN, int plotX, int plotY, int plotW, int plotH, float axisMax, float anim, int s) {
        float groupSlot = (float) plotW / visibleN;
        float groupPad = groupSlot * groupInnerPadding;
        float barWf = groupSlot - groupPad;

        for (int localI = 0; localI < visibleN; localI++) {
            int globalI = visibleDataIndices[localI];
            float groupStart = plotX + localI * groupSlot + groupPad * 0.5f;
            float cumulative = 0;

            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) {
                    barX[localI][j] = barY[localI][j] = barW[localI][j] = barH[localI][j] = 0;
                    continue;
                }

                float v = getDisplayedValue(globalI, j) * anim;
                int bx = Math.round(groupStart);
                int bw2 = Math.max(1, Math.round(barWf));
                int yTop = plotY + plotH - Math.round(map(cumulative + v, 0, axisMax, 0, plotH));
                int yBottom = plotY + plotH - Math.round(map(cumulative, 0, axisMax, 0, plotH));
                int bh2 = Math.max(0, yBottom - yTop);

                barX[localI][j] = bx;
                barY[localI][j] = yTop;
                barW[localI][j] = bw2;
                barH[localI][j] = bh2;
                cumulative += v;
            }
        }
    }

    private void drawHorizontalGroupedBars(int start, int visibleN, int plotX, int plotY, int plotW, int plotH, float axisMax, float anim, int s) {
        float rowSlot = (float) plotH / visibleN;
        float rowPad = rowSlot * groupInnerPadding;
        float groupH = rowSlot - rowPad;
        float barSlot = groupH / s;
        float barGap = barSlot * barInnerGap;
        float barHf = barSlot - barGap;

        for (int localI = 0; localI < visibleN; localI++) {
            int globalI = visibleDataIndices[localI];
            float groupStart = plotY + localI * rowSlot + rowPad * 0.5f;

            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) {
                    barX[localI][j] = barY[localI][j] = barW[localI][j] = barH[localI][j] = 0;
                    continue;
                }

                float v = getDisplayedValue(globalI, j);
                float barWF = map(v, 0, axisMax, 0, plotW) * anim;

                int bx = plotX;
                int bw2 = Math.max(0, Math.round(barWF));
                int by2 = Math.round(groupStart + j * barSlot + barGap * 0.5f);
                int bh2 = Math.max(1, Math.round(barHf));

                barX[localI][j] = bx;
                barY[localI][j] = by2;
                barW[localI][j] = bw2;
                barH[localI][j] = bh2;
            }
        }
    }

    private void drawHorizontalStackedBars(int start, int visibleN, int plotX, int plotY, int plotW, int plotH, float axisMax, float anim, int s) {
        float rowSlot = (float) plotH / visibleN;
        float rowPad = rowSlot * groupInnerPadding;
        float barHf = rowSlot - rowPad;

        for (int localI = 0; localI < visibleN; localI++) {
            int globalI = visibleDataIndices[localI];
            float groupStart = plotY + localI * rowSlot + rowPad * 0.5f;
            float cumulative = 0;

            for (int j = 0; j < s; j++) {
                if (!isSeriesVisible(j)) {
                    barX[localI][j] = barY[localI][j] = barW[localI][j] = barH[localI][j] = 0;
                    continue;
                }

                float v = getDisplayedValue(globalI, j) * anim;
                int xLeft = plotX + Math.round(map(cumulative, 0, axisMax, 0, plotW));
                int xRight = plotX + Math.round(map(cumulative + v, 0, axisMax, 0, plotW));
                int by2 = Math.round(groupStart);
                int bh2 = Math.max(1, Math.round(barHf));

                barX[localI][j] = xLeft;
                barY[localI][j] = by2;
                barW[localI][j] = Math.max(0, xRight - xLeft);
                barH[localI][j] = bh2;
                cumulative += v;
            }
        }
    }

    private int resolveSeriesBaseColor(int seriesIndex) {
        if (useExplicitSeriesColors && seriesColors != null && seriesIndex >= 0 && seriesIndex < seriesColors.length) {
            return seriesColors[seriesIndex];
        }

        if (seriesIndex < palette.length) {
            return palette[seriesIndex];
        }

        if (autoGenerateSeriesColors) {
            int total = (seriesNames != null && seriesNames.length > 0) ? seriesNames.length : (seriesIndex + 1);
            return defaultAutoColor(seriesIndex, total);
        }

        return palette[seriesIndex % palette.length];
    }

    private int resolveSeriesHoverColor(int seriesIndex) {
        if (useExplicitHoverColors && seriesHoverColors != null && seriesIndex >= 0 && seriesIndex < seriesHoverColors.length) {
            return seriesHoverColors[seriesIndex];
        }
        return lighten(resolveSeriesBaseColor(seriesIndex), hoverLightenAmount);
    }

    private int resolveSeriesSelectedColor(int seriesIndex) {
        if (useExplicitSelectedColors && seriesSelectedColors != null && seriesIndex >= 0 && seriesIndex < seriesSelectedColors.length) {
            return seriesSelectedColors[seriesIndex];
        }
        return lighten(resolveSeriesBaseColor(seriesIndex), selectedLightenAmount);
    }

    private int defaultAutoColor(int index, int totalSeries) {
        if (index < palette.length) return palette[index];

        int total = Math.max(totalSeries, index + 1);
        float hue = (index % total) / (float) total;
        float sat = darkMode ? 0.60f : 0.72f;
        float bri = darkMode ? 0.95f : 0.86f;
        return hsbToRgb(hue, sat, bri);
    }

    private int hsbToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        int i = (int) Math.floor(h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);

        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }

        int ri = clamp255(Math.round(r * 255f));
        int gi = clamp255(Math.round(g * 255f));
        int bi = clamp255(Math.round(b * 255f));
        return 0xff000000 | (ri << 16) | (gi << 8) | bi;
    }

    private void drawSelectedGroupTotal(int globalIndex, int start, int plotX, int plotY, int plotW, int plotH, int visibleN) {
        float total = getCategoryVisibleTotal(globalIndex);
        String label = (barMode == BarMode.PERCENT_STACKED) ? "Total: 100%" : "Total: " + formatTick(total);
        int localIndex = globalIndex - start;

        p.noStroke();
        p.fill(textColor);
        p.textSize(11);

        if (orientation == Orientation.VERTICAL) {
            float groupSlot = (float) plotW / visibleN;
            int cx = Math.round(plotX + localIndex * groupSlot + groupSlot / 2.0f);
            drawTextCenterBottom(label, cx, plotY - 6);
        } else {
            float rowSlot = (float) plotH / visibleN;
            int cy = Math.round(plotY + localIndex * rowSlot + rowSlot / 2.0f);
            drawTextLeftCenter(label, plotX + plotW + 8, cy);
        }
    }

    private void drawCategoryLabel(String label, int localIndex, int visibleN, int plotX, int plotY, int plotW, int plotH) {
        p.noStroke();
        p.fill(textColor);
        p.textSize(tickSize);

        if (orientation == Orientation.VERTICAL) {
            float groupSlot = (float) plotW / visibleN;
            int cx = Math.round(plotX + localIndex * groupSlot + groupSlot / 2.0f);
            int labelY = plotY + plotH + 10;

            String display = label;
            if (clipLongLabels && !rotateCategoryLabels) {
                display = fitLabel(label, groupSlot * 0.9f);
            }

            if (rotateCategoryLabels) {
                p.pushMatrix();
                p.translate(cx, labelY + 6);
                p.rotate(categoryLabelAngle);
                p.textAlign(PConstants.RIGHT, PConstants.CENTER);
                p.text(label, 0, 0);
                p.popMatrix();
            } else {
                drawTextCenterTop(display, cx, labelY);
            }
        } else {
            float rowSlot = (float) plotH / visibleN;
            int cy = Math.round(plotY + localIndex * rowSlot + rowSlot / 2.0f);
            String display = clipLongLabels ? fitLabel(label, marginLeft - 16) : label;
            drawTextRight(display, plotX - 10, cy + 4);
        }
    }

    private void drawTitles(int x, int y, int w, int h, int panelW) {
        int centerX = Math.round(x + (w - panelW) / 2.0f);

        if (!title.isEmpty()) {
            p.fill(titleColor);
            p.textSize(titleSize);
            drawTextCenterCenter(title, centerX, Math.round(y + marginTop / 2.0f));
        }

        if (!xLabel.isEmpty()) {
            p.fill(axisLabelColor);
            p.textSize(labelSize);
            drawTextCenterCenter(xLabel, centerX, Math.round(y + h - marginBottom / 2.0f));
        }

        if (!yLabel.isEmpty()) {
            p.fill(axisLabelColor);
            p.pushMatrix();
            p.translate(Math.round(x + marginLeft / 2.0f), Math.round(y + h / 2.0f));
            p.rotate(-PConstants.HALF_PI);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(labelSize);
            p.text(yLabel, 0, 0);
            p.popMatrix();
        }
    }

    private void drawLegend(int rightX, int topY) {
        int s = seriesNames.length;
        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.textSize(tickSize);

        int box = 12;
        int gap = 8;
        int lineH = legendRowH;

        float maxW = 0;
        for (String name : seriesNames) maxW = Math.max(maxW, p.textWidth(name));

        int bw = Math.round(maxW) + box + gap + 16;
        int bh = s * lineH + 12;

        int bx = rightX - bw;
        int by = topY;

        legendBoxX = bx;
        legendBoxY = by;
        legendBoxW = bw;
        legendBoxH = bh;

        if (legendRowY == null || legendRowY.length != s) legendRowY = new int[s];

        p.noStroke();
        p.fill(darkMode ? 0xff242a31 : 0xffffffff);
        p.rect(bx, by, bw, bh);

        p.stroke(axisColor);
        p.strokeWeight(strokeWeight);
        p.line(bx, by, bx + bw, by);
        p.line(bx, by + bh, bx + bw, by + bh);
        p.line(bx, by, bx, by + bh);
        p.line(bx + bw, by, bx + bw, by + bh);

        for (int j = 0; j < s; j++) {
            int yy = by + 10 + j * lineH + 6;
            legendRowY[j] = yy;

            int fill = resolveSeriesBaseColor(j);
            boolean vis = isSeriesVisible(j);
            int swatchFill = vis ? fill : (darkMode ? 0xff6b7280 : 0xffcccccc);

            p.noStroke();
            p.fill(swatchFill);
            p.rect(bx + 8, yy - box / 2, box, box);

            p.stroke(barStroke);
            p.strokeWeight(strokeWeight);
            p.line(bx + 8, yy - box / 2, bx + 8 + box, yy - box / 2);
            p.line(bx + 8, yy + box / 2, bx + 8 + box, yy + box / 2);
            p.line(bx + 8, yy - box / 2, bx + 8, yy + box / 2);
            p.line(bx + 8 + box, yy - box / 2, bx + 8 + box, yy + box / 2);

            p.noStroke();
            p.fill(vis ? textColor : (darkMode ? 0xff9ca3af : 0xff777777));
            drawTextLeftCenter(seriesNames[j], bx + 8 + box + gap, yy);
        }

        legendGeometryValid = true;
    }

    private void drawDetailsPanel(int x, int y, int w, int h) {
        p.noStroke();
        p.fill(detailsPanelFill);
        p.rect(x, y, w, h);

        p.stroke(detailsPanelStroke);
        p.strokeWeight(1);
        p.line(x, y, x + w, y);
        p.line(x, y + h, x + w, y + h);
        p.line(x, y, x, y + h);
        p.line(x + w, y, x + w, y + h);

        int tx = x + 12;
        int ty = y + 14;
        int lineH = 18;

        p.noStroke();
        p.fill(textColor);
        p.textSize(14);
        drawTextLeftTop("Analysis", tx, ty);
        ty += 28;

        p.textSize(12);

        if (selectedI >= 0 && selectedJ >= 0) {
            drawTextLeftTop("Category:", tx, ty); ty += lineH;
            drawTextLeftTop(categories[selectedI], tx, ty); ty += lineH + 4;
            drawTextLeftTop("Series:", tx, ty); ty += lineH;
            drawTextLeftTop(seriesNames[selectedJ], tx, ty); ty += lineH + 4;
            drawTextLeftTop("Value:", tx, ty); ty += lineH;
            drawTextLeftTop(formatTick(values[selectedI][selectedJ]), tx, ty); ty += lineH + 6;
            drawTextLeftTop("Category total:", tx, ty); ty += lineH;
            drawTextLeftTop(formatTick(getCategoryVisibleTotal(selectedI)), tx, ty); ty += lineH + 10;
        } else {
            drawTextLeftTop("No bar selected.", tx, ty);
            ty += lineH + 10;
        }

        drawTextLeftTop("Current state", tx, ty); ty += 24;
        drawTextLeftTop("Page: " + (currentPage + 1) + " / " + getPageCount(), tx, ty); ty += 16;
        drawTextLeftTop("Sort: " + getSortLabel(), tx, ty); ty += 16;
        drawTextLeftTop("Top N: " + (useTopN ? String.valueOf(topN) : "off"), tx, ty); ty += 16;
        drawTextLeftTop("Mode: " + (darkMode ? "Dark" : "Light"), tx, ty); ty += 16;
    }

    private void drawStatusOverlay(int plotX, int plotY) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Page " + (currentPage + 1) + "/" + getPageCount());
        lines.add("Sort: " + getSortLabel());
        lines.add("Top N: " + (useTopN ? topN : "off"));
        lines.add("Theme: " + (darkMode ? "Dark" : "Light"));
        lines.add("Chart: " + barMode.toString());
        lines.add("Axis: " + orientation.toString());

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
        p.line(bx, by, bx + bw, by);
        p.line(bx, by + bh, bx + bw, by + bh);
        p.line(bx, by, bx, by + bh);
        p.line(bx + bw, by, bx + bw, by + bh);

        p.noStroke();
        p.fill(textColor);

        int ty = by + 6;
        for (String line : lines) {
            drawTextLeftTop(line, bx + 8, ty);
            ty += 15;
        }
    }

    private void drawHelpOverlay(int x, int y, int plotW) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Controls");
        lines.add("1..9 sort by series");
        lines.add("A/D ascending/descending");
        lines.add("LEFT/RIGHT change page");
        lines.add("T toggle Top-N");
        lines.add("N type custom Top-N");
        lines.add("X reset initial view");
        lines.add("S show all series");
        lines.add("C clear selection");
        lines.add("M toggle light/dark");
        lines.add("B colorblind palette");
        lines.add("L pastel palette");
        lines.add("K high-contrast palette");
        lines.add("U default palette");
        lines.add("Y toggle custom colors");
        lines.add("E grouped bars");
        lines.add("F stacked bars");
        lines.add("J 100% stacked");
        lines.add("W vertical layout");
        lines.add("Z horizontal layout");
        lines.add("P details panel");
        lines.add("O status overlay");
        lines.add("H help overlay");
        lines.add("V value labels");
        lines.add("Q rotate labels");
        lines.add("R restart animation");

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
        p.line(bx, by, bx + bw, by);
        p.line(bx, by + bh, bx + bw, by + bh);
        p.line(bx, by, bx, by + bh);
        p.line(bx + bw, by, bx + bw, by + bh);

        p.noStroke();
        p.fill(textColor);

        int ty = by + 6;
        for (String line : lines) {
            drawTextLeftTop(line, bx + 8, ty);
            ty += 14;
        }
    }

    private void drawTopNInputOverlay(int x, int y, int w) {
        int bw = 220;
        int bh = 70;
        int bx = x + (w - bw) / 2;
        int by = y + 40;

        p.noStroke();
        p.fill(darkMode ? 0xff2a3038 : 0xfff9f9f9);
        p.rect(bx, by, bw, bh);

        p.stroke(darkMode ? 0xff566170 : 0xffbbbbbb);
        p.strokeWeight(1);
        p.line(bx, by, bx + bw, by);
        p.line(bx, by + bh, bx + bw, by + bh);
        p.line(bx, by, bx, by + bh);
        p.line(bx + bw, by, bx + bw, by + bh);

        p.noStroke();
        p.fill(textColor);
        p.textSize(12);
        drawTextLeftTop("Enter Top N, then press Enter", bx + 12, by + 10);
        drawTextLeftTop("Value: " + (topNInputBuffer.length() == 0 ? "_" : topNInputBuffer), bx + 12, by + 32);
    }

    private void drawTooltip(String text, int mx, int my) {
        p.textSize(tickSize);
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
        p.line(bx, by, bx + bw, by);
        p.line(bx, by + bh, bx + bw, by + bh);
        p.line(bx, by, bx, by + bh);
        p.line(bx + bw, by, bx + bw, by + bh);

        p.noStroke();
        p.fill(textColor);
        p.textSize(12);
        drawTextLeftCenter(text, bx + padding, by + bh / 2);
    }

    private void drawMessage(String msg, int x, int y) {
        p.fill(textColor);
        p.textSize(12);
        drawTextLeftTop(msg, x + 20, y + 20);
    }

    private void drawTextLeftTop(String text, float x, float y) {
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextLeftCenter(String text, float x, float y) {
        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextCenterTop(String text, float x, float y) {
        p.textAlign(PConstants.CENTER, PConstants.TOP);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextCenterCenter(String text, float x, float y) {
        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextCenterBottom(String text, float x, float y) {
        p.textAlign(PConstants.CENTER, PConstants.BOTTOM);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextRight(String text, float x, float y) {
        p.textAlign(PConstants.RIGHT, PConstants.CENTER);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void drawTextRightTop(String text, float x, float y) {
        p.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
        p.text(text, Math.round(x), Math.round(y));
    }

    private void invalidateGeometry() {
        geometryValid = false;
        legendGeometryValid = false;
        hoverI = -1;
        hoverJ = -1;
    }

    private boolean dataValid() {
        if (categories == null || seriesNames == null || values == null) return false;
        if (categories.length == 0 || seriesNames.length == 0) return false;
        if (values.length != categories.length) return false;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || values[i].length != seriesNames.length) return false;
        }
        return true;
    }

    private int getSeriesIndex(String name) {
        if (name == null || seriesNames == null) return -1;
        for (int i = 0; i < seriesNames.length; i++) {
            if (name.equals(seriesNames[i])) return i;
        }
        return -1;
    }

    private boolean isSeriesVisible(int j) {
        return (seriesVisible == null || j < 0 || j >= seriesVisible.length) || seriesVisible[j];
    }

    private float getVisibleAverage(int start, int end) {
        float sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            for (int j = 0; j < seriesNames.length; j++) {
                if (isSeriesVisible(j)) {
                    sum += values[i][j];
                    count++;
                }
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private float getCategoryVisibleTotal(int rowIndex) {
        float total = 0;
        for (int j = 0; j < seriesNames.length; j++) {
            if (isSeriesVisible(j)) total += values[rowIndex][j];
        }
        return total;
    }

    private String getSortLabel() {
        if (sortSeriesName == null) return "original order";
        return sortSeriesName + " " + (sortOrder == SortOrder.DESCENDING ? "DESC" : "ASC");
    }


    private int getKnownSeriesIndex(String seriesName) {
        if (seriesName == null || knownSeriesNames == null) return -1;
        for (int i = 0; i < knownSeriesNames.length; i++) {
            if (seriesName.equals(knownSeriesNames[i])) return i;
        }
        return -1;
    }

    private void reapplyNamedSeriesColors() {
        if (knownSeriesNames == null) return;

        if (!pendingNamedBaseColors.isEmpty()) {
            for (Map.Entry<String, Integer> entry : pendingNamedBaseColors.entrySet()) {
                int idx = getKnownSeriesIndex(entry.getKey());
                if (idx >= 0) {
                    ensureSeriesColorCapacity();
                    if (seriesColors != null && idx < seriesColors.length) {
                        seriesColors[idx] = entry.getValue();
                        useExplicitSeriesColors = true;
                    }
                }
            }
        }

        if (customColorMode && customColorSeriesNames != null && customColorValues != null) {
            int n = Math.min(customColorSeriesNames.length, customColorValues.length);
            for (int i = 0; i < n; i++) {
                int idx = getKnownSeriesIndex(customColorSeriesNames[i]);
                if (idx >= 0) {
                    ensureSeriesColorCapacity();
                    if (seriesColors != null && idx < seriesColors.length) {
                        seriesColors[idx] = customColorValues[i];
                        useExplicitSeriesColors = true;
                    }
                }
            }
        }
    }

    private int[] getPaletteCopy() {
        return palette == null ? null : Arrays.copyOf(palette, palette.length);
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

    private float map(float v, float inMin, float inMax, float outMin, float outMax) {
        if (Math.abs(inMax - inMin) < 0.000001f) return outMin;
        return (v - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    private float niceCeil(float v) {
        if (v <= 0) return 1;
        float exp = (float) Math.floor(Math.log10(v));
        float f = v / (float) Math.pow(10, exp);

        float nf;
        if (f <= 1) nf = 1;
        else if (f <= 2) nf = 2;
        else if (f <= 5) nf = 5;
        else nf = 10;

        return nf * (float) Math.pow(10, exp);
    }

    private String formatTick(float v) {
        float av = Math.abs(v);
        if (av >= 1000000) return String.format("%.1fM", v / 1000000f);
        if (av >= 1000) return String.format("%.1fk", v / 1000f);
        if (av >= 10) return String.format("%.0f", v);
        return String.format("%.2f", v);
    }

    private float clamp01(float v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private float easeOutCubic(float t) {
        float u = 1 - t;
        return 1 - u * u * u;
    }

    private int lighten(int argb, int amount) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

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

    private String fitLabel(String label, float maxWidth) {
        if (label == null) return "";
        if (p.textWidth(label) <= maxWidth) return label;

        String ellipsis = "...";
        String current = label;

        while (current.length() > 1 && p.textWidth(current + ellipsis) > maxWidth) {
            current = current.substring(0, current.length() - 1);
        }
        return current + ellipsis;
    }
}