package model;

public class DecisionTreeNode {

    private final boolean isleaf;
    private final int predictedclass;
    private final int splitfeatureindex;
    private final double splitthreshold;
    private DecisionTreeNode leftchild;
    private DecisionTreeNode rightchild;

    public DecisionTreeNode(int predictedclass) {
        this.isleaf = true;
        this.predictedclass = predictedclass;
        this.splitfeatureindex = -1;
        this.splitthreshold = 0.0;
    }

    public DecisionTreeNode(int splitfeatureindex, double splitthreshold) {
        this.isleaf = false;
        this.predictedclass = -1;
        this.splitfeatureindex = splitfeatureindex;
        this.splitthreshold = splitthreshold;
    }

    public boolean isLeaf() {
        return isleaf;
    }

    public int getPredictedClass() {
        return predictedclass;
    }

    public int getSplitFeatureIndex() {
        return splitfeatureindex;
    }

    public double getSplitThreshold() {
        return splitthreshold;
    }

    public DecisionTreeNode getLeftChild() {
        return leftchild;
    }

    public DecisionTreeNode getRightChild() {
        return rightchild;
    }

    public void setLeftChild(DecisionTreeNode leftchild) {
        this.leftchild = leftchild;
    }

    public void setRightChild(DecisionTreeNode rightchild) {
        this.rightchild = rightchild;
    }
}
