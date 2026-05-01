package data;

import model.SaleRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrainTestSplitter {

    private static final double trainratio = 0.80;

    public DataSplit split(List<SaleRecord> records, long seed) {
        List<SaleRecord> shuffled = new ArrayList<>(records);
        Collections.shuffle(shuffled, new Random(seed));

        int trainsize = (int) Math.round(shuffled.size() * trainratio);
        List<SaleRecord> trainingdata = shuffled.subList(0, trainsize);
        List<SaleRecord> testdata = shuffled.subList(trainsize, shuffled.size());

        DataSplit split = new DataSplit(new ArrayList<>(trainingdata), new ArrayList<>(testdata));

        System.out.println("[TrainTestSplitter] Training set size : " + split.getTrainingSize());
        System.out.println("[TrainTestSplitter] Test set size     : " + split.getTestSize());

        return split;
    }
}
