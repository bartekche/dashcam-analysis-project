
package com.candidate1064557.dashcam_ingest;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * Ingest job options for sample ingest module instances.
 */
public class DashcamIngestJobSettings implements IngestModuleIngestJobSettings {

    private static final long serialVersionUID = 1L;
    private boolean skipKnownFiles = true;
    private boolean removeOutliers = true;

    DashcamIngestJobSettings() {
    }

    DashcamIngestJobSettings(boolean skipKnownFiles, boolean removeOutliers) {
        this.skipKnownFiles = skipKnownFiles;
        this.removeOutliers = removeOutliers;
    }

    @Override
    public long getVersionNumber() {
        return serialVersionUID;
    }

    void setSkipKnownFiles(boolean enabled) {
        skipKnownFiles = enabled;
    }

    boolean skipKnownFiles() {
        return skipKnownFiles;
    }
    
    void setRemoveOutliers(boolean enabled) {
        removeOutliers = enabled;
    }

    boolean removeOutliers() {
        return removeOutliers;
    }
    
}
