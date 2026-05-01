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

    private static final String modelknn = "KNN";
    private static final String modeldt = "Decision Tree";
    private static final String modelcompare = "compare both";

    private JTextField filepathfield;
    private JComboBox<String> modelselector;
    private JSpinner kspinner;
    private JSpinner maxdepthspinner;
    private JSpinner seedspinner;
    private JButton browsebutton;
    private JButton loadbutton;
    private JButton runbutton;
    private JButton comparebutton;
    private ResultPanel resultpanel;

    private File loadedfile;
    private List<SaleRecord> validrecords;
    private DataSplit datasplit;
    private PreProcessor preprocessor;
    private List<ProcessedRecord> trainprocessed;
    private List<ProcessedRecord> testprocessed;
    private String[] classlabels;

    public MainFrame() {
        setTitle("KNN vs Decision Tree - Market Sales Kocaeli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));

        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel mainpanel = new JPanel(new BorderLayout(8, 8));
        mainpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainpanel.add(buildTopBar(), BorderLayout.NORTH);
        mainpanel.add(buildLeftControls(), BorderLayout.WEST);

        resultpanel = new ResultPanel();
        mainpanel.add(resultpanel, BorderLayout.CENTER);

        setContentPane(mainpanel);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("dataset file"));

        filepathfield = new JTextField("no file selected", 40);
        filepathfield.setEditable(false);
        filepathfield.setForeground(Color.GRAY);

        browsebutton = new JButton("browse...");
        browsebutton.addActionListener(this::onBrowse);

        loadbutton = new JButton("load data");
        loadbutton.setEnabled(false);
        loadbutton.addActionListener(this::onLoadData);

        JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttonpanel.add(browsebutton);
        buttonpanel.add(loadbutton);

        panel.add(filepathfield, BorderLayout.CENTER);
        panel.add(buttonpanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildLeftControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("model settings"));
        panel.setPreferredSize(new Dimension(200, 0));

        modelselector = new JComboBox<>(new String[]{modelknn, modeldt, modelcompare});
        addLabeledRow(panel, "algorithm:", modelselector);

        kspinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        addLabeledRow(panel, "K for KNN:", kspinner);

        maxdepthspinner = new JSpinner(new SpinnerNumberModel(10, 1, 30, 1));
        addLabeledRow(panel, "max depth for Decision Tree:", maxdepthspinner);

        seedspinner = new JSpinner(new SpinnerNumberModel(42, 0, 9999, 1));
        addLabeledRow(panel, "random seed:", seedspinner);

        panel.add(Box.createVerticalStrut(16));

        runbutton = new JButton("run selected model");
        runbutton.setEnabled(false);
        runbutton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runbutton.addActionListener(this::onRunModel);
        panel.add(runbutton);

        panel.add(Box.createVerticalStrut(8));

        comparebutton = new JButton("compare both");
        comparebutton.setEnabled(false);
        comparebutton.setAlignmentX(Component.CENTER_ALIGNMENT);
        comparebutton.addActionListener(this::onCompare);
        panel.add(comparebutton);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void addLabeledRow(JPanel parent, String labeltext, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(4, 4));
        row.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(new JLabel(labeltext), BorderLayout.NORTH);
        row.add(component, BorderLayout.CENTER);
        parent.add(row);
    }

    private void onBrowse(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("select Excel file (.xlsx)");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadedfile = chooser.getSelectedFile();
            filepathfield.setText(loadedfile.getAbsolutePath());
            filepathfield.setForeground(Color.BLACK);
            loadbutton.setEnabled(true);
        }
    }

    private void onLoadData(ActionEvent event) {
        if (loadedfile == null) {
            return;
        }

        setButtonsEnabled(false);
        resultpanel.setText("loading file: " + loadedfile.getName() + "\nplease wait...\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("[1/4] reading Excel file...\n");
                DataLoader loader = new DataLoader();
                List<SaleRecord> rawrecords = loader.loadFile(loadedfile);
                publish("      rows scanned : " + loader.getTotalRowsRead() + "\n");
                publish("      skipped rows : " + loader.getSkippedRows() + "\n");

                publish("[2/4] checking records...\n");
                PreProcessor validator = new PreProcessor();
                validrecords = validator.removeInvalidRecords(rawrecords);
                publish("      valid records : " + validrecords.size() + "\n");

                int seed = (Integer) seedspinner.getValue();
                publish("[3/4] splitting 80/20 with seed=" + seed + "...\n");
                TrainTestSplitter splitter = new TrainTestSplitter();
                datasplit = splitter.split(validrecords, seed);
                publish("      training size : " + datasplit.getTrainingSize() + "\n");
                publish("      test size     : " + datasplit.getTestSize() + "\n");

                publish("[4/4] fitting encoders on training data...\n");
                preprocessor = new PreProcessor();
                preprocessor.fitOnTrainingData(datasplit.getTrainingData());
                classlabels = preprocessor.getCategoryLabels();
                publish("      categories found : " + classlabels.length + "\n");
                publish("      " + String.join(" | ", classlabels) + "\n\n");

                publish("transforming training data...\n");
                trainprocessed = preprocessor.transformAll(datasplit.getTrainingData());

                publish("transforming test data...\n");
                testprocessed = preprocessor.transformAll(datasplit.getTestData());

                publish("\ndataset loaded successfully. ready to run models.\n");
                publish("\nfeatures used:\n");
                publish("  [0] GENDER        (encoded: E=0.0, K=1.0)\n");
                publish("  [1] BRAND         (label-encoded, normalized)\n");
                publish("  [2] LINENETTOTAL  (min-max normalized)\n");
                publish("\ntarget: CATEGORY_NAME1 (" + classlabels.length + " classes)\n");
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultpanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    runbutton.setEnabled(true);
                    comparebutton.setEnabled(true);
                    loadbutton.setEnabled(true);
                    browsebutton.setEnabled(true);
                } catch (Exception ex) {
                    resultpanel.appendText("\nerror loading file: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                    loadbutton.setEnabled(true);
                    browsebutton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void onRunModel(ActionEvent event) {
        if (!isDataReady()) {
            JOptionPane.showMessageDialog(this, "please load a dataset first.", "no dataset", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedmodel = (String) modelselector.getSelectedItem();
        if (modelcompare.equals(selectedmodel)) {
            onCompare(event);
            return;
        }

        setButtonsEnabled(false);
        resultpanel.setText("running selected model...\n\n");

        SwingWorker<EvaluationResult, String> worker = new SwingWorker<>() {
            @Override
            protected EvaluationResult doInBackground() {
                IClassifier classifier = buildSelectedClassifier(selectedmodel);
                publish("running: " + classifier.getName() + "\n\n");

                Evaluator evaluator = new Evaluator();
                return evaluator.evaluate(classifier, trainprocessed, testprocessed, classlabels);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultpanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    EvaluationResult result = get();
                    displayResult(result);

                    List<EvaluationResult> singleresult = new ArrayList<>();
                    singleresult.add(result);
                    resultpanel.showChart(singleresult);
                } catch (Exception ex) {
                    resultpanel.appendText("error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    private void onCompare(ActionEvent event) {
        if (!isDataReady()) {
            JOptionPane.showMessageDialog(this, "please load a dataset first.", "no dataset", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setButtonsEnabled(false);
        resultpanel.setText("running comparison...\n\n");

        SwingWorker<List<EvaluationResult>, String> worker = new SwingWorker<>() {
            @Override
            protected List<EvaluationResult> doInBackground() {
                Evaluator evaluator = new Evaluator();
                List<EvaluationResult> results = new ArrayList<>();

                int k = (Integer) kspinner.getValue();
                int maxdepth = (Integer) maxdepthspinner.getValue();

                List<IClassifier> classifiers = new ArrayList<>();
                classifiers.add(new KNNClassifier(k));
                classifiers.add(new DecisionTreeClassifier(maxdepth));

                for (IClassifier classifier : classifiers) {
                    publish("running: " + classifier.getName() + "\n");
                    results.add(evaluator.evaluate(classifier, trainprocessed, testprocessed, classlabels));
                }

                return results;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultpanel.appendText(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    List<EvaluationResult> results = get();
                    resultpanel.setText("comparison results\n\n");
                    for (EvaluationResult result : results) {
                        resultpanel.appendText(result.getSummary() + "\n");
                    }

                    appendComparisonConclusion(results);
                    resultpanel.showConfusionMatrix(results.get(results.size() - 1));
                    resultpanel.showChart(results);
                } catch (Exception ex) {
                    resultpanel.appendText("error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    private boolean isDataReady() {
        return trainprocessed != null && testprocessed != null;
    }

    private IClassifier buildSelectedClassifier(String modelname) {
        int k = (Integer) kspinner.getValue();
        int maxdepth = (Integer) maxdepthspinner.getValue();

        if (modelknn.equals(modelname)) {
            return new KNNClassifier(k);
        }
        return new DecisionTreeClassifier(maxdepth);
    }

    private void displayResult(EvaluationResult result) {
        StringBuilder text = new StringBuilder();
        text.append(result.getSummary()).append("\n");
        text.append("correct predictions : ").append(countCorrect(result)).append("\n");
        text.append("evaluated test rows : ").append(result.getEvaluatedCount()).append("\n");
        text.append("total test rows     : ").append(result.getTotalTestCount()).append("\n");

        if (result.getSkippedCount() > 0) {
            text.append("skipped test rows   : ").append(result.getSkippedCount())
                    .append(" (class not present in training data)\n");
        }

        resultpanel.setText(text.toString());
        resultpanel.showConfusionMatrix(result);
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

        EvaluationResult knnresult = results.get(0);
        EvaluationResult decisiontreeresult = results.get(1);

        resultpanel.appendText("quick result\n");
        if (knnresult.getAccuracy() > decisiontreeresult.getAccuracy()) {
            resultpanel.appendText("KNN had higher accuracy on this dataset.\n");
        } else if (decisiontreeresult.getAccuracy() > knnresult.getAccuracy()) {
            resultpanel.appendText("Decision Tree had higher accuracy on this dataset.\n");
        } else {
            resultpanel.appendText("both models had the same accuracy.\n");
        }

        if (knnresult.getPredictionTimeMs() > decisiontreeresult.getPredictionTimeMs()) {
            resultpanel.appendText("Decision Tree was faster at prediction time.\n");
        } else if (decisiontreeresult.getPredictionTimeMs() > knnresult.getPredictionTimeMs()) {
            resultpanel.appendText("KNN was faster at prediction time for this run.\n");
        } else {
            resultpanel.appendText("both models had the same measured prediction time.\n");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        runbutton.setEnabled(enabled);
        comparebutton.setEnabled(enabled);
        browsebutton.setEnabled(enabled);
        loadbutton.setEnabled(enabled);
    }
}
