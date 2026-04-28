package data;

import model.ProcessedRecord;
import model.SaleRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Converts raw SaleRecord objects into numeric ProcessedRecord objects.
 *
 * The important rule is that encoders and min/max values are fitted on
 * training data only. Test data is only transformed with those fitted values.
 */
public class PreProcessor {

    private static final int UNKNOWN_LABEL = -1;
    private static final double UNKNOWN_GENDER_VALUE = 0.5;
    private static final double UNKNOWN_BRAND_VALUE = 0.0;

    private final Map<String, Integer> brandEncoder = new HashMap<>();
    private final Map<String, Integer> categoryEncoder = new HashMap<>();
    private String[] categoryLabels = new String[0];

    private double lineNetMin;
    private double lineNetMax;
    private double amountMin;
    private double amountMax;
    private int totalBrands;

    private boolean isFitted = false;

    /**
     * Keeps only rows that have the fields required by the model.
     * This happens before the train/test split.
     */
    public List<SaleRecord> removeInvalidRecords(List<SaleRecord> records) {
        List<SaleRecord> valid = new ArrayList<>();
        for (SaleRecord record : records) {
            if (isValidRecord(record)) {
                valid.add(record);
            }
        }
        System.out.println("[PreProcessor] Valid records after validation: " + valid.size());
        return valid;
    }

    private boolean isValidRecord(SaleRecord record) {
        if (record == null) return false;
        if (isBlank(record.getGender())) return false;
        if (isBlank(record.getCategoryName1())) return false;
        if (Double.isNaN(record.getLineNet()) || record.getLineNet() < 0) return false;
        if (Double.isNaN(record.getAmount()) || record.getAmount() < 0) return false;
        return true;
    }

    /**
     * Fits encoders and numeric ranges using only the training records.
     */
    public void fitOnTrainingData(List<SaleRecord> trainingRecords) {
        if (trainingRecords == null || trainingRecords.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be empty.");
        }

        buildBrandEncoder(trainingRecords);
        buildCategoryEncoder(trainingRecords);
        computeNumericStats(trainingRecords);
        isFitted = true;

        System.out.println("[PreProcessor] Fitted on " + trainingRecords.size() + " training records.");
        System.out.println("[PreProcessor] Categories: " + Arrays.toString(categoryLabels));
        System.out.println("[PreProcessor] Brands found: " + totalBrands);
    }

    /**
     * Converts one raw record into four features:
     * gender, brand, lineNet, and amount.
     */
    public ProcessedRecord transform(SaleRecord record) {
        if (!isFitted) {
            throw new IllegalStateException("PreProcessor must be fitted before calling transform().");
        }

        double[] features = new double[4];
        features[0] = encodeGender(record.getGender());
        features[1] = encodeBrand(record.getBrand());
        features[2] = normalizeValue(record.getLineNet(), lineNetMin, lineNetMax);
        features[3] = normalizeValue(record.getAmount(), amountMin, amountMax);

        int label = encodeCategory(record.getCategoryName1());
        return new ProcessedRecord(features, label, record.getCategoryName1());
    }

    public List<ProcessedRecord> transformAll(List<SaleRecord> records) {
        List<ProcessedRecord> result = new ArrayList<>();
        for (SaleRecord record : records) {
            result.add(transform(record));
        }
        return result;
    }

    private void buildBrandEncoder(List<SaleRecord> records) {
        brandEncoder.clear();

        int index = 0;
        for (SaleRecord record : records) {
            String brand = record.getBrand();
            if (!isBlank(brand) && !brandEncoder.containsKey(brand)) {
                brandEncoder.put(brand, index++);
            }
        }

        totalBrands = brandEncoder.size();
        if (totalBrands == 0) {
            totalBrands = 1;
        }
    }

    private void buildCategoryEncoder(List<SaleRecord> records) {
        categoryEncoder.clear();

        Set<String> uniqueCategories = new TreeSet<>();
        for (SaleRecord record : records) {
            if (!isBlank(record.getCategoryName1())) {
                uniqueCategories.add(record.getCategoryName1());
            }
        }

        int index = 0;
        for (String category : uniqueCategories) {
            categoryEncoder.put(category, index++);
        }

        categoryLabels = new String[categoryEncoder.size()];
        for (Map.Entry<String, Integer> entry : categoryEncoder.entrySet()) {
            categoryLabels[entry.getValue()] = entry.getKey();
        }
    }

    private void computeNumericStats(List<SaleRecord> records) {
        lineNetMin = Double.POSITIVE_INFINITY;
        lineNetMax = Double.NEGATIVE_INFINITY;
        amountMin = Double.POSITIVE_INFINITY;
        amountMax = Double.NEGATIVE_INFINITY;

        for (SaleRecord record : records) {
            lineNetMin = Math.min(lineNetMin, record.getLineNet());
            lineNetMax = Math.max(lineNetMax, record.getLineNet());
            amountMin = Math.min(amountMin, record.getAmount());
            amountMax = Math.max(amountMax, record.getAmount());
        }

        if (lineNetMin == lineNetMax) {
            lineNetMax = lineNetMin + 1.0;
        }
        if (amountMin == amountMax) {
            amountMax = amountMin + 1.0;
        }
    }

    private double encodeGender(String gender) {
        if ("E".equalsIgnoreCase(gender)) return 0.0;
        if ("K".equalsIgnoreCase(gender)) return 1.0;
        return UNKNOWN_GENDER_VALUE;
    }

    private double encodeBrand(String brand) {
        int brandIndex = brandEncoder.getOrDefault(brand, 0);
        if (!brandEncoder.containsKey(brand)) {
            return UNKNOWN_BRAND_VALUE;
        }
        return totalBrands > 1 ? (double) brandIndex / (totalBrands - 1) : 0.0;
    }

    private int encodeCategory(String categoryName) {
        return categoryEncoder.getOrDefault(categoryName, UNKNOWN_LABEL);
    }

    private double normalizeValue(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String[] getCategoryLabels() {
        return categoryLabels;
    }

    public int getNumberOfClasses() {
        return categoryEncoder.size();
    }

    public Map<String, Integer> getCategoryEncoder() {
        return Collections.unmodifiableMap(categoryEncoder);
    }

    public Map<String, Integer> getBrandEncoder() {
        return Collections.unmodifiableMap(brandEncoder);
    }
}
