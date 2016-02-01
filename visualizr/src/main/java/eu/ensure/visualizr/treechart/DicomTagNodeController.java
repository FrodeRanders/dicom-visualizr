package eu.ensure.visualizr.treechart;

import eu.ensure.visualizr.VisualizrGuiController;
import eu.ensure.visualizr.model.DicomObject;
import eu.ensure.visualizr.model.DicomTag;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 */
public class DicomTagNodeController implements Initializable {
    private static final Logger log = LogManager.getLogger(DicomTagNodeController.class);

    @FXML
    public TitledPane titlePane;

    @FXML
    public TableView tagTable;

    @FXML
    public TextArea valueText;

    private ObservableList<DicomTag> observableTags; // Just one :)
    private SimpleStringProperty title;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        observableTags = FXCollections.observableArrayList();
        tagTable.setItems(observableTags);

        TableColumn<DicomTag, String> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("id"));

        TableColumn<DicomTag, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("description"));

        TableColumn<DicomTag, String> vrColumn = new TableColumn<>("VR");
        vrColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("VR"));

        TableColumn<DicomTag, String> vmColumn = new TableColumn<>("VM");
        vmColumn.setCellValueFactory(new PropertyValueFactory<DicomTag, String>("VM"));

        tagTable.getColumns().setAll(idColumn, descriptionColumn, vrColumn, vmColumn);
        tagTable.setSelectionModel(new NullTableViewSelectionModel(tagTable));


        /*
        tagTable.setFixedCellSize(2); // Title row + one value row
        tagTable.prefHeightProperty().bind(tagTable.fixedCellSizeProperty().multiply(Bindings.size(tagTable.getItems()).add(2.01)));
        tagTable.minHeightProperty().bind(tagTable.prefHeightProperty());
        tagTable.maxHeightProperty().bind(tagTable.prefHeightProperty());
        */

        title = new SimpleStringProperty("");
        titlePane.textProperty().bind(title);
    }

    /**
     *
     *
     */
    public void setDicomTag(DicomTag dicomTag){
        String id = dicomTag.getId();
        title.setValue(id + " " + dicomTag.getDescription());

        try {
            observableTags.clear();
            observableTags.addAll(dicomTag);

            valueText.setWrapText(true);
            valueText.setText(dicomTag.getValue());

        } catch (Exception ex) {
            String info = "Could not observe loading of DICOM tag: " + id;
            log.info(info, ex);

            this.observableTags.clear();
            tagTable.setDisable(true);
        }
    }
}
