package model;

public class EvaluationResult {

    private final String classifierName;
    private final double accuracy;
    private final int[][] confusionMatrix;
    private final String[] classLabels;
    private final int evaluatedCount;
    private final int skippedCount;
    private final long trainingTimeMs;
    private final long predictionTimeMs;

    public EvaluationResult(String classifierName,
                            double accuracy,
                            int[][] confusionMatrix,
                            String[] classLabels,
                            int evaluatedCount,
                            int skippedCount,
                            long trainingTimeMs,
                            long predictionTimeMs) {
        this.classifierName = classifierName;
        this.accuracy = accuracy;
        this.confusionMatrix = confusionMatrix;
        this.classLabels = classLabels;
        this.evaluatedCount = evaluatedCount;
        this.skippedCount = skippedCount;
        this.trainingTimeMs = trainingTimeMs;
        this.predictionTimeMs = predictionTimeMs;
    }
    public String getClassifierName() {
        return classifierName;
    }
    public double getAccuracy() {
        return accuracy;
    }
    public int[][] getConfusionMatrix() {
        return confusionMatrix;
    }
    public String[] getClassLabels() {
        return classLabels;
    }
    public int getEvaluatedCount() {
        return evaluatedCount;
    }
    public int getSkippedCount() {
        return skippedCount;
    }
    public int getTotalTestCount() {
        return evaluatedCount + skippedCount;
    }
    public long getTrainingTimeMs() {
        return trainingTimeMs;
    }
    public long getPredictionTimeMs() {
        return predictionTimeMs;
    }

    public String getTrainingTimeText() {
        return formatElapsedTime(trainingTimeMs);
    }
    public String getPredictionTimeText() {
        return formatElapsedTime(predictionTimeMs);
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(classifierName).append("\n");
        summary.append(String.format("accuracy        : %.2f%%\n", accuracy * 100));
        summary.append("training time   : ").append(getTrainingTimeText()).append("\n");
        summary.append("prediction time : ").append(getPredictionTimeText()).append("\n");
        if (skippedCount > 0) {
            summary.append("skipped records : ").append(skippedCount).append(" (unknown class labels)\n");
        }
        return summary.toString();
    }
    public static String formatElapsedTime(long timeMs) {
        if (timeMs <= 0) {
            return "< 1 ms";
        }
        return timeMs + " ms";
    }
}
