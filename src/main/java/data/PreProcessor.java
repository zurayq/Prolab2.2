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

public class PreProcessor {

    private static final int unknownlabel = -1;
    private static final double unknowngender = 0.5;
    private static final double unknownbrand = 0.0;

    private final Map<String, Integer> brandencoder = new HashMap<>();
    private final Map<String, Integer> categoryencoder = new HashMap<>();
    private String[] categorylabels = new String[0];

    private double linenettotalmin;
    private double linenettotalmax;
    private int brandcount;
    private boolean fitted = false;

    public List<SaleRecord> removeInvalidRecords(List<SaleRecord> records) {
        List<SaleRecord> validrecords = new ArrayList<>();
        for (SaleRecord record : records) {
            if (isValidRecord(record)) {
                validrecords.add(record);
            }
        }
        System.out.println("[PreProcessor] Valid records after validation: " + validrecords.size());
        return validrecords;
    }

    private boolean isValidRecord(SaleRecord record) {
        if (record == null) return false;
        if (isBlank(record.getGender())) return false;
        if (isBlank(record.getCategoryName1())) return false;
        if (Double.isNaN(record.getLineNetTotal()) || record.getLineNetTotal() < 0) return false;
        return true;
    }

    public void fitOnTrainingData(List<SaleRecord> trainingrecords) {
        if (trainingrecords == null || trainingrecords.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be empty.");
        }

        buildBrandEncoder(trainingrecords);
        buildCategoryEncoder(trainingrecords);
        computeNumericStats(trainingrecords);
        fitted = true;

        System.out.println("[PreProcessor] Fitted on " + trainingrecords.size() + " training records.");
        System.out.println("[PreProcessor] Categories: " + Arrays.toString(categorylabels));
        System.out.println("[PreProcessor] Brands found: " + brandcount);
    }

    public ProcessedRecord transform(SaleRecord record) {
        if (!fitted) {
            throw new IllegalStateException("PreProcessor must be fitted before calling transform().");
        }

        double[] features = new double[3];
        features[0] = encodeGender(record.getGender());
        features[1] = encodeBrand(record.getBrandCode());
        features[2] = normalizeValue(record.getLineNetTotal(), linenettotalmin, linenettotalmax);

        int label = categoryencoder.getOrDefault(record.getCategoryName1(), unknownlabel);
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
        brandencoder.clear();
        int index = 0;

        for (SaleRecord record : records) {
            String brandcode = record.getBrandCode();
            if (!isBlank(brandcode) && !brandencoder.containsKey(brandcode)) {
                brandencoder.put(brandcode, index++);
            }
        }

        brandcount = brandencoder.size();
        if (brandcount == 0) {
            brandcount = 1;
        }
    }

    private void buildCategoryEncoder(List<SaleRecord> records) {
        categoryencoder.clear();
        Set<String> categories = new TreeSet<>();

        for (SaleRecord record : records) {
            if (!isBlank(record.getCategoryName1())) {
                categories.add(record.getCategoryName1());
            }
        }

        int index = 0;
        for (String category : categories) {
            categoryencoder.put(category, index++);
        }

        categorylabels = new String[categoryencoder.size()];
        for (Map.Entry<String, Integer> entry : categoryencoder.entrySet()) {
            categorylabels[entry.getValue()] = entry.getKey();
        }
    }

    private void computeNumericStats(List<SaleRecord> records) {
        linenettotalmin = Double.POSITIVE_INFINITY;
        linenettotalmax = Double.NEGATIVE_INFINITY;

        for (SaleRecord record : records) {
            linenettotalmin = Math.min(linenettotalmin, record.getLineNetTotal());
            linenettotalmax = Math.max(linenettotalmax, record.getLineNetTotal());
        }

        if (linenettotalmin == linenettotalmax) {
            linenettotalmax = linenettotalmin + 1.0;
        }
    }

    private double encodeGender(String gender) {
        if ("E".equalsIgnoreCase(gender)) return 0.0;
        if ("K".equalsIgnoreCase(gender)) return 1.0;
        if ("M".equalsIgnoreCase(gender)) return 0.0;
        if ("F".equalsIgnoreCase(gender)) return 1.0;
        return unknowngender;
    }

    private double encodeBrand(String brandcode) {
        if (!brandencoder.containsKey(brandcode)) {
            return unknownbrand;
        }
        int index = brandencoder.get(brandcode);
        return brandcount > 1 ? (double) index / (brandcount - 1) : 0.0;
    }

    private double normalizeValue(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String[] getCategoryLabels() {
        return categorylabels;
    }

    public int getNumberOfClasses() {
        return categoryencoder.size();
    }

    public Map<String, Integer> getCategoryEncoder() {
        return Collections.unmodifiableMap(categoryencoder);
    }

    public Map<String, Integer> getBrandEncoder() {
        return Collections.unmodifiableMap(brandencoder);
    }
}
