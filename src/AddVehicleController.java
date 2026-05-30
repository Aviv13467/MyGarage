import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;

public class AddVehicleController {

    @FXML
    private TextField licenseField;

    @FXML
    private TextField makeField;

    @FXML
    private TextField modelField;

    @FXML
    private DatePicker registrationDatePicker;


    /**
     * initialize function initializes the screen upon loading
     */
    @FXML
    public void initialize() {
        // Event listener that restricts the user from typing anything but digits, and limits the user for maximum 8 digits (Israeli license plate max)
        licenseField.textProperty().addListener((_, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) licenseField.setText(newValue.replaceAll("\\D", ""));
            if (licenseField.getText().length() > 8) licenseField.setText(oldValue);
        });
    }

    /**
     * validateFields is a function that checks whether the user has entered values into all the required fields
     */
    private void validateFields() {
        boolean isValid = true;
        Control[] fields = {licenseField, makeField, modelField, registrationDatePicker};

        for (Control field : fields) {
            boolean isEmpty = false;

            if (field instanceof TextField) {
                String text = ((TextField) field).getText();
                if (text == null || text.trim().isEmpty()) isEmpty = true;
            } else if (field instanceof DatePicker) {
                if (((DatePicker) field).getValue() == null) isEmpty = true;
            }

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
    }

    /**
     * handleVehicleAdd function handles the user input from the different textFields and saves them
     */
    @FXML
    void handleVehicleAdd(ActionEvent event) {
        String license = licenseField.getText().trim();
        String make = makeField.getText().trim();
        String model = modelField.getText().trim();
        LocalDate regDate = registrationDatePicker.getValue();

        if (license.isEmpty() || model.isEmpty() || regDate == null || make.isEmpty()) {
            validateFields();
            return;
        }
        if(!validateLicensePlate()){
            Utils.showAlert(Alert.AlertType.ERROR,"Error","Please enter a valid license plate (7 or 8 digits only)");
            return;
        }
        if(!validateMake()){
            Utils.showAlert(Alert.AlertType.ERROR,"Error","Please enter a valid Vehicle Make (Letters Only)");
            return;
        }

        // Save Vehicle into the DB
        try {
            VehicleRepository.insertVehicle(new Vehicle(license, make, model, regDate));
            Utils.showAlert(Alert.AlertType.INFORMATION, "Success", "The Vehicle was saved successfully!");
            closeWindow(event);
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                Utils.showAlert(Alert.AlertType.ERROR, "Error", "License number already exists in the DB");
            } else {
                Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "There was an error saving the vehicle");
            }
        }
    }

    /**
     * validateLicensePlate function checks whether the license plate is valid
     * @return True if yes, False otherwise
     */
    private boolean validateLicensePlate(){
        return licenseField.getText().matches("\\d{7,8}");
    }

    /**
     * validateMake function checks if the Make is valid
     * @return True if yes, False Otherwise
     */
    private boolean validateMake(){
        return makeField.getText().matches("[a-zA-Z]+");
    }

    /**
     * closeWindow function closes the current window
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}