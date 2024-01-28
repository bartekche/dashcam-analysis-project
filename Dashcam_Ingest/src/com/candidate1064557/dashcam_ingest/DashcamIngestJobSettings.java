
package com.candidate1064557.dashcam_ingest;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * Ingest job options for sample ingest module instances.
 */
public class DashcamIngestJobSettings implements IngestModuleIngestJobSettings {

    private static final long serialVersionUID = 1L;
    private boolean skipKnownFiles = true;
    private boolean removeOutliers = true;
    private boolean analyseMov = true;
    private boolean analyseMp4 = true;

    DashcamIngestJobSettings() {
    }

    DashcamIngestJobSettings(boolean skipKnownFiles, boolean removeOutliers, boolean analyseMp4, boolean analyseMov) {
        this.skipKnownFiles = skipKnownFiles;
        this.removeOutliers = removeOutliers;
        this.analyseMp4 = analyseMp4;
        this.analyseMov = analyseMov;
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
    void setAnalyseMov(boolean enabled) {
        analyseMov = enabled;
    }

    boolean analyseMov() {
        return analyseMov;
    }
    void setAnalyseMp4(boolean enabled) {
        analyseMp4 = enabled;
    }

    boolean analyseMp4() {
        return analyseMp4;
    }
    
}
