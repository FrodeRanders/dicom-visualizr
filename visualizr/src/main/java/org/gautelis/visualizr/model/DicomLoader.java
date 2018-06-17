package org.gautelis.visualizr.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by froran on 2016-01-28.
 */
public class DicomLoader {
    private static final Logger log = LogManager.getLogger(DicomLoader.class);

    private List<DicomFile> loadedFiles = new ArrayList<DicomFile>();
    private String fileName;

    /**
     * Load a DICOM file.
     *
     * @param dicomFile
     * @throws IOException In case of IO error.
     */
    public DicomLoader(File dicomFile) throws IOException {
        fileName = dicomFile.getName();

        try (DicomInputStream dicomInputStream = new DicomInputStream(new FileInputStream(dicomFile))) {
            Attributes ds = dicomInputStream.readDataset(-1, -1);

            if ("DICOMDIR".equals(fileName)) {
                loadDICOMDIR(ds, dicomFile);
            } else {
                // Not really supported at the moment
                loadFile(ds, dicomFile);
            }
        }
    }

    /**
     * Get all DICOM files associated with DICOMDIR
     *
     * @return The list of classes.
     */
    public List<DicomFile> getFiles() {
        return this.loadedFiles;
    }


    public String getFileName() {
        return this.fileName;
    }


    public void loadDICOMDIR(final Attributes dataset, final File file) throws IOException {

        Map<String, String> data = new HashMap<>();
        DicomFile dicomdirFile = new DicomFile(new DicomObject(file.getName(), dataset), file);
        loadedFiles.add(dicomdirFile);

        // (0004,1220) (DirectoryRecordSequence)
        Sequence sequence = dataset.getSequence(TagUtils.toTag(0x0004, 0x1220));
        if (null != sequence) {
            for (int i = 0; i < sequence.size(); i++) {
                Attributes record = sequence.get(i);
                String recordType = DicomObject.directoryRecordType(record);

                switch (recordType) {
                    case "PATIENT":
                        data.put("PatientID", DicomObject.patientID(record));
                        break;

                    case "STUDY":
                        data.put("StudyInstanceUID", DicomObject.studyInstanceUID(record));
                        break;

                    case "SR DOCUMENT": {
                        /*
                        String seriesInstanceUid = DicomObject.seriesInstanceUID(record);
                        String seriesDescription = DicomObject.seriesDescription(record);
                        String sopInstanceUid = DicomObject.sopInstanceUID(record);
                        String modality = DicomObject.modality(record);
                        String physicianName = DicomObject.performingPhysicianName(record);
                        */

                        File referencedFile = file.getParentFile(); // Start relative to DICOMDIR
                        String[] referencedFileId = record.getStrings(TagUtils.toTag(0x0004, 0x1500));
                        if (null != referencedFileId) {
                            for (String part : referencedFileId) {
                                referencedFile = new File(referencedFile, part);
                            }
                        }

                        if (referencedFile.exists() && referencedFile.canRead()) {
                            String info = "Referenced file: " + referencedFile.getPath();
                            log.debug(info);

                            try (DicomInputStream dicomInputStream = new DicomInputStream(new FileInputStream(referencedFile))) {
                                Attributes ds = dicomInputStream.readDataset(-1, -1);
                                loadFile(ds, referencedFile);
                            }
                        } else {
                            String info = "Referenced file does not exist: " + referencedFile.getPath();
                            log.warn(info);
                        }
                    }
                    break;

                    case "IMAGE": {
                        /*
                        String seriesInstanceUid = DicomObject.seriesInstanceUID(record);
                        String seriesDescription = DicomObject.seriesDescription(record);
                        String sopInstanceUid = DicomObject.sopInstanceUID(record);
                        String modality = DicomObject.modality(record);
                        String physicianName = DicomObject.performingPhysicianName(record);
                        */

                        File referencedFile = file.getParentFile(); // Start relative to DICOMDIR
                        String[] referencedFileId = record.getStrings(TagUtils.toTag(0x0004, 0x1500));
                        if (null != referencedFileId) {
                            for (String part : referencedFileId) {
                                referencedFile = new File(referencedFile, part);
                            }
                        }

                        if (referencedFile.exists() && referencedFile.canRead()) {
                            String info = "Referenced file: " + referencedFile.getPath();
                            log.debug(info);

                            try (DicomInputStream dicomInputStream = new DicomInputStream(new FileInputStream(referencedFile))) {
                                Attributes ds = dicomInputStream.readDataset(-1, -1);
                                loadFile(ds, referencedFile);
                            }
                        } else {
                            String info = "Referenced file does not exist: " + referencedFile.getPath();
                            log.warn(info);
                        }
                    }
                    break;

                    case "SERIES":
                    default:
                        break;
                }

                log.debug("------------------------------------------------------------------------------------------");
            }
        }
    }

    public void loadFile(final Attributes dataset, final File file) throws IOException {

        //Map<String, String> data = new HashMap<>();
        DicomFile dicomFile = new DicomFile(new DicomObject(file.getName(), dataset), file);
        loadedFiles.add(dicomFile);

        /*
        Sequence sequence = dataset.getSequence(TagUtils.toTag(0x0004, 0x1220));
        if (null != sequence) {
            for (int i = 0; i < sequence.size(); i++) {
                Attributes record = sequence.get(i);

                log.debug("------------------------------------------------------------------------------------------");
            }
        }
        */
    }
}
