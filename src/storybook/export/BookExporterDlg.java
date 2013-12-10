/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package storybook.export;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.hibernate.Session;
import storybook.SbConstants;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Preference;
import storybook.model.hbn.entity.Scene;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.PrefUtil;
import storybook.toolkit.filefilter.StdFileFilter;
import storybook.toolkit.html.HtmlUtil;
import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class BookExporterDlg extends javax.swing.JDialog {

	private MainFrame mainFrame;

	private boolean isUsehtmlText,
		isUseHtmDescr,
		isExportChapterNumbers,
		isExportRomanNumerals,
		isExportChapterTitles,
		isExportChapterDatesLocations,
		isExportSceneTitle,
		isExportPartTitles;


	private String ExportType="txt";

	/**
	 * Creates new form BookExporterDlg
	 */
	public BookExporterDlg(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	public BookExporterDlg(MainFrame mFrame) {
		super(mFrame, true);
		mainFrame=mFrame;
		isUsehtmlText = DocumentUtil.isUseHtmlScenes(mainFrame);
		isUseHtmDescr=DocumentUtil.isUseHtmlDescr(mainFrame);
		isExportChapterNumbers=DocumentUtil.isExportChapterNumbers(mainFrame);
		isExportRomanNumerals=DocumentUtil.isExportRomanNumerals(mainFrame);
		isExportChapterTitles=DocumentUtil.isExportChapterTitles(mainFrame);
		isExportChapterDatesLocations=DocumentUtil.isExportChapterDatesLocations(mainFrame);
		isExportSceneTitle=DocumentUtil.isExportSceneTitle(mainFrame);
		isExportPartTitles=DocumentUtil.isExportPartTitles(mainFrame);
		initComponents();
	}

	public void setExportFor(String x){
		ExportType=x;
	}

	public void exportToFile(){
		/* TODO exportToFile
		 */
		File of=getNewFileDialog(ExportType);
		if (of==null) {
			return;
		}
		/*
		 * attribution du non du fichier
		 * recherche de l'objet à exporter, peut y en avoir plusieurs
		 * ouvrir le fichier en ecriture
		 * ecrire les textes
		 *		si type txt rupture de ligne à 80 caracteres maxi
		 *		si type ODT RAS
		 * fermer le fichier
		 * retour
		 */
	}

	public void exportToClipboard(){
		JOptionPane.showMessageDialog(null,
			this.getClass().getName()+"\n\n"+I18N.getMsg("msg.common.not.available"),
			this.getClass().getName(), JOptionPane.ERROR_MESSAGE);
	}

	public void setExportOnlyCurrentPart(boolean z) {

	}

	public void setExportTableOfContentsLink(boolean z) {

	}

	public void setStrandIdsToExport(HashSet<Long> strandIds) {
	}

	public String getContent(){
		String x="";
		if ("txt".equals(ExportType)) {
			x=getBookText();
		}
		return(x);
	}
	private DocumentModel model;

	private String getTableOfContent() {
		Part currentPart = mainFrame.getCurrentPart();

		model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Chapter> chapters = dao
				.findAllOrderByChapterNoAndSceneNo(currentPart);
		model.commit();

		StringBuilder buf = new StringBuilder();

		// table of contents
		if (ExportType.equals("html")) {
			buf.append("<p style='font-weight:bold'>");
			buf.append("<a name='toc'>");
			buf.append(I18N.getMsg("msg.table.of.contents"));
			buf.append("</a></p>\n");
		} else {
			buf.append(I18N.getMsg("msg.table.of.contents")).append("\n");
		}
		for (Chapter chapter : chapters) {
			String no = chapter.getChapternoStr();
			if (ExportType.equals("html")) {
				buf.append("<p><a href='#").append(no).append("'>");
			}
			buf.append(no).append(": ").append(chapter.getTitle());
			String descr = chapter.getDescription();
			if (descr != null) {
				if (!descr.isEmpty()) {
					buf.append(": ").append(chapter.getDescription());
				}
			}
			buf.append("\n");
		}
		return(buf.toString());
	}

	private String getBookText() {
		Part currentPart = mainFrame.getCurrentPart();

		model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Chapter> chapters = dao
				.findAllOrderByChapterNoAndSceneNo(currentPart);
		model.commit();

		StringBuilder buf = new StringBuilder();

		for (Chapter chapter : chapters) {
			buf.append(getChapter(chapter));
			SceneDAOImpl scenedao = new SceneDAOImpl(session);
			List<Scene> scenes = scenedao.findByChapter(chapter);
			model.commit();
			for (Scene scene : scenes) {
				buf.append(getScene(session, scene));
			}
		}

		buf.append("\n");

		return(buf.toString());
	}

	public String getChapter(Chapter chapter) {
		StringBuilder buf=new StringBuilder();
		String no = chapter.getChapternoStr();
		if (ExportType.equals("html")) {
			buf.append("<p><a href='#").append(no).append("'>");
		}
		buf.append(no).append(": ").append(chapter.getTitle());
		if (ExportType.equals("html")) {buf.append("</p>");}
		buf.append("\n");
		String descr = chapter.getDescription();
		if (descr != null) {
			if (!descr.isEmpty()) {
				if (ExportType.equals("html")) {
					if (!isUsehtmlText) {buf.append("<p>");}
					buf.append(descr);
					if (!isUsehtmlText) {buf.append("</p>\n");}
				} else {
					if (!isUsehtmlText) {
						buf.append(descr).append("\n");
					} else {
						buf.append(HtmlUtil.htmlToText(descr));
					}
				}
			}
		}
		return(buf.toString());
	}

	public String getScene(Session session, Scene scene) {
		StringBuilder buf=new StringBuilder();
		String no = scene.getSceneno().toString();
		if (ExportType.equals("html")) {
			buf.append("<p><a href='#").append(no).append("'>");
		}
		buf.append(no).append(": ").append(scene.getTitle());
		if (ExportType.equals("html")) {buf.append("</p>");}
		buf.append("\n");
		String descr = scene.getText();
		if (descr != null) {
			if (!descr.isEmpty()) {
				if (ExportType.equals("html")) {
					if (!isUsehtmlText) {buf.append("<p>");}
					buf.append(descr);
					if (!isUsehtmlText) {buf.append("</p>\n");}
				} else {
					if (!isUsehtmlText) {
						buf.append(descr).append("\n");
					} else {
						buf.append(HtmlUtil.htmlToText(descr));
					}
				}
			}
		}
		return(buf.toString());
	}

	public static File getNewFileDialog(String ext) {
		final JFileChooser fc = new JFileChooser();
		Preference pref = PrefUtil.get(SbConstants.PreferenceKey.LAST_OPEN_DIR,
				DocumentUtil.getHomeDir());
		fc.setCurrentDirectory(new File(pref.getStringValue()));
		switch (ext) {
			case "txt":
				{
					StdFileFilter filter = new StdFileFilter("txt",I18N.getMsg("msg.export.file.txt"));
					fc.addChoosableFileFilter(filter);
					fc.setFileFilter(filter);
					break;
				}
			case "odt":
				{
					StdFileFilter filter = new StdFileFilter("odt",I18N.getMsg("msg.export.file.odt"));
					fc.addChoosableFileFilter(filter);
					fc.setFileFilter(filter);
					break;
				}
			case "html":
				{
					StdFileFilter filter = new StdFileFilter("html",I18N.getMsg("msg.export.file.html"));
					fc.addChoosableFileFilter(filter);
					fc.setFileFilter(filter);
					break;
				}
		}
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(
						null,
						I18N.getMsg("msg.common.file.exists", file),
						I18N.getMsg("msg.export.choose.file"),
						JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) {
					return null;
				}
			}
			return file;
		}
		return null;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        rbText = new javax.swing.JRadioButton();
        rbOdt = new javax.swing.JRadioButton();
        rbHtml = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        rbClipboard = new javax.swing.JRadioButton();
        rbFile = new javax.swing.JRadioButton();
        btOK = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        ckParts = new javax.swing.JCheckBox();
        ckChapters = new javax.swing.JCheckBox();
        ckScenes = new javax.swing.JCheckBox();
        ckChaptersNumbers = new javax.swing.JCheckBox();
        ckAsRoman = new javax.swing.JCheckBox();
        ckChaptersTitles = new javax.swing.JCheckBox();
        ckDates = new javax.swing.JCheckBox();
        ckScenesTitles = new javax.swing.JCheckBox();
        ckChaptersDescriptions = new javax.swing.JCheckBox();
        ckChaptersNotes = new javax.swing.JCheckBox();
        ckScenesTexts = new javax.swing.JCheckBox();
        ckScenesPersons = new javax.swing.JCheckBox();
        ckScenesLocations = new javax.swing.JCheckBox();
        ckScenesNumbers = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Format"));
        jPanel1.setName("Format"); // NOI18N

        buttonGroup1.add(rbText);
        rbText.setText("Full text (*.txt)");

        buttonGroup1.add(rbOdt);
        rbOdt.setText("Open Document Format (*.odt)");
        rbOdt.setEnabled(false);

        buttonGroup1.add(rbHtml);
        rbHtml.setText("HTML file (*.html)");
        rbHtml.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbHtml)
                    .addComponent(rbOdt)
                    .addComponent(rbText))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(rbText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbOdt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbHtml))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Export type"));

        rbClipboard.setText("to Clipboard");

        rbFile.setText("to File");
        rbFile.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbClipboard)
                    .addComponent(rbFile))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbClipboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbFile)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        btOK.setText("OK");
        btOK.setMaximumSize(new java.awt.Dimension(46, 26));
        btOK.setMinimumSize(new java.awt.Dimension(46, 26));
        btOK.setPreferredSize(new java.awt.Dimension(67, 26));
        btOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOKActionPerformed(evt);
            }
        });

        btCancel.setText("Cancel");
        btCancel.setPreferredSize(new java.awt.Dimension(67, 26));
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Objects"));

        ckParts.setText("Parts titles");

        ckChapters.setText("Chapters");
        ckChapters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckChaptersActionPerformed(evt);
            }
        });

        ckScenes.setText("Scenes");
        ckScenes.setPreferredSize(new java.awt.Dimension(72, 24));
        ckScenes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckScenesActionPerformed(evt);
            }
        });

        ckChaptersNumbers.setText("numbers");

        ckAsRoman.setText("as Roman");

        ckChaptersTitles.setText("titles");

        ckDates.setText("dates et locations");
        ckDates.setActionCommand("dates and locations");

        ckScenesTitles.setText("titles");

        ckChaptersDescriptions.setText("descriptions");

        ckChaptersNotes.setText("notes");

        ckScenesTexts.setText("texts");

        ckScenesPersons.setText("persons");

        ckScenesLocations.setText("locations");

        ckScenesNumbers.setText("numbers");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckParts)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(ckChapters)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ckDates)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(ckChaptersTitles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ckChaptersNumbers)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ckAsRoman))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(ckScenes, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(ckScenesTexts)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ckScenesPersons)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ckScenesLocations))
                            .addComponent(ckScenesTitles)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(ckChaptersDescriptions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ckScenesNumbers)
                            .addComponent(ckChaptersNotes))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(ckParts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckChapters)
                    .addComponent(ckChaptersNumbers)
                    .addComponent(ckAsRoman)
                    .addComponent(ckChaptersTitles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckDates)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckChaptersDescriptions)
                    .addComponent(ckChaptersNotes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckScenes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ckScenesTitles)
                    .addComponent(ckScenesNumbers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ckScenesTexts)
                    .addComponent(ckScenesPersons)
                    .addComponent(ckScenesLocations))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btOK, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btOK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOKActionPerformed
        if (rbOdt.isSelected()) {
			ExportType="odt";
		} else if (rbHtml.isSelected()) {
			ExportType="html";
		} else {
			ExportType="txt";
		}
		dispose();
    }//GEN-LAST:event_btOKActionPerformed

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        // TODO add your handling code here:
		dispose();
    }//GEN-LAST:event_btCancelActionPerformed

    private void ckChaptersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckChaptersActionPerformed
        boolean b=false;
		if (ckChapters.isSelected()) {
			b=true;
		}
		ckChaptersDescriptions.setEnabled(b);
		ckChaptersNotes.setEnabled(b);
		ckChaptersTitles.setEnabled(b);
		ckChaptersNumbers.setEnabled(b);
		ckDates.setEnabled(b);
		ckAsRoman.setEnabled(b);
    }//GEN-LAST:event_ckChaptersActionPerformed

    private void ckScenesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckScenesActionPerformed
        boolean b=false;
		if (ckScenes.isSelected()) {
			b=true;
		}
		ckScenesTexts.setEnabled(b);
		ckScenesTitles.setEnabled(b);
		ckScenesNumbers.setEnabled(b);
		ckScenesPersons.setEnabled(b);
		ckScenesLocations.setEnabled(b);
    }//GEN-LAST:event_ckScenesActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException
			| InstantiationException
			| IllegalAccessException
			| javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(BookExporterDlg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				BookExporterDlg dialog = new BookExporterDlg(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOK;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox ckAsRoman;
    private javax.swing.JCheckBox ckChapters;
    private javax.swing.JCheckBox ckChaptersDescriptions;
    private javax.swing.JCheckBox ckChaptersNotes;
    private javax.swing.JCheckBox ckChaptersNumbers;
    private javax.swing.JCheckBox ckChaptersTitles;
    private javax.swing.JCheckBox ckDates;
    private javax.swing.JCheckBox ckParts;
    private javax.swing.JCheckBox ckScenes;
    private javax.swing.JCheckBox ckScenesLocations;
    private javax.swing.JCheckBox ckScenesNumbers;
    private javax.swing.JCheckBox ckScenesPersons;
    private javax.swing.JCheckBox ckScenesTexts;
    private javax.swing.JCheckBox ckScenesTitles;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton rbClipboard;
    private javax.swing.JRadioButton rbFile;
    private javax.swing.JRadioButton rbHtml;
    private javax.swing.JRadioButton rbOdt;
    private javax.swing.JRadioButton rbText;
    // End of variables declaration//GEN-END:variables
}
