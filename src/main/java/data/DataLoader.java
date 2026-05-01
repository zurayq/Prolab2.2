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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {

    private static final int minnonnullcells = 5;

    private int totalrowsread = 0;
    private int skippedrows = 0;
    private int loadedrows = 0;
    private final DataFormatter formatter = new DataFormatter();
    private Map<String, Integer> columnmap = new HashMap<>();

    public List<SaleRecord> loadFile(File file) throws Exception {
        List<SaleRecord> records = new ArrayList<>();

        totalrowsread = 0;
        skippedrows = 0;
        loadedrows = 0;

        try (FileInputStream fileinputstream = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fileinputstream)) {

            Sheet sheet = workbook.getSheetAt(0);
            columnmap = readHeader(sheet.getRow(0));

            for (int rowindex = 1; rowindex <= sheet.getLastRowNum(); rowindex++) {
                Row row = sheet.getRow(rowindex);
                totalrowsread++;

                if (row == null || countNonBlankCells(row) < minnonnullcells) {
                    skippedrows++;
                    continue;
                }

                SaleRecord record = parseRow(row);
                if (record == null) {
                    skippedrows++;
                    continue;
                }

                records.add(record);
                loadedrows++;
            }
        }

        System.out.println("[DataLoader] Total rows scanned : " + totalrowsread);
        System.out.println("[DataLoader] Skipped rows       : " + skippedrows);
        System.out.println("[DataLoader] Loaded records     : " + loadedrows);

        return records;
    }

    private Map<String, Integer> readHeader(Row header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) {
            return map;
        }

        for (Cell cell : header) {
            String name = formatter.formatCellValue(cell).trim().toUpperCase();
            if (!name.isEmpty()) {
                map.put(name, cell.getColumnIndex());
            }
        }
        return map;
    }

    private SaleRecord parseRow(Row row) {
        String clientcode = getString(row, column("CLIENTCODE", 9));
        String gender = getString(row, column("GENDER", 17));
        String brandcode = getString(row, column("BRANDCODE", 10));
        String brand = getString(row, column("BRAND", 11));
        String category = getString(row, column("CATEGORY_NAME1", 12));
        double linenettotal = getDouble(row, column("LINENETTOTAL", 7));

        if (isBlank(gender) || isBlank(category)) {
            return null;
        }

        if (Double.isNaN(linenettotal)) {
            return null;
        }

        return new SaleRecord(clientcode, gender, brandcode, brand, linenettotal, category);
    }

    private int column(String name, int fallback) {
        return columnmap.getOrDefault(name, fallback);
    }

    private int countNonBlankCells(Row row) {
        int count = 0;
        for (Cell cell : row) {
            String value = formatter.formatCellValue(cell).trim();
            if (!value.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private String getString(Row row, int colindex) {
        if (colindex < 0) {
            return null;
        }

        Cell cell = row.getCell(colindex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private double getDouble(Row row, int colindex) {
        if (colindex < 0) {
            return Double.NaN;
        }

        Cell cell = row.getCell(colindex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return Double.NaN;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim().replace(",", "."));
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
        return totalrowsread;
    }

    public int getSkippedRows() {
        return skippedrows;
    }

    public int getLoadedRows() {
        return loadedrows;
    }
}
