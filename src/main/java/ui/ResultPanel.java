package ui;

import model.EvaluationResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * ResultPanel is the right-side output area of the main window.
 *
 * It contains three sub-areas stacked vertically:
 *   1. A text area showing dataset summary and model results
 *   2. A scrollable JTable showing the confusion matrix
 *   3. A chart panel showing accuracy and runtime comparison bars
 */
public class ResultPanel extends JPanel {

    private final JTextArea          summaryTextArea;
    private final JTable             matrixTable;
    private final ConfusionMatrixTableModel matrixModel;
    private final ComparisonChartPanel chartPanel;

    public ResultPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Top: Text output area ---
        summaryTextArea = new JTextArea(10, 40);
        summaryTextArea.setEditable(false);
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryTextArea.setLineWrap(false);
        summaryTextArea.setText("Load a dataset to begin.\n");
        JScrollPane textScroll = new JScrollPane(summaryTextArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Results & Metrics"));

        // --- Middle: Confusion Matrix Table ---
        matrixModel = new ConfusionMatrixTableModel();
        matrixTable = new JTable(matrixModel);
        matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matrixTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        matrixTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        matrixTable.setRowHeight(22);

        // Highlight diagonal cells (correct predictions) with a light green
        matrixTable.setDefaultRenderer(Integer.class, new DiagonalHighlightRenderer());

        JScrollPane tableScroll = new JScrollPane(matrixTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Confusion Matrix"));
        tableScroll.setPreferredSize(new Dimension(600, 200));

        // --- Bottom: Chart ---
        chartPanel = new ComparisonChartPanel();

        // Arrange vertically
        JPanel topHalf = new JPanel(new GridLayout(1, 1));
        topHalf.add(textScroll);

        JPanel bottomHalf = new JPanel(new BorderLayout(0, 8));
        bottomHalf.add(tableScroll, BorderLayout.CENTER);
        bottomHalf.add(chartPanel,  BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topHalf, bottomHalf);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);
    }

    /** Appends text to the results area. */
    public void appendText(String text) {
        summaryTextArea.append(text);
        summaryTextArea.setCaretPosition(summaryTextArea.getDocument().getLength());
    }

    /** Clears and sets the results area to the given text. */
    public void setText(String text) {
        summaryTextArea.setText(text);
        summaryTextArea.setCaretPosition(0);
    }

    /** Updates the confusion matrix table with the given result. */
    public void showConfusionMatrix(EvaluationResult result) {
        matrixModel.setData(result);
        adjustColumnWidths();
    }

    /** Updates the comparison chart with a list of results. */
    public void showChart(List<EvaluationResult> results) {
        chartPanel.setResults(results);
    }

    /** Auto-sizes columns in the confusion matrix table after data update. */
    private void adjustColumnWidths() {
        for (int col = 0; col < matrixTable.getColumnCount(); col++) {
            int maxWidth = 60;
            if (col == 0) maxWidth = 160; // wider first column for label names
            matrixTable.getColumnModel().getColumn(col).setPreferredWidth(maxWidth);
        }
    }

    /**
     * Cell renderer that highlights diagonal cells (i.e., row == col - 1)
     * with a light green background to make correct predictions stand out.
     */
    private static class DiagonalHighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            // column 0 is the label column, so diagonal is when column - 1 == row
            boolean isDiagonal = (column > 0) && (column - 1 == row);

            if (!isSelected) {
                if (isDiagonal) {
                    c.setBackground(new Color(200, 240, 200)); // light green
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            setHorizontalAlignment(column == 0 ? LEFT : CENTER);
            return c;
        }
    }
}
