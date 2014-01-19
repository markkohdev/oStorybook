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
package storybook.ui.jasperreports;

import storybook.SbConstants.InternalKey;
import storybook.model.hbn.entity.Internal;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.IconButton;
import storybook.toolkit.swing.SwingUtil;
import storybook.ui.MainFrame;
import storybook.ui.dialog.AbstractDialog;
import storybook.ui.interfaces.IPaintable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import storybook.SbConstants;

public class ExportPrintDialog extends AbstractDialog
	implements ActionListener {

	private JTextField tfDir;
	private ExportPreview previewPanel;
	private List<JRadioButton> formatList;
	private String formatKey = "pdf";
	private JComboBox reportCombo;
	private Timer timer = new Timer(50, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
			ExportPrintDialog.this.preview();
		}
	});

	public ExportPrintDialog(MainFrame paramMainFrame) {
		super(paramMainFrame);
		timer.start();
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		MigLayout localMigLayout = new MigLayout("fill,wrap 2", "", "20[]20[top]");
		setTitle(I18N.getMsg("msg.dlg.export.title"));
		setLayout(localMigLayout);
		setPreferredSize(new Dimension(800, 800));
		JLabel localJLabel1 = new JLabel(I18N.getMsgColon("msg.dlg.export.folder"));
		this.tfDir = new JTextField(20);
		Internal localInternal = DocumentUtil.restoreInternal(this.mainFrame, SbConstants.InternalKey.EXPORT_DIRECTORY, EnvUtil.getDefaultExportDir(this.mainFrame));
		this.tfDir.setText(localInternal.getStringValue());
		if (this.tfDir.getText().isEmpty()) {
			this.tfDir.setText(FileUtils.getUserDirectoryPath());
		}
		JButton localJButton1 = new JButton();
		localJButton1.setAction(getChooseFolderAction());
		localJButton1.setText(I18N.getMsg("msg.common.choose.folder"));
		JPanel localJPanel1 = new JPanel(new MigLayout("flowy"));
		JPanel localJPanel2 = new JPanel(new MigLayout("flowy,fill"));
		SwingUtil.setMaxPreferredSize(localJPanel2);
		JLabel localJLabel2 = new JLabel(I18N.getMsgColon("msg.dlg.export.report"));
		DefaultComboBoxModel localDefaultComboBoxModel = new DefaultComboBoxModel();
		ExportManager localExportManager = new ExportManager(this.mainFrame);
		Object localObject1 = localExportManager.getReportList().iterator();
		while (((Iterator) localObject1).hasNext()) {
			ExportReport localObject2 = (ExportReport) ((Iterator) localObject1).next();
			localDefaultComboBoxModel.addElement(localObject2);
		}
		this.reportCombo = new JComboBox(localDefaultComboBoxModel);
		this.reportCombo.setMaximumRowCount(15);
		this.reportCombo.addActionListener(this);
		localObject1 = new JLabel(I18N.getMsgColon("msg.dlg.export.format"));
		this.formatList = new ArrayList();
		Object localObject2 = localExportManager.getFormatMap();
		ButtonGroup localButtonGroup = new ButtonGroup();
		Object localObject3 = ((HashMap) localObject2).keySet().iterator();
		while (((Iterator) localObject3).hasNext()) {
			String localObject4 = (String) ((Iterator) localObject3).next();
			JRadioButton localObject5 = new JRadioButton((String) ((HashMap) localObject2).get(localObject4));
			((JRadioButton) localObject5).setActionCommand((String) localObject4);
			((JRadioButton) localObject5).addActionListener(this);
			if ("pdf".equals(localObject4)) {
				((JRadioButton) localObject5).setSelected(true);
			}
			this.formatList.add(localObject5);
			localButtonGroup.add((AbstractButton) localObject5);
		}
		localObject3 = new JButton();
		((JButton) localObject3).setAction(getExportAction());
		((JButton) localObject3).setText(I18N.getMsg("msg.common.export"));
		((JButton) localObject3).setIcon(I18N.getIcon("icon.small.export"));
		SwingUtil.addEnterAction((JComponent) localObject3, getExportAction());
		this.previewPanel = new ExportPreview(null);
		Object localObject4 = new IconButton("icon.small.refresh", getPreviewAction());
		((IconButton) localObject4).setToolTipText(I18N.getMsg("msg.dlg.export.refresh.preview"));
		Object localObject5 = new JButton();
		((JButton) localObject5).setAction(getEnlargeAction());
		((JButton) localObject5).setText(I18N.getMsg("msg.dlg.export.enlarge.preview"));
		JButton localJButton2 = new JButton();
		localJButton2.setAction(getCloseAction());
		localJButton2.setText(I18N.getMsg("msg.common.close"));
		localJButton2.setIcon(I18N.getIcon("icon.small.close"));
		SwingUtil.addEscAction(localJButton2, getCloseAction());
		localJPanel1.add(localJLabel2);
		localJPanel1.add(this.reportCombo, "gapbottom 20");
		localJPanel1.add((Component) localObject1);
		Iterator localIterator = this.formatList.iterator();
		while (localIterator.hasNext()) {
			JRadioButton localJRadioButton = (JRadioButton) localIterator.next();
			localJPanel1.add(localJRadioButton);
		}
		localJPanel2.add(this.previewPanel, "grow");
		add(localJLabel1, "span,split 3");
		add(this.tfDir, "grow");
		add(localJButton1);
		add(localJPanel1, "gp 1");
		add(localJPanel2, "grow,gp 200");
		add((Component) localObject3, "span,split 4");
		add((Component) localObject5);
		add((Component) localObject4, "gap 20");
		add(localJButton2, "gap push");
	}

	private JDialog getThis() {
		return this;
	}

	private AbstractAction getChooseFolderAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				JFileChooser localJFileChooser = new JFileChooser(ExportPrintDialog.this.tfDir.getText());
				localJFileChooser.setFileSelectionMode(1);
				int i = localJFileChooser.showOpenDialog(ExportPrintDialog.this.getThis());
				if (i != 0) {
					return;
				}
				File localFile = localJFileChooser.getSelectedFile();
				ExportPrintDialog.this.tfDir.setText(localFile.getAbsolutePath());
			}
		};
	}

	private AbstractAction getExportAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				SwingUtil.setWaitingCursor(ExportPrintDialog.this.getThis());
				File localFile = new File(ExportPrintDialog.this.tfDir.getText());
				ExportManager localExportManager = new ExportManager(ExportPrintDialog.this.mainFrame);
				String str = localExportManager.export(
					localFile, ExportPrintDialog.this.formatKey,
					(ExportReport) ExportPrintDialog.this.reportCombo.getSelectedItem());
				SwingUtil.setDefaultCursor(ExportPrintDialog.this.getThis());
				if (str.isEmpty()) {
					return;
				}
				 SwingUtil.showModalDialog(
					 new ExportPrintDialog.ConfirmDialog(str),
					 ExportPrintDialog.this.mainFrame);
			}
		};
	}

	private AbstractAction getEnlargeAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				SwingUtil.setWaitingCursor(ExportPrintDialog.this.getThis());
				ExportManager localExportManager = new ExportManager(ExportPrintDialog.this.mainFrame);
				localExportManager.export(null, "preview", (ExportReport) ExportPrintDialog.this.reportCombo.getSelectedItem());
				SwingUtil.setDefaultCursor(ExportPrintDialog.this.getThis());
			}
		};
	}

	private AbstractAction getPreviewAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				ExportPrintDialog.this.preview();
			}
		};
	}

	private AbstractAction getCloseAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				DocumentUtil.storeInternal(ExportPrintDialog.this.mainFrame, SbConstants.InternalKey.EXPORT_DIRECTORY, ExportPrintDialog.this.tfDir.getText());
				ExportPrintDialog.this.getThis().dispose();
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		if ((paramActionEvent.getSource() instanceof JRadioButton)) {
			this.formatKey = paramActionEvent.getActionCommand();
		}
		if (paramActionEvent.getSource() == this.reportCombo) {
			preview();
		}
	}

	private void preview() {
		try {
			SwingUtil.setWaitingCursor(getThis());
			ExportManager localExportManager = new ExportManager(this.mainFrame);
			localExportManager.fillReport((ExportReport) this.reportCombo.getSelectedItem());
			this.previewPanel.loadReport(localExportManager.getJasperPrint());
			SwingUtil.setDefaultCursor(getThis());
			this.timer.stop();
		} catch (Exception localException) {
		}
	}

	private class ConfirmDialog extends JDialog
		implements IPaintable {

		String fileName;

		public ConfirmDialog(String arg2) {
			fileName = null;
			init();
			initUi();
		}

		@Override
		public final void init() {
		}

		@Override
		public final void initUi() {
			MigLayout localMigLayout = new MigLayout("wrap");
			setLayout(localMigLayout);
			setTitle(I18N.getMsg("msg.dlg.export.done.title"));
			JTextArea localJTextArea = new JTextArea(I18N.getMsg("msg.dlg.export.done.text", this.fileName));
			localJTextArea.setEditable(false);
			JButton localJButton1 = new JButton();
			localJButton1.setAction(getOpenAction());
			localJButton1.setText(I18N.getMsg("msg.common.open"));
			localJButton1.setIcon(I18N.getIcon("icon.small.open"));
			SwingUtil.addEnterAction(localJButton1, getOpenAction());
			JButton localJButton2 = new JButton();
			localJButton2.setAction(getCloseAction());
			localJButton2.setText(I18N.getMsg("msg.common.close"));
			localJButton2.setIcon(I18N.getIcon("icon.small.close"));
			SwingUtil.addEscAction(localJButton2, getCloseAction());
			add(localJTextArea);
			add(localJButton1, "split 2,gap push,sg");
			add(localJButton2, "sg");
		}

		private AbstractAction getOpenAction() {
			return new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
					try {
						Desktop.open(new File(ExportPrintDialog.ConfirmDialog.this.fileName));
						ExportPrintDialog.ConfirmDialog.this.getCloseAction().actionPerformed(paramAnonymousActionEvent);
					} catch (DesktopException localDesktopException) {
					}
				}
			};
		}

		private AbstractAction getCloseAction() {
			return new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
					ExportPrintDialog.ConfirmDialog.this.getThat().dispose();
				}
			};
		}

		private JDialog getThat() {
			return this;
		}
	}
}