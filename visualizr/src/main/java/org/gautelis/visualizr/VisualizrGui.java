package org.gautelis.visualizr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by froran on 2016-01-28.
 */
public class VisualizrGui extends Application {
    private static final Logger log = LogManager.getLogger(VisualizrGui.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.DECORATED);

        // Get on known turf and don't use whatever is floating around on the system
        if (!System.getProperty("file.encoding").equals("ISO-8859-1")) {
            System.out.println(
                    "Changing system encoding from '" + System.getProperty("file.encoding") +
                            "' to 'ISO-8859-1'"
            );
            System.setProperty("file.encoding", "ISO-8859-1");
        }

        //
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("VisualizrGui.fxml"));
        Parent root = fxmlLoader.load();
        VisualizrGuiController controller = fxmlLoader.<VisualizrGuiController>getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(VisualizrGui.class.getResource("style.css").toURI().toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("DICOM Visualizr");
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
