package com.candidate1064557.dashcam_ingest;

import org.openide.util.lookup.ServiceProvider;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleGlobalSettingsPanel;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

/**
 * A factory that creates sample data source and file ingest modules.
 * This factory implements an interface that must be implemented by all
 * providers of Autopsy ingest modules. An ingest module factory is used to
 * create instances of a type of data source ingest module, a type of file
 * ingest module, or both.
 */
@ServiceProvider(service = IngestModuleFactory.class) // Sample is discarded at runtime 
public class DashcamIngestModuleFactory implements IngestModuleFactory {

    private static final String VERSION_NUMBER = "1.0.0";

    static String getModuleName() {
        return NbBundle.getMessage(DashcamIngestModuleFactory.class, "DashcamIngestModuleFactory.moduleName");
    }

    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }

    @Override
    public String getModuleDescription() {
        return NbBundle.getMessage(DashcamIngestModuleFactory.class, "DashcamIngestModuleFactory.moduleDescription");
    }

    @Override
    public String getModuleVersionNumber() {
        return VERSION_NUMBER;
    }

    @Override
    public boolean hasGlobalSettingsPanel() {
        return false;
    }

    @Override
    public IngestModuleGlobalSettingsPanel getGlobalSettingsPanel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new DashcamIngestJobSettings();
    }

    @Override
    public boolean hasIngestJobSettingsPanel() {
        return true;
    }

    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof DashcamIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof DashcamIngestJobSettings");
        }
        return new DashcamIngestJobSettingsPanel((DashcamIngestJobSettings) settings);
    }

    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return true;
    }

    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof DashcamIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof DashcamIngestJobSettings");
        }
        return new DashcamIngestModule((DashcamIngestJobSettings) settings);
    }

    @Override
    public boolean isFileIngestModuleFactory() {
        return false;
    }

    @Override
    public FileIngestModule createFileIngestModule(IngestModuleIngestJobSettings settings) {
        throw new UnsupportedOperationException();
    }

}
