import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AddPresetsController {

    // Form Elements
    @FXML private TextField serviceNameField;

    @FXML private ComboBox<String> modelComboBox;

    @FXML private TextField partSearchField;

    // Parts Selector
    @FXML private TableView<Part> availablePartsTable;

    @FXML private TableColumn<Part, String> partNameColumn;

    @FXML private TableColumn<Part, String> skuColumn;

    @FXML private TableColumn<Part, Double> priceColumn;

    // Parts Selected
    @FXML private TableView<Part> selectedPartsTable;

    @FXML private TableColumn<Part, String> partNameSelectedColumn;

    @FXML private TableColumn<Part, String> skuSelectedColumn;

    @FXML private TableColumn<Part, Double> priceSelectedColumn;

    @FXML private TableColumn<Part, Integer> quantityColumn;

    private final ObservableList<Part> availablePartsList = FXCollections.observableArrayList();
    private final ObservableList<Part> selectedPartsList = FXCollections.observableArrayList();

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {

        // Linking Parts Selector columns to Parts table
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Linking Parts Selected columns to Parts table
        partNameSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        skuSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        priceSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        availablePartsTable.setItems(availablePartsList);
        selectedPartsTable.setItems(selectedPartsList);

        // Load models into Vehicle ComboBox
        modelComboBox.getItems().setAll(PresetRepository.getAllModels());

        // Event Listener for the Part Search TextField
        partSearchField.textProperty().addListener((_, _, newValue) -> filterParts(newValue));

        // Event Listener for the Vehicle model ComboBox
        modelComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            // There was a change in model selected, clear the tables
            if (newValue != null) {
                partSearchField.clear();
                availablePartsList.clear();
                selectedPartsList.clear();
                loadPartsForModel(newValue);
            }
        });
    }

    /**
     * onModelSelected function handles user model selection (e.g. shows matching Parts)
     */
    @FXML
    void onModelSelected() {
        String selectedModel = modelComboBox.getValue();
        if (selectedModel != null) {
            loadPartsForModel(selectedModel); // Load matching parts for the model selected
        }
    }

    /**
     * loadPartsForModel handles loading Parts from the DB for the selected model
     * @param modelName is the model name to show Parts for
     */
    private void loadPartsForModel(String modelName) {
        String make = VehicleRepository.getMakeByModel(modelName);
        boolean isAsian = Vehicle.isAsianMake(make);
        availablePartsList.setAll(PartsRepository.getPartsForModel(modelName, isAsian));
    }

    /**
     * handleAddPart handles when the user Adds a Part from the Parts Table
     */
    @FXML
    void handleAddPart() {
        Part selected = availablePartsTable.getSelectionModel().getSelectedItem();

        if (selected == null){
            Utils.showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a part from the table to add.");
        }
        else {
            Part existingInSelected = null; // Checks if the user already added this part to avoid having multiple entries instead of incrementing quantity counter
            for (Part p : selectedPartsList) {
                if (p.getSku().equals(selected.getSku())) {
                    existingInSelected = p;
                    break;
                }
            }
            // The part already exist, increment the quantity counter by 1
            if (existingInSelected != null) {
                existingInSelected.setQuantity(existingInSelected.getQuantity() + 1);
                selectedPartsTable.refresh(); // Fixes an issue where the table won't show changes
            } else {
                // the Part is new, add a new entry with quantity of 1
                selected.setQuantity(1);
                selectedPartsList.add(selected);
            }
        }
    }

    /**
     * handleRemovePart handles when the user Removes a Part from the Selected Parts Table
     */
    @FXML
    void handleRemovePart() {
        Part selected = selectedPartsTable.getSelectionModel().getSelectedItem(); // get the selected Part to be deleted
        if (selected == null) {
            Utils.showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a part from the table to remove.");
            return;
        }
        if (selected.getQuantity() > 1) {
            selected.setQuantity(selected.getQuantity() - 1);
            selectedPartsTable.refresh();
        } else selectedPartsList.remove(selected); // remove Part from the list
    }

    /**
     * handleSavePreset handles when the user trys to Save a new Preset
     */
    @FXML
    void handleSavePreset() {
        String serviceName = serviceNameField.getText().trim();
        String modelName = modelComboBox.getValue();

        if(!validateFields()) return;

        boolean success = PresetRepository.savePreset(serviceName, modelName, selectedPartsList);
        if (success) {
            Utils.showAlert(Alert.AlertType.INFORMATION, "Success", serviceName + " was saved successfully!");
            selectedPartsList.clear();
            serviceNameField.clear();
        }
    }


    /**
     * filterParts filter parts by user Parts search TextField
     * @param searchText is the user text to filter
     */
    private void filterParts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            availablePartsTable.setItems(availablePartsList);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Part> filteredData = FXCollections.observableArrayList();

        for (Part part : availablePartsList) {
            // search part by SKU
            if (part.getPartName().toLowerCase().contains(lowerCaseFilter) ||
                    part.getSku().toLowerCase().contains(lowerCaseFilter)) {
                filteredData.add(part);
            }
        }
        availablePartsTable.setItems(filteredData);
    }

    /**
     * validateFields is a function that checks whether the user has entered values into all the required fields
     */
    private boolean validateFields() {
        boolean isValid = true;

        Control[] fields = {serviceNameField, modelComboBox, selectedPartsTable};

        for (Control field : fields) {
            boolean isEmpty = isFieldEmpty(field);

            if (isEmpty) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                isValid = false;
            } else field.setStyle("");
        }

        if (!isValid) Utils.showAlert(Alert.AlertType.ERROR, "Validation Error",
                "Please fill all required fields and ensure at least one part is added to the preset.");

        return isValid;
    }

    /**
     * isFieldEmpty is a Helper function for validateFields to check whether a TextField is empty
     * @param field the field to check
     * @return True if empty, False otherwise
     */
    private static boolean isFieldEmpty(Control field) {
        boolean isEmpty = false;

        if (field instanceof TextField) {
            String text = ((TextField) field).getText();
            if (text == null || text.trim().isEmpty()) {
                isEmpty = true;
            }
        }
        else if (field instanceof ComboBox<?>) {
            if (((ComboBox<?>) field).getValue() == null) {
                isEmpty = true;
            }
        }
        else if (field instanceof TableView<?>) {
            if (((TableView<?>) field).getItems().isEmpty()) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }
}