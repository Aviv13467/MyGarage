import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class EditGarageController {

    @FXML
    private TableColumn<Vehicle, LocalDate> dateColumn;

    @FXML
    private TableColumn<Vehicle, String> licenseColumn;

    @FXML
    private TableColumn<Vehicle, String> makeColumn;

    @FXML
    private TableColumn<Vehicle, String> modelColumn;

    @FXML
    private TableView<Vehicle> vehicleTable;

    private final ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {

        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        makeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        loadVehiclesFromDB();
    }

    /**
     * loadVehiclesFromDB function loads all the vehicles form the DB into the list for the user to see
     */
    private void loadVehiclesFromDB() {
        vehicleList.clear();
        vehicleList.setAll(VehicleRepository.getAllVehicles());
        vehicleTable.setItems(vehicleList);
    }
    /**
     * handleDeleteVehicle handles the user selection to delete an existing Vehicle from the list
     */
    @FXML
    private void handleDeleteVehicle() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean result = Utils.showConfirmation("Delete Vehicle", "This action will remove all service history for this vehicle!" +
                " Are you sure you want to delete " + selected.getLicenseNumber() + "?");

        if (result) {
            VehicleRepository.deleteVehicle(selected.getLicenseNumber());
            vehicleList.remove(selected);
        }
    }

    /**
     * handleOpenAddVehicle open the AddVehicle menu if the user presses the '+' button to add a new Vehicle to the Garage
     */
    @FXML
    private void handleOpenAddVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddVehicle.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add a new Vehicle");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadVehiclesFromDB(); // update the table to show new added vehicle

        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }
}