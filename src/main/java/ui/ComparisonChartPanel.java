package ui;

import model.EvaluationResult;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class ComparisonChartPanel extends JPanel {

    private static final Color COLOR_KNN = new Color(70, 130, 180);
    private static final Color COLOR_DT = new Color(60, 179, 113);
    private static final Color COLOR_BG = new Color(245, 245, 250);
    private static final Color COLOR_GRID = new Color(200, 200, 210);

    private final List<EvaluationResult> results = new ArrayList<>();

    public ComparisonChartPanel() {
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(500, 260));
        setBorder(BorderFactory.createTitledBorder("accuracy and prediction time"));
    }

    public void setResults(List<EvaluationResult> results) {
        this.results.clear();
        this.results.addAll(results);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (results.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("run a model to see the comparison chart.", 60, getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = 50;
        int chartWidth = getWidth() - margin * 2;
        int chartHeight = getHeight() - margin * 2;
        int chartTop = margin;
        int chartBottom = getHeight() - margin;

        drawGrid(g2, margin, chartTop, chartWidth, chartHeight);

        int numberOfGroups = 2;
        int barsPerGroup = results.size();
        int groupGap = 30;
        int barGap = 5;
        int groupWidth = (chartWidth - groupGap * (numberOfGroups - 1)) / numberOfGroups;
        int barWidth = (groupWidth - barGap * (barsPerGroup + 1)) / barsPerGroup;

        drawAccuracyBars(g2, margin, groupWidth, barWidth, barGap, chartHeight, chartBottom);
        drawPredictionTimeBars(g2, margin + groupWidth + groupGap, groupWidth, barWidth,
                barGap, chartTop, chartHeight, chartBottom);
        drawLegend(g2, margin, chartTop - 10);
    }

    private void drawAccuracyBars(Graphics2D g2, int groupX, int groupWidth, int barWidth,
                                  int barGap, int chartHeight, int chartBottom) {
        drawGroupLabel(g2, groupX + groupWidth / 2, chartBottom + 20, "accuracy (%)");

        for (int i = 0; i < results.size(); i++) {
            EvaluationResult result = results.get(i);
            int barHeight = (int) (result.getAccuracy() * chartHeight);
            int barX = groupX + barGap + i * (barWidth + barGap);
            int barY = chartBottom - barHeight;

            g2.setColor(getBarColor(result.getClassifierName()));
            g2.fillRect(barX, barY, barWidth, barHeight);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(String.format("%.1f%%", result.getAccuracy() * 100), barX + 2, barY - 4);
        }
    }

    private void drawPredictionTimeBars(Graphics2D g2, int groupX, int groupWidth, int barWidth,
                                        int barGap, int chartTop, int chartHeight, int chartBottom) {
        long maxTime = 1;
        for (EvaluationResult result : results) {
            maxTime = Math.max(maxTime, result.getPredictionTimeMs());
        }

        drawGroupLabel(g2, groupX + groupWidth / 2, chartBottom + 20, "prediction time");

        for (int i = 0; i < results.size(); i++) {
            EvaluationResult result = results.get(i);
            long timeMs = result.getPredictionTimeMs();
            double ratio = (double) timeMs / maxTime;
            int barHeight = (int) (ratio * chartHeight);
            int barX = groupX + barGap + i * (barWidth + barGap);
            int barY = chartBottom - barHeight;

            g2.setColor(getBarColor(result.getClassifierName()));
            g2.fillRect(barX, barY, barWidth, barHeight);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(result.getPredictionTimeText(), barX + 2, Math.max(barY - 4, chartTop + 12));
        }
    }

    private void drawGrid(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(COLOR_GRID);
        for (int i = 0; i <= 4; i++) {
            int lineY = y + height - (i * height / 4);
            g2.drawLine(x, lineY, x + width, lineY);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(x, y + height, x + width, y + height);
        g2.drawLine(x, y, x, y + height);
    }

    private void drawGroupLabel(Graphics2D g2, int centerX, int y, String text) {
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        int boxSize = 12;
        int currentX = x;

        for (EvaluationResult result : results) {
            String name = result.getClassifierName();
            g2.setColor(getBarColor(name));
            g2.fillRect(currentX, y, boxSize, boxSize);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(name, currentX + boxSize + 4, y + 10);
            currentX += boxSize + g2.getFontMetrics().stringWidth(name) + 20;
        }
    }

    private Color getBarColor(String classifierName) {
        if (classifierName != null && classifierName.startsWith("KNN")) {
            return COLOR_KNN;
        }
        return COLOR_DT;
    }
}
