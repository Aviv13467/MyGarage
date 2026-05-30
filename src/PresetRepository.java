import javafx.scene.control.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresetRepository {

    /**
     * getPresetsForModel returns all distinct preset names for a given model
     * @param modelName the vehicle model to look up
     * @return List of preset name strings
     */
    public static List<String> getPresetsForModel(String modelName) {
        List<String> presets = new ArrayList<>();
        String sql = "SELECT DISTINCT treatment_name FROM presets WHERE model_name = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, modelName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    presets.add(rs.getString("treatment_name"));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return presets;
    }

    /**
     * getPresetParts returns all parts for a given preset, with quantity and total price aggregated
     * @param treatmentName the preset name
     * @param modelName the vehicle model
     * @return List of Part objects
     */
    public static List<Part> getPresetParts(String treatmentName, String modelName) {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT p.sku, p.part_name, (p.price * COUNT(pre.sku)) AS total_price, p.model_name, COUNT(pre.sku) AS total_qty " +
                "FROM presets pre " +
                "JOIN parts p ON pre.sku = p.sku " +
                "WHERE pre.treatment_name = ? AND pre.model_name = ? " +
                "GROUP BY p.sku, p.part_name, p.price, p.model_name";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, treatmentName);
            pstmt.setString(2, modelName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    parts.add(new Part(
                            rs.getString("sku"),
                            rs.getString("part_name"),
                            rs.getDouble("total_price"),
                            rs.getString("model_name"),
                            rs.getInt("total_qty")
                    ));
                }
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return parts;
    }

    /**
     * getPresetPartsForService returns parts for a preset to be loaded into the Add Service screen,
     * filtering by model and Asian vehicle compatibility
     * @param treatmentName the preset name
     * @param modelName the vehicle model
     * @param isAsian whether the vehicle is an Asian make
     * @return List of Part objects
     */
    public static List<Part> getPresetPartsForService(String treatmentName, String modelName, boolean isAsian) {
        List<Part> parts = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.sku, p.part_name, p.price, p.model_name " +
                        "FROM presets pr " +
                        "JOIN parts p ON pr.sku = p.sku " +
                        "WHERE pr.treatment_name = ? " +
                        "AND (p.model_name = ? OR p.model_name = 'All Models'"
        );
        if (isAsian) sql.append(" OR p.model_name = 'Asian Vehicles'");
        sql.append(")");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, treatmentName);
            pstmt.setString(2, modelName);
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
     * savePreset saves a new preset to the DB, overwriting any existing preset with the same name and model
     * @param name the preset name
     * @param model the vehicle model
     * @param parts the list of parts to include in the preset
     * @return true if saved successfully, false otherwise
     */
    public static boolean savePreset(String name, String model, List<Part> parts) {
        String deleteSql = "DELETE FROM presets WHERE treatment_name = ? AND model_name = ?";
        String insertSql = "INSERT INTO presets (treatment_name, model_name, sku) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                    deletePstmt.setString(1, name);
                    deletePstmt.setString(2, model);
                    deletePstmt.executeUpdate();
                }

                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    for (Part part : parts) {
                        for (int i = 0; i < part.getQuantity(); i++) {
                            insertPstmt.setString(1, name);
                            insertPstmt.setString(2, model);
                            insertPstmt.setString(3, part.getSku());
                            insertPstmt.addBatch();
                        }
                    }
                    insertPstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                Utils.showAlert(Alert.AlertType.ERROR, "Error", "Failed saving the preset: " + e.getMessage());
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return false;
    }

    /**
     * deletePreset deletes a preset from the DB
     * @param treatmentName the preset name
     * @param modelName the vehicle model
     */
    public static void deletePreset(String treatmentName, String modelName) {
        String sql = "DELETE FROM presets WHERE treatment_name = ? AND model_name = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, treatmentName);
            pstmt.setString(2, modelName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
    }

    /**
     * getAllModels returns all distinct model names for the preset form's ComboBox
     * @return List of model name strings
     */
    public static List<String> getAllModels() {
        List<String> models = new ArrayList<>();
        String sql = "SELECT DISTINCT model_name FROM parts " +
                "WHERE model_name NOT IN ('Asian Vehicles', 'All Models') " +
                "AND model_name IS NOT NULL " +
                "UNION " +
                "SELECT DISTINCT model_name FROM vehicles";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                models.add(rs.getString("model_name"));
            }
        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Database Error", "Please check your connection.");
        }
        return models;
    }
}