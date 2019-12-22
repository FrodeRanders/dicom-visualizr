package org.gautelis.visualizr.treechart;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gautelis.visualizr.VisualizrGuiController;
import org.gautelis.visualizr.model.DicomObject;
import org.gautelis.visualizr.model.DicomTag;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 */
public class DicomObjectNodeController implements Initializable {
    private static final Logger log = LogManager.getLogger(DicomObjectNodeController.class);

    @FXML
    public AnchorPane anchorPane;

    @FXML
    public TitledPane titlePane;

    @FXML
    public TableView<DicomTag> tagsTable;

    private DicomObject dicomObject = null;
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

        tagsTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                dumpStructureToClipboard();
            }
        });

        tagsTable.setOnKeyPressed(event -> dumpStructureToClipboard());
    }

    private void dumpStructureToClipboard() {
        log.debug("Creating tree-dump in clipboard");
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        String text = dicomObject.asStructuredText();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     *
     */
    public void setDicomObject(DicomObject dicomObject) {
        this.dicomObject = dicomObject;

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
            this.observableTags.sort(Comparator.comparing(DicomTag::getId));
        } catch (Exception ex) {
            String info = "Could not observe loading of DICOM object: " + titleText;
            log.info(info, ex);

            observableTags.clear();
            tagsTable.setDisable(true);
        }
    }

    public void setParentController(final VisualizrGuiController controller) {
        tagsTable.getSelectionModel().selectedItemProperty().addListener((observableObject, oldSelection, newSelection) -> {

            if (null != newSelection) {
                DicomTagPopup popup = new DicomTagPopup(controller);
                popup.show(newSelection);
            }
        });
    }
}
