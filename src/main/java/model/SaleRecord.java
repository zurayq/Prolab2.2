package model;

/**
 * SaleRecord represents one raw row read directly from the spreadsheet.
 *
 * All fields are private and set only through the constructor.
 * This class models the data exactly as it exists in the source file,
 * before any encoding or normalization.
 *
 * Columns used:
 *   GENDER        - customer gender (E or K)
 *   BRAND         - product brand name
 *   LINENET       - net line amount paid (numeric)
 *   AMOUNT        - quantity bought (numeric)
 *   CATEGORY_NAME1 - top-level product category (the target label)
 *
 * Columns kept in raw model but excluded from feature vector:
 *   CLIENTCODE - pure identifier, no behavioral meaning
 *   ID         - internal row ID, not a feature
 *
 * Columns excluded entirely from the model:
 *   ITEMCODE, ITEMNAME, FICHENO - item/transaction identifiers
 *   DATE_, STARTDATE, ENDDATE   - timestamp metadata
 *   PRICE, LINENETTOTAL         - redundant with LINENET
 *   BRANDCODE                   - numeric code for BRAND (duplicate signal)
 *   CATEGORY_NAME2, CATEGORY_NAME3 - sub-categories (too sparse, not needed)
 */
public class SaleRecord {

    private final String clientCode;
    private final String gender;
    private final String brand;
    private final double lineNet;
    private final double amount;
    private final String categoryName1;

    public SaleRecord(String clientCode, String gender, String brand,
                      double lineNet, double amount, String categoryName1) {
        this.clientCode    = clientCode;
        this.gender        = gender;
        this.brand         = brand;
        this.lineNet       = lineNet;
        this.amount        = amount;
        this.categoryName1 = categoryName1;
    }

    public String getClientCode()    { return clientCode; }
    public String getGender()        { return gender; }
    public String getBrand()         { return brand; }
    public double getLineNet()       { return lineNet; }
    public double getAmount()        { return amount; }
    public String getCategoryName1() { return categoryName1; }

    @Override
    public String toString() {
        return "SaleRecord{clientCode=" + clientCode
                + ", gender=" + gender
                + ", brand=" + brand
                + ", lineNet=" + lineNet
                + ", amount=" + amount
                + ", category=" + categoryName1 + "}";
    }
}
