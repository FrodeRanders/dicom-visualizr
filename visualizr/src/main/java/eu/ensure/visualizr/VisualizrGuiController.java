package eu.ensure.visualizr;

import de.chimos.ui.treechart.layout.TreePane;
import eu.ensure.visualizr.model.DicomFile;
import eu.ensure.visualizr.model.DicomLoader;
import eu.ensure.visualizr.treechart.DicomObjectTreeTask;
import eu.ensure.visualizr.treeview.DicomFileTreeNode;
import eu.ensure.visualizr.treeview.DicomFileTreeViewTask;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by froran on 2016-01-28.
 */
public class VisualizrGuiController implements Initializable {
    private static final Logger log = LogManager.getLogger(VisualizrGuiController.class);

    private static final double ZOOM_MAX_SCALE = 1d;
    private static final double ZOOM_MIN_SCALE = .1d;
    private static final double ZOOM_DELTA = .1d;

    @FXML
    public Pane rootPane;
    @FXML
    public TreeView fileList;
    @FXML
    public SizeableScrollPane scrollPane;
    @FXML
    public HBox pathBox;

    private FileChooser fileChooser;
    private SimpleBooleanProperty loading;

    /**
     * Prompt the a open dialog and call {@link VisualizrGuiController#doLoad(java.io.File)} with the selected file.
     */
    @FXML
    public void handleOpen(){
        File file = this.fileChooser.showOpenDialog(this.getScene().getWindow());
        if (file != null && file.canRead()){
            this.doLoad(file);
        }
    }

    /**
     * Scan a DICOM file
     * @param dicomdir
     */
    public void doLoad(File dicomdir){
        loading.set(true);
        unloadTreeChart();

        DicomLoaderTask loaderBuilder = new DicomLoaderTask(dicomdir);
        loaderBuilder.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                DicomLoader loader = (DicomLoader) event.getSource().getValue();

                DicomFileTreeViewTask accordionBuilder = new DicomFileTreeViewTask(loader.getFiles(), loader.getFileName(), fileList);
                accordionBuilder.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    public void handle(WorkerStateEvent t) {
                        TreeItem<DicomFileTreeNode> root = (TreeItem<DicomFileTreeNode>) t.getSource().getValue();
                        fileList.setRoot(root);

                        loading.set(false);
                    }
                });
                accordionBuilder.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        new Alert(Alert.AlertType.ERROR, "Could not create list of packages", ButtonType.OK).showAndWait();

                        loading.set(false);
                    }
                });
                new Thread(accordionBuilder).start();
            }
        });
        loaderBuilder.setOnFailed(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent t) {
                loading.set(false);
            }
        });
        new Thread(loaderBuilder).start();
    }

    /**
     * Close the application.
     */
    @FXML
    public void handleClose(){
        System.exit(0);
    }

    /**
     * Get the main scene.
     *
     * @return The main scene.
     */
    public Scene getScene(){
        return rootPane.getScene();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loading = new SimpleBooleanProperty(false);

        scrollPane.setScaleX(1d);
        scrollPane.setScaleY(1d);
        scrollPane.setScaleZ(1d);
        scrollPane.setPannable(true);

        fileList.disableProperty().bind(this.loading);
        fileList.setShowRoot(false);
        fileList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<DicomFileTreeNode>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<DicomFileTreeNode>> observable, TreeItem<DicomFileTreeNode> oldSelection, TreeItem<DicomFileTreeNode> newSelection) {
                if (newSelection != null){
                    DicomFile dicomFile = newSelection.getValue().getDicomFile();
                    if (null != dicomFile) {
                        loadTreeChart(dicomFile);
                    }
                }
                else{
                    unloadTreeChart();
                }
            }
        });
        fileList.setCellFactory(new Callback<TreeView<DicomFileTreeNode>, TreeCell<DicomFileTreeNode>>() {
            private Image folderImage = new Image(VisualizrGuiController.class.getResourceAsStream("folder.png"));
            private Image fileImage = new Image(VisualizrGuiController.class.getResourceAsStream("file.png"));

            @Override
            public TreeCell<DicomFileTreeNode> call(TreeView<DicomFileTreeNode> p) {
                final Tooltip tooltip = new Tooltip();
                final TreeCell<DicomFileTreeNode> cell = new TreeCell<DicomFileTreeNode>() {
                    @Override
                    public void updateItem(DicomFileTreeNode item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            ImageView iv = new ImageView();
                            iv.setFitHeight(16);
                            iv.setFitWidth(16);
                            iv.setPreserveRatio(true);

                            if (item.isDICOMDIR()) {
                                setText(item.getName());
                                iv.setImage(folderImage);

                            } else {
                                setText(item.getName());
                                iv.setImage(fileImage);
                                iv.setOpacity(0.5d);

                                DicomFile dicomFile = item.getDicomFile();
                                if (null != dicomFile) {
                                    switch (dicomFile.getType()) {
                                        case Basic_Text_SR:
                                        case Enhanced_SR:
                                        case Comprehensive_SR:
                                            this.setTextFill(Color.DARKBLUE); // DARKGREEN, DARKORANGE
                                            break;

                                        case Unknown:
                                            this.setTextFill(Color.GRAY);
                                            break;

                                        default:
                                            this.setTextFill(Color.BLACK);
                                            break;
                                    }

                                    tooltip.setText(dicomFile.getType().getDescription());
                                    setTooltip(tooltip);
                                    setCursor(Cursor.HAND);
                                }
                            }
                            this.setGraphic(iv);
                        }
                    }
                };
                return cell;
            }
        });

        scrollPane.disableProperty().bind(this.loading);
        scrollPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                scrollPane.requestFocus();
            }
        });

        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>(){
            public void handle(ScrollEvent t) {
                if (t.isShortcutDown()) {
                    if(t.getDeltaY() > 0) {
                        handleZoomIn();
                    }
                    else{
                        handleZoomOut();
                    }
                    t.consume();
                }
            }
        });

        fileChooser = new FileChooser();
        fileChooser.setTitle("Select DICOM file (DICOMDIR)");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DICOM files", "DICOMDIR", "*.DCM"));
    }

    @FXML
    public void handlePrint() {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.A0, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
        //double scaleX = pageLayout.getPrintableWidth() / scrollPane.getBoundsInParent().getWidth();
        //double scaleY = pageLayout.getPrintableHeight() / scrollPane.getBoundsInParent().getHeight();
        //scrollPane.getTransforms().add(new Scale(scaleX, scaleY));

        PrinterJob printerJob = PrinterJob.createPrinterJob(printer);
        if (printerJob.showPrintDialog(stage.getOwner())) {
            boolean success = printerJob.printPage(pageLayout, scrollPane);
            if (success) {
                printerJob.endJob();
            }
        }
    }

    @FXML
    public void handleZoomIn(){
        doZoom(ZOOM_DELTA);
    }

    @FXML
    public void handleZoomOut(){
        doZoom(-ZOOM_DELTA);
    }

    private void doZoom(double delta) {
        // Observe: This takes precedence over zooming in SizeableScrollPane

        final TreePane scrollContent = (TreePane) scrollPane.getContent();

        double scale = scrollContent.getScaleX() + delta;

        if (scale <= ZOOM_MIN_SCALE) {
            scale = ZOOM_MIN_SCALE;
        }
        else if (scale >= ZOOM_MAX_SCALE) {
            scale = ZOOM_MAX_SCALE;
        }

        //scrollContent.setLayoutX(Math.signum(delta) * 100d);

        scrollContent.setScaleX(scale);
        scrollContent.setScaleY(scale);

    }

    public void loadTreeChart(final DicomFile dicomFile){
        loading.set(true);

        unloadTreeChart();

        DicomObjectTreeTask treeBuilder = new DicomObjectTreeTask(dicomFile, this);
        treeBuilder.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                DicomObjectTreeTask.DicomObjectTreeChart treeChart = (DicomObjectTreeTask.DicomObjectTreeChart) event.getSource().getValue();

                scrollPane.setContent(treeChart.getTreePane());
                scrollPane.setMiddlePoint(treeChart.getMiddleX(), treeChart.getMiddleY());
                setPath(dicomFile);

                loading.set(false);
            }
        });
        treeBuilder.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle(WorkerStateEvent t) {
                new Alert(Alert.AlertType.WARNING, "Tree construction error.", ButtonType.OK).showAndWait();

                loading.set(false);
            }
        });
        new Thread(treeBuilder).start();
    }

    private void setPath(DicomFile dicomFile){
        try {
            clearPath();
            Label label = new Label(dicomFile.getPath());
            pathBox.getChildren().add(label);

        } catch (Exception ex) {
            pathBox.getChildren().add(new Label("?"));
            String info = "Could not determine path to file: " + dicomFile;
            log.info(info, ex);
        }
    }

    private void clearPath() {
        if (null != pathBox) {
            pathBox.getChildren().clear();
        }
    }

    /**
     * Clear the tree chart view.
     */
    public void unloadTreeChart(){
        this.clearPath();
        this.scrollPane.setContent(new Pane());
    }

    private Stage stage;
    public void setStage(Stage stage){
        this.stage = stage;
    }
    public Stage getStage(){
        return stage;
    }
}
