package data;

import model.SaleRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the Excel spreadsheet and converts valid rows into SaleRecord objects.
 * This class only handles file reading and row parsing.
 */
public class DataLoader {

    private static final int COL_CLIENTCODE = 9;
    private static final int COL_GENDER = 17;
    private static final int COL_BRAND = 11;
    private static final int COL_LINENET = 8;
    private static final int COL_AMOUNT = 5;
    private static final int COL_CATEGORY1 = 12;

    private static final int MIN_NON_NULL_CELLS = 5;
    private static final String UNKNOWN_BRAND = "UNKNOWN";

    private int totalRowsRead = 0;
    private int skippedRows = 0;
    private int loadedRows = 0;

    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     * Loads the first sheet of the Excel file.
     * Blank rows and rows missing required fields are skipped.
     */
    public List<SaleRecord> loadFile(File file) throws Exception {
        List<SaleRecord> records = new ArrayList<>();

        totalRowsRead = 0;
        skippedRows = 0;
        loadedRows = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Row 0 is the header row.
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                totalRowsRead++;

                if (shouldSkipRow(row)) {
                    skippedRows++;
                    continue;
                }

                SaleRecord record = parseRow(row);
                if (record == null) {
                    skippedRows++;
                    continue;
                }

                records.add(record);
                loadedRows++;
            }
        }

        System.out.println("[DataLoader] Total rows scanned : " + totalRowsRead);
        System.out.println("[DataLoader] Skipped rows       : " + skippedRows);
        System.out.println("[DataLoader] Loaded records     : " + loadedRows);

        return records;
    }

    private SaleRecord parseRow(Row row) {
        try {
            String gender = getCellAsString(row, COL_GENDER);
            String brand = getCellAsString(row, COL_BRAND);
            String clientCode = getCellAsString(row, COL_CLIENTCODE);
            String category1 = getCellAsString(row, COL_CATEGORY1);

            if (isBlank(gender) || isBlank(category1)) {
                return null;
            }
            if (isBlank(brand)) {
                brand = UNKNOWN_BRAND;
            }

            double lineNet = getCellAsDouble(row, COL_LINENET);
            double amount = getCellAsDouble(row, COL_AMOUNT);
            if (Double.isNaN(lineNet) || Double.isNaN(amount)) {
                return null;
            }

            return new SaleRecord(clientCode, gender, brand, lineNet, amount, category1);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldSkipRow(Row row) {
        return row == null || countNonBlankCells(row) < MIN_NON_NULL_CELLS;
    }

    private int countNonBlankCells(Row row) {
        int count = 0;
        for (Cell cell : row) {
            String value = dataFormatter.formatCellValue(cell).trim();
            if (!value.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private String getCellAsString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value = dataFormatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private double getCellAsDouble(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return Double.NaN;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public int getTotalRowsRead() {
        return totalRowsRead;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public int getLoadedRows() {
        return loadedRows;
    }
}
