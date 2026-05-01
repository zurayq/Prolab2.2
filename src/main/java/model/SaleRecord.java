package model;

public class SaleRecord {

    private final String clientcode;
    private final String gender;
    private final String brandcode;
    private final String brand;
    private final double linenettotal;
    private final String categoryname1;

    public SaleRecord(String clientcode, String gender, String brandcode, String brand,
                      double linenettotal, String categoryname1) {
        this.clientcode = clientcode;
        this.gender = gender;
        this.brandcode = brandcode;
        this.brand = brand;
        this.linenettotal = linenettotal;
        this.categoryname1 = categoryname1;
    }

    public String getClientCode() {
        return clientcode;
    }

    public String getGender() {
        return gender;
    }

    public String getBrandCode() {
        return brandcode;
    }

    public String getBrand() {
        return brand;
    }

    public double getLineNetTotal() {
        return linenettotal;
    }

    public String getCategoryName1() {
        return categoryname1;
    }

    @Override
    public String toString() {
        return "SaleRecord{clientCode=" + clientcode
                + ", gender=" + gender
                + ", brandCode=" + brandcode
                + ", brand=" + brand
                + ", lineNetTotal=" + linenettotal
                + ", category=" + categoryname1 + "}";
    }
}
