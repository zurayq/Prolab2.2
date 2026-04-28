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

    private final JTextArea summaryTextArea;
    private final JTable matrixTable;
    private final ConfusionMatrixTableModel matrixModel;
    private final ComparisonChartPanel chartPanel;

    public ResultPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        summaryTextArea = new JTextArea(10, 40);
        summaryTextArea.setEditable(false);
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryTextArea.setLineWrap(false);
        summaryTextArea.setText("Load a dataset to begin.\n");

        JScrollPane textScroll = new JScrollPane(summaryTextArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Results & Metrics"));

        matrixModel = new ConfusionMatrixTableModel();
        matrixTable = new JTable(matrixModel);
        matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matrixTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        matrixTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        matrixTable.setRowHeight(22);
        matrixTable.setDefaultRenderer(Integer.class, new DiagonalHighlightRenderer());

        JScrollPane tableScroll = new JScrollPane(matrixTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Confusion Matrix"));
        tableScroll.setPreferredSize(new Dimension(600, 200));

        chartPanel = new ComparisonChartPanel();

        JPanel topHalf = new JPanel(new GridLayout(1, 1));
        topHalf.add(textScroll);

        JPanel bottomHalf = new JPanel(new BorderLayout(0, 8));
        bottomHalf.add(tableScroll, BorderLayout.CENTER);
        bottomHalf.add(chartPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topHalf, bottomHalf);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);
    }

    public void appendText(String text) {
        summaryTextArea.append(text);
        summaryTextArea.setCaretPosition(summaryTextArea.getDocument().getLength());
    }

    public void setText(String text) {
        summaryTextArea.setText(text);
        summaryTextArea.setCaretPosition(0);
    }

    public void showConfusionMatrix(EvaluationResult result) {
        matrixModel.setData(result);
        adjustColumnWidths();
    }

    public void showChart(List<EvaluationResult> results) {
        chartPanel.setResults(results);
    }

    private void adjustColumnWidths() {
        for (int col = 0; col < matrixTable.getColumnCount(); col++) {
            int maxWidth = col == 0 ? 160 : 60;
            matrixTable.getColumnModel().getColumn(col).setPreferredWidth(maxWidth);
        }
    }

    private static class DiagonalHighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            boolean diagonal = column > 0 && column - 1 == row;

            if (!isSelected) {
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
