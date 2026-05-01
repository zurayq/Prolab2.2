package classifier;

import model.DecisionTreeNode;
import model.ProcessedRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DecisionTreeClassifier extends BaseClassifier {

    public static final int defaultmaxdepth = 10;
    private static final int minsamplestosplit = 5;

    private int maxdepth;
    private DecisionTreeNode root;

    public DecisionTreeClassifier(int maxdepth) {
        if (maxdepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1.");
        }
        this.maxdepth = maxdepth;
    }

    public DecisionTreeClassifier() {
        this(defaultmaxdepth);
    }

    @Override
    public void train(List<ProcessedRecord> trainingdata) {
        if (trainingdata == null || trainingdata.isEmpty()) {
            throw new IllegalArgumentException("Decision Tree needs at least one training record.");
        }

        root = buildTree(trainingdata, 0);
        System.out.println("[Decision Tree] Tree built. maxDepth=" + maxdepth);
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

        double value = features[node.getSplitFeatureIndex()];
        if (value <= node.getSplitThreshold()) {
            return traverseTree(node.getLeftChild(), features);
        }
        return traverseTree(node.getRightChild(), features);
    }

    private DecisionTreeNode buildTree(List<ProcessedRecord> records, int depth) {
        if (shouldCreateLeaf(records, depth)) {
            return createLeaf(records);
        }

        BestSplit best = findBestSplit(records);
        if (best == null) {
            return createLeaf(records);
        }

        SplitResult split = splitRecords(records, best.featureindex, best.threshold);
        if (!split.isUsable()) {
            return createLeaf(records);
        }

        DecisionTreeNode node = new DecisionTreeNode(best.featureindex, best.threshold);
        node.setLeftChild(buildTree(split.leftrecords, depth + 1));
        node.setRightChild(buildTree(split.rightrecords, depth + 1));
        return node;
    }

    private boolean shouldCreateLeaf(List<ProcessedRecord> records, int depth) {
        return records.size() < minsamplestosplit || depth >= maxdepth || isPure(records);
    }

    private BestSplit findBestSplit(List<ProcessedRecord> records) {
        int featurecount = records.get(0).getFeatureCount();
        double bestgini = computeGini(records);
        BestSplit best = null;

        for (int featureindex = 0; featureindex < featurecount; featureindex++) {
            Set<Double> thresholds = getCandidateThresholds(records, featureindex);

            for (double threshold : thresholds) {
                SplitResult split = splitRecords(records, featureindex, threshold);
                if (!split.isUsable()) {
                    continue;
                }

                double gini = computeWeightedGini(split, records.size());
                if (gini < bestgini) {
                    bestgini = gini;
                    best = new BestSplit(featureindex, threshold);
                }
            }
        }

        return best;
    }

    private SplitResult splitRecords(List<ProcessedRecord> records, int featureindex, double threshold) {
        List<ProcessedRecord> left = new ArrayList<>();
        List<ProcessedRecord> right = new ArrayList<>();

        for (ProcessedRecord record : records) {
            if (record.getFeatures()[featureindex] <= threshold) {
                left.add(record);
            } else {
                right.add(record);
            }
        }

        return new SplitResult(left, right);
    }

    private double computeGini(List<ProcessedRecord> records) {
        if (records.isEmpty()) {
            return 0.0;
        }

        int total = records.size();
        double sumsquares = 0.0;

        for (int count : countByClass(records).values()) {
            double ratio = (double) count / total;
            sumsquares += ratio * ratio;
        }

        return 1.0 - sumsquares;
    }

    private double computeWeightedGini(SplitResult split, int totalrecords) {
        double leftweight = (double) split.leftrecords.size() / totalrecords;
        double rightweight = (double) split.rightrecords.size() / totalrecords;
        return leftweight * computeGini(split.leftrecords) + rightweight * computeGini(split.rightrecords);
    }

    private Set<Double> getCandidateThresholds(List<ProcessedRecord> records, int featureindex) {
        List<Double> values = new ArrayList<>();
        for (ProcessedRecord record : records) {
            values.add(record.getFeatures()[featureindex]);
        }

        Collections.sort(values);

        Set<Double> thresholds = new LinkedHashSet<>();
        for (int i = 1; i < values.size(); i++) {
            double previous = values.get(i - 1);
            double current = values.get(i);
            if (Double.compare(previous, current) != 0) {
                thresholds.add((previous + current) / 2.0);
            }
        }

        return thresholds;
    }

    private boolean isPure(List<ProcessedRecord> records) {
        if (records.isEmpty()) {
            return true;
        }

        int firstlabel = records.get(0).getLabel();
        for (ProcessedRecord record : records) {
            if (record.getLabel() != firstlabel) {
                return false;
            }
        }
        return true;
    }

    private DecisionTreeNode createLeaf(List<ProcessedRecord> records) {
        int label = records.isEmpty() ? 0 : majorityClass(records);
        return new DecisionTreeNode(label);
    }

    @Override
    public String getName() {
        return "Decision Tree (maxDepth=" + maxdepth + ")";
    }

    public int getMaxDepth() {
        return maxdepth;
    }

    public void setMaxDepth(int maxdepth) {
        if (maxdepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1.");
        }
        this.maxdepth = maxdepth;
    }

    private static class BestSplit {
        private final int featureindex;
        private final double threshold;

        private BestSplit(int featureindex, double threshold) {
            this.featureindex = featureindex;
            this.threshold = threshold;
        }
    }

    private static class SplitResult {
        private final List<ProcessedRecord> leftrecords;
        private final List<ProcessedRecord> rightrecords;

        private SplitResult(List<ProcessedRecord> leftrecords, List<ProcessedRecord> rightrecords) {
            this.leftrecords = leftrecords;
            this.rightrecords = rightrecords;
        }

        private boolean isUsable() {
            return !leftrecords.isEmpty() && !rightrecords.isEmpty();
        }
    }
}
