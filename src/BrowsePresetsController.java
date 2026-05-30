import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class BrowsePresetsController {

    @FXML private TableView<String> presetsTable;
    @FXML private TableColumn<String, String> nameColumn;
    @FXML private ComboBox<String> vehicleCombo;

    private final ObservableList<String> vehicleList = FXCollections.observableArrayList();
    private final ObservableList<String> presetDataList = FXCollections.observableArrayList();

    private String currentSelectedModel = null;

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        presetsTable.setItems(presetDataList);
        presetsTable.setPlaceholder(new Label("Please select a vehicle"));

        vehicleCombo.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                currentSelectedModel = newValue.split(" - ")[1].trim();

                presetsTable.setPlaceholder(new Label("No Presets for this vehicle"));

                loadPresetsForModel(currentSelectedModel);
            } else {
                currentSelectedModel = null;
                presetDataList.clear();
            }
        });

        presetsTable.setRowFactory(_ -> {
            TableRow<String> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String selectedTreatment = row.getItem();
                    if (selectedTreatment != null && currentSelectedModel != null) {
                        openPresetDetailsPopup(selectedTreatment, currentSelectedModel);
                    }
                }
            });

            return row;
        });

        loadVehiclesIntoCombo();
    }

    /**
     * loadPresetsForModel handles loading Presets from the DB for the selected model
     * @param modelName is the model name to show Presets for
     */
    private void loadPresetsForModel(String modelName) {
        presetDataList.setAll(PresetRepository.getPresetsForModel(modelName));
    }


    /**
     * handleDeletePreset handles when the user Removes a Preset from the Selected Parts Table
     */
    @FXML
    private void handleDeletePreset() {
        String selectedTreatment = presetsTable.getSelectionModel().getSelectedItem();
        if (selectedTreatment == null || currentSelectedModel == null) {
            Utils.showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a preset to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                PresetRepository.deletePreset(selectedTreatment, currentSelectedModel);
                presetDataList.remove(selectedTreatment);
                Utils.showAlert(Alert.AlertType.INFORMATION, "Success", "Preset deleted successfully.");
            }
        });
    }


    /**
     * loadVehiclesIntoCombo function load Models into the modelComboBox
     */
    private void loadVehiclesIntoCombo() {
        vehicleList.setAll(VehicleRepository.getAllVehiclePlates());
        vehicleCombo.setItems(vehicleList);
    }

    /**
     * openPresetDetailsPopup opens the Preset Details Pop-up screen
     * @param treatmentName load Parts for Preset named treatmentName
     * @param modelName load Presets for model named modelName
     */
    private void openPresetDetailsPopup(String treatmentName, String modelName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/PresetDetails.fxml"));
            Parent root = loader.load();
            PresetDetailsController controller = loader.getController();
            controller.loadPresetParts(treatmentName, modelName);
            Stage popupStage = new Stage();
            popupStage.setTitle("Preset: " + treatmentName + " (" + modelName + ")");
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "UI Error", "Cannot open Preset details window: " + e.getMessage());
        }
    }

    /**
     * handleAddPreset opens the Add Preset Pop-up screen
     */
    @FXML
    private void handleAddPreset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddPresets.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New Preset");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (currentSelectedModel != null) {
                loadPresetsForModel(currentSelectedModel);
            }

        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "UI Error", "Cannot open Add Preset window: " + e.getMessage());
        }
    }
}