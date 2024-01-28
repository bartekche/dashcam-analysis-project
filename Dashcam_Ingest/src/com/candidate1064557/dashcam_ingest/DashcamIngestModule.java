package com.candidate1064557.dashcam_ingest;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints;
import org.sleuthkit.datamodel.blackboardutils.GeoArtifactsHelper;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints.TrackPoint;
import java.util.ArrayList;
import org.sleuthkit.datamodel.Blackboard.BlackboardException;

class DashcamIngestModule implements DataSourceIngestModule {

    private final String windowsExifCommand = "exiftool.exe -p \"$gpslatitude#|$gpslongitude#|$gpsspeed#|${GPSDateTime;DateFmt('%s%f')}\" -ee3 ";
    private final boolean skipKnownFiles;
    private final boolean removeOutliers;
    private final boolean analyseMp4;
    private final boolean analyseMov;
    private IngestJobContext context = null;
    private final String moduleName = DashcamIngestModuleFactory.getModuleName();
    private final Logger logger = IngestServices.getInstance().getLogger(moduleName);

    DashcamIngestModule(DashcamIngestJobSettings settings) {
        this.skipKnownFiles = settings.skipKnownFiles();
        this.removeOutliers = settings.removeOutliers();
        this.analyseMp4 = settings.analyseMp4();
        this.analyseMov = settings.analyseMov();
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {

        try {
            FileManager fileManager = Case.getCurrentCaseThrows()
                    .getServices().getFileManager();
            List<AbstractFile> fileList = new ArrayList<>();
            if(analyseMp4){
                List<AbstractFile> mp4FileList = fileManager.findFiles(dataSource, "%.mp4");
                fileList.addAll(mp4FileList);
            }
            if(analyseMov){
                List<AbstractFile> movFileList = fileManager.findFiles(dataSource, "%.mov");
                fileList.addAll(movFileList);
            }
            
            final int numberOfFiles = fileList.size();
            progressBar.switchToDeterminate(numberOfFiles);

            final boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");

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
                double frameLatitude, frameLongitude, frameSpeed;
                long frameTime;
                GeoTrackPoints pointList = new GeoTrackPoints();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    while ((frameData = reader.readLine()) != null) {
                        String[] frameDataSeparated = frameData.split("\\|");
                        try {
                            frameLatitude = Double.parseDouble(frameDataSeparated[0]);
                            frameLongitude = Double.parseDouble(frameDataSeparated[1]);
                            frameSpeed = Double.parseDouble(frameDataSeparated[2]);
                            frameTime = (long) (Double.parseDouble(frameDataSeparated[3]));
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, "Parsing error - skipping frame");
                            continue;
                        }

                        TrackPoint framePoint = new TrackPoint(frameLatitude, frameLongitude, null, null, frameSpeed, null, null, frameTime);
                        pointList.addPoint(framePoint);

                    }

                }
                if (pointList.isEmpty()) {
                    String msgText = String.format("No track found in %s", fileName);
                    IngestMessage message = IngestMessage.createMessage(
                            IngestMessage.MessageType.WARNING,
                            moduleName,
                            msgText);
                    IngestServices.getInstance().postMessage(message);
                    continue;
                }

                (new GeoArtifactsHelper(Case.getCurrentCaseThrows().getSleuthkitCase(),
                        moduleName,
                        "xd",
                        currentFile,
                        context.getJobId()
                )).addTrack(currentFile.getName(), pointList, new ArrayList<>());

                currentFileCount += 1;

                // check if we were cancelled
                if (context.dataSourceIngestIsCancelled()) {
                    return IngestModule.ProcessResult.OK;
                }

            }
            progressBar.progress(numberOfFiles);
            return IngestModule.ProcessResult.OK;

        } catch (TskCoreException | NoCurrentCaseException | BlackboardException ex) {
            logger.log(Level.SEVERE, "File query failed", ex);
            return IngestModule.ProcessResult.ERROR;

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed command execution", ex);
            return IngestModule.ProcessResult.ERROR;

        }
    }
}
