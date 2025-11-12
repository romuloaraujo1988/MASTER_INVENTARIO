package com.inventario.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.awt.*;
import java.util.Map;

/**
 * Factory para criação de gráficos JFreeChart
 * Centraliza a criação e estilização de gráficos
 */
public class InventarioChartFactory {

    // Cores modernas para gráficos
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(155, 89, 182);
    private static final Color SECONDARY_COLOR = new Color(149, 165, 166);

    private static final Color[] CHART_COLORS = {
        PRIMARY_COLOR, SUCCESS_COLOR, WARNING_COLOR, 
        DANGER_COLOR, INFO_COLOR, SECONDARY_COLOR
    };

    /**
     * Cria gráfico de pizza
     */
    public static JFreeChart createPieChart(String title, Map<String, Number> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
            title,
            dataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );

        stylePieChart(chart);
        return chart;
    }

    /**
     * Cria gráfico de barras vertical
     */
    public static JFreeChart createBarChart(String title, String categoryAxisLabel, 
                                           String valueAxisLabel, CategoryDataset dataset) {
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            title,
            categoryAxisLabel,
            valueAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );

        styleBarChart(chart);
        return chart;
    }

    /**
     * Cria gráfico de barras horizontal
     */
    public static JFreeChart createHorizontalBarChart(String title, String categoryAxisLabel,
                                                     String valueAxisLabel, CategoryDataset dataset) {
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            title,
            categoryAxisLabel,
            valueAxisLabel,
            dataset,
            PlotOrientation.HORIZONTAL,
            true,
            true,
            false
        );

        styleBarChart(chart);
        return chart;
    }

    /**
     * Cria gráfico de linhas
     */
    public static JFreeChart createLineChart(String title, String categoryAxisLabel,
                                            String valueAxisLabel, CategoryDataset dataset) {
        JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
            title,
            categoryAxisLabel,
            valueAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        styleLineChart(chart);
        return chart;
    }

    /**
     * Cria dataset de categoria simples
     */
    public static DefaultCategoryDataset createCategoryDataset(Map<String, Number> data, String seriesName) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((category, value) -> dataset.addValue(value, seriesName, category));
        return dataset;
    }

    /**
     * Cria dataset de categoria com múltiplas séries
     */
    public static DefaultCategoryDataset createMultiSeriesDataset(Map<String, Map<String, Number>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((series, values) -> 
            values.forEach((category, value) -> 
                dataset.addValue(value, series, category)
            )
        );
        return dataset;
    }

    // Métodos de estilização privados

    private static void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        
        // Aplicar cores personalizadas
        int colorIndex = 0;
        for (Object key : plot.getDataset().getKeys()) {
            plot.setSectionPaint((Comparable) key, CHART_COLORS[colorIndex % CHART_COLORS.length]);
            colorIndex++;
        }
    }

    private static void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        
        // Aplicar cores às barras
        for (int i = 0; i < CHART_COLORS.length; i++) {
            renderer.setSeriesPaint(i, CHART_COLORS[i]);
        }
    }

    private static void styleLineChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        
        // Aplicar cores às linhas
        for (int i = 0; i < CHART_COLORS.length; i++) {
            renderer.setSeriesPaint(i, CHART_COLORS[i]);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }
    }
}
