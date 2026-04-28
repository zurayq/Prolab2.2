package model;

public class DecisionTreeNode {

    private final boolean isLeaf;
    private final int predictedClass;
    private final int splitFeatureIndex;
    private final double splitThreshold;
    private DecisionTreeNode leftChild;
    private DecisionTreeNode rightChild;

    public DecisionTreeNode(int predictedClass) {
        this.isLeaf = true;
        this.predictedClass = predictedClass;
        this.splitFeatureIndex = -1;
        this.splitThreshold = 0.0;
    }

    public DecisionTreeNode(int splitFeatureIndex, double splitThreshold) {
        this.isLeaf = false;
        this.predictedClass = -1;
        this.splitFeatureIndex = splitFeatureIndex;
        this.splitThreshold = splitThreshold;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public int getPredictedClass() {
        return predictedClass;
    }

    public int getSplitFeatureIndex() {
        return splitFeatureIndex;
    }

    public double getSplitThreshold() {
        return splitThreshold;
    }

    public DecisionTreeNode getLeftChild() {
        return leftChild;
    }

    public DecisionTreeNode getRightChild() {
        return rightChild;
    }

    public void setLeftChild(DecisionTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(DecisionTreeNode rightChild) {
        this.rightChild = rightChild;
    }
}
