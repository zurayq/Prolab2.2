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

    private static final Color colorknn = new Color(70, 130, 180);
    private static final Color colordt = new Color(60, 179, 113);
    private static final Color colorbg = new Color(245, 245, 250);
    private static final Color colorgrid = new Color(200, 200, 210);

    private final List<EvaluationResult> results = new ArrayList<>();

    public ComparisonChartPanel() {
        setBackground(colorbg);
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
        int chartwidth = getWidth() - margin * 2;
        int chartheight = getHeight() - margin * 2;
        int charttop = margin;
        int chartbottom = getHeight() - margin;

        drawGrid(g2, margin, charttop, chartwidth, chartheight);

        int numberofgroups = 2;
        int barspergroup = results.size();
        int groupgap = 30;
        int bargap = 5;
        int groupwidth = (chartwidth - groupgap * (numberofgroups - 1)) / numberofgroups;
        int barwidth = (groupwidth - bargap * (barspergroup + 1)) / barspergroup;

        drawAccuracyBars(g2, margin, groupwidth, barwidth, bargap, chartheight, chartbottom);
        drawPredictionTimeBars(g2, margin + groupwidth + groupgap, groupwidth, barwidth,
                bargap, charttop, chartheight, chartbottom);
        drawLegend(g2, margin, charttop - 10);
    }

    private void drawAccuracyBars(Graphics2D g2, int groupx, int groupwidth, int barwidth,
                                  int bargap, int chartheight, int chartbottom) {
        drawGroupLabel(g2, groupx + groupwidth / 2, chartbottom + 20, "accuracy (%)");

        for (int i = 0; i < results.size(); i++) {
            EvaluationResult result = results.get(i);
            int barheight = (int) (result.getAccuracy() * chartheight);
            int barx = groupx + bargap + i * (barwidth + bargap);
            int bary = chartbottom - barheight;

            g2.setColor(getBarColor(result.getClassifierName()));
            g2.fillRect(barx, bary, barwidth, barheight);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(String.format("%.1f%%", result.getAccuracy() * 100), barx + 2, bary - 4);
        }
    }

    private void drawPredictionTimeBars(Graphics2D g2, int groupx, int groupwidth, int barwidth,
                                        int bargap, int charttop, int chartheight, int chartbottom) {
        long maxtime = 1;
        for (EvaluationResult result : results) {
            maxtime = Math.max(maxtime, result.getPredictionTimeMs());
        }

        drawGroupLabel(g2, groupx + groupwidth / 2, chartbottom + 20, "prediction time");

        for (int i = 0; i < results.size(); i++) {
            EvaluationResult result = results.get(i);
            long timems = result.getPredictionTimeMs();
            double ratio = (double) timems / maxtime;
            int barheight = (int) (ratio * chartheight);
            int barx = groupx + bargap + i * (barwidth + bargap);
            int bary = chartbottom - barheight;

            g2.setColor(getBarColor(result.getClassifierName()));
            g2.fillRect(barx, bary, barwidth, barheight);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(result.getPredictionTimeText(), barx + 2, Math.max(bary - 4, charttop + 12));
        }
    }

    private void drawGrid(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(colorgrid);
        for (int i = 0; i <= 4; i++) {
            int liney = y + height - (i * height / 4);
            g2.drawLine(x, liney, x + width, liney);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(x, y + height, x + width, y + height);
        g2.drawLine(x, y, x, y + height);
    }

    private void drawGroupLabel(Graphics2D g2, int centerx, int y, String text) {
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerx - metrics.stringWidth(text) / 2, y);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        int boxsize = 12;
        int currentx = x;

        for (EvaluationResult result : results) {
            String name = result.getClassifierName();
            g2.setColor(getBarColor(name));
            g2.fillRect(currentx, y, boxsize, boxsize);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(name, currentx + boxsize + 4, y + 10);
            currentx += boxsize + g2.getFontMetrics().stringWidth(name) + 20;
        }
    }

    private Color getBarColor(String classifiername) {
        if (classifiername != null && classifiername.startsWith("KNN")) {
            return colorknn;
        }
        return colordt;
    }
}
