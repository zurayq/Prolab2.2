package classifier;

import model.ProcessedRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KNNClassifier extends BaseClassifier {

    public static final int DEFAULT_K = 5;

    private int k;
    private List<ProcessedRecord> trainingData = new ArrayList<>();

    public KNNClassifier(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K must be at least 1.");
        }
        this.k = k;
    }

    public KNNClassifier() {
        this(DEFAULT_K);
    }

    @Override
    public void train(List<ProcessedRecord> trainingData) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("KNN needs at least one training record.");
        }

        this.trainingData = new ArrayList<>(trainingData);
        System.out.println("[KNN] Training complete. Stored " + this.trainingData.size() + " records. K=" + k);
    }

    @Override
    public int predict(ProcessedRecord record) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("KNN must be trained before prediction.");
        }

        List<Neighbor> komsular = findNeighbors(record);
        int voteCount = Math.min(k, komsular.size());

        List<Integer> labels = new ArrayList<>();
        for (int i = 0; i < voteCount; i++) {
            labels.add(komsular.get(i).label);
        }

        return majorityVote(labels);
    }

    private List<Neighbor> findNeighbors(ProcessedRecord record) {
        List<Neighbor> komsular = new ArrayList<>();
        for (ProcessedRecord trainRecord : trainingData) {
            double distance = computeDistance(record.getFeatures(), trainRecord.getFeatures());
            komsular.add(new Neighbor(distance, trainRecord.getLabel()));
        }

        komsular.sort(Comparator.comparingDouble(neighbor -> neighbor.distance));
        return komsular;
    }

    private double computeDistance(double[] featuresA, double[] featuresB) {
        if (featuresA.length != featuresB.length) {
            throw new IllegalArgumentException("Feature vectors must have the same length.");
        }

        double toplam = 0.0;
        for (int i = 0; i < featuresA.length; i++) {
            double fark = featuresA[i] - featuresB[i];
            toplam += fark * fark;
        }
        return Math.sqrt(toplam);
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
