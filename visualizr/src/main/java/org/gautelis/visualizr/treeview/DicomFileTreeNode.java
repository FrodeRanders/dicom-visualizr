package org.gautelis.visualizr.treeview;

import org.gautelis.visualizr.model.DicomFile;

/**
 *
 */
public class DicomFileTreeNode {
    private final String name;
    private final DicomFile dicomFile;

    /**
     * @param name
     */
    public DicomFileTreeNode(String name) {
        this.name = name;
        this.dicomFile = null;
    }

    public DicomFileTreeNode(DicomFile dicomFile) {
        this.dicomFile = dicomFile;
        this.name = dicomFile.getName();
    }

    public boolean isDICOMDIR() {
        return "DICOMDIR".equals(getName());
    }

    public String getName() {
        return name;
    }

    public DicomFile getDicomFile() {
        return dicomFile;
    }
}
