package model;

/**
 * ProcessedRecord holds the ML-ready version of a SaleRecord.
 *
 * After preprocessing is complete, each SaleRecord becomes a ProcessedRecord
 * with:
 *   - a numeric feature vector (encoded + normalized)
 *   - an integer label (encoded category index)
 *   - the original category string (for reporting in the UI)
 *
 * Feature vector layout (index → field):
 *   0 → gender encoded      (0.0 = E, 1.0 = K)
 *   1 → brand encoded       (integer index, then normalized)
 *   2 → lineNet normalized  (min-max scaled to [0, 1])
 *   3 → amount normalized   (min-max scaled to [0, 1])
 *
 * Normalization is applied to numeric features only (lineNet, amount).
 * Categorical features use label encoding (not ordinal distance math).
 */
public class ProcessedRecord {

    // Feature vector used by both classifiers
    private final double[] features;

    // Integer label used during training and evaluation
    private final int label;

    // Human-readable original category name (for confusion matrix labels)
    private final String originalCategoryLabel;

    public ProcessedRecord(double[] features, int label, String originalCategoryLabel) {
        this.features              = features;
        this.label                 = label;
        this.originalCategoryLabel = originalCategoryLabel;
    }

    public double[] getFeatures()            { return features; }
    public int      getLabel()               { return label; }
    public String   getOriginalCategoryLabel() { return originalCategoryLabel; }

    /** Convenience: how many features this record has */
    public int getFeatureCount() {
        return features.length;
    }
}
