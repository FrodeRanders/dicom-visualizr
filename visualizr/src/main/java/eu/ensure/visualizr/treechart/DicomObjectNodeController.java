package eu.ensure.visualizr.treechart;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

import eu.ensure.visualizr.VisualizrGuiController;
import eu.ensure.visualizr.model.DicomObject;
import eu.ensure.visualizr.model.DicomTag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private DicomObject dicomObject;
    private ObservableList<DicomTag> observableTags;
    private SimpleStringProperty title;
    private SimpleStringProperty titlePre;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        observableTags = FXCollections.observableArrayList();
        tagsTable.setItems(observableTags);

        TableColumn<DicomTag, String> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("id"));

        TableColumn<DicomTag, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("description"));

        TableColumn<DicomTag, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("value"));

        tagsTable.getColumns().setAll(idColumn, descriptionColumn, valueColumn);
        tagsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        this.title = new SimpleStringProperty("");
        this.titlePre = new SimpleStringProperty("");
        this.titlePane.textProperty().bind(this.titlePre.concat(this.title));
    }

    /**
     *
     *
     */
    public void setDicomObject(DicomObject dicomObject){
        this.dicomObject = dicomObject;

        String name = dicomObject.getName();
        title.setValue(name);

        try {
            this.observableTags.addAll(dicomObject.getDicomTags());
            Collections.sort(this.observableTags, new Comparator<DicomTag>(){
                @Override
                public int compare(DicomTag t, DicomTag t1) {
                    return t.getId().compareTo(t1.getId());
                }
            });
        } catch (Exception ex) {
            String info = "Could not observe loading of DICOM object: " + name;
            log.info(info, ex);

            this.observableTags.clear();
            tagsTable.setDisable(true);
        }
    }

    public void setTitlePrefix(String prefix){
        this.titlePre.set(prefix);
    }

    public void setParentController(final VisualizrGuiController controller){
        tagsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DicomTag>() {
            @Override
            public void changed(ObservableValue observableObject, DicomTag oldSelection, DicomTag newSelection) {

                if (null != newSelection) {
                    DicomTagPopup popup = controller.getTagPopup();
                    popup.show(newSelection);
                }
            }
        });
    }
}
