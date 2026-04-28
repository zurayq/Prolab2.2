package classifier;

import model.ProcessedRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseClassifier is an abstract class that provides shared utility methods
 * used by both KNN and Decision Tree.
 *
 * It implements the IClassifier interface but leaves train() and predict()
 * abstract — each subclass defines those independently.
 *
 * The main shared utility is majorityVote(), which both classifiers
 * use to determine the winning class from a collection of labels.
 *
 * Using abstract class here demonstrates:
 *   - inheritance: KNNClassifier and DecisionTreeClassifier extend this
 *   - code reuse: majorityVote() is written once, used twice
 *   - partial implementation: getName() stays abstract, forcing subclasses to name themselves
 */
public abstract class BaseClassifier implements IClassifier {

    /**
     * Given a list of class labels, returns the one that appears most often.
     * In case of a tie, the smaller class index wins (deterministic tie-breaking).
     *
     * @param labels  list of integer class labels to vote on
     * @return        the majority label
     */
    protected int majorityVote(List<Integer> labels) {
        // Count how many times each label appears
        Map<Integer, Integer> counts = new HashMap<>();
        for (int label : labels) {
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }

        // Find the label with the highest count
        int bestLabel = -1;
        int bestCount = -1;

        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int label = entry.getKey();
            int count = entry.getValue();

            if (count > bestCount || (count == bestCount && label < bestLabel)) {
                bestLabel = label;
                bestCount = count;
            }
        }

        return bestLabel;
    }

    /**
     * Counts how many records in the list belong to each class.
     * Returns a map of class index → count.
     */
    protected Map<Integer, Integer> countByClass(List<ProcessedRecord> records) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (ProcessedRecord r : records) {
            int label = r.getLabel();
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }
        return counts;
    }

    /**
     * Returns the most common class label in the given list.
     * Used for decision tree leaf creation.
     */
    protected int majorityClass(List<ProcessedRecord> records) {
        Map<Integer, Integer> counts = countByClass(records);

        int bestLabel = -1;
        int bestCount = -1;

        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int label = entry.getKey();
            int count = entry.getValue();
            if (count > bestCount || (count == bestCount && label < bestLabel)) {
                bestLabel = label;
                bestCount = count;
            }
        }

        return bestLabel;
    }
}
