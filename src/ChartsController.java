import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ChartsController {

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private DatePicker endDate;

    @FXML
    private DatePicker startDate;

    @FXML
    private Label totalResultLabel;

    @FXML
    private ComboBox<String> vehicleCombo;

    private MyGarageRemote remoteService;

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {
        try {
            barChart.setAnimated(false); // Fixes an issue where the animation will get buggy

            // RMI setup
            this.remoteService = (MyGarageRemote) Naming.lookup("rmi://localhost/GarageService");

            List<String> vehicles = remoteService.getAllVehiclePlates(); // load all vehicles from the DB
            vehicleCombo.setItems(FXCollections.observableArrayList(vehicles)); // load the results into the Vehicle ComboBox

            // Add a listener to check for user vehicle selection
            vehicleCombo.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
                if (newVal != null) {
                    String licensePlate = newVal.split(" - ")[0].trim(); // this will trim the license_plate form the full name (plate,model)
                    barChart.getData().clear(); // clear any previous data
                    loadChartData(licensePlate); // load data for the selected Vehicle
                    try {
                        double totalAllTime = remoteService.getTotalCostsByRange(licensePlate, null, null);
                        totalResultLabel.setText(String.format("Total All-Time Cost: %.2f ₪", totalAllTime));

                        startDate.setValue(null);
                        endDate.setValue(null);
                    } catch (RemoteException e) {
                        Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
                    }
                }
            });
        } catch (RemoteException | java.rmi.NotBoundException | java.net.MalformedURLException e) {
            System.err.println("RMI Initialization failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General error during initialization: " + e.getMessage());
        }
    }

    /**
     * loadChartData function loads the Chart Data into the screen
     * @param licenseNumber is the license number for the car we want to show data for
     */
    private void loadChartData(String licenseNumber) {
        try {
            Map<String, Double> data = remoteService.getMonthlyExpenses(licenseNumber);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Expenses");

            data.forEach((month, total) -> series.getData().add(new XYChart.Data<>(month, total)));
            barChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading chart data: " + e.getMessage());
        }
    }

    /**
     * handleCalculateRange function calculate the total expenses for a selected time frame
     */
    @FXML
    private void handleCalculateRange() {
        try {
            String license = vehicleCombo.getValue().split(" - ")[0]; // license plate extracted form full vehicle name
            LocalDate start = startDate.getValue(); // start date from DatePicker
            LocalDate end = endDate.getValue(); // end date from DatePicker

            // check if date is valid
            if (start != null && end != null && !end.isBefore(start)) {
                double total = remoteService.getTotalCostsByRange(license, start, end); // calculate total cost using RMI

                totalResultLabel.setText(String.format("%.2f ₪", total));
            } else {
                Utils.showAlert(Alert.AlertType.ERROR, "Date Range is not valid", "Please select a valid date range!");
            }
        } catch (Exception e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }
}

