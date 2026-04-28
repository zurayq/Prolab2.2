package data;

import model.SaleRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * TrainTestSplitter shuffles the data and splits it into training and test sets.
 *
 * Split ratio: 80% training, 20% testing.
 * The shuffle uses a fixed random seed so results are reproducible across runs.
 *
 * This is placed BEFORE preprocessing to ensure:
 *   - Training and test data are separated early
 *   - Encoders and normalization are fitted only on training data
 *   - No information from the test set leaks into training (no data leakage)
 */
public class TrainTestSplitter {

    private static final double TRAIN_RATIO = 0.80;

    /**
     * Shuffles the input list using the given seed, then splits it 80/20.
     *
     * @param records   all valid raw records
     * @param seed      random seed for reproducibility (e.g. 42)
     * @return          DataSplit containing separate train and test lists
     */
    public DataSplit split(List<SaleRecord> records, long seed) {
        // Make a copy so we don't modify the original list
        List<SaleRecord> shuffled = new ArrayList<>(records);

        // Shuffle with a fixed seed — same seed = same split every time
        Collections.shuffle(shuffled, new Random(seed));

        int trainSize = (int) Math.round(shuffled.size() * TRAIN_RATIO);

        List<SaleRecord> trainingData = shuffled.subList(0, trainSize);
        List<SaleRecord> testData     = shuffled.subList(trainSize, shuffled.size());

        // Use new ArrayList to detach from the subList view
        DataSplit split = new DataSplit(new ArrayList<>(trainingData), new ArrayList<>(testData));

        System.out.println("[TrainTestSplitter] Training set size : " + split.getTrainingSize());
        System.out.println("[TrainTestSplitter] Test set size     : " + split.getTestSize());

        return split;
    }
}
