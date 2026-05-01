package evaluation;

import classifier.IClassifier;
import model.EvaluationResult;
import model.ProcessedRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Evaluator {

    public EvaluationResult evaluate(IClassifier classifier,
                                     List<ProcessedRecord> trainingdata,
                                     List<ProcessedRecord> testdata,
                                     String[] classlabels) {

        if (classlabels == null || classlabels.length == 0) {
            throw new IllegalArgumentException("At least one class label is required for evaluation.");
        }

        int classcount = classlabels.length;

        long trainstart = System.nanoTime();
        classifier.train(trainingdata);
        long trainingtimems = elapsedMillisSince(trainstart);

        int[][] confusionmatrix = new int[classcount][classcount];
        int correctcount = 0;
        int evaluatedcount = 0;
        int skippedcount = 0;

        long predictstart = System.nanoTime();

        for (ProcessedRecord testrecord : testdata) {
            int predicted = classifier.predict(testrecord);
            int actual = testrecord.getLabel();

            if (isKnownLabel(actual, classcount) && isKnownLabel(predicted, classcount)) {
                confusionmatrix[actual][predicted]++;
                evaluatedcount++;

                if (predicted == actual) {
                    correctcount++;
                }
            } else {
                skippedcount++;
            }
        }

        long predictiontimems = elapsedMillisSince(predictstart);
        double accuracy = evaluatedcount == 0 ? 0.0 : (double) correctcount / evaluatedcount;

        EvaluationResult result = new EvaluationResult(
                classifier.getName(),
                accuracy,
                confusionmatrix,
                classlabels,
                evaluatedcount,
                skippedcount,
                trainingtimems,
                predictiontimems
        );

        printSummary(result);
        return result;
    }

    private boolean isKnownLabel(int label, int classcount) {
        return label >= 0 && label < classcount;
    }

    private long elapsedMillisSince(long startnanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startnanos);
    }

    private void printSummary(EvaluationResult result) {
        StringBuilder message = new StringBuilder();
        message.append("[Evaluator] ").append(result.getClassifierName());
        message.append(" accuracy=").append(String.format("%.2f%%", result.getAccuracy() * 100));
        message.append(", training=").append(result.getTrainingTimeText());
        message.append(", prediction=").append(result.getPredictionTimeText());
        if (result.getSkippedCount() > 0) {
            message.append(", skipped=").append(result.getSkippedCount());
        }
        System.out.println(message);
    }
}
