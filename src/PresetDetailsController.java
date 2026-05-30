import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PresetDetailsController {

    @FXML
    private TableView<Part> partsTable;

    @FXML
    private TableColumn<Part, String> skuColumn;

    @FXML
    private TableColumn<Part, String> nameColumn;

    @FXML
    private TableColumn<Part, Integer> quantityColumn;

    @FXML private TableColumn<Part, Double> priceColumn;

    private final ObservableList<Part> partsList = FXCollections.observableArrayList();

    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        partsTable.setItems(partsList);
    }

    /**
     * loadPresetParts handles loading Parts for the selected Preset from the DB
     */
    public void loadPresetParts(String treatmentName, String presetModelName) {
        partsList.setAll(PresetRepository.getPresetParts(treatmentName, presetModelName));
    }
}