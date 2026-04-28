package evaluation;

import classifier.IClassifier;
import model.EvaluationResult;
import model.ProcessedRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Trains a classifier, predicts the test records, and computes metrics.
 */
public class Evaluator {

    public EvaluationResult evaluate(IClassifier classifier,
                                     List<ProcessedRecord> trainingData,
                                     List<ProcessedRecord> testData,
                                     String[] classLabels) {

        if (classLabels == null || classLabels.length == 0) {
            throw new IllegalArgumentException("At least one class label is required for evaluation.");
        }

        int numberOfClasses = classLabels.length;

        long trainStart = System.nanoTime();
        classifier.train(trainingData);
        long trainingTimeMs = elapsedMillisSince(trainStart);

        int[][] confusionMatrix = new int[numberOfClasses][numberOfClasses];
        int correctCount = 0;
        int evaluatedCount = 0;
        int skippedCount = 0;

        long predictStart = System.nanoTime();

        for (ProcessedRecord testRecord : testData) {
            int predicted = classifier.predict(testRecord);
            int actual = testRecord.getLabel();

            if (isKnownLabel(actual, numberOfClasses) && isKnownLabel(predicted, numberOfClasses)) {
                confusionMatrix[actual][predicted]++;
                evaluatedCount++;

                if (predicted == actual) {
                    correctCount++;
                }
            } else {
                skippedCount++;
            }
        }

        long predictionTimeMs = elapsedMillisSince(predictStart);
        double accuracy = evaluatedCount == 0 ? 0.0 : (double) correctCount / evaluatedCount;

        EvaluationResult result = new EvaluationResult(
                classifier.getName(),
                accuracy,
                confusionMatrix,
                classLabels,
                evaluatedCount,
                skippedCount,
                trainingTimeMs,
                predictionTimeMs
        );

        printSummary(result);
        return result;
    }

    private boolean isKnownLabel(int label, int numberOfClasses) {
        return label >= 0 && label < numberOfClasses;
    }

    private long elapsedMillisSince(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private void printSummary(EvaluationResult result) {
        StringBuilder message = new StringBuilder();
        message.append("[Evaluator] ").append(result.getClassifierName());
        message.append(" -> Accuracy=").append(String.format("%.2f%%", result.getAccuracy() * 100));
        message.append(", Train=").append(result.getTrainingTimeText());
        message.append(", Predict=").append(result.getPredictionTimeText());
        if (result.getSkippedCount() > 0) {
            message.append(", Skipped=").append(result.getSkippedCount());
        }
        System.out.println(message);
    }
}
