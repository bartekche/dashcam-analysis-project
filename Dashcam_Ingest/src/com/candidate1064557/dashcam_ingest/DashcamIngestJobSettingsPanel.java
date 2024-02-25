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
        useCalculatedSpeedCheckBox.setSelected(settings.useCalculatedSpeed());
        removeOutliersCheckBox.setSelected(settings.removeOutliers());
        mp4CheckBox.setSelected(settings.analyseMp4());
        movCheckBox.setSelected(settings.analyseMov());
    }

    /**
     * Gets the ingest job settings for an ingest module.
     *
     * @return The ingest settings.
     */
    @Override
    public IngestModuleIngestJobSettings getSettings() {
        return new DashcamIngestJobSettings(useCalculatedSpeedCheckBox.isSelected(),
                                            removeOutliersCheckBox.isSelected(), 
                                            mp4CheckBox.isSelected(),
                                            movCheckBox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        useCalculatedSpeedCheckBox = new javax.swing.JCheckBox();
        removeOutliersCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        mp4CheckBox = new javax.swing.JCheckBox();
        movCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(useCalculatedSpeedCheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.useCalculatedSpeedCheckBox.text")); // NOI18N
        useCalculatedSpeedCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.useCalculatedSpeedCheckBox.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeOutliersCheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.removeOutliersCheckBox.text")); // NOI18N
        removeOutliersCheckBox.setActionCommand(org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.removeOutliersCheckBox.actionCommand")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mp4CheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.mp4CheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(movCheckBox, org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.movCheckBox.text")); // NOI18N
        movCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useCalculatedSpeedCheckBox)
                    .addComponent(removeOutliersCheckBox)
                    .addComponent(mp4CheckBox)
                    .addComponent(movCheckBox))
                .addContainerGap(167, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useCalculatedSpeedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeOutliersCheckBox)
                .addGap(30, 30, 30)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mp4CheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(movCheckBox)
                .addContainerGap(146, Short.MAX_VALUE))
        );

        useCalculatedSpeedCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DashcamIngestJobSettingsPanel.class, "DashcamIngestJobSettingsPanel.useCalculatedSpeedCheckBox.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void movCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_movCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox movCheckBox;
    private javax.swing.JCheckBox mp4CheckBox;
    private javax.swing.JCheckBox removeOutliersCheckBox;
    private javax.swing.JCheckBox useCalculatedSpeedCheckBox;
    // End of variables declaration//GEN-END:variables
}
