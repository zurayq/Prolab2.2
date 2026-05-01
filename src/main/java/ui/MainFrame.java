package ui;

import classifier.DecisionTreeClassifier;
import classifier.IClassifier;
import classifier.KNNClassifier;
import data.DataLoader;
import data.DataSplit;
import data.PreProcessor;
import data.TrainTestSplitter;
import evaluation.Evaluator;
import model.EvaluationResult;
import model.ProcessedRecord;
import model.SaleRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String MODEL_KNN = "KNN";
    private static final String MODEL_DT = "Decision Tree";
    private static final String MODEL_COMPARE = "Compare Both";

    private JTextField filePathField;
    private JComboBox<String> modelSelector;
    private JSpinner kSpinner;
    private JSpinner maxDepthSpinner;
    private JSpinner seedSpinner;
    private JButton browseButton;
    private JButton loadButton;
    private JButton runButton;
    private JButton compareButton;
    private ResultPanel resultPanel;

    private File loadedFile;
    private List<SaleRecord> validRecords;
    private DataSplit dataSplit;
    private PreProcessor preProcessor;
    private List<ProcessedRecord> trainProcessed;
    private List<ProcessedRecord> testProcessed;
    private String[] classLabels;

    public MainFrame() {
        setTitle("KNN vs Decision Tree - Market Sales Kocaeli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));

        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(buildTopBar(), BorderLayout.NORTH);
        mainPanel.add(buildLeftControls(), BorderLayout.WEST);

        resultPanel = new ResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Dataset File"));

        filePathField = new JTextField("No file selected", 40);
        filePathField.setEditable(false);
        filePathField.setForeground(Color.GRAY);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(this::onBrowse);

        loadButton = new JButton("Load Data");
        loadButton.setEnabled(false);
        loadButton.addActionListener(this::onLoadData);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttonPanel.add(browseButton);
        buttonPanel.add(loadButton);

        panel.add(filePathField, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildLeftControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Model Settings"));
        panel.setPreferredSize(new Dimension(200, 0));

        modelSelector = new JComboBox<>(new String[]{MODEL_KNN, MODEL_DT, MODEL_COMPARE});
        addLabeledRow(panel, "Algorithm:", modelSelector);

        kSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        addLabeledRow(panel, "K (for KNN):", kSpinner);

        maxDepthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 30, 1));
        addLabeledRow(panel, "Max Depth (DT):", maxDepthSpinner);

        seedSpinner = new JSpinner(new SpinnerNumberModel(42, 0, 9999, 1));
        addLabeledRow(panel, "Random Seed:", seedSpinner);

        panel.add(Box.createVerticalStrut(16));

        runButton = new JButton("Run Selected Model");
        runButton.setEnabled(false);
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.addActionListener(this::onRunModel);
        panel.add(runButton);

        panel.add(Box.createVerticalStrut(8));

        compareButton = new JButton("Compare Both");
        compareButton.setEnabled(false);
        compareButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        compareButton.addActionListener(this::onCompare);
        panel.add(compareButton);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void addLabeledRow(JPanel parent, String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(4, 4));
        row.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(new JLabel(labelText), BorderLayout.NORTH);
        row.add(component, BorderLayout.CENTER);
        parent.add(row);
    }

    private void onBrowse(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Excel File (.xlsx)");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadedFile = chooser.getSelectedFile();
            filePathField.setText(loadedFile.getAbsolutePath());
            filePathField.setForeground(Color.BLACK);
            loadButton.setEnabled(true);
        }
    }

    private void onLoadData(ActionEvent event) {
        if (loadedFile == null) {
            return;
        }

        setButtonsEnabled(false);
        resultPanel.setText("Loading file: " + loadedFile.getName() + "\nPlease wait...\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("[1/4] Reading Excel file...\n");
                DataLoader loader = new DataLoader();
                List<SaleRecord> rawRecords = loader.loadFile(loadedFile);
                publish("      Total rows scanned : " + loader.getTotalRowsRead() + "\n");
                publish("      Skipped rows       : " + loader.getSkippedRows() + "\n");

                publish("[2/4] Validating records...\n");
                PreProcessor validator = new PreProcessor();
                validRecords = validator.removeInvalidRecords(rawRecords);
                publish("      Valid records      : " + validRecords.size() + "\n");

                int seed = (Integer) seedSpinner.getValue();
                publish("[3/4] Splitting 80/20 with seed=" + seed + "...\n");
                TrainTestSplitter splitter = new TrainTestSplitter();
                dataSplit = splitter.split(validRecords, seed);
                publish("      Training set size  : " + dataSplit.getTrainingSize() + "\n");
                publish("      Test set size      : " + dataSplit.getTestSize() + "\n");

                publish("[4/4] Fitting encoders on training data only...\n");
                preProcessor = new PreProcessor();
                preProcessor.fitOnTrainingData(dataSplit.getTrainingData());
                classLabels = preProcessor.getCategoryLabels();
                publish("      Categories found   : " + classLabels.length + "\n");
                publish("      " + String.join(" | ", classLabels) + "\n\n");

                publish("Transforming training data...\n");
                trainProcessed = preProcessor.transformAll(dataSplit.getTrainingData());

                publish("Transforming test data...\n");
                testProcessed = preProcessor.transformAll(dataSplit.getTestData());

                publish("\nOK - Dataset loaded successfully. Ready to run models.\n");
                publish("\nFeatures used:\n");
                publish("  [0] GENDER        (encoded: E=0.0, K=1.0)\n");
                publish("  [1] BRAND         (label-encoded, normalized)\n");
                publish("  [2] LINENETTOTAL  (min-max normalized)\n");
                publish("\nTarget: CATEGORY_NAME1 (" + classLabels.length + " classes)\n");
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultPanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    runButton.setEnabled(true);
                    compareButton.setEnabled(true);
                    loadButton.setEnabled(true);
                    browseButton.setEnabled(true);
                } catch (Exception ex) {
                    resultPanel.appendText("\nError loading file: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                    loadButton.setEnabled(true);
                    browseButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void onRunModel(ActionEvent event) {
        if (!isDataReady()) {
            JOptionPane.showMessageDialog(this, "Please load a dataset first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedModel = (String) modelSelector.getSelectedItem();
        if (MODEL_COMPARE.equals(selectedModel)) {
            onCompare(event);
            return;
        }

        setButtonsEnabled(false);
        resultPanel.setText("Running selected model...\n\n");

        SwingWorker<EvaluationResult, String> worker = new SwingWorker<>() {
            @Override
            protected EvaluationResult doInBackground() {
                IClassifier classifier = buildSelectedClassifier(selectedModel);
                publish("Running: " + classifier.getName() + "\n\n");

                Evaluator evaluator = new Evaluator();
                return evaluator.evaluate(classifier, trainProcessed, testProcessed, classLabels);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultPanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    EvaluationResult result = get();
                    displayResult(result);

                    List<EvaluationResult> singleResult = new ArrayList<>();
                    singleResult.add(result);
                    resultPanel.showChart(singleResult);
                } catch (Exception ex) {
                    resultPanel.appendText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    private void onCompare(ActionEvent event) {
        if (!isDataReady()) {
            JOptionPane.showMessageDialog(this, "Please load a dataset first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setButtonsEnabled(false);
        resultPanel.setText("Running comparison...\n\n");

        SwingWorker<List<EvaluationResult>, String> worker = new SwingWorker<>() {
            @Override
            protected List<EvaluationResult> doInBackground() {
                Evaluator evaluator = new Evaluator();
                List<EvaluationResult> results = new ArrayList<>();

                int k = (Integer) kSpinner.getValue();
                int maxDepth = (Integer) maxDepthSpinner.getValue();

                publish("Running KNN (k=" + k + ")...\n");
                KNNClassifier knn = new KNNClassifier(k);
                results.add(evaluator.evaluate(knn, trainProcessed, testProcessed, classLabels));

                publish("Running Decision Tree (maxDepth=" + maxDepth + ")...\n");
                DecisionTreeClassifier decisionTree = new DecisionTreeClassifier(maxDepth);
                results.add(evaluator.evaluate(decisionTree, trainProcessed, testProcessed, classLabels));

                return results;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultPanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    List<EvaluationResult> results = get();
                    resultPanel.setText("=== COMPARISON RESULTS ===\n\n");
                    for (EvaluationResult result : results) {
                        resultPanel.appendText(result.getSummary() + "\n");
                    }

                    appendComparisonConclusion(results);
                    resultPanel.showConfusionMatrix(results.get(results.size() - 1));
                    resultPanel.showChart(results);
                } catch (Exception ex) {
                    resultPanel.appendText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    private boolean isDataReady() {
        return trainProcessed != null && testProcessed != null;
    }

    private IClassifier buildSelectedClassifier(String modelName) {
        int k = (Integer) kSpinner.getValue();
        int maxDepth = (Integer) maxDepthSpinner.getValue();

        if (MODEL_KNN.equals(modelName)) {
            return new KNNClassifier(k);
        }
        return new DecisionTreeClassifier(maxDepth);
    }

    private void displayResult(EvaluationResult result) {
        StringBuilder text = new StringBuilder();
        text.append(result.getSummary()).append("\n");
        text.append("Correctly classified : ").append(countCorrect(result)).append("\n");
        text.append("Evaluated test rows  : ").append(result.getEvaluatedCount()).append("\n");
        text.append("Total test rows      : ").append(result.getTotalTestCount()).append("\n");

        if (result.getSkippedCount() > 0) {
            text.append("Skipped test rows    : ").append(result.getSkippedCount())
                    .append(" (class not present in training data)\n");
        }

        resultPanel.setText(text.toString());
        resultPanel.showConfusionMatrix(result);
    }

    private int countCorrect(EvaluationResult result) {
        int[][] matrix = result.getConfusionMatrix();
        int correct = 0;
        for (int i = 0; i < matrix.length; i++) {
            correct += matrix[i][i];
        }
        return correct;
    }

    private void appendComparisonConclusion(List<EvaluationResult> results) {
        if (results.size() < 2) {
            return;
        }

        EvaluationResult knnResult = results.get(0);
        EvaluationResult decisionTreeResult = results.get(1);

        resultPanel.appendText("--- Analysis ---\n");
        if (knnResult.getAccuracy() > decisionTreeResult.getAccuracy()) {
            resultPanel.appendText("-> KNN achieved higher accuracy on this dataset.\n");
        } else if (decisionTreeResult.getAccuracy() > knnResult.getAccuracy()) {
            resultPanel.appendText("-> Decision Tree achieved higher accuracy on this dataset.\n");
        } else {
            resultPanel.appendText("-> Both models achieved the same accuracy.\n");
        }

        if (knnResult.getPredictionTimeMs() > decisionTreeResult.getPredictionTimeMs()) {
            resultPanel.appendText("-> Decision Tree was faster at prediction time.\n");
        } else if (decisionTreeResult.getPredictionTimeMs() > knnResult.getPredictionTimeMs()) {
            resultPanel.appendText("-> KNN was faster at prediction time for this run.\n");
        } else {
            resultPanel.appendText("-> Both models had the same measured prediction time.\n");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
        compareButton.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        loadButton.setEnabled(enabled);
    }
}
