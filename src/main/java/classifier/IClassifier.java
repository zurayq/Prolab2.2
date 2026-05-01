package classifier;

import model.ProcessedRecord;

import java.util.List;

public interface IClassifier {

    void train(List<ProcessedRecord> trainingdata);

    int predict(ProcessedRecord record);

    String getName();
}
