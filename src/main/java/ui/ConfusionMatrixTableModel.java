package ui;

import model.EvaluationResult;

import javax.swing.table.AbstractTableModel;

public class ConfusionMatrixTableModel extends AbstractTableModel {

    private int[][] matrix;
    private String[] labels;
    private int numclasses;

    public ConfusionMatrixTableModel() {
        matrix = new int[0][0];
        labels = new String[0];
        numclasses = 0;
    }

    public void setData(EvaluationResult result) {
        matrix = result.getConfusionMatrix();
        labels = result.getClassLabels();
        numclasses = labels.length;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return numclasses;
    }

    @Override
    public int getColumnCount() {
        return numclasses + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "actual \\ predicted";
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
