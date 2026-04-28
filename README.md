# KNN vs Decision Tree Sales Classifier

This is a Programming Laboratory II project demonstrating a side-by-side comparison of K-Nearest Neighbors (KNN) and Decision Tree classifiers implemented completely from scratch in Java.

## How to Build and Run

**Prerequisites:** Java Development Kit (JDK) 11+.

1. Open a terminal in the project directory.
2. Run the build script:
   - On Windows: `build.bat`
   - *(The script will automatically compile the Java files and package them into `out\prolab2.jar`)*
3. Run the application:
   - On Windows: `run.bat`
   - Or manually: `java -jar out\prolab2.jar`

## Testing Checklist

1. **Launch App**: Verify the Swing window opens.
2. **Load Data**: Click "Browse," select the provided `.xlsx` file, and click "Load Data". You should see the progress logs and row counts.
3. **Run KNN**: Select "KNN", set K (e.g., 5), click Run. Check accuracy and confusion matrix.
4. **Run DT**: Select "Decision Tree", set Max Depth (e.g., 10), click Run. Evaluate accuracy and confusion matrix.
5. **Compare Both**: Click "Compare Both" to see the side-by-side performance chart. Notice how KNN might take longer to predict (lazy learning) while Decision Tree is faster at predicting but does its heavy lifting during tree building.

---

## 1. Project Architecture

The codebase strictly follows standard Object-Oriented Programming (OOP) principles, avoiding 'spaghetti code' while remaining readable for presentation.

**Key Packages:**
*   `model`: Encapsulates data (e.g., `SaleRecord` for raw Excel rows, `ProcessedRecord` for normalized numeric vectors, `DecisionTreeNode`).
*   `data`: Handles data ingestion (`DataLoader` using Apache POI, the only allowed third-party library) and preprocessing (`PreProcessor` for Min-Max normalization and label encoding). `TrainTestSplitter` ensures data is randomized before fitting.
*   `classifier`: Core implementations. Includes an `IClassifier` interface ensuring polymorphism, a `BaseClassifier` for shared utilities (e.g., majority votes), and the `KNNClassifier` and `DecisionTreeClassifier` implementations.
*   `evaluation`: Contains the `Evaluator` class, which calculates accuracy, timings, and custom JTable-ready confusion matrices.
*   `ui`: Swing frontend with dynamic chart rendering.

---

## 2. Design Decisions

| Decision | Rationale |
| :--- | :--- |
| **Apache POI Library** | Used *only* for reading `.xlsx` safely. No machine learning libraries (like Weka or Smile) were used. |
| **Target Column: CATEGORY_NAME1** | This top-level category provides distinct, clear groups (e.g., GIDA, KOZMETİK) rather than overly sparse deep hierarchies (CATEGORY_NAME3). |
| **Variables Used** | Evaluated on `GENDER` (categorical), `BRAND` (categorical), `AMOUNT` (numeric), `LINENET` (numeric). |
| **Variables Excluded** | Identifiers (`ID`, `CLIENTCODE`, `FICHENO`) were excluded because they don't hold behavioral signals and would cause overfitting/noise. |
| **Train/Test Fit Safety** | Min-max normalization stats and label encoders are fitted *after* the split and *only* on the training data. This prevents data leakage. |
| **Decision Tree Metric** | We manually compute **Gini Impurity** to evaluate splits. It is slightly cheaper to calculate than Information Gain (entropy) and straightforward to defend. |
| **KNN Distance Metric** | Normalized Euclidean distance. Categorical traits (Gender, Brand) contribute robustly (0 if match, 1 if mismatch) since the other numeric features are scaled between [0.0 - 1.0]. |

---

## 3. Professor Defense Notes (Q&A)

### Q1: Where are the algorithms? Did you use Scikit-Learn or Weka?
**A:** Everything is coded from scratch in the `classifier` package. The only library used is Apache POI, which is specifically for extracting data from the Excel file in `DataLoader.java`. You can inspect `KNNClassifier.predict()` and `DecisionTreeClassifier.buildTree()` to see the manual mathematics.

### Q2: How does your Decision Tree decide where to split?
**A:** It computes the **Gini Impurity**. At each node, `findBestSplit()` tests every possible unique threshold for every feature. It calculates the weighted Gini impurity size of the left and right splits. The split that produces the lowest Gini score (the purest split) is chosen.

### Q3: Why is KNN predicting slowly compared to the Decision Tree?
**A:** This visibly demonstrates the nature of KNN as a "lazy learner." Decision Tree does all its heavy lifting during training (building the tree), so predictions just follow an `if/else` path. KNN has zero training time, but for every single test row, it must calculate the distance against *every* training record in memory, resulting in high runtime costs.

### Q4: How is Object-Oriented design utilized here?
**A:** 
- **Encapsulation:** All fields in `SaleRecord` and `ProcessedRecord` are private and configured via constructors.
- **Interfaces / Polymorphism:** Our testing suite (`Evaluator.java`) only talks to the `IClassifier` interface. It doesn't know if it's evaluating a KNN or Decision Tree, making it easily extendable.
- **Inheritance:** We have a class `BaseClassifier` that implements common logic (like `majorityVote()`) to keep the code DRY, and both specific classifiers extend it.

### Q5: How did you handle category/string variables in algorithms that require numbers?
**A:** In the `PreProcessor`, we map categories (like `GENDER=E/K`) to integers (`E=0, K=1`). For Decision Tree, this acts as a valid threshold. For KNN, because doing continuous math on categories is wrong, we measure distance as `0` if the categories match and `1` if they differ.

### Q6: Does your model suffer from Data Leakage?
**A:** No. `TrainTestSplitter` isolates the 20% test data first. `PreProcessor.fitOnTrainingData()` establishes the min/max bounds and label mappings using *only* the 80% training data. The scale is then applied to the test data. If we normalized looking at the whole dataset first, we would be cheating.

---

## 4. Report Writing Guide

When translating this into your final PDF report:

- **Introduction**: Introduce K-nearest neighbors and Decision Trees. State that the goal is implementing these from scratch in Java on a real-world supermarket dataset.
- **Data Set characteristics**: Mention there are 600,000+ rows, but to handle missing values, we filter rows needing complete demographics (Gender, Brand, Amount, LineNet). This leaves ~4,800 pristine records which prevents algorithm crashes.
- **Implementation (OOP)**: Reiterate the architecture (model, data, classifier packages). Provide a UML class diagram describing `IClassifier` -> `BaseClassifier` -> `KNNClassifier`/`DecisionTreeClassifier`.
- **Results**: Take screenshots of the completed UI chart after running "Compare Both". Discuss test-accuracy differences and runtime cost comparisons (KNN = slow prediction, DT = fast prediction).
