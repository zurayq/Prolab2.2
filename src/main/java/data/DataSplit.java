package data;

import model.SaleRecord;

import java.util.List;

/**
 * DataSplit is a simple container that holds the result of a train/test split.
 *
 * It holds two separate lists:
 *   - trainingData : 80% of the records, used to train and fit the models
 *   - testData     : 20% of the records, used only for evaluation
 */
public class DataSplit {

    private final List<SaleRecord> trainingData;
    private final List<SaleRecord> testData;

    public DataSplit(List<SaleRecord> trainingData, List<SaleRecord> testData) {
        this.trainingData = trainingData;
        this.testData     = testData;
    }

    public List<SaleRecord> getTrainingData() { return trainingData; }
    public List<SaleRecord> getTestData()     { return testData; }

    public int getTrainingSize() { return trainingData.size(); }
    public int getTestSize()     { return testData.size(); }
}
