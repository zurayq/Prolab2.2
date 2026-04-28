package model;

public class SaleRecord {

    private final String clientCode;
    private final double age;
    private final String gender;
    private final String city;
    private final double lineNetTotal;
    private final String categoryName1;

    public SaleRecord(String clientCode, double age, String gender, String city,
                      double lineNetTotal, String categoryName1) {
        this.clientCode = clientCode;
        this.age = age;
        this.gender = gender;
        this.city = city;
        this.lineNetTotal = lineNetTotal;
        this.categoryName1 = categoryName1;
    }

    public String getClientCode() {
        return clientCode;
    }

    public double getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getCity() {
        return city;
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
                + ", age=" + age
                + ", gender=" + gender
                + ", city=" + city
                + ", lineNetTotal=" + lineNetTotal
                + ", category=" + categoryName1 + "}";
    }
}
