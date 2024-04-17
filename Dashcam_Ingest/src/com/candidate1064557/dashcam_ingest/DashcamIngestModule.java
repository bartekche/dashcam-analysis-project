package com.candidate1064557.dashcam_ingest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.Date;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard.BlackboardException;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;
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
        this.isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
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

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        try {
            if (isGeofenceEnabled) {
                System.out.println(dateGeofence);
                try {
                    latitudeGeofence = Double.parseDouble(latitudeGeofenceText);
                    longitudeGeofence = Double.parseDouble(longitudeGeofenceText);
                    radiusGeofence = Double.parseDouble(radiusGeofenceText);
                } catch (NumberFormatException ex) {
                    logger.log(Level.WARNING, "Geofence coordinate parse failed - skipping", ex);
                    sendMsg("Geofence coordinate parse failed - skipping", IngestMessage.MessageType.WARNING);
                    isGeofenceEnabled = false;
                }
                if (!DashcamUtilities.isCoordinateInBounds(longitudeGeofence, latitudeGeofence)) {
                    logger.log(Level.WARNING, "Geofence coordinate out of bounds - skipping");
                    sendMsg("Geofence coordinate out of bounds - skipping", IngestMessage.MessageType.WARNING);
                    isGeofenceEnabled = false;
                }
            }

            if (!isWindows) {
                // todo - linux command & test
                org.sleuthkit.autopsy.coreutils.MessageNotifyUtil.Notify
                        .show("DashcamIngest", "Linux not supported", MessageNotifyUtil.MessageType.ERROR);
                logger.log(Level.SEVERE, "Dashcam ingest for linux not supported");
                return IngestModule.ProcessResult.ERROR;
            }

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

            if (isGeofenceEnabled) {
                GeoWaypoints geofenceList = new GeoWaypoints();
                Waypoint geofencePoint = new Waypoint(latitudeGeofence, longitudeGeofence, null, null);
                geofenceList.addPoint(geofencePoint);
                (new GeoArtifactsHelper(Case.getCurrentCaseThrows().getSleuthkitCase(),
                        moduleName,
                        "Dashcam Ingest",
                        dataSource,
                        context.getJobId()
                )).addRoute("Geofence center", null, geofenceList, new ArrayList<>());
            }

            final int numberOfFiles = fileList.size();
            progressBar.switchToDeterminate(numberOfFiles);

            int currentFileCount = 0;
            for (AbstractFile currentFile : fileList) {

                final String fileName = currentFile.getName();
                progressBar.progress(fileName, currentFileCount);
                ProcessBuilder builder = new ProcessBuilder();
                if (isWindows) {
                    String currentCommand = windowsExifCommand + currentFile.getLocalAbsPath();
                    builder.command(currentCommand);
                } else {
                    // todo - linux command & test
                }
                builder.directory(new File(System.getProperty("user.home")));
                Process process = builder.start();

                String frameData;
                double frameLatitude, frameLongitude, metadataSpeed;
                double calculatedSpeed = 0.0d, calculatedDistance = 0.0d;
                double speedToUse;
                double accumulatedDistance = 0.0d;
                double lastFrameLatitude = 0.0d;
                double lastFrameLongitude = 0.0d;;
                long lastFrameTime = 0;
                long frameTime;
                double minDistanceToGeofence = Double.MAX_VALUE;
                boolean haveRemovedOutliers = false;
                GeoTrackPoints pointList = new GeoTrackPoints();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    while ((frameData = reader.readLine()) != null) {

                        //Reading data for a single waypoint:
                        String[] frameDataSeparated = frameData.split("\\|");
                        try {
                            frameLatitude = Double.parseDouble(frameDataSeparated[0]);
                            frameLongitude = Double.parseDouble(frameDataSeparated[1]);
                            metadataSpeed = Double.parseDouble(frameDataSeparated[2]);
                            frameTime = (long) (Double.parseDouble(frameDataSeparated[3]));
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, "Parsing error - skipping frame");
                            continue;
                        }

                        /*if (!DashcamUtilities.isCoordinateInBounds(frameLongitude, frameLatitude)) {
                            logger.log(Level.WARNING, "Frame coordinates out of bounds - skipping frame");
                            continue;
                        }*/
                        minDistanceToGeofence = Math.min(minDistanceToGeofence,
                                DashcamUtilities.getHaversineDistance(frameLongitude, frameLatitude, longitudeGeofence, latitudeGeofence));

                        //Additional checks for first point added, skip distance calculations in that case
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

                        //Calculate distance
                        calculatedDistance
                                = DashcamUtilities.getHaversineDistance(frameLongitude, frameLatitude, lastFrameLongitude, lastFrameLatitude);
                        accumulatedDistance += calculatedDistance;

                        //Calculate speed if time increased (only once per second)
                        if (frameTime != lastFrameTime) {
                            calculatedSpeed = 3.6 * accumulatedDistance / (frameTime - lastFrameTime);
                            //System.out.println("+++++:");
                            //System.out.println("Speed in metadata:");
                            //System.out.println(metadataSpeed); //unit??
                            //System.out.println("Speed calculated:");
                            //System.out.println(calculatedSpeed); //km/h
                            accumulatedDistance = 0.0d;
                        }
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
                }
                if (pointList.isEmpty()) {
                    sendMsg(String.format("No track found in %s", fileName), IngestMessage.MessageType.WARNING);
                    continue;
                }
                if (removeOutliers) {
                    msgText = haveRemovedOutliers
                            ? String.format("Removed outliers in %s", fileName)
                            : String.format("No outliers found in %s", fileName);
                    sendMsg(msgText, IngestMessage.MessageType.INFO);
                }
                if (!isGeofenceEnabled || minDistanceToGeofence < radiusGeofence) {
                    (new GeoArtifactsHelper(Case.getCurrentCaseThrows().getSleuthkitCase(),
                            moduleName,
                            "Dashcam Ingest",
                            currentFile,
                            context.getJobId()
                    )).addTrack(currentFile.getName(), pointList, new ArrayList<>());
                }
                System.out.println(minDistanceToGeofence);

                currentFileCount += 1;

                // check if we were cancelled
                if (context.dataSourceIngestIsCancelled()) {
                    return IngestModule.ProcessResult.OK;
                }

            }
            progressBar.progress(numberOfFiles);
            return IngestModule.ProcessResult.OK;

        } catch (TskCoreException | NoCurrentCaseException | BlackboardException ex) {
            logger.log(Level.SEVERE, "Module failed", ex);
            return IngestModule.ProcessResult.ERROR;

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Command execution failed", ex);
            return IngestModule.ProcessResult.ERROR;

        }
    }
}
