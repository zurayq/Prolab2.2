package model;

/**
 * DecisionTreeNode represents a single node inside the decision tree.
 *
 * A node can be either:
 *   - a LEAF node: stores a predicted class (majority of samples at that node)
 *   - a SPLIT node: stores which feature to split on, the threshold value,
 *                   and references to the left and right child nodes.
 *
 * Split rule:
 *   if record.features[splitFeatureIndex] <= splitThreshold → go LEFT
 *   otherwise → go RIGHT
 *
 * This design keeps prediction traversal extremely simple and readable.
 */
public class DecisionTreeNode {

    // --- Fields for leaf nodes ---
    private final boolean isLeaf;
    private final int     predictedClass;   // valid only when isLeaf = true

    // --- Fields for split nodes ---
    private final int    splitFeatureIndex; // which feature column to test
    private final double splitThreshold;   // the value we compare against
    private DecisionTreeNode leftChild;    // samples <= threshold
    private DecisionTreeNode rightChild;   // samples >  threshold

    /** Constructor for a LEAF node */
    public DecisionTreeNode(int predictedClass) {
        this.isLeaf            = true;
        this.predictedClass    = predictedClass;
        this.splitFeatureIndex = -1;
        this.splitThreshold    = 0.0;
    }

    /** Constructor for an internal SPLIT node */
    public DecisionTreeNode(int splitFeatureIndex, double splitThreshold) {
        this.isLeaf            = false;
        this.predictedClass    = -1;
        this.splitFeatureIndex = splitFeatureIndex;
        this.splitThreshold    = splitThreshold;
    }

    public boolean isLeaf()             { return isLeaf; }
    public int     getPredictedClass()  { return predictedClass; }
    public int     getSplitFeatureIndex() { return splitFeatureIndex; }
    public double  getSplitThreshold()  { return splitThreshold; }

    public DecisionTreeNode getLeftChild()  { return leftChild; }
    public DecisionTreeNode getRightChild() { return rightChild; }

    public void setLeftChild(DecisionTreeNode left)   { this.leftChild  = left; }
    public void setRightChild(DecisionTreeNode right) { this.rightChild = right; }
}
