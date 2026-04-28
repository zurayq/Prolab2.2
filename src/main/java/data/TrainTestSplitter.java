package data;

import model.SaleRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrainTestSplitter {

    private static final double TRAIN_RATIO = 0.80;

    public DataSplit split(List<SaleRecord> records, long seed) {
        List<SaleRecord> shuffled = new ArrayList<>(records);
        Collections.shuffle(shuffled, new Random(seed));

        int trainSize = (int) Math.round(shuffled.size() * TRAIN_RATIO);
        List<SaleRecord> trainingData = shuffled.subList(0, trainSize);
        List<SaleRecord> testData = shuffled.subList(trainSize, shuffled.size());

        DataSplit split = new DataSplit(new ArrayList<>(trainingData), new ArrayList<>(testData));

        System.out.println("[TrainTestSplitter] Training set size : " + split.getTrainingSize());
        System.out.println("[TrainTestSplitter] Test set size     : " + split.getTestSize());

        return split;
    }
}
