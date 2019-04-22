package org.gautelis.visualizr;

import de.chimos.ui.treechart.layout.TreePane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

/**
 *
 */
public class SizeableScrollPane extends ScrollPane {

    private static final double ZOOM_MAX_SCALE = 1d;
    private static final double ZOOM_MIN_SCALE = .1d;
    private static final double ZOOM_DELTA = .008d;

    private double middleX;
    private double middleY;

    /**
     *
     */
    public SizeableScrollPane() {

        setPannable(true);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        addEventFilter(ScrollEvent.ANY, event -> {
            TreePane treePane = (TreePane) getContent();

            // Original size of the image.
            double treePaneWidth = treePane.getWidth();
            double treePaneHeight = treePane.getHeight();

            // Old values of the scrollbars.
            double oldHvalue = getHvalue();
            double oldVvalue = getVvalue();

            // Tree pane pixels outside the visible area which need to be scrolled.
            double scrollXFactor = Math.max(0.0001, treePaneWidth - getWidth());
            double scrollYFactor = Math.max(0.0001, treePaneHeight - getHeight());

            // Relative position of the mouse in the tree pane.
            double mouseXPosition = (scrollXFactor * oldHvalue) / treePaneWidth;
            double mouseYPosition = (scrollYFactor * oldVvalue) / treePaneHeight;

            // New scrollbar positions keeping the mouse position.
            double newHvalue = ((mouseXPosition * treePaneWidth)) / scrollXFactor;
            double newVvalue = ((mouseYPosition * treePaneHeight)) / scrollYFactor;

            setHvalue(newHvalue);
            setVvalue(newVvalue);
        });

        addEventFilter(ZoomEvent.ANY, event -> {
            double zoomFactor = event.getZoomFactor();
            if (!Double.isNaN(zoomFactor)) {
                TreePane treePane = (TreePane) getContent();

                boolean zoomingOut = zoomFactor < 1.0;
                double delta = (zoomingOut ? -ZOOM_DELTA : ZOOM_DELTA);
                double scale = treePane.getScaleX() + delta;

                if (scale <= ZOOM_MIN_SCALE) {
                    scale = ZOOM_MIN_SCALE;
                } else if (scale >= ZOOM_MAX_SCALE) {
                    scale = ZOOM_MAX_SCALE;
                }

                if (zoomingOut) {
                    // Original size of the image.
                    double treePaneWidth = treePane.getWidth();
                    double treePaneHeight = treePane.getHeight();

                    // Old values of the scrollbars.
                    double oldHvalue = getHvalue();
                    double oldVvalue = getVvalue();

                    // Tree pane pixels outside the visible area which need to be scrolled.
                    double scrollXFactor = Math.max(0.0001, treePaneWidth - getWidth());
                    double scrollYFactor = Math.max(0.0001, treePaneHeight - getHeight());

                    // Relative position of the mouse in the tree pane.
                    double mouseXPosition = (scrollXFactor * oldHvalue) / treePaneWidth;
                    double mouseYPosition = (scrollYFactor * oldVvalue) / treePaneHeight;

                    double translationX = (middleX - (mouseXPosition * treePaneWidth)) / 50.0;
                    double translationY = (middleY - (mouseYPosition * treePaneHeight)) / 250.0;

                    // New scrollbar positions keeping the mouse position.
                    double newHvalue = ((mouseXPosition * treePaneWidth) + translationX) / scrollXFactor;
                    double newVvalue = ((mouseYPosition * treePaneHeight) + translationY) / scrollYFactor;

                    setHvalue(newHvalue);
                    setVvalue(newVvalue);
                }

                treePane.setScaleX(scale);
                treePane.setScaleY(scale);
            }
        });
    }

    public void setMiddlePoint(double middleX, double middleY) {
        this.middleX = middleX;
        this.middleY = middleY;
    }
}