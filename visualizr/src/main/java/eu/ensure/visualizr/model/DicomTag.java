package eu.ensure.visualizr.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by froran on 2016-01-28.
 */
public class DicomTag {
    private static final Logger log = LogManager.getLogger(DicomTag.class);

    private static ElementDictionary dict = ElementDictionary.getStandardElementDictionary();

    private final int tag;
    private final VR vr;
    private final int vm;
    private final String id;
    private final String description;
    private final String value;

    public DicomTag(int tag, VR vr, int vm, String value) {
        this.tag = tag;
        this.vr = vr;
        this.vm = vm;
        this.id = TagUtils.toString(tag);
        this.description = dict.keywordOf(tag);
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getVR() {
        return vr.toString();
    }

    public String getVM() {
        return "" + vm;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPrivate() {
        return TagUtils.isPrivateTag(tag);
    }

    public String getValue() { return value; }

    @Override
    public String toString(){
        return getId() + " " + getDescription();
    }
}
