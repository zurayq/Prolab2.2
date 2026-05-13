package model;

public class SaleRecord {

    private final String clientCode;
    private final String gender;
    private final String brandCode;
    private final String brand;
    private final double lineNetTotal;
    private final String categoryName1;
    public SaleRecord(String clientCode, String gender, String brandCode, String brand,
                      double lineNetTotal, String categoryName1) {
        this.clientCode = clientCode;
        this.gender = gender;
        this.brandCode = brandCode;
        this.brand = brand;
        this.lineNetTotal = lineNetTotal;
        this.categoryName1 = categoryName1;
    }

    public String getClientCode() {
        return clientCode;
    }
    public String getGender() {
        return gender;
    }
    public String getBrandCode() {
        return brandCode;
    }
    public String getBrand() {
        return brand;
    }
    public double getLineNetTotal() {
        return lineNetTotal;
    }
    public String getCategoryName1() {
        return categoryName1;
    }
    @Override
    public String toString() {
        return "SaleRecord{clientCode=" + clientCode
                + ", gender=" + gender
                + ", brandCode=" + brandCode
                + ", brand=" + brand
                + ", lineNetTotal=" + lineNetTotal
                + ", category=" + categoryName1 + "}";
    }
}
