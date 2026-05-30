import javafx.scene.control.Alert;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehicleRepository {

    /**
     * getAllVehicles returns all vehicles from the DB
     * @return List of Vehicle objects
     */
    public static List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT license_number, make, model_name, registration_date FROM vehicles";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate regDate = null;
                java.sql.Date sqlDate = rs.getDate("registration_date");
                if (sqlDate != null) regDate = sqlDate.toLocalDate();

                vehicles.add(new Vehicle(
                        rs.getString("license_number"),
                        rs.getString("make"),
                        rs.getString("model_name"),
                        regDate
                ));
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return vehicles;
    }

    /**
     * getAllVehiclePlates returns all vehicles as "license - model" strings for ComboBoxes
     * @return List of formatted strings
     */
    public static List<String> getAllVehiclePlates() {
        List<String> plates = new ArrayList<>();
        String sql = "SELECT license_number, model_name FROM vehicles";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                plates.add(rs.getString("license_number") + " - " + rs.getString("model_name"));
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return plates;
    }

    /**
     * getMakeByModel returns the make of a vehicle by its model name
     * @param modelName the model name to look up
     * @return the make string, or empty string if not found
     */
    public static String getMakeByModel(String modelName) {
        String sql = "SELECT make FROM vehicles WHERE model_name = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, modelName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String make = rs.getString("make");
                    return make != null ? make : "";
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return "";
    }

    /**
     * insertVehicle saves a new vehicle to the DB
     * @param vehicle the Vehicle to insert
     * @throws SQLException if a duplicate license or DB error occurs
     */
    public static void insertVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (license_number, make, model_name, registration_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicle.getLicenseNumber());
            pstmt.setString(2, vehicle.getMake());
            pstmt.setString(3, vehicle.getModelName());
            pstmt.setDate(4, java.sql.Date.valueOf(vehicle.getRegistrationDate()));
            pstmt.executeUpdate();
        }
    }

    /**
     * deleteVehicle deletes a vehicle and all its service history from the DB
     * @param licenseNumber the license number of the vehicle to delete
     */
    public static void deleteVehicle(String licenseNumber) {
        String deleteHistory = "DELETE FROM service_history WHERE license_number = ?";
        String deleteCar = "DELETE FROM vehicles WHERE license_number = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pst1 = conn.prepareStatement(deleteHistory);
                 PreparedStatement pst2 = conn.prepareStatement(deleteCar)) {

                pst1.setString(1, licenseNumber);
                pst1.executeUpdate();

                pst2.setString(1, licenseNumber);
                pst2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
    }
}