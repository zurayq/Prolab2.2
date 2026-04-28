package ui;

import model.EvaluationResult;

import javax.swing.table.AbstractTableModel;

public class ConfusionMatrixTableModel extends AbstractTableModel {

    private int[][] matrix;
    private String[] labels;
    private int numClasses;

    public ConfusionMatrixTableModel() {
        matrix = new int[0][0];
        labels = new String[0];
        numClasses = 0;
    }

    public void setData(EvaluationResult result) {
        matrix = result.getConfusionMatrix();
        labels = result.getClassLabels();
        numClasses = labels.length;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return numClasses;
    }

    @Override
    public int getColumnCount() {
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
            return labels[row];
        }
        return matrix[row][column - 1];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
    }
}
