package classifier;

import model.ProcessedRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseClassifier implements IClassifier {

    protected int majorityVote(List<Integer> labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int label : labels) {
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }

        int bestlabel = -1;
        int bestcount = -1;

        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int label = entry.getKey();
            int count = entry.getValue();

            if (count > bestcount || (count == bestcount && label < bestlabel)) {
                bestlabel = label;
                bestcount = count;
            }
        }

        return bestlabel;
    }

    protected Map<Integer, Integer> countByClass(List<ProcessedRecord> records) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (ProcessedRecord record : records) {
            int label = record.getLabel();
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }
        return counts;
    }

    protected int majorityClass(List<ProcessedRecord> records) {
        Map<Integer, Integer> counts = countByClass(records);

        int bestlabel = -1;
        int bestcount = -1;

        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int label = entry.getKey();
            int count = entry.getValue();

            if (count > bestcount || (count == bestcount && label < bestlabel)) {
                bestlabel = label;
                bestcount = count;
            }
        }

        return bestlabel;
    }
}
