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

    private static final int UNKNOWN_LABEL = -1;
    private static final double UNKNOWN_GENDER = 0.5;
    private static final double UNKNOWN_BRAND = 0.0;

    private final Map<String, Integer> brandEncoder = new HashMap<>();
    private final Map<String, Integer> categoryEncoder = new HashMap<>();
    private String[] categoryLabels = new String[0];

    private double lineNetTotalMin;
    private double lineNetTotalMax;
    private int brandCount;
    private boolean fitted = false;

    public List<SaleRecord> removeInvalidRecords(List<SaleRecord> records) {
        List<SaleRecord> validRecords = new ArrayList<>();
        for (SaleRecord kayit : records) {
            if (isValidRecord(kayit)) {
                validRecords.add(kayit);
            }
        }
        System.out.println("[PreProcessor] Valid records after validation: " + validRecords.size());
        return validRecords;
    }

    private boolean isValidRecord(SaleRecord kayit) {
        if (kayit == null) return false;
        if (isBlank(kayit.getGender())) return false;
        if (isBlank(kayit.getCategoryName1())) return false;
        if (Double.isNaN(kayit.getLineNetTotal()) || kayit.getLineNetTotal() < 0) return false;
        return true;
    }

    public void fitOnTrainingData(List<SaleRecord> trainingRecords) {
        if (trainingRecords == null || trainingRecords.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be empty.");
        }

        buildBrandEncoder(trainingRecords);
        buildCategoryEncoder(trainingRecords);
        computeNumericStats(trainingRecords);
        fitted = true;

        System.out.println("[PreProcessor] Fitted on " + trainingRecords.size() + " training records.");
        System.out.println("[PreProcessor] Categories: " + Arrays.toString(categoryLabels));
        System.out.println("[PreProcessor] Brands found: " + brandCount);
    }

    public ProcessedRecord transform(SaleRecord kayit) {
        if (!fitted) {
            throw new IllegalStateException("PreProcessor must be fitted before calling transform().");
        }

        double[] features = new double[3];
        features[0] = encodeGender(kayit.getGender());
        features[1] = encodeBrand(kayit.getBrandCode());
        features[2] = normalizeValue(kayit.getLineNetTotal(), lineNetTotalMin, lineNetTotalMax);

        int label = categoryEncoder.getOrDefault(kayit.getCategoryName1(), UNKNOWN_LABEL);
        return new ProcessedRecord(features, label, kayit.getCategoryName1());
    }

    public List<ProcessedRecord> transformAll(List<SaleRecord> records) {
        List<ProcessedRecord> sonuc = new ArrayList<>();
        for (SaleRecord kayit : records) {
            sonuc.add(transform(kayit));
        }
        return sonuc;
    }

    private void buildBrandEncoder(List<SaleRecord> records) {
        brandEncoder.clear();
        int index = 0;

        for (SaleRecord kayit : records) {
            String brandCode = kayit.getBrandCode();
            if (!isBlank(brandCode) && !brandEncoder.containsKey(brandCode)) {
                brandEncoder.put(brandCode, index++);
            }
        }

        brandCount = brandEncoder.size();
        if (brandCount == 0) {
            brandCount = 1;
        }
    }

    private void buildCategoryEncoder(List<SaleRecord> records) {
        categoryEncoder.clear();
        Set<String> categories = new TreeSet<>();

        for (SaleRecord kayit : records) {
            if (!isBlank(kayit.getCategoryName1())) {
                categories.add(kayit.getCategoryName1());
            }
        }

        int index = 0;
        for (String kategori : categories) {
            categoryEncoder.put(kategori, index++);
        }

        categoryLabels = new String[categoryEncoder.size()];
        for (Map.Entry<String, Integer> entry : categoryEncoder.entrySet()) {
            categoryLabels[entry.getValue()] = entry.getKey();
        }
    }

    private void computeNumericStats(List<SaleRecord> records) {
        lineNetTotalMin = Double.POSITIVE_INFINITY;
        lineNetTotalMax = Double.NEGATIVE_INFINITY;

        for (SaleRecord kayit : records) {
            lineNetTotalMin = Math.min(lineNetTotalMin, kayit.getLineNetTotal());
            lineNetTotalMax = Math.max(lineNetTotalMax, kayit.getLineNetTotal());
        }

        if (lineNetTotalMin == lineNetTotalMax) {
            lineNetTotalMax = lineNetTotalMin + 1.0;
        }
    }

    private double encodeGender(String gender) {
        if ("E".equalsIgnoreCase(gender)) return 0.0;
        if ("K".equalsIgnoreCase(gender)) return 1.0;
        if ("M".equalsIgnoreCase(gender)) return 0.0;
        if ("F".equalsIgnoreCase(gender)) return 1.0;
        return UNKNOWN_GENDER;
    }

    private double encodeBrand(String brandCode) {
        if (!brandEncoder.containsKey(brandCode)) {
            return UNKNOWN_BRAND;
        }
        int index = brandEncoder.get(brandCode);
        return brandCount > 1 ? (double) index / (brandCount - 1) : 0.0;
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
