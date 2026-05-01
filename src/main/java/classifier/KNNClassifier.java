package classifier;

import model.ProcessedRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KNNClassifier extends BaseClassifier {

    public static final int defaultk = 5;

    private int k;
    private List<ProcessedRecord> trainingdata = new ArrayList<>();

    public KNNClassifier(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K must be at least 1.");
        }
        this.k = k;
    }

    public KNNClassifier() {
        this(defaultk);
    }

    @Override
    public void train(List<ProcessedRecord> trainingdata) {
        if (trainingdata == null || trainingdata.isEmpty()) {
            throw new IllegalArgumentException("KNN needs at least one training record.");
        }

        this.trainingdata = new ArrayList<>(trainingdata);
        System.out.println("[KNN] Training complete. Stored " + this.trainingdata.size() + " records. K=" + k);
    }

    @Override
    public int predict(ProcessedRecord record) {
        if (trainingdata.isEmpty()) {
            throw new IllegalStateException("KNN must be trained before prediction.");
        }

        List<Neighbor> neibors = findNeighbors(record);
        int votecount = Math.min(k, neibors.size());

        List<Integer> labels = new ArrayList<>();
        for (int i = 0; i < votecount; i++) {
            labels.add(neibors.get(i).label);
        }

        return majorityVote(labels);
    }

    private List<Neighbor> findNeighbors(ProcessedRecord record) {
        List<Neighbor> neibors = new ArrayList<>();
        for (ProcessedRecord trainrecord : trainingdata) {
            double distance = computeDistance(record.getFeatures(), trainrecord.getFeatures());
            neibors.add(new Neighbor(distance, trainrecord.getLabel()));
        }

        neibors.sort(Comparator.comparingDouble(neighbor -> neighbor.distance));
        return neibors;
    }

    private double computeDistance(double[] featuresa, double[] featuresb) {
        if (featuresa.length != featuresb.length) {
            throw new IllegalArgumentException("Feature vectors must have the same length.");
        }

        double sum = 0.0;
        for (int i = 0; i < featuresa.length; i++) {
            double diff = featuresa[i] - featuresb[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public String getName() {
        return "KNN (k=" + k + ")";
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K must be at least 1.");
        }
        this.k = k;
    }

    private static class Neighbor {
        private final double distance;
        private final int label;

        private Neighbor(double distance, int label) {
            this.distance = distance;
            this.label = label;
        }
    }
}
