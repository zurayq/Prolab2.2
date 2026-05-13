package model;

public class ProcessedRecord {

    private final double[] features;
    private final int label;
    private final String originalCategoryLabel;

    public ProcessedRecord(double[] features, int label, String originalCategoryLabel) {
        this.features = features;
        this.label = label;
        this.originalCategoryLabel = originalCategoryLabel;
    }
    public double[] getFeatures() {
        return features;
    }
    public int getLabel() {
        return label;
    }
    public String getOriginalCategoryLabel() {
        return originalCategoryLabel;
    }
    public int getFeatureCount() {
        return features.length;
    }
}
