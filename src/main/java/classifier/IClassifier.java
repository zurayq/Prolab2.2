package classifier;

import model.ProcessedRecord;
import java.util.List;

/**
 * IClassifier is the common interface that both KNN and Decision Tree implement.
 *
 * This is the core OOP interface that provides:
 *   - polymorphism: the Evaluator can work with any classifier through this interface
 *   - a clear contract: every classifier must train, predict, and identify itself
 *
 * By programming against this interface, we can easily swap algorithms,
 * add new ones, or compare them without changing the evaluation code.
 */
public interface IClassifier {

    /**
     * Train the classifier on the given list of processed training records.
     * After this call, the classifier is ready to make predictions.
     *
     * @param trainingData list of encoded, normalized records for training
     */
    void train(List<ProcessedRecord> trainingData);

    /**
     * Predict the class label for a single record.
     * Returns the predicted class as an integer index.
     *
     * @param record  the encoded, normalized record to classify
     * @return        predicted class index
     */
    int predict(ProcessedRecord record);

    /**
     * Returns a human-readable name for this classifier (e.g., "KNN" or "Decision Tree").
     * Used for display in the UI and evaluation reports.
     */
    String getName();
}
