package org.redcross.openmapkit.odkcollect;

import android.os.Environment;

import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMXmlWriter;

import org.redcross.openmapkit.ExternalStorage;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 2/9/15.
 * nhallahan@spatialdev.com
 * * * 
 */
public class ODKCollectData {
    
    private String formId;
    private String instanceId;
    private String instanceDir;
    private List<ODKTag> requiredTags;
    private List<File> editedOSM = new ArrayList<>();
    
    private String editedXml;
    private String osmClassName;
    private long osmId;
    
    public ODKCollectData ( String formId, 
                            String formFileName,
                            String instanceId, 
                            String instanceDir, 
                            List<ODKTag> requiredTags ) {
        this.formId = formId;
        this.instanceId = instanceId;
        this.instanceDir = instanceDir;
        this.requiredTags = requiredTags;
        findEditedOSMForForm(formFileName);
    }

    private void findEditedOSMForForm(String formFileName) {
        String instances = new File(instanceDir).getParent();
        File[] instancesDirs = new File(instances).listFiles();
        for (int i = 0; i < instancesDirs.length; ++i) {
            File dir = instancesDirs[i];
            if (!dir.isDirectory()) {
                continue;
            }
            // check if the instance dir is for the form we are dealing with
            // it is 0 if the form file name is the first substring of the dirname
            if (dir.getName().indexOf(formFileName) != 0) {
                continue;
            }
            
            String[] files = dir.list();
            for (int j = 0; j < files.length; ++j) {
                String fname = files[j];
                if (fname.lastIndexOf(".osm") > -1) {
                    File osmFile = new File(dir, fname);
                    editedOSM.add(osmFile);
                }
            }
        }
    }
    
    public List<File> getEditedOSM() {
        return editedOSM;
    }

    public String getFormId() {
        return formId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceDir() {
        return instanceDir;
    }

    public List<ODKTag> getRequiredTags() {
        return requiredTags;
    }
    
    public void consumeOSMElement(OSMElement el) throws IOException {
        osmClassName = el.getClass().getSimpleName();
        osmId = el.getId();
        editedXml = OSMXmlWriter.elementToString(el, "theoutpost");
    }
    
    public void writeXmlToOdkCollectInstanceDir() throws IOException {
        if ( ! isODKCollectInstanceDirectoryAvailable() ) {
            throw new IOException("The ODK Collect Instance Directory cannot be accessed!");
        }
        File f = new File( getOSMFileFullPath() );
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        writer.append(editedXml);
        writer.close();
        fos.close();
    }

    public String getOSMFileName() {
        return osmClassName + osmId + ".osm";
    }
    
    public String getOSMFileFullPath() {
        return instanceDir + "/" + getOSMFileName();
    }

    
    private boolean isODKCollectInstanceDirectoryAvailable() {
        if ( ! ExternalStorage.isWritable() ) {
            return false;
        }
        File dir = new File(instanceDir);
        if (dir.exists()) {
            return true;
        }
        return false;
    }
    
}
