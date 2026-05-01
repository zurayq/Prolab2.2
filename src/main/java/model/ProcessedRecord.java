package model;

public class ProcessedRecord {

    private final double[] features;
    private final int label;
    private final String originalcategorylabel;

    public ProcessedRecord(double[] features, int label, String originalcategorylabel) {
        this.features = features;
        this.label = label;
        this.originalcategorylabel = originalcategorylabel;
    }

    public double[] getFeatures() {
        return features;
    }

    public int getLabel() {
        return label;
    }

    public String getOriginalCategoryLabel() {
        return originalcategorylabel;
    }

    public int getFeatureCount() {
        return features.length;
    }
}
