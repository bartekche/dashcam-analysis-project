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
import org.sleuthkit.datamodel.TskData;


class DashcamIngestModule implements DataSourceIngestModule {

    private static final String windowsExifCommand = "exiftool.exe -p \"$gpslatitude# $gpslongitude# $gpsspeed# ${GPSDateTime;DateFmt('%s%f')}\" -ee3 ";

    private final boolean skipKnownFiles;
    private IngestJobContext context = null;

    DashcamIngestModule(DashcamIngestJobSettings settings) {
        this.skipKnownFiles = settings.skipKnownFiles();
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {

        try {
            // send msg for each file with .mp4 extension.
            FileManager fileManager = Case.getCurrentCaseThrows()
                        .getServices().getFileManager();
            List<AbstractFile> mp4Files = fileManager.findFiles(dataSource, "%.mp4");
            final int numberOfMP4Files = mp4Files.size();
            
            progressBar.switchToDeterminate(numberOfMP4Files);
            
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");
            
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                builder.command(
                        "exiftool.exe -p \"$gpslatitude# $gpslongitude# $gpsspeed# ${GPSDateTime;DateFmt('%s%f')}\" -ee3 C:\\Users\\Bartek\\Desktop\\102SAVED\\GRMS0001.MP4");
            } else {
                builder.command("sh", "-c", "ls"); // todo
            }
            
            builder.directory(new File(System.getProperty("user.home")));
                Process process = builder.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line;
            System.out.println("=========dir=========");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("=========dir=========");
            reader.close();

            int currentFileCount = 0;
            for (AbstractFile mp4File : mp4Files) {
                progressBar.progress(mp4File.getName(),currentFileCount);
                
                String currentCommand = windowsExifCommand + mp4File.getLocalAbsPath();
                        
                        
                String msgText = String.format("Found %s file", mp4File.getNameExtension());
                IngestMessage message = IngestMessage.createMessage(
                    IngestMessage.MessageType.DATA,
                    DashcamIngestModuleFactory.getModuleName(),
                    msgText);
                IngestServices.getInstance().postMessage(message);
                currentFileCount+=1;
            }
            progressBar.progress(currentFileCount);
            return IngestModule.ProcessResult.OK;
            

            // check if we were cancelled
           // if (context.dataSourceIngestIsCancelled()) {
            //    return IngestModule.ProcessResult.OK;
           // }

        

        } catch (TskCoreException | NoCurrentCaseException ex) {
            IngestServices ingestServices = IngestServices.getInstance();
            Logger logger = ingestServices.getLogger(DashcamIngestModuleFactory.getModuleName());
            logger.log(Level.SEVERE, "File query failed", ex);
            return IngestModule.ProcessResult.ERROR;
            
        } catch (IOException ex){
            IngestServices ingestServices = IngestServices.getInstance();
            Logger logger = ingestServices.getLogger(DashcamIngestModuleFactory.getModuleName());
            logger.log(Level.SEVERE, "Failed command execution", ex);
            return IngestModule.ProcessResult.ERROR;

        }
    }
}
