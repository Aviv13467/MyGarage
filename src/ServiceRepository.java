import javafx.scene.control.Alert;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {

    /**
     * getAllServices returns all services joined with vehicle model names, ordered by most recent
     * @return List of Service objects
     */
    public static List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT h.*, v.model_name " +
                "FROM service_history h " +
                "JOIN vehicles v ON h.license_number = v.license_number " +
                "ORDER BY h.treatment_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                services.add(new Service(
                        rs.getInt("treatment_id"),
                        rs.getString("license_number"),
                        rs.getString("model_name"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getInt("current_km"),
                        rs.getDouble("total_cost")
                ));
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return services;
    }

    /**
     * getLatestMileage returns the most recent recorded mileage for a vehicle
     * @param licenseNumber the vehicle's license number
     * @return the latest km value, or 0 if no history exists
     */
    public static int getLatestMileage(String licenseNumber) {
        String sql = "SELECT current_km FROM service_history " +
                "WHERE license_number = ? ORDER BY service_date DESC, current_km DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, licenseNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("current_km");
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return 0;
    }

    /**
     * getLastServiceRecord returns the most recent service date and mileage for a vehicle,
     * used for mileage/date validation before saving a new service
     * @param licenseNumber the vehicle's license number
     * @return a two-element array [LocalDate lastDate, Integer lastKm], or null values if no history
     */
    public static Object[] getLastServiceRecord(String licenseNumber) {
        String sql = "SELECT service_date, current_km FROM service_history " +
                "WHERE license_number = ? ORDER BY service_date DESC, current_km DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, licenseNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("service_date");
                    LocalDate lastDate = sqlDate != null ? sqlDate.toLocalDate() : null;
                    int lastKm = rs.getInt("current_km");
                    return new Object[]{lastDate, lastKm};
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return new Object[]{null, 0};
    }

    /**
     * saveService saves a new service and its associated parts to the DB atomically
     * @param licenseNumber the vehicle's license number
     * @param date the service date
     * @param mileage the current mileage at service time
     * @param totalCost the total cost of the service
     * @param parts the list of parts used in the service
     * @return true if saved successfully, false otherwise
     */
    public static boolean saveService(String licenseNumber, LocalDate date, int mileage, double totalCost, List<Part> parts) {
        String historySql = "INSERT INTO service_history (license_number, service_date, current_km, total_cost) " +
                "VALUES (?, ?, ?, ?) RETURNING treatment_id";
        String partsSql = "INSERT INTO service_parts (treatment_id, sku, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int generatedId = -1;

                try (PreparedStatement pstmt = conn.prepareStatement(historySql)) {
                    pstmt.setString(1, licenseNumber);
                    pstmt.setDate(2, java.sql.Date.valueOf(date));
                    pstmt.setInt(3, mileage);
                    pstmt.setDouble(4, totalCost);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) generatedId = rs.getInt(1);
                }

                try (PreparedStatement pstmt = conn.prepareStatement(partsSql)) {
                    for (Part part : parts) {
                        pstmt.setInt(1, generatedId);
                        pstmt.setString(2, part.getSku());
                        pstmt.setInt(3, part.getQuantity());
                        pstmt.setDouble(4, part.getPrice());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return false;
    }

    /**
     * deleteService deletes a service and its associated parts from the DB atomically
     * @param treatmentId the ID of the service to delete
     */
    public static void deleteService(int treatmentId) {
        String deleteParts = "DELETE FROM service_parts WHERE treatment_id = ?";
        String deleteService = "DELETE FROM service_history WHERE treatment_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstParts = conn.prepareStatement(deleteParts);
                 PreparedStatement pstService = conn.prepareStatement(deleteService)) {

                pstParts.setInt(1, treatmentId);
                pstParts.executeUpdate();

                pstService.setInt(1, treatmentId);
                pstService.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
    }

    /**
     * getServiceDetails returns the parts used in a specific service
     * @param treatmentId the service ID to look up
     * @return List of ServiceDetailRow objects
     */
    public static List<ServiceDetailRow> getServiceDetails(int treatmentId) {
        List<ServiceDetailRow> details = new ArrayList<>();
        String sql = "SELECT p.part_name, sp.sku, sp.quantity, (sp.quantity * sp.price) as total_line_cost " +
                "FROM service_parts sp " +
                "JOIN parts p ON sp.sku = p.sku " +
                "WHERE sp.treatment_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, treatmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new ServiceDetailRow(
                            rs.getString("part_name"),
                            rs.getString("sku"),
                            rs.getInt("quantity"),
                            rs.getDouble("total_line_cost")
                    ));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return details;
    }
}