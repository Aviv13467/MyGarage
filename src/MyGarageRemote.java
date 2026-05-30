import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MyGarageRemote extends Remote {
    Map<String, Double> getMonthlyExpenses(String licenseNumber) throws RemoteException;
    String getServiceRecommendations(String licenseNumber, int currentMileage) throws RemoteException;
    double getTotalCostsByRange(String licenseNumber, LocalDate start, LocalDate end) throws RemoteException;
    List<String> getAllVehiclePlates() throws RemoteException;
    int getLatestVehicleMileage(String licenseNumber) throws RemoteException;
}