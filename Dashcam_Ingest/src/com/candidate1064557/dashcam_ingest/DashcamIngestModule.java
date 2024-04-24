package com.candidate1064557.dashcam_ingest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.Date;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.blackboardutils.GeoArtifactsHelper;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoWaypoints;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoWaypoints.Waypoint;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints.TrackPoint;

class DashcamIngestModule implements DataSourceIngestModule {

    private final String windowsExifCommand = "exiftool.exe -p \"$gpslatitude#|$gpslongitude#|$gpsspeed#|${GPSDateTime;DateFmt('%s%f')}\" -ee3 ";
    private final boolean useCalculatedSpeed;
    private final boolean removeOutliers;
    private final boolean analyseMp4;
    private final boolean analyseMov;
    private boolean isGeofenceEnabled;
    private final String latitudeGeofenceText;
    private final String longitudeGeofenceText;
    private final String radiusGeofenceText;
    private final Date dateGeofence;
    private String msgText;
    private IngestJobContext context = null;
    private final boolean isWindows;
    private final String moduleName = DashcamIngestModuleFactory.getModuleName();
    private final Logger logger = IngestServices.getInstance().getLogger(moduleName);
    private final double distanceThreshold = 300;
    private double latitudeGeofence, longitudeGeofence, radiusGeofence;

    DashcamIngestModule(DashcamIngestJobSettings settings) {
        this.useCalculatedSpeed = settings.useCalculatedSpeed();
        this.removeOutliers = settings.removeOutliers();
        this.analyseMp4 = settings.analyseMp4();
        this.analyseMov = settings.analyseMov();
        this.isGeofenceEnabled = settings.geofence();
        this.latitudeGeofenceText = settings.latitudeGeofence();
        this.longitudeGeofenceText = settings.longitudeGeofence();
        this.radiusGeofenceText = settings.radiusGeofence();
        this.dateGeofence = settings.dateGeofence();
        this.isWindows = PlatformUtil.isWindowsOS();
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
    }

    private void sendMsg(String msgText, IngestMessage.MessageType severity) {
        IngestMessage message = IngestMessage.createMessage(
                severity,
                moduleName,
                msgText);
        IngestServices.getInstance().postMessage(message);

    }

    private GeoTrackPoints extractTrack(AbstractFile currentFile) {
        String frameData;
        double frameLatitude, frameLongitude, metadataSpeed;
        double calculatedSpeed = 0.0d, calculatedDistance;
        double speedToUse;
        double accumulatedDistance = 0.0d;
        double lastFrameLatitude = 0.0d;
        double lastFrameLongitude = 0.0d;
        long lastFrameTime = 0;
        long frameTime;
        boolean haveRemovedOutliers = false;
        final String fileName = currentFile.getName();
        GeoTrackPoints pointList = new GeoTrackPoints();
        // Build and execute the exiftool command
        ProcessBuilder builder = new ProcessBuilder();
        String currentCommand = windowsExifCommand + currentFile.getLocalAbsPath();
        Process process;
        try {
            builder.command(currentCommand);
            builder.directory(new File(System.getProperty("user.home")));
            process = builder.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Command builder failed - skipping file %s", fileName), e);
            sendMsg(String.format("Command builder failed - skipping file %s", fileName), IngestMessage.MessageType.WARNING);
            return new GeoTrackPoints();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((frameData = reader.readLine()) != null) {
                //Reading data for a single waypoint:
                String[] frameDataSeparated = frameData.split("\\|");
                try {
                    frameLatitude = Double.parseDouble(frameDataSeparated[0]);
                    frameLongitude = Double.parseDouble(frameDataSeparated[1]);
                    metadataSpeed = Double.parseDouble(frameDataSeparated[2]);
                    frameTime = (long) (Double.parseDouble(frameDataSeparated[3]));
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Parsing error - skipping frame", e);
                    continue;
                }

                //Adding first waypoint different - outlier removal cannot be based
                //on distance to the last waypoint
                if (pointList.isEmpty()) {
                    if (!removeOutliers || DashcamUtilities.isValidTrackCoordinate(frameLongitude, frameLatitude)) {
                        TrackPoint framePoint
                                = new TrackPoint(frameLatitude, frameLongitude, null, null, metadataSpeed, null, null, frameTime);
                        pointList.addPoint(framePoint);
                        lastFrameLongitude = frameLongitude;
                        lastFrameLatitude = frameLatitude;
                        lastFrameTime = frameTime;
                    } else if (removeOutliers && !DashcamUtilities.isValidTrackCoordinate(frameLongitude, frameLatitude)) {
                        haveRemovedOutliers = true;
                    }
                    continue;
                }

                //Calculate distance between this waypoint and the last one recorded
                calculatedDistance
                        = DashcamUtilities.getHaversineDistance(frameLongitude, frameLatitude, lastFrameLongitude, lastFrameLatitude);
                accumulatedDistance += calculatedDistance;

                //Calculate speed if time increased
                if (frameTime != lastFrameTime) {
                    calculatedSpeed = 3.6 * accumulatedDistance / (frameTime - lastFrameTime); //km\h
                    accumulatedDistance = 0.0d;
                }

                //Outlier removal - skip waypoint if the distance between waypooints is too high
                if (removeOutliers && calculatedDistance > distanceThreshold) {
                    haveRemovedOutliers = true;
                } else {
                    speedToUse = useCalculatedSpeed ? calculatedSpeed : metadataSpeed;
                    TrackPoint framePoint
                            = new TrackPoint(frameLatitude, frameLongitude, null, null, speedToUse, null, null, frameTime);
                    pointList.addPoint(framePoint);
                    lastFrameLongitude = frameLongitude;
                    lastFrameLatitude = frameLatitude;
                    lastFrameTime = frameTime;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Exiftool output parsing failed - skipping file %s", fileName), e);
            sendMsg(String.format("Exiftool output failed - skipping file %s", fileName), IngestMessage.MessageType.WARNING);
            return new GeoTrackPoints();
        }
        //Inform the user if any waypoints were discarded in the file
        if (removeOutliers && !pointList.isEmpty()) {
            msgText = haveRemovedOutliers
                    ? String.format("Removed outliers in %s", fileName)
                    : String.format("No outliers found in %s", fileName);
            sendMsg(msgText, IngestMessage.MessageType.INFO);
        }

        return pointList;
    }

    private double getDistanceToPOI(GeoTrackPoints track) {
        double minDistanceToPOI = Double.MAX_VALUE;
        double longitudePoint, latitudePoint;
        for (TrackPoint currentPoint : track) {
            longitudePoint = currentPoint.getLongitude();
            latitudePoint = currentPoint.getLatitude();
            minDistanceToPOI = Math.min(minDistanceToPOI,
                    DashcamUtilities.getHaversineDistance(longitudePoint, latitudePoint, longitudeGeofence, latitudeGeofence));
        }
        return minDistanceToPOI;
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        try {
            // Return error if OS not supported
            if (!isWindows) {
                org.sleuthkit.autopsy.coreutils.MessageNotifyUtil.Notify
                        .show("DashcamIngest", "Current OS is not supported", MessageNotifyUtil.MessageType.ERROR);
                logger.log(Level.SEVERE, "Dashcam ingest is not supported for current OS");
                return IngestModule.ProcessResult.ERROR;
            }

            // Parse doubles for point of interest filtering 
            if (isGeofenceEnabled) {
                try {
                    latitudeGeofence = Double.parseDouble(latitudeGeofenceText);
                    longitudeGeofence = Double.parseDouble(longitudeGeofenceText);
                    radiusGeofence = Double.parseDouble(radiusGeofenceText);
                } catch (NumberFormatException ex) {
                    logger.log(Level.WARNING, "Point of interest coordinate parse failed - skipping", ex);
                    sendMsg("Point of interest coordinate parse failed - skipping", IngestMessage.MessageType.WARNING);
                    isGeofenceEnabled = false;
                }
                if (!DashcamUtilities.isCoordinateInBounds(longitudeGeofence, latitudeGeofence)) {
                    logger.log(Level.WARNING, "Point of interest coordinate out of bounds - skipping");
                    sendMsg("Point of interest coordinate out of bounds - skipping", IngestMessage.MessageType.WARNING);
                    isGeofenceEnabled = false;
                }
            }

            // Get the files from the data source to examine
            FileManager fileManager = Case.getCurrentCaseThrows()
                    .getServices().getFileManager();
            List<AbstractFile> fileList = new ArrayList<>();

            if (analyseMp4) {
                List<AbstractFile> mp4FileList = fileManager.findFiles(dataSource, "%.mp4");
                fileList.addAll(mp4FileList);
            }
            if (analyseMov) {
                List<AbstractFile> movFileList = fileManager.findFiles(dataSource, "%.mov");
                fileList.addAll(movFileList);
            }

            // Generate a waypoint for the point of interest
            if (isGeofenceEnabled) {
                GeoWaypoints geofenceList = new GeoWaypoints();
                Waypoint geofencePoint = new Waypoint(latitudeGeofence, longitudeGeofence, null, "Point of interest");
                geofenceList.addPoint(geofencePoint);
                (new GeoArtifactsHelper(Case.getCurrentCaseThrows().getSleuthkitCase(),
                        moduleName,
                        "Dashcam Ingest",
                        dataSource,
                        context.getJobId()
                )).addRoute("Point of interest", dateGeofence.getTime() / 1000, geofenceList, new ArrayList<>());
            }

            // Initialise a progress bar
            final int numberOfFiles = fileList.size();
            progressBar.switchToDeterminate(numberOfFiles);

            int currentFileCount = 0;
            for (AbstractFile currentFile : fileList) {
                progressBar.progress(currentFile.getName(), currentFileCount);

                GeoTrackPoints resultingTrack = extractTrack(currentFile);

                if (resultingTrack.isEmpty()) {
                    sendMsg(String.format("No track found in %s", currentFile.getName()), IngestMessage.MessageType.INFO);
                } else {
                    // If proximity filtering enabled upload track only if
                    // at least one of its waypoints in the radius
                    if (!isGeofenceEnabled || getDistanceToPOI(resultingTrack) < radiusGeofence) {
                        (new GeoArtifactsHelper(Case.getCurrentCaseThrows().getSleuthkitCase(),
                                moduleName,
                                "Dashcam Ingest",
                                currentFile,
                                context.getJobId()
                        )).addTrack(currentFile.getName(), resultingTrack, new ArrayList<>());
                    }
                }

                currentFileCount += 1;

                // Check if the processs were cancelled
                if (context.dataSourceIngestIsCancelled()) {
                    return IngestModule.ProcessResult.OK;
                }

            }
            progressBar.progress(numberOfFiles);
            return IngestModule.ProcessResult.OK;

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Module failed", ex);
            return IngestModule.ProcessResult.ERROR;
        }
    }
}
