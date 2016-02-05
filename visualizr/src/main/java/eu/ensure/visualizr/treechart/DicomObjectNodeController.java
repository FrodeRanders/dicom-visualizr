package eu.ensure.visualizr.treechart;

import eu.ensure.visualizr.VisualizrGuiController;
import eu.ensure.visualizr.model.DicomObject;
import eu.ensure.visualizr.model.DicomTag;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.PlatformLoggingMXBean;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 */
public class DicomObjectNodeController implements Initializable {
    private static final Logger log = LogManager.getLogger(DicomObjectNodeController.class);

    @FXML
    public TitledPane titlePane;

    @FXML
    public TableView tagsTable;

    private ObservableList<DicomTag> observableTags;
    private SimpleStringProperty title = new SimpleStringProperty("");

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        observableTags = FXCollections.observableArrayList();
        tagsTable.setItems(observableTags);

        TableColumn<DicomTag, String> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<DicomTag, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<DicomTag, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        tagsTable.getColumns().setAll(idColumn, descriptionColumn, valueColumn);
        tagsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        titlePane.textProperty().bind(title);
    }

    /**
     *
     *
     */
    public void setDicomObject(DicomObject dicomObject){

        String titleText = dicomObject.getId();
        if (titleText.length() > 0) {
            titleText += " ";
        }
        titleText += dicomObject.getName();
        title.setValue(titleText);

        final Tooltip tooltip = new Tooltip();
        tooltip.setText(dicomObject.getDescription());
        titlePane.setTooltip(tooltip);
        titlePane.setCursor(Cursor.HAND);

        try {
            this.observableTags.addAll(dicomObject.getDicomTags());
            Collections.sort(this.observableTags, new Comparator<DicomTag>(){
                @Override
                public int compare(DicomTag t, DicomTag t1) {
                    return t.getId().compareTo(t1.getId());
                }
            });
        } catch (Exception ex) {
            String info = "Could not observe loading of DICOM object: " + titleText;
            log.info(info, ex);

            observableTags.clear();
            tagsTable.setDisable(true);
        }
    }

    public void setParentController(final VisualizrGuiController controller){
        tagsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DicomTag>() {
            @Override
            public void changed(ObservableValue observableObject, DicomTag oldSelection, DicomTag newSelection) {

                if (null != newSelection) {
                    DicomTagPopup popup = new DicomTagPopup(controller);
                    popup.show(newSelection);
                }
            }
        });
    }
}
