package data;

import model.SaleRecord;

import java.util.List;

public class DataSplit {

    private final List<SaleRecord> trainingData;
    private final List<SaleRecord> testData;

    public DataSplit(List<SaleRecord> trainingData, List<SaleRecord> testData) {
        this.trainingData = trainingData;
        this.testData = testData;
    }

    public List<SaleRecord> getTrainingData() {
        return trainingData;
    }

    public List<SaleRecord> getTestData() {
        return testData;
    }

    public int getTrainingSize() {
        return trainingData.size();
    }

    public int getTestSize() {
        return testData.size();
    }
}
