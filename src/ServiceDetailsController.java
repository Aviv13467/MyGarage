import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServiceDetailsController {
    @FXML
    private TableView<ServiceDetailRow> partsTable;

    @FXML
    private TableColumn<ServiceDetailRow, String> partNameColumn;

    @FXML
    private TableColumn<ServiceDetailRow, String> skuColumn;

    @FXML
    private TableColumn<ServiceDetailRow, Integer> quantityColumn;

    @FXML private TableColumn<ServiceDetailRow, Double> costColumn;

    private final ObservableList<ServiceDetailRow> detailsList = FXCollections.observableArrayList();

    /**
     * initializeDetails binds each column to the designated value
     * @param treatmentId is the service id for the specific service
     */
    public void initializeDetails(int treatmentId) {
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        loadData(treatmentId);
    }

    /**
     * loadData functions load all the Parts to the detailed service list Table
     * @param treatmentId is the service id for the specific service
     */
    private void loadData(int treatmentId) {
        detailsList.setAll(ServiceRepository.getServiceDetails(treatmentId));
        partsTable.setItems(detailsList);
    }
}