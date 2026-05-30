import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class PartsSearchController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Part> availablePartsTable;

    @FXML
    private TableColumn<Part, String> searchNameColumn;

    @FXML
    private TableColumn<Part, String> searchSkuColumn;

    @FXML
    private TableColumn<Part, Double> searchPriceColumn;

    private final ObservableList<Part> allParts = FXCollections.observableArrayList();
    private ObservableList<Part> mainPartsList;

    private String currentCarModel;

    /**
     * setCarModel functions load the parts for the selected model
     * @param modelName the car model to load parts for
     */
    public void setCarModel(String modelName) {
        this.currentCarModel = modelName;
        loadPartsForModel();
    }

    /**
     * loadPartsForModel function loads all the Parts that matches the user selected Car Model into the Part Search window
     */
    private void loadPartsForModel() {
        allParts.setAll(PartsRepository.getPartsForModelWithAsianArray(currentCarModel));
        availablePartsTable.setItems(allParts);
    }


    /**
     * setMainPartsList receives the parts list from the calling controller
     * so that any parts added in the search window are reflected directly
     * in the parent screen
     * @param mainList the observable parts list from the calling controller
     */
    public void setMainPartsList(ObservableList<Part> mainList) {
        this.mainPartsList = mainList;
    }

    /**
     * initialize function initializes the JavaFX screen
     */
    @FXML
    public void initialize() {

        // Add an Event Listener to wait for User input in the Search Text Field
        searchField.textProperty().addListener((_, _, newValue) -> filterList(newValue));

        // Load Parts into the Table
        searchNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        searchSkuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        searchPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        availablePartsTable.setItems(allParts);
    }

    /**
     * filterList function filters the list using the User input in the Search TextField
     * @param searchText is the user input in the Search TextField
     */
    private void filterList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            availablePartsTable.setItems(allParts);
        } else {
            ObservableList<Part> filteredList = allParts.filtered(part ->
                    part.getPartName().toLowerCase().contains(searchText.toLowerCase())
            );
            availablePartsTable.setItems(filteredList); // update the list to include only matching results
        }
    }

    /**
     * handleAddSelectedPart handles the addition of parts from the Search Screen
     */
    @FXML
    private void handleAddSelectedPart() {

        Part selectedPart = availablePartsTable.getSelectionModel().getSelectedItem(); // get the user selection

        if (selectedPart != null && mainPartsList != null) {
            boolean exists = false;

            // check if the part already exist in the service part list, if so, increment its quantity
            for (Part p : mainPartsList) {
                if (p.getSku().equals(selectedPart.getSku())) {
                    p.setQuantity(p.getQuantity() + 1);
                    availablePartsTable.refresh();
                    exists = true;
                    break;
                }
            }
            //  otherwise add it as a new entry with quantity of 1
            if (!exists) {
                selectedPart.setQuantity(1);
                mainPartsList.add(selectedPart);
            }

            Stage stage = (Stage) availablePartsTable.getScene().getWindow(); // fixed an issue where the table wouldn't update
            stage.close();
        } else {
            Utils.showAlert(Alert.AlertType.WARNING, "Selection Error","Select a part to add!");
        }
    }
}