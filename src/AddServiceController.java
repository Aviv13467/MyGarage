import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddServiceController {

    @FXML
    private ComboBox<String> vehicleCombo;

    @FXML
    private ComboBox<String> presetCombo;

    @FXML
    private TableView<Part> partsTable;

    @FXML
    private TableColumn<Part, String> skuColumn;

    @FXML
    private TableColumn<Part, String> nameColumn;

    @FXML
    private TableColumn<Part, Double> priceColumn;

    @FXML
    private TableColumn<Part, Integer> qtyColumn;

    @FXML
    private TextField mileageField;

    @FXML
    private DatePicker datePicker;


    /**
    Initialize the AddService Screen
     */
    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now()); // Sets Today's date for user convenience
        loadPresetsFromDatabase(null);
        vehicleCombo.setItems(FXCollections.observableArrayList(VehicleRepository.getAllVehiclePlates()));


        /*
        Event Listener Implementation so we can change the Presets ComboBox according to the select model (different model have different parts)
         */
        vehicleCombo.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                clearFields(); // Clear any prior selection
                updateMileagePlaceholder(newValue);

                try {
                    String modelName = newValue.split(" - ")[1].trim(); // Trims the model name out of the '1234567-ModelName' format
                    loadPresetsFromDatabase(modelName); // Loads the corresponding presets for that model name

                } catch (Exception e) {
                    System.out.println("Error parsing model name from combo: " + e.getMessage());
                }
            }
        });

        /*
        Set the column names for the Preset Parts Table
         */
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void loadPresetsFromDatabase(String modelName) {
        presetCombo.getItems().clear();
        presetCombo.getItems().setAll(PresetRepository.getPresetsForModel(modelName));
    }

    /**
     * handlePresetSelection handles the user selected Preset, loading into the table the corresponding Parts while making a distinction between parts that shares
     * the same name but are for different models (e.g. Oil Filter)
     */
    @FXML
    private void handlePresetSelection() {
        String selectedTreatment = presetCombo.getValue();
        String selectedVehicle = vehicleCombo.getValue();

        if (selectedTreatment == null || selectedVehicle == null) {
            return;
        }
        String modelName = selectedVehicle.split(" - ")[1].trim();
        String make = VehicleRepository.getMakeByModel(modelName);
        boolean isAsian = Vehicle.isAsianMake(make);
        List<Part> parts = PresetRepository.getPresetPartsForService(selectedTreatment, modelName, isAsian);

        partsTable.getItems().clear();
        for (Part p : parts) {
            boolean exists = false;
            for (Part existing : partsTable.getItems()) {
                if (existing.getSku().equals(p.getSku())) {
                    existing.setQuantity(existing.getQuantity() + 1);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                p.setQuantity(1);
                partsTable.getItems().add(p);
            }
        }
        partsTable.refresh();
    }


    /**
     * handleRemovePart handles a user selection to remove a certain Part from the Table allowing for modular treatments where the user isn't strictly limited to use all parts
     * (for e.g. when the user already replaced a Part in recent service, and it is not necessary to replace it again)
     */
    @FXML
    private void handleRemovePart() {
        Part selectedPart = partsTable.getSelectionModel().getSelectedItem(); // Get the user selection for the Part to be deleted

        if (selectedPart != null) {
            // If the quantity is larger than 1, just decrement by 1
            if (selectedPart.getQuantity() > 1) {
                selectedPart.setQuantity(selectedPart.getQuantity() - 1);
            } else {
                // If the Quantity is equal to 1, delete it
                partsTable.getItems().remove(selectedPart);
            }

            partsTable.refresh(); // Fixes an issue where the quantity won't update

        } else {
            Utils.showAlert(Alert.AlertType.WARNING, "Selection Error","Select a part to add!");
        }
    }

    /**
     * handleOpenPartsSearch handles the 'Add' button in the Parts table, it opens a new screen where the user can search for additional parts for his service
     */
    @FXML
    private void handleOpenPartsSearch() {
        try {
            String selectedVehicle = vehicleCombo.getValue(); // Get the selected vehicle from the Vehicle ComboBox
            // If no Vehicle is selected from the ComboBox then return
            if (selectedVehicle == null) {
                Utils.showAlert(Alert.AlertType.WARNING, "Selection Error","Select a vehicle in order to search Parts!");
                return;
            }

            String[] parts = selectedVehicle.split(" - "); // Turn the String into an Array in format [LicenseNumber,ModelName]
            String modelName = parts[1].trim();

            // Open the new screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/PartsSearch.fxml"));
            Parent root = loader.load();
            PartsSearchController controller = loader.getController();

            if (controller == null) {
                return;
            }

            controller.setMainPartsList(partsTable.getItems()); // Passes the list in the new controller
            controller.setCarModel(modelName); // Passes the car model in the new controller



            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Set the new screen as a modal (screen inside a screen)
            stage.setTitle("Search Parts for: " + modelName); // Set the new window title
            stage.setResizable(false); // don't allow resizing
            stage.showAndWait();
            partsTable.refresh();

        } catch (Exception e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * handleSaveService handles the 'Save' button to the service
     */
    @FXML
    private void handleSaveService() {
        if (!validateFields()) return;


        // Saving info from the different fields
        String selectedVehicle = vehicleCombo.getValue();
        String mileageStr = mileageField.getText();
        LocalDate date = datePicker.getValue();
        ObservableList<Part> partsInTable = partsTable.getItems();

        String licenseNumber = selectedVehicle.split(" - ")[0].trim();
        if (!validateMileage()) {
            Utils.showAlert(Alert.AlertType.ERROR,"Error","Please enter a valid mileage between 1-9,999,999 (numbers only).");
            return;
        }
        int mileage = Integer.parseInt(mileageStr);

        // Calculate the cost
        double totalCost = 0;
        for (Part part : partsInTable) {
            totalCost += (part.getPrice() * part.getQuantity());
        }

        Object[] lastRecord = ServiceRepository.getLastServiceRecord(licenseNumber);
        LocalDate lastDate = (LocalDate) lastRecord[0];
        int lastMileage = (int) lastRecord[1];

        if (lastDate != null) {
            if (date.isAfter(lastDate) && mileage < lastMileage) {
                Utils.showAlert(Alert.AlertType.ERROR, "Error",
                        String.format("The new date is AFTER the last recorded service (%s),\n" +
                                        "but the entered mileage (%d km) is LOWER than that service (%d km).",
                                lastDate, mileage, lastMileage));
                return;
            }
            if (date.isBefore(lastDate) && mileage > lastMileage) {
                Utils.showAlert(Alert.AlertType.ERROR, "Error",
                        String.format("The new date is BEFORE the last recorded service (%s),\n" +
                                        "but the entered mileage (%d km) is HIGHER than that later service (%d km).",
                                lastDate, mileage, lastMileage));
                return;
            }
        }

        boolean saved = ServiceRepository.saveService(licenseNumber, date, mileage, totalCost, new ArrayList<>(partsInTable));
        if (saved) {
            Utils.showAlert(Alert.AlertType.INFORMATION, "Confirmation", "Service was saved successfully!");
            Stage stage = (Stage) mileageField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * validateFields is a function that checks whether the user has entered values into all the required fields
     */
    private boolean validateFields() {
        boolean isValid = true;

        Control[] fields = {vehicleCombo, datePicker, mileageField, partsTable};

        for (Control field : fields) {
            boolean isEmpty = false;

            if (field instanceof ComboBox && ((ComboBox<?>) field).getValue() == null) isEmpty = true;
            else if (field instanceof DatePicker && ((DatePicker) field).getValue() == null) isEmpty = true;
            else if (field instanceof TextField && ((TextField) field).getText().trim().isEmpty()) isEmpty = true;
            else if (field instanceof TableView && ((TableView<?>) field).getItems().isEmpty()) isEmpty = true;

            // change border color to red to indicate an error
            if (isEmpty) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                isValid = false;
            } else {
                field.setStyle("");
            }
        }

        if (!isValid) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
        }

        return isValid;
    }

    /**
     * handleClearForm function clear all the forms for e.g. when a user selects a new vehicle, hence the results for the previous selection may not be accurate
     */
    @FXML
    private void handleClearForm() {
        presetCombo.getSelectionModel().clearSelection();
        clearFields();

    }

    /**
    * clearFields is a helper function to clear all the fields
     */
    private void clearFields() {
        presetCombo.setValue(null);
        partsTable.getItems().clear();
        mileageField.clear();
        clearValidationStyles();
    }

    /**
     * clearValidationStyles is a helper function to clear the previous validation error upon clearing the forms
     */
    private void clearValidationStyles() {
        Control[] fields = {vehicleCombo, datePicker, mileageField, partsTable};

        for (Control field : fields) {
            field.setStyle("");
        }
    }

    /**
     * validateMileage checks whether the user input for mileage is valid (e.g. only numbers in the range 1-9,999,999, not letters)
     * @return True if mileage is valid, False otherwise
     */
   private boolean validateMileage() {
       String mileageText = mileageField.getText();
       if (mileageField == null || mileageText.trim().isEmpty()) return false;

       try {
           int mileage = Integer.parseInt(mileageText.trim());
           return mileage > 0 && mileage <= 9999999;
       } catch (NumberFormatException e) {
           return false;
       }
   }

    private void updateMileagePlaceholder(String selectedVehicleFullString) {
        if (selectedVehicleFullString == null) return;

        String licenseNumber = selectedVehicleFullString.split(" - ")[0].trim();

        int latestRecordedKm = Utils.getLatestVehicleMileage(licenseNumber);

        javafx.application.Platform.runLater(() -> {
            if (latestRecordedKm > 0) {
                mileageField.setText(""); // clear any previous text
                mileageField.setPromptText("Last service: " + latestRecordedKm + "km");
            }
        });
    }
}