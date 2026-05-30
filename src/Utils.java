import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class Utils {

    /**
     * showAlert shows an alert on the screen
     */
    public static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * showConfirmation shows a confirmation dialog on the screen
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * validateMileage checks whether the input for Mileage is valid
     * @param mileageText is the user input for Mileage
     * @return True if valid, False if not
     */
    public static boolean validateMileage(String mileageText) {
        if (mileageText == null || mileageText.trim().isEmpty()) {
            return false;
        }
        try {
            int mileage = Integer.parseInt(mileageText.trim());
            return mileage >= 0 && mileage <= 9999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * getLatestVehicleMileage function pulls the last service mileage from the DB for the given license number
     * @param licenseNumber is the Vehicle's license number
     * @return the last service mileage
     */
    public static int getLatestVehicleMileage(String licenseNumber) {
        return ServiceRepository.getLatestMileage(licenseNumber);
    }
}