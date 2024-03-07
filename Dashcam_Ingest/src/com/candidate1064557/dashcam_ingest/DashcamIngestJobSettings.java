package com.candidate1064557.dashcam_ingest;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * Ingest job options for sample ingest module instances.
 */
public class DashcamIngestJobSettings implements IngestModuleIngestJobSettings {

    private static final long serialVersionUID = 1L;
    private boolean useCalculatedSpeed = true;
    private boolean removeOutliers = true;
    private boolean analyseMov = true;
    private boolean analyseMp4 = true;
    private boolean geofence = true;

    DashcamIngestJobSettings() {
    }

    DashcamIngestJobSettings(boolean useCalculatedSpeed, boolean removeOutliers, boolean analyseMp4, boolean analyseMov, boolean geofence) {
        this.useCalculatedSpeed = useCalculatedSpeed;
        this.removeOutliers = removeOutliers;
        this.analyseMp4 = analyseMp4;
        this.analyseMov = analyseMov;
        this.geofence = geofence;
        
    }

    @Override
    public long getVersionNumber() {
        return serialVersionUID;
    }

    void setUseCalculatedSpeed(boolean enabled) {
        useCalculatedSpeed = enabled;
    }

    boolean useCalculatedSpeed() {
        return useCalculatedSpeed;
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
    
    void setGeofence(boolean enabled) {
        geofence = enabled;
    }

    boolean geofence() {
        return geofence;
    }

}
