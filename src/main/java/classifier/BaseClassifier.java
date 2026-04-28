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
