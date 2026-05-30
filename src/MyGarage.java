import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;

public class MyGarage extends Application {

    /**
     * start function starts the Application loading the Main Menu screen
     */
    @Override
    public void start(Stage stage) throws Exception {
        startRMIServer();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("resources/fxml/MainMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),400 ,600 );
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("resources/style.css")).toExternalForm());
        stage.setTitle("MyGarage");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * startRMIServer function starts the RMI server
     */
    private void startRMIServer() {
        Thread serverThread = new Thread(() -> {
            try {
                ServerMain.main(new String[]{});
            } catch (Exception e) {
                Utils.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
            }
        });

        // Set Daemon so the server will close automatically when the Application is closed
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}