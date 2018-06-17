package org.gautelis.visualizr.treechart;

import de.chimos.ui.treechart.layout.NodePosition;
import de.chimos.ui.treechart.layout.TreePane;
import org.gautelis.visualizr.VisualizrGuiController;
import org.gautelis.visualizr.model.DicomFile;
import org.gautelis.visualizr.model.DicomObject;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DicomObjectTreeTask extends Task<DicomObjectTreeTask.DicomObjectTreeChart> {
    private static final Logger log = LogManager.getLogger(DicomObjectTreeTask.class);

    public class DicomObjectTreeChart {
        private TreePane pane;
        private final double minX;
        private final double minY;
        private final double maxX;
        private final double maxY;

        public DicomObjectTreeChart(TreePane pane, double minX, double minY, double maxX, double maxY) {
            this.pane = pane;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public TreePane getTreePane() {
            return this.pane;
        }

        public double getMiddleX() {
            return (maxX - minX) / 2.0;
        }

        public double getMiddleY() {
            return (maxY - minY) / 2.0;
        }
    }

    private final VisualizrGuiController caller;
    private final DicomFile dicomFile;

    private static final double X_SPACING = 80d;
    private static final double Y_SPACING = 100d;

    public DicomObjectTreeTask(DicomFile dicomFile, VisualizrGuiController caller) {
        this.dicomFile = dicomFile;
        this.caller = caller;
    }

    /**
     */
    private Node loadDicomObject(DicomObject dicomObject) {
        Node element = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DicomObject.fxml"));
            element = fxmlLoader.load();

            DicomObjectNodeController elementController = fxmlLoader.<DicomObjectNodeController>getController();
            elementController.setDicomObject(dicomObject);
            elementController.setParentController(caller);

        } catch (IOException ex) {
            String info = "Could not load DICOM object onto chart area: " + dicomObject.getName();
            log.error(info, ex);
        }
        return element;
    }


    /**
     */
    private Node loadTreeNodeChildren(DicomObject dicomObject, TreePane treePane, NodePosition parentPosition) throws Exception {

        NodePosition position;
        if (null == parentPosition) {
            position = NodePosition.ROOT;
        } else {
            position = parentPosition.getChild(0);
        }

        final Node node = loadDicomObject(dicomObject);
        treePane.addChild(node, position);

        int childIndex = 0;
        for (DicomObject sequence : dicomObject.getSequences()) {
            NodePosition childPosition = position.getChild(childIndex++);
            updateConvexHull(loadTreeNodeChildren(sequence, treePane, childPosition));
        }
        return node;
    }

    double  minX = Double.MAX_VALUE,
            minY = Double.MAX_VALUE,
            maxX = 0.0,
            maxY = 0.0;

    private void updateConvexHull(Node node) {
        double x = node.getLayoutX();
        double y = node.getLayoutY();
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    @Override
    protected DicomObjectTreeChart call() throws Exception {
        TreePane treePane = new TreePane();
        treePane.setXAxisSpacing(X_SPACING);
        treePane.setYAxisSpacing(Y_SPACING);

        minX = minY = Double.MAX_VALUE;
        maxX = maxY = 0.0;

        updateConvexHull(loadTreeNodeChildren(dicomFile.getRootObject(), treePane, null));
        return new DicomObjectTreeChart(treePane, minX, minY, maxX, maxY);
    }
}
