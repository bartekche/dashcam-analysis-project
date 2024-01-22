package com.candidate1064557.dashcam_ingest;

import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

/**
 * UI component used to make per ingest job settings for sample ingest modules.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public class DashcamIngestJobSettingsPanel extends IngestModuleIngestJobSettingsPanel {

    /**
     * Creates new form SampleIngestModuleIngestJobSettings
     */
    public DashcamIngestJobSettingsPanel(DashcamIngestJobSettings settings) {
        initComponents();
        customizeComponents(settings);
    }

    private void customizeComponents(DashcamIngestJobSettings settings) {
        skipKnownFilesCheckBox.setSelected(settings.skipKnownFiles());
        removeOutliersCheckBox.setSelected(settings.removeOutliers());
    }

    /**
     * Gets the ingest job settings for an ingest module.
     *
     * @return The ingest settings.
     */
    @Override
    public IngestModuleIngestJobSettings getSettings() {
        return new DashcamIngestJobSettings(skipKnownFilesCheckBox.isSelected(), removeOutliersCheckBox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        skipKnownFilesCheckBox = new javax.swing.JCheckBox();
        removeOutliersCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(skipKnownFilesCheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.skipKnownFilesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeOutliersCheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.removeOutliersCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(skipKnownFilesCheckBox)
                    .addComponent(removeOutliersCheckBox))
                .addContainerGap(155, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(skipKnownFilesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeOutliersCheckBox)
                .addContainerGap(244, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox removeOutliersCheckBox;
    private javax.swing.JCheckBox skipKnownFilesCheckBox;
    // End of variables declaration//GEN-END:variables
}
