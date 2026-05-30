import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class MainMenuController {

    /**
     * openAddService function opens the Add Service Screen
     */
    @FXML
    public void openAddService(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddService.fxml"));
            Parent root = loader.load();
            Stage addServiceStage = new Stage();
            addServiceStage.setTitle("Add Service");
            Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            addServiceStage.initOwner(mainStage);
            Scene scene = new Scene(root);
            addServiceStage.setScene(scene);
            addServiceStage.centerOnScreen();
            addServiceStage.setResizable(false);
            addServiceStage.initModality(Modality.APPLICATION_MODAL);
            addServiceStage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openServiceHistory function opens the Service History Screen
     */
    @FXML
    private void openServiceHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/ServiceHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Service History");
            stage.centerOnScreen();
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openAddVehicle function opens the Add Vehicle Screen
     */
    @FXML
    private void openAddVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddVehicle.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Vehicle");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openEditGarage function opens the Edit Garage Screen
     */
    @FXML
    private void openEditGarage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/EditGarage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Edit Garage");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openAddPresets function opens the Add Presets Screen
     */
    @FXML
    private void openAddPresets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/AddPresets.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Presets");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openCharts function opens the Charts Screen
     */
    @FXML
    private void openCharts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/Charts.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Charts");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openRecommendation functions open the Recommendation Screen
     */
    @FXML
    private void openRecommendation(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/Recommendation.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Recommendation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }

    /**
     * openBrowsePresets functions open the Browse Presets Screen
     */
    @FXML
    private void openBrowsePresets(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/BrowsePresets.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Browse Presets");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }
}
