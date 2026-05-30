import javafx.scene.control.Alert;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsRepository {

    /**
     * getMonthlyExpenses function calculates service expenses for each month
     * @param licenseNumber is the license number for the vehicle
     * @return Map<String,Double> of <Month,Cost>
     */
    public static Map<String, Double> getMonthlyExpenses(String licenseNumber) {
        Map<String, Double> stats = new LinkedHashMap<>();
        String query = "SELECT TO_CHAR(service_date, 'YYYY-MM') as month, SUM(total_cost) as total " +
                "FROM service_history WHERE license_number = ? " +
                "GROUP BY TO_CHAR(service_date, 'YYYY-MM') ORDER BY month";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, licenseNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("month"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return stats;
    }

    /**
     * getTotalCostsByRange function calculates total expenses for service in a Date Range
     * @param licenseNumber license number of the vehicle
     * @param start start date
     * @param end end date
     * @return Total Cost
     */
    public static double getTotalCostsByRange(String licenseNumber, LocalDate start, LocalDate end) throws RemoteException {
        double total = 0;
        // Build the query using StringBuilder
        StringBuilder query = new StringBuilder("SELECT SUM(total_cost) FROM service_history WHERE license_number = ?");

        if (start != null && end != null) {
            query.append(" AND service_date BETWEEN ? AND ?");
        }

        // Establish a connection to the DB
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            pstmt.setString(1, licenseNumber);
            if (start != null && end != null) {
                pstmt.setObject(2, start);
                pstmt.setObject(3, end);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return total;
    }
}