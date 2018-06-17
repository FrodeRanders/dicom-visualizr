package org.gautelis.visualizr.treeview;

import org.gautelis.visualizr.model.DicomFile;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class DicomFileTreeViewTask extends Task<TreeItem<DicomFileTreeNode>> {

    private final List<DicomFile> files;
    private final TreeView treeView;
    private final TreeItem<DicomFileTreeNode> root;

    public DicomFileTreeViewTask(List<DicomFile> files, String rootName, TreeView treeView) {
        this.files = files;
        this.treeView = treeView;

        root = new TreeItem<>(new DicomFileTreeNode(rootName));
    }

    private void addItemToTree(DicomFile dicomFile) {
        final TreeItem<DicomFileTreeNode> node = new TreeItem<DicomFileTreeNode>(new DicomFileTreeNode(dicomFile));

        if (root.getChildren().size() > 0) {
            TreeItem<DicomFileTreeNode> dicomdir = root.getChildren().get(0);
            dicomdir.getChildren().add(node);
        } else {
            node.setExpanded(true);
            /*
            node.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeItem.TreeModificationEvent<DicomFileTreeNode>>() {

                @Override
                public void handle(TreeItem.TreeModificationEvent<DicomFileTreeNode> event) {
                    treeView.refresh();
                }
            });
            node.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent<DicomFileTreeNode>>() {
                @Override
                public void handle(TreeItem.TreeModificationEvent<DicomFileTreeNode> event) {
                    treeView.refresh();
                }
            });
            */
            root.getChildren().add(node); // DICOMDIR
        }
    }

    @Override
    protected TreeItem<DicomFileTreeNode> call() throws Exception {
        try{
            Collections.sort(this.files, new Comparator<DicomFile>() {
                public int compare(DicomFile o1, DicomFile o2) {
                    if (o1.getName().equals(o2.getName())) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        if (o1.getName().startsWith(o2.getName())) {
                            return -1;
                        } else if (o2.getName().startsWith(o1.getName())) {
                            return 1;
                        } else {
                            return o1.getName().compareTo(o2.getName());
                        }
                    }
                }
            });

            for (DicomFile file : this.files){
                this.addItemToTree(file);
            }
            this.updateProgress(1, 1);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return this.root;
    }
}
