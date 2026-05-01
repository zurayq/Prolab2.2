package ui;

import model.EvaluationResult;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class ResultPanel extends JPanel {

    private final JTextArea summarytextarea;
    private final JTable matrixtable;
    private final ConfusionMatrixTableModel matrixmodel;
    private final ComparisonChartPanel chartpanel;

    public ResultPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        summarytextarea = new JTextArea(10, 40);
        summarytextarea.setEditable(false);
        summarytextarea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summarytextarea.setLineWrap(false);
        summarytextarea.setText("load a dataset to begin.\n");

        JScrollPane textscroll = new JScrollPane(summarytextarea);
        textscroll.setBorder(BorderFactory.createTitledBorder("results and metrics"));

        matrixmodel = new ConfusionMatrixTableModel();
        matrixtable = new JTable(matrixmodel);
        matrixtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matrixtable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        matrixtable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        matrixtable.setRowHeight(22);
        matrixtable.setDefaultRenderer(Integer.class, new DiagonalHighlightRenderer());

        JScrollPane tablescroll = new JScrollPane(matrixtable);
        tablescroll.setBorder(BorderFactory.createTitledBorder("confusion matrix"));
        tablescroll.setPreferredSize(new Dimension(600, 200));

        chartpanel = new ComparisonChartPanel();

        JPanel tophalf = new JPanel(new GridLayout(1, 1));
        tophalf.add(textscroll);

        JPanel bottomhalf = new JPanel(new BorderLayout(0, 8));
        bottomhalf.add(tablescroll, BorderLayout.CENTER);
        bottomhalf.add(chartpanel, BorderLayout.SOUTH);

        JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tophalf, bottomhalf);
        splitpane.setResizeWeight(0.35);
        splitpane.setDividerSize(6);

        add(splitpane, BorderLayout.CENTER);
    }

    public void appendText(String text) {
        summarytextarea.append(text);
        summarytextarea.setCaretPosition(summarytextarea.getDocument().getLength());
    }

    public void setText(String text) {
        summarytextarea.setText(text);
        summarytextarea.setCaretPosition(0);
    }

    public void showConfusionMatrix(EvaluationResult result) {
        matrixmodel.setData(result);
        adjustColumnWidths();
    }

    public void showChart(List<EvaluationResult> results) {
        chartpanel.setResults(results);
    }

    private void adjustColumnWidths() {
        for (int col = 0; col < matrixtable.getColumnCount(); col++) {
            int maxwidth = col == 0 ? 160 : 60;
            matrixtable.getColumnModel().getColumn(col).setPreferredWidth(maxwidth);
        }
    }

    private static class DiagonalHighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isselected,
                boolean hasfocus, int row, int column) {

            Component component = super.getTableCellRendererComponent(
                    table, value, isselected, hasfocus, row, column);

            boolean diagonal = column > 0 && column - 1 == row;

            if (!isselected) {
                if (diagonal) {
                    component.setBackground(new Color(200, 240, 200));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }

            setHorizontalAlignment(column == 0 ? LEFT : CENTER);
            return component;
        }
    }
}
