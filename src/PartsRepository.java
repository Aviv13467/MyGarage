import javafx.scene.control.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartsRepository {

    /**
     * getPartsForModel returns all parts compatible with a given model,
     * including universal parts and Asian vehicle parts where applicable
     * @param modelName the vehicle model name
     * @param isAsian whether the vehicle is an Asian make
     * @return List of Part objects
     */
    public static List<Part> getPartsForModel(String modelName, boolean isAsian) {
        List<Part> parts = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT sku, part_name, price, model_name FROM parts " +
                        "WHERE (model_name = ? OR model_name IN ('All Models')"
        );
        if (isAsian) sql.append(" OR model_name = 'Asian Vehicles'");
        sql.append(")");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, modelName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    parts.add(new Part(
                            rs.getString("sku"),
                            rs.getString("part_name"),
                            rs.getDouble("price"),
                            rs.getString("model_name")
                    ));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return parts;
    }

    /**
     * getPartsForModelWithAsianArray returns all parts compatible with a given model,
     * using a SQL array for Asian makes check (used in PartsSearchController)
     * @param modelName the vehicle model name
     * @return List of Part objects
     */
    public static List<Part> getPartsForModelWithAsianArray(String modelName) {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT sku, part_name, price, model_name FROM parts " +
                "WHERE model_name = ? " +
                "   OR model_name IN ('All Models') " +
                "   OR (model_name = 'Asian Vehicles' AND EXISTS (" +
                "       SELECT 1 FROM vehicles v " +
                "       WHERE v.model_name = ? " +
                "       AND TRIM(LOWER(v.make)) = ANY(?)" +
                "   ))";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, modelName);
            pstmt.setString(2, modelName);
            String[] makesArray = Vehicle.getAsianMakesArray();
            java.sql.Array sqlArray = conn.createArrayOf("VARCHAR", makesArray);
            pstmt.setArray(3, sqlArray);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    parts.add(new Part(
                            rs.getString("sku"),
                            rs.getString("part_name"),
                            rs.getDouble("price"),
                            rs.getString("model_name")
                    ));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return parts;
    }
}