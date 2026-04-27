package ui;

import model.EvaluationResult;

import javax.swing.table.AbstractTableModel;

/**
 * ConfusionMatrixTableModel adapts an EvaluationResult's confusion matrix
 * into a format that JTable can display.
 *
 * The matrix is displayed with:
 *   - Row headers = actual class labels (what the data really is)
 *   - Column headers = predicted class labels (what the model guessed)
 *   - Cell values = count of records in that actual/predicted combination
 *
 * Diagonal cells (actual == predicted) are correctly classified records.
 * Off-diagonal cells are misclassifications.
 */
public class ConfusionMatrixTableModel extends AbstractTableModel {

    private int[][]  matrix;
    private String[] labels;
    private int      numClasses;

    public ConfusionMatrixTableModel() {
        this.matrix    = new int[0][0];
        this.labels    = new String[0];
        this.numClasses = 0;
    }

    /** Update the table with new evaluation results. */
    public void setData(EvaluationResult result) {
        this.matrix    = result.getConfusionMatrix();
        this.labels    = result.getClassLabels();
        this.numClasses = labels.length;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return numClasses;
    }

    @Override
    public int getColumnCount() {
        // +1 for the row header column (actual class name)
        return numClasses + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Actual \\ Predicted";
        }
        return labels[column - 1];
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            // First column shows the actual class label
            return labels[row];
        }
        // Remaining columns show confusion matrix counts
        return matrix[row][column - 1];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // table is read-only
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
    }
}
