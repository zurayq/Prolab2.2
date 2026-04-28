package classifier;

import model.DecisionTreeNode;
import model.ProcessedRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decision Tree classifier from scratch using Gini impurity.
 *
 * Each split tests one feature against one threshold. Records with
 * feature <= threshold go left; the rest go right.
 */
public class DecisionTreeClassifier extends BaseClassifier {

    public static final int DEFAULT_MAX_DEPTH = 10;
    private static final int MIN_SAMPLES_TO_SPLIT = 5;

    private int maxDepth;
    private DecisionTreeNode root;

    public DecisionTreeClassifier(int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1.");
        }
        this.maxDepth = maxDepth;
    }

    public DecisionTreeClassifier() {
        this(DEFAULT_MAX_DEPTH);
    }

    @Override
    public void train(List<ProcessedRecord> trainingData) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Decision Tree needs at least one training record.");
        }

        root = buildTree(trainingData, 0);
        System.out.println("[Decision Tree] Tree built. maxDepth=" + maxDepth);
    }

    @Override
    public int predict(ProcessedRecord record) {
        if (root == null) {
            throw new IllegalStateException("Decision Tree must be trained before prediction.");
        }
        return traverseTree(root, record.getFeatures());
    }

    private int traverseTree(DecisionTreeNode node, double[] features) {
        if (node.isLeaf()) {
            return node.getPredictedClass();
        }

        double featureValue = features[node.getSplitFeatureIndex()];
        if (featureValue <= node.getSplitThreshold()) {
            return traverseTree(node.getLeftChild(), features);
        }
        return traverseTree(node.getRightChild(), features);
    }

    private DecisionTreeNode buildTree(List<ProcessedRecord> records, int currentDepth) {
        if (shouldCreateLeaf(records, currentDepth)) {
            return createLeaf(records);
        }

        BestSplit bestSplit = findBestSplit(records);
        if (bestSplit == null) {
            return createLeaf(records);
        }

        SplitResult split = splitRecords(records, bestSplit.featureIndex, bestSplit.threshold);
        if (!split.isUsable()) {
            return createLeaf(records);
        }

        DecisionTreeNode node = new DecisionTreeNode(bestSplit.featureIndex, bestSplit.threshold);
        node.setLeftChild(buildTree(split.leftRecords, currentDepth + 1));
        node.setRightChild(buildTree(split.rightRecords, currentDepth + 1));
        return node;
    }

    private boolean shouldCreateLeaf(List<ProcessedRecord> records, int currentDepth) {
        return records.size() < MIN_SAMPLES_TO_SPLIT
                || currentDepth >= maxDepth
                || isPure(records);
    }

    private BestSplit findBestSplit(List<ProcessedRecord> records) {
        int numberOfFeatures = records.get(0).getFeatureCount();
        double bestGini = computeGini(records);
        BestSplit bestSplit = null;

        for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
            Set<Double> thresholds = getUniqueValues(records, featureIndex);

            for (double threshold : thresholds) {
                SplitResult split = splitRecords(records, featureIndex, threshold);
                if (!split.isUsable()) {
                    continue;
                }

                double splitGini = computeWeightedGini(split, records.size());
                if (splitGini < bestGini) {
                    bestGini = splitGini;
                    bestSplit = new BestSplit(featureIndex, threshold);
                }
            }
        }

        return bestSplit;
    }

    private SplitResult splitRecords(List<ProcessedRecord> records, int featureIndex, double threshold) {
        List<ProcessedRecord> leftRecords = new ArrayList<>();
        List<ProcessedRecord> rightRecords = new ArrayList<>();

        for (ProcessedRecord record : records) {
            if (record.getFeatures()[featureIndex] <= threshold) {
                leftRecords.add(record);
            } else {
                rightRecords.add(record);
            }
        }

        return new SplitResult(leftRecords, rightRecords);
    }

    private double computeGini(List<ProcessedRecord> records) {
        if (records.isEmpty()) {
            return 0.0;
        }

        int total = records.size();
        double sumOfSquaredProportions = 0.0;

        for (int count : countByClass(records).values()) {
            double proportion = (double) count / total;
            sumOfSquaredProportions += proportion * proportion;
        }

        return 1.0 - sumOfSquaredProportions;
    }

    private double computeWeightedGini(SplitResult split, int totalRecords) {
        double leftWeight = (double) split.leftRecords.size() / totalRecords;
        double rightWeight = (double) split.rightRecords.size() / totalRecords;
        return leftWeight * computeGini(split.leftRecords)
                + rightWeight * computeGini(split.rightRecords);
    }

    private Set<Double> getUniqueValues(List<ProcessedRecord> records, int featureIndex) {
        Set<Double> values = new HashSet<>();
        for (ProcessedRecord record : records) {
            values.add(record.getFeatures()[featureIndex]);
        }
        return values;
    }

    private boolean isPure(List<ProcessedRecord> records) {
        if (records.isEmpty()) {
            return true;
        }

        int firstLabel = records.get(0).getLabel();
        for (ProcessedRecord record : records) {
            if (record.getLabel() != firstLabel) {
                return false;
            }
        }
        return true;
    }

    private DecisionTreeNode createLeaf(List<ProcessedRecord> records) {
        int majorityLabel = records.isEmpty() ? 0 : majorityClass(records);
        return new DecisionTreeNode(majorityLabel);
    }

    @Override
    public String getName() {
        return "Decision Tree (maxDepth=" + maxDepth + ")";
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1.");
        }
        this.maxDepth = maxDepth;
    }

    private static class BestSplit {
        private final int featureIndex;
        private final double threshold;

        private BestSplit(int featureIndex, double threshold) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
        }
    }

    private static class SplitResult {
        private final List<ProcessedRecord> leftRecords;
        private final List<ProcessedRecord> rightRecords;

        private SplitResult(List<ProcessedRecord> leftRecords, List<ProcessedRecord> rightRecords) {
            this.leftRecords = leftRecords;
            this.rightRecords = rightRecords;
        }

        private boolean isUsable() {
            return !leftRecords.isEmpty() && !rightRecords.isEmpty();
        }
    }
}
