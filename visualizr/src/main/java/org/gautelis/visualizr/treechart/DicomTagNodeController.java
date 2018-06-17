package org.gautelis.visualizr.treechart;

import org.gautelis.visualizr.model.DicomTag;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
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

    private double xOffset = 0d;
    private double yOffset = 0d;

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

    public void setSelectable(boolean isSelectable) {
        if (!isSelectable) {
            tagTable.getSelectionModel().selectedItemProperty().addListener((c, oldSelection, newSelection) -> {
                if (null != newSelection) {
                    Platform.runLater(() -> tagTable.getSelectionModel().clearSelection());
                }
            });

            valueText.setEditable(false);
        }
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
