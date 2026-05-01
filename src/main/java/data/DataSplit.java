package data;

import model.SaleRecord;

import java.util.List;

public class DataSplit {

    private final List<SaleRecord> trainingdata;
    private final List<SaleRecord> testdata;

    public DataSplit(List<SaleRecord> trainingdata, List<SaleRecord> testdata) {
        this.trainingdata = trainingdata;
        this.testdata = testdata;
    }

    public List<SaleRecord> getTrainingData() {
        return trainingdata;
    }

    public List<SaleRecord> getTestData() {
        return testdata;
    }

    public int getTrainingSize() {
        return trainingdata.size();
    }

    public int getTestSize() {
        return testdata.size();
    }
}
