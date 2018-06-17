package org.gautelis.visualizr.treechart;

import org.gautelis.visualizr.VisualizrGuiController;
import org.gautelis.visualizr.model.DicomTag;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Popup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by froran on 2016-02-01.
 */
public class DicomTagPopup extends Popup {
    private static final Logger log = LogManager.getLogger(DicomTagPopup.class);

    private final VisualizrGuiController parentController;
    private DicomTagNodeController popupController = null;

    public DicomTagPopup(VisualizrGuiController parentController) {
        this.parentController = parentController;

        //setScene(parentController.getScene());
        setX(30);
        setY(30);
        setAutoHide(false);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DicomTag.fxml"));
            Node node = fxmlLoader.load();

            if (null != node) {
                getContent().add(node);
            }
            popupController = fxmlLoader.<DicomTagNodeController>getController();
            popupController.setSelectable(false);

        } catch (IOException ioe) {
            String info = "Failed to load DICOM tag popup: " + ioe.getMessage();
            log.warn(info, ioe);
        }
    }

    public void show(DicomTag dicomTag) {
        if (null != popupController) {
            popupController.setDicomTag(dicomTag);
            show(parentController.getStage());
        }
    }
}
