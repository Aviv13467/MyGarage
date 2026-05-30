import javafx.scene.control.Alert;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class ServerMain extends UnicastRemoteObject implements MyGarageRemote {

    protected ServerMain() throws RemoteException {
        super();
    }

    private boolean isServiceDue(Connection conn, String licenseNumber, int currentMileage, String partPattern, int kmInterval, int yearsInterval) throws SQLException {

        String sql = "SELECT MAX(sh.current_km) as last_km, MAX(sh.service_date) as last_date " +
                "FROM service_parts sp " +
                "JOIN service_history sh ON sp.treatment_id = sh.treatment_id " +
                "JOIN parts p ON sp.sku = p.sku " +
                "WHERE sh.license_number = ? AND (LOWER(p.part_name) LIKE ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, licenseNumber);
            pstmt.setString(2, "%" + partPattern.toLowerCase() + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int lastKm = rs.getInt("last_km");
                    java.sql.Date lastSqlDate = rs.getDate("last_date");

                    // if no service history is found, recommend to check
                    if (rs.wasNull() || lastSqlDate == null) {
                        return true;
                    }

                    // calculate differences in mileage/years
                    int mileageDiff = currentMileage - lastKm;
                    long yearsDiff = ChronoUnit.YEARS.between(lastSqlDate.toLocalDate(), LocalDate.now());

                    // check if it's time for service
                    return mileageDiff >= kmInterval || yearsDiff >= yearsInterval;
                }
            }
        }
        return true; // by default if there is no service history, recommend to check
    }

    private boolean checkOilService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // engine oil (10,000km or 1 year)
        return isServiceDue(conn, licenseNumber, currentMileage, "Engine Oil", 10000, 1);
    }

    private boolean checkBrakesService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // brakes (30,000km or 2 years)
        return isServiceDue(conn, licenseNumber, currentMileage, "brake", 30000, 2);
    }

    private boolean checkCoolantService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // coolant (120,000km or 10 years)
        return isServiceDue(conn, licenseNumber, currentMileage, "Coolant", 120000, 10);
    }

    private boolean checkSparkPlugsService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // spark plugs (60,000km or 4 years)
        return isServiceDue(conn, licenseNumber, currentMileage, "spark", 60000, 4);
    }

    private boolean checkTimingService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // timing belt/chain (90,000km or 5 years)
        return isServiceDue(conn, licenseNumber, currentMileage, "timing", 90000, 5);
    }

    private boolean checkTransmissionService(Connection conn, String licenseNumber, int currentMileage) throws SQLException {
        // transmission (80,000km or 4 years)
        return isServiceDue(conn, licenseNumber, currentMileage, "transmission", 80000, 4);
    }

    @Override
    public Map<String, Double> getMonthlyExpenses(String licenseNumber) throws RemoteException { return StatsRepository.getMonthlyExpenses(licenseNumber);}

    @Override
    public String getServiceRecommendations(String licenseNumber, int currentMileage) throws RemoteException {
        String checkMaxKmSql = "SELECT MAX(current_km) as max_km FROM service_history WHERE license_number = ?";
        StringBuilder report = new StringBuilder();

        // Establish a connection to the DB
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkMaxKmSql)) {

            pstmt.setString(1, licenseNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int latestRecordedKm = rs.getInt("max_km");
                    if (!rs.wasNull() && currentMileage < latestRecordedKm) {
                        throw new RemoteException("The entered mileage is lower than the last recorded service ("
                                + latestRecordedKm + " km).\nPlease enter a valid, higher mileage.");
                    }
                }
            }

            // engine oil check
            if (checkOilService(conn, licenseNumber, currentMileage)) report.append("oil,");
            // brake check
            if (checkBrakesService(conn, licenseNumber, currentMileage)) report.append("brakes,");
            // engine coolant check
            if (checkCoolantService(conn, licenseNumber, currentMileage)) report.append("coolant,");
            // spark plug check
            if (checkSparkPlugsService(conn, licenseNumber, currentMileage)) report.append("spark,");
            // timing belt/chain check
            if (checkTimingService(conn, licenseNumber, currentMileage)) report.append("timing,");
            // transmission check
            if (checkTransmissionService(conn, licenseNumber, currentMileage)) report.append("transmission,");

        } catch (SQLException e) {
            Utils.showAlert(Alert.AlertType.ERROR,"Error","Server failed executing recommendations");
            throw new RemoteException("Database error during recommendations framework", e);
        }
        return report.toString();
    }

    @Override
    public double getTotalCostsByRange(String licenseNumber, LocalDate start, LocalDate end) throws RemoteException { return StatsRepository.getTotalCostsByRange(licenseNumber, start, end);}

    @Override
    public List<String> getAllVehiclePlates() throws RemoteException { return VehicleRepository.getAllVehiclePlates(); }

    @Override
    public int getLatestVehicleMileage(String licenseNumber) throws RemoteException { return ServiceRepository.getLatestMileage(licenseNumber); }

    public static void main(String[] args) {
        try {
            try {
                java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(1099);
                registry.list();
            } catch (RemoteException e) {
                java.rmi.registry.LocateRegistry.createRegistry(1099);
            }
            ServerMain server = new ServerMain(); // create a new Server
            java.rmi.Naming.rebind("GarageService", server); // change registry name to GarageService
            System.out.println("RMI Server is running...");
        } catch (Exception e) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
        }
    }
}