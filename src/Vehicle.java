import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

public class Vehicle implements Serializable {

    private String licenseNumber;
    private final String make;
    private final String modelName;
    private final LocalDate registrationDate;

    private static final Set<String> ASIAN_MAKES = Set.of(
            "hyundai", "kia", "nissan", "mazda", "mitsubishi",
            "toyota", "infinity", "daihatsu", "honda", "subaru",
            "suzuki", "lexus", "isuzu"
    );

    public Vehicle(String licenseNumber, String make, String modelName, LocalDate registrationDate) {
        this.licenseNumber = licenseNumber;
        this.make = make;
        this.modelName = modelName;
        this.registrationDate = registrationDate;
    }

    public static boolean isAsianMake(String make) {
        if (make == null) return false;
        return ASIAN_MAKES.contains(make.trim().toLowerCase());
    }

    public String getLicenseNumber() { return licenseNumber; }
    public String getModelName() { return modelName; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getMake() { return make; }
    public static String[] getAsianMakesArray() {
        return ASIAN_MAKES.stream()
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }

    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    @Override
    public String toString() {
        return modelName + " (" + licenseNumber + ")";
    }
}