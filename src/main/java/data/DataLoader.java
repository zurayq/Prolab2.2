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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {

    private static final String DEFAULT_DATASET_RESOURCE = "/data/MarketSalesKocaeli.xlsx";
    private static final int MIN_NON_NULL_CELLS = 5;
    private int totalRowsRead = 0;
    private int skippedRows = 0;
    private int loadedRows = 0;
    private final DataFormatter formatter = new DataFormatter();
    private Map<String, Integer> columnMap = new HashMap<>();

    public List<SaleRecord> loadFile(File file) throws Exception {
        // Reads Excel rows into SaleRecord objects.
        try (InputStream inputStream = new FileInputStream(file)) {
            return loadWorkbook(inputStream);
        }
    }
    public List<SaleRecord> loadInputStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }

        try (InputStream stream = inputStream) {
            return loadWorkbook(stream);
        }
    }
    public List<SaleRecord> loadDefaultDataset() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream(DEFAULT_DATASET_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException(
                    "Default dataset is missing. Put MarketSalesKocaeli.xlsx under src/main/resources/data/");
        }
        return loadInputStream(inputStream);
    }
    private List<SaleRecord> loadWorkbook(InputStream inputStream) throws Exception {
        List<SaleRecord> records = new ArrayList<>();

        totalRowsRead = 0;
        skippedRows = 0;
        loadedRows = 0;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            columnMap = readHeader(sheet.getRow(0));

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                totalRowsRead++;

                if (row == null || countNonBlankCells(row) < MIN_NON_NULL_CELLS) {
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
        String clientCode = getString(row, column("CLIENTCODE", 9));
        String gender = getString(row, column("GENDER", 17));
        String brandCode = getString(row, column("BRANDCODE", 10));
        String brand = getString(row, column("BRAND", 11));
        String category = getString(row, column("CATEGORY_NAME1", 12));
        double lineNetTotal = getDouble(row, column("LINENETTOTAL", 7));

        if (isBlank(gender) || isBlank(category)) {
            return null;
        }

        if (Double.isNaN(lineNetTotal)) {
            return null;
        }

        return new SaleRecord(clientCode, gender, brandCode, brand, lineNetTotal, category);
    }

    private int column(String name, int fallback) {
        return columnMap.getOrDefault(name, fallback);
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
    private String getString(Row row, int colIndex) {
        if (colIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private double getDouble(Row row, int colIndex) {
        if (colIndex < 0) {
            return Double.NaN;
        }
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
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
        return totalRowsRead;
    }
    public int getSkippedRows() {
        return skippedRows;
    }
    public int getLoadedRows() {
        return loadedRows;
    }
}
