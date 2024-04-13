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
    private String latitudeGeofence = "0.000";
    private String longitudeGeofence = "0.000";
    private String radiusGeofence = "0.00";

    DashcamIngestJobSettings() {
    }

    DashcamIngestJobSettings(boolean useCalculatedSpeed, boolean removeOutliers, boolean analyseMp4, boolean analyseMov, boolean geofence, String latitudeGeofence, String longitudeGeofence, String radiusGeofence) {
        this.useCalculatedSpeed = useCalculatedSpeed;
        this.removeOutliers = removeOutliers;
        this.analyseMp4 = analyseMp4;
        this.analyseMov = analyseMov;
        this.geofence = geofence;
        this.latitudeGeofence = latitudeGeofence;
        this.longitudeGeofence = longitudeGeofence;
        this.radiusGeofence = radiusGeofence;
        
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
    
    void setLatitudeGeofence(String latitude) {
        latitudeGeofence = latitude;
    }
    
    String latitudeGeofence(){
        return latitudeGeofence;
    }
    
    void setLongitudeGeofence(String longitude) {
        longitudeGeofence = longitude;
    }
    
    String longitudeGeofence(){
        return longitudeGeofence;
    }
    
    void setRadiusGeofence(String radius) {
        radiusGeofence = radius;
    }
    
    String radiusGeofence(){
        return radiusGeofence;
    }
}
