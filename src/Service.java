import java.io.Serializable;
import java.time.LocalDate;

public class Service implements Serializable {

    private final int treatmentId;
    private final String licenseNumber;
    private final String modelName;
    private final LocalDate treatmentDate;
    private final int currentKm;
    private final double totalCost;

    public Service(int treatmentId, String licenseNumber, String modelName, LocalDate treatmentDate, int currentKm, double totalCost) {
        this.treatmentId = treatmentId;
        this.licenseNumber = licenseNumber;
        this.modelName = modelName;
        this.treatmentDate = treatmentDate;
        this.currentKm = currentKm;
        this.totalCost = totalCost;
    }

    public int getTreatmentId() { return treatmentId; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getModelName() { return modelName; }
    public LocalDate getTreatmentDate() { return treatmentDate; }
    public int getCurrentKm() { return currentKm; }
    public double getTotalCost() { return totalCost; }
}