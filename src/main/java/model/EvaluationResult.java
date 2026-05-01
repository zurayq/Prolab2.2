package model;

public class EvaluationResult {

    private final String classifiername;
    private final double accuracy;
    private final int[][] confusionmatrix;
    private final String[] classlabels;
    private final int evaluatedcount;
    private final int skippedcount;
    private final long trainingtimems;
    private final long predictiontimems;

    public EvaluationResult(String classifiername,
                            double accuracy,
                            int[][] confusionmatrix,
                            String[] classlabels,
                            int evaluatedcount,
                            int skippedcount,
                            long trainingtimems,
                            long predictiontimems) {
        this.classifiername = classifiername;
        this.accuracy = accuracy;
        this.confusionmatrix = confusionmatrix;
        this.classlabels = classlabels;
        this.evaluatedcount = evaluatedcount;
        this.skippedcount = skippedcount;
        this.trainingtimems = trainingtimems;
        this.predictiontimems = predictiontimems;
    }

    public String getClassifierName() {
        return classifiername;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int[][] getConfusionMatrix() {
        return confusionmatrix;
    }

    public String[] getClassLabels() {
        return classlabels;
    }

    public int getEvaluatedCount() {
        return evaluatedcount;
    }

    public int getSkippedCount() {
        return skippedcount;
    }

    public int getTotalTestCount() {
        return evaluatedcount + skippedcount;
    }

    public long getTrainingTimeMs() {
        return trainingtimems;
    }

    public long getPredictionTimeMs() {
        return predictiontimems;
    }

    public String getTrainingTimeText() {
        return formatElapsedTime(trainingtimems);
    }

    public String getPredictionTimeText() {
        return formatElapsedTime(predictiontimems);
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(classifiername).append("\n");
        summary.append(String.format("accuracy        : %.2f%%\n", accuracy * 100));
        summary.append("training time   : ").append(getTrainingTimeText()).append("\n");
        summary.append("prediction time : ").append(getPredictionTimeText()).append("\n");
        if (skippedcount > 0) {
            summary.append("skipped records : ").append(skippedcount).append(" (unknown class labels)\n");
        }
        return summary.toString();
    }

    public static String formatElapsedTime(long timems) {
        if (timems <= 0) {
            return "< 1 ms";
        }
        return timems + " ms";
    }
}
