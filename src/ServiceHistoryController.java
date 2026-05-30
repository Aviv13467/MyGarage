import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ServiceHistoryController {

    @FXML
    private TableColumn<Service, Double> costColumn;

    @FXML
    private TableColumn<Service, String> dateColumn;

    @FXML
    private TableColumn<Service, Integer> idColumn;

    @FXML
    private TableColumn<Service, String> licenseColumn;

    @FXML
    private TableColumn<Service, Integer> mileageColumn;

    @FXML
    private TableColumn<Service, String> modelColumn;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Service> historyTable;

    private final ObservableList<Service> allServices = FXCollections.observableArrayList();

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("treatmentId"));
        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("treatmentDate"));
        mileageColumn.setCellValueFactory(new PropertyValueFactory<>("currentKm"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        FilteredList<Service> filteredData = new FilteredList<>(allServices, _ -> true);

        // Add event listener for search by model name or license plate
        searchField.textProperty().addListener((_, _, newValue) -> filteredData.setPredicate(service -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = newValue.toLowerCase();

            // search by license plate
            if (service.getLicenseNumber().contains(lowerCaseFilter)) {
                return true;
            }
            // search by model name
            else return service.getModelName().toLowerCase().contains(lowerCaseFilter);
        }));

        // wrap the filteredData in a SortedList to keep the list sorted
        SortedList<Service> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());

        // Bind the sorted data to the table view
        historyTable.setItems(sortedData);

        // load service history from the DB
        loadServiceHistory();
        Platform.runLater(() -> historyTable.requestFocus()); // Disable automatic focus on text field so it'll show the prompt text

        // for each row, double-clicking it will open the detailed service screen
        historyTable.setRowFactory(_ -> {
            TableRow<Service> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Service selectedService = row.getItem();

                    // open detailed service window
                    openDetailsModal(selectedService.getTreatmentId());
                }
            });
            return row;
        });
    }

    /**
     * loadServiceHistory load the service history from the DB
     */
    public void loadServiceHistory() {
        allServices.setAll(ServiceRepository.getAllServices());
    }

    /**
     * openDetailsModal function opens a new screen the preview the select service (parts used)
     * @param treatmentId is the specific treatment_id that was selected
     */
    private void openDetailsModal(int treatmentId) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/ServiceDetails.fxml")); // open the ServiceDetails screen
            Parent root = loader.load();
            ServiceDetailsController controller = loader.getController(); // get the controller id
            controller.initializeDetails(treatmentId); // call the function fom ServiceDetailsController.java

            // Create a new Stage and set as Modal (screen pop-up)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Parts used in: " + treatmentId);
            stage.initModality(Modality.APPLICATION_MODAL); // set as modal
            stage.show();

        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     *
     * handleAddService function handles the user action to add a new Service from the ServiceHistory screen
     */
    @FXML
    private void handleAddService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddService.fxml")); // load the AddService screen
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Service");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadServiceHistory(); // update the table to show the new service

        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * handleDeleteService function handles the user action to delete an existing Service from the ServiceHistory screen
     */
    @FXML
    private void handleDeleteService() {
        Service selectedService = historyTable.getSelectionModel().getSelectedItem();

        if (selectedService == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please choose a service to delete");
            alert.show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this service? This Action is irreversible!");
        confirm.setHeaderText("Delete service #" + selectedService.getTreatmentId());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ServiceRepository.deleteService(selectedService.getTreatmentId());
            loadServiceHistory();
        }
    }

}
