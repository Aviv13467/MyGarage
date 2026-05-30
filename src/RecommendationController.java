import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.rmi.Naming;
import java.util.List;

public class RecommendationController {

    @FXML
    private ComboBox<String> vehicleCombo;

    @FXML
    private TextField mileageTextField;

    @FXML
    private Label oilLabel;

    @FXML
    private Label brakesLabel;

    @FXML
    private Label coolantLabel;

    @FXML
    private Label sparkPlugsLabel;

    @FXML
    private Label timingLabel;

    @FXML
    private Label transmissionLabel;

    private MyGarageRemote remoteService;

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {
        clearLabels();
        setupRMIAndLoadVehicles();

        // Event listener to the Vehicle ComboBox - to clear labels when switching vehicles to avoid showing irrelevant recommendations from previous search
        vehicleCombo.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            clearLabels();
            if (newValue != null) autoFillLatestMileage(newValue); // automatically fills the last service mileage for user convenience
        });
    }

    /**
     * setupRMIAndLoadVehicles functions set up the RMI connection and load all Vehicles from the DB
     */
    private void setupRMIAndLoadVehicles() {
        try {
            this.remoteService = (MyGarageRemote) Naming.lookup("rmi://localhost/GarageService");

            List<String> vehicles = remoteService.getAllVehiclePlates();
            vehicleCombo.setItems(FXCollections.observableArrayList(vehicles));

        } catch (Exception e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Connection Error",
                    "Could not connect to the RMI server. Please make sure the server is running.");
        }
    }

    /**
     * parseAndApplyRecommendations functions goes over all recommendations and check which applies
     * @param recommendations is the recommendation String containing all recommendations
     */
    private void parseAndApplyRecommendations(String recommendations) {
        if (recommendations == null || recommendations.trim().isEmpty() || recommendations.contains("No recommendations")) {
            setAllClear();
            return;
        }

        String lower = recommendations.toLowerCase();

        String dangerStyle = "-fx-text-fill: #d32f2f; -fx-font-weight: bold;";
        String successStyle = "-fx-text-fill: #388e3c; -fx-font-weight: normal;";
        String clearText = "No Recommendations!";

        if (lower.contains("oil")) {
            oilLabel.setText("10,000km Service is due");
            oilLabel.setStyle(dangerStyle);
        } else {
            oilLabel.setText(clearText);
            oilLabel.setStyle(successStyle);
        }

        if (lower.contains("brake") || lower.contains("pads")) {
            brakesLabel.setText("Inspect Brakes and Brake Fluid");
            brakesLabel.setStyle(dangerStyle);
        } else {
            brakesLabel.setText(clearText);
            brakesLabel.setStyle(successStyle);
        }

        if (lower.contains("coolant")) {
            coolantLabel.setText("Replace Coolant");
            coolantLabel.setStyle(dangerStyle);
        } else {
            coolantLabel.setText(clearText);
            coolantLabel.setStyle(successStyle);
        }

        if (lower.contains("spark")) {
            sparkPlugsLabel.setText("Replace Spark Plugs");
            sparkPlugsLabel.setStyle(dangerStyle);
        } else {
            sparkPlugsLabel.setText(clearText);
            sparkPlugsLabel.setStyle(successStyle);
        }

        if (lower.contains("timing")) {
            timingLabel.setText("Inspect Timing Belt (Not Required if equipped with Timing Chain");
            timingLabel.setStyle(dangerStyle);
        } else {
            timingLabel.setText(clearText);
            timingLabel.setStyle(successStyle);
        }

        if (lower.contains("transmission") || lower.contains("gear")) {
            transmissionLabel.setText("Check Transmission Fluid!");
            transmissionLabel.setStyle(dangerStyle);
        } else {
            transmissionLabel.setText(clearText);
            transmissionLabel.setStyle(successStyle);
        }
    }

    /**
     * setAllClear functions set all labels to "No Recommendations!"
     */
    private void setAllClear() {
        String clearText = "No Recommendations!";
        String successStyle = "-fx-text-fill: #388e3c; -fx-font-weight: normal;";
        setLabelText(clearText, successStyle);
    }

    /**
     * clearLabels functions set all labels to "Select a vehicle"
     */
    private void clearLabels() {
        String waitingText = "Select a vehicle";
        String grayStyle = "-fx-text-fill: #757575;";
        setLabelText(waitingText, grayStyle);
    }

    /**
     * setLabelText function set the label text
     * @param textType is the text
     * @param successStyle is the style
     */
    private void setLabelText(String textType, String successStyle) {
        oilLabel.setText(textType);
        oilLabel.setStyle(successStyle);
        brakesLabel.setText(textType);
        brakesLabel.setStyle(successStyle);
        coolantLabel.setText(textType);
        coolantLabel.setStyle(successStyle);
        sparkPlugsLabel.setText(textType);
        sparkPlugsLabel.setStyle(successStyle);
        timingLabel.setText(textType);
        timingLabel.setStyle(successStyle);
        transmissionLabel.setText(textType);
        transmissionLabel.setStyle(successStyle);
    }

    /**
     * handleCheckRecommendations function handle the checking of the recommendations
     */
    @FXML
    private void handleCheckRecommendations() {
        String selectedVehicle = vehicleCombo.getValue();
        String mileageText = mileageTextField.getText();

        if (selectedVehicle == null) {
            Utils.showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a vehicle first.");
            return;
        }

        if (!Utils.validateMileage(mileageText)) {
            mileageTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
            Utils.showAlert(Alert.AlertType.WARNING, "Input Error",
                    "Please enter a valid mileage between 0 and 9,999,999 (numbers only).");
            return;
        }


        mileageTextField.setStyle("");
        try {
            int currentMileage = Integer.parseInt(mileageText.trim());
            String licenseNumber = selectedVehicle.split(" - ")[0].trim();
            int latestRecordedKm = remoteService.getLatestVehicleMileage(licenseNumber);
            if (currentMileage < latestRecordedKm) {
                Utils.showAlert(
                        Alert.AlertType.ERROR, "Invalid Mileage", String.format("""
                                The entered mileage is lower than the last recorded service for this vehicle (%d km).
                                
                                Please enter the current, higher mileage.""", latestRecordedKm));
                return;
            }
            String recommendations = remoteService.getServiceRecommendations(licenseNumber, currentMileage);
            parseAndApplyRecommendations(recommendations);
        }
            catch (Exception e) {
                Utils.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while communicating with the server.");
        }
    }

    /**
     * autoFillLatestMileage automatically sets the user selected Vehicle's last service mileage in the mileage TextField
     * @param selectedVehicleFullString is the selected Vehicle from the ComboBox
     */
    private void autoFillLatestMileage(String selectedVehicleFullString) {
        if (selectedVehicleFullString == null) return;

        String licenseNumber = selectedVehicleFullString.split(" - ")[0].trim();

        try {
            int latestRecordedKm = remoteService.getLatestVehicleMileage(licenseNumber);
            javafx.application.Platform.runLater(() -> {
                mileageTextField.setText(String.valueOf(latestRecordedKm));
                mileageTextField.setStyle("");
            });
        } catch (Exception e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "Could not fetch latest mileage.");
        }
    }
}
