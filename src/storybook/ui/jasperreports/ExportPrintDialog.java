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

import storybook.model.hbn.entity.Internal;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.IconButton;
import storybook.toolkit.swing.SwingUtil;
import storybook.ui.MainFrame;
import storybook.ui.dialog.AbstractDialog;
import storybook.ui.interfaces.IPaintable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import storybook.StorybookApp;

public class ExportPrintDialog extends AbstractDialog implements ActionListener {

	private JTextField tfDir;
	private ExportPreview previewPanel;
	private List<JRadioButton> formatList;
	private String formatKey = "pdf";
	private JComboBox reportCombo;
	private Timer timer = new Timer(50, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent param) {
			ExportPrintDialog.this.preview();
		}
	});

	public ExportPrintDialog(MainFrame param) {
		super(param);
		timer.start();
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		StorybookApp.trace("ExportPrintDialog.initUi()");
		MigLayout localMigLayout = new MigLayout("fill,wrap 2", "", "20[]20[top]");
		setTitle(I18N.getMsg("msg.dlg.export.title"));
		setLayout(localMigLayout);
		setPreferredSize(new Dimension(800, 800));
		JLabel lbFolder = new JLabel(I18N.getMsgColon("msg.dlg.export.folder"));
		tfDir = new JTextField(20);
		Internal internal = DocumentUtil.restoreInternal(mainFrame,
				SbConstants.InternalKey.EXPORT_DIRECTORY, EnvUtil.getDefaultExportDir(mainFrame));
		tfDir.setText(internal.getStringValue());
		if (tfDir.getText().isEmpty())
			tfDir.setText(FileUtils.getUserDirectoryPath());
		JButton btnChooseFolder = setButton(getChooseFolderAction(), "msg.common.choose.folder", null);
		JPanel panel1 = new JPanel(new MigLayout("flowy"));
		JPanel panel2 = new JPanel(new MigLayout("flowy,fill"));
		SwingUtil.setMaxPreferredSize(panel2);
		JLabel lblReport = new JLabel(I18N.getMsgColon("msg.dlg.export.report"));
		DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
		ExportManager xpManager = new ExportManager(mainFrame);
		for (ExportReport xp : xpManager.getReportList()) {
			cbModel.addElement(xp);
		}
		reportCombo = new JComboBox(cbModel);
		reportCombo.setMaximumRowCount(15);
		reportCombo.addActionListener(this);
		JLabel lblFormat = new JLabel(I18N.getMsgColon("msg.dlg.export.format"));
		formatList = new ArrayList();
		Object mapFormat = xpManager.getFormatMap();
		ButtonGroup buttonGroup = new ButtonGroup();
		{
			Iterator list = ((HashMap) mapFormat).keySet().iterator();
			while (list.hasNext()) {
				String str = (String) list.next();
				JRadioButton rdButton = new JRadioButton((String) ((HashMap) mapFormat).get(str));
				rdButton.setActionCommand((String) str);
				rdButton.addActionListener(this);
				if ("pdf".equals(str))
					rdButton.setSelected(true);
				formatList.add(rdButton);
				buttonGroup.add((AbstractButton) rdButton);
			}
		}
		JButton btnExport = setButton(getExportAction(), "msg.common.export", "icon.small.export");
		SwingUtil.addEnterAction(btnExport, getExportAction());
		previewPanel = new ExportPreview(null);
		IconButton btnPreview = new IconButton("icon.small.refresh", getPreviewAction());
		btnPreview.setToolTipText(I18N.getMsg("msg.dlg.export.refresh.preview"));
		JButton btnEnlarge = setButton(getEnlargeAction(), "msg.dlg.export.enlarge.preview", null);
		JButton btnClose = setButton(getCloseAction(), "msg.common.close", "icon.small.close");
		SwingUtil.addEscAction(btnClose, getCloseAction());
		panel1.add(lblReport);
		panel1.add(this.reportCombo, "gapbottom 20");
		panel1.add(lblFormat);
		for (JRadioButton lRB : formatList) {
			panel1.add(lRB);
		}
		panel2.add(previewPanel, "grow");
		add(lbFolder, "span,split 3");
		add(tfDir, "grow");
		add(btnChooseFolder);
		add(panel1, "gp 1");
		add(panel2, "grow,gp 200");
		add(btnExport, "span,split 4");
		add(btnEnlarge);
		add(btnPreview, "gap 20");
		add(btnClose, "gap push");
	}

	private JButton setButton(Action action, String text, String icon) {
		JButton button = new JButton();
		button.setAction(action);
		button.setText(I18N.getMsg(text));
		if (icon != null)
			button.setIcon(I18N.getIcon(icon));
		SwingUtil.addEnterAction(button, action);
		return button;
	}

	private JDialog getThis() {
		return this;
	}

	private AbstractAction getChooseFolderAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser(ExportPrintDialog.this.tfDir.getText());
				chooser.setFileSelectionMode(1);
				int i = chooser.showOpenDialog(ExportPrintDialog.this.getThis());
				if (i != 0)
					return;
				File file = chooser.getSelectedFile();
				ExportPrintDialog.this.tfDir.setText(file.getAbsolutePath());
			}
		};
	}

	private AbstractAction getExportAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingUtil.setWaitingCursor(ExportPrintDialog.this.getThis());
				File localFile = new File(ExportPrintDialog.this.tfDir.getText());
				ExportManager localExportManager = new ExportManager(ExportPrintDialog.this.mainFrame);
				String str = localExportManager.export(
						localFile, ExportPrintDialog.this.formatKey,
						(ExportReport) ExportPrintDialog.this.reportCombo.getSelectedItem());
				SwingUtil.setDefaultCursor(ExportPrintDialog.this.getThis());
				if (str.isEmpty()) return;
				SwingUtil.showModalDialog(new ExportPrintDialog.ConfirmDialog(str), ExportPrintDialog.this.mainFrame);
			}
		};
	}

	private AbstractAction getEnlargeAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingUtil.setWaitingCursor(ExportPrintDialog.this.getThis());
				ExportManager xpManager = new ExportManager(ExportPrintDialog.this.mainFrame);
				xpManager.export(null, "preview", (ExportReport) ExportPrintDialog.this.reportCombo.getSelectedItem());
				SwingUtil.setDefaultCursor(ExportPrintDialog.this.getThis());
			}
		};
	}

	private AbstractAction getPreviewAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ExportPrintDialog.this.preview();
			}
		};
	}

	private AbstractAction getCloseAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				DocumentUtil.storeInternal(ExportPrintDialog.this.mainFrame, SbConstants.InternalKey.EXPORT_DIRECTORY, ExportPrintDialog.this.tfDir.getText());
				ExportPrintDialog.this.getThis().dispose();
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if ((event.getSource() instanceof JRadioButton))
			this.formatKey = event.getActionCommand();
		if (event.getSource() == this.reportCombo)
			preview();
	}

	private void preview() {
		try {
			SwingUtil.setWaitingCursor(getThis());
			ExportManager xpManager = new ExportManager(mainFrame);
			xpManager.fillReport((ExportReport) reportCombo.getSelectedItem());
			previewPanel.loadReport(xpManager.getJasperPrint());
			SwingUtil.setDefaultCursor(getThis());
			timer.stop();
		} catch (Exception exc) {
			StorybookApp.error("ExportPrintDialog.preview()", exc);
		}
	}

	private class ConfirmDialog extends JDialog implements IPaintable {

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
			JTextArea text = new JTextArea(I18N.getMsg("msg.dlg.export.done.text", this.fileName));
			text.setEditable(false);
			JButton btOpen = new JButton();
			btOpen.setAction(getOpenAction());
			btOpen.setText(I18N.getMsg("msg.common.open"));
			btOpen.setIcon(I18N.getIcon("icon.small.open"));
			SwingUtil.addEnterAction(btOpen, getOpenAction());
			JButton btClose = new JButton();
			btClose.setAction(getCloseAction());
			btClose.setText(I18N.getMsg("msg.common.close"));
			btClose.setIcon(I18N.getIcon("icon.small.close"));
			SwingUtil.addEscAction(btClose, getCloseAction());
			add(text);
			add(btOpen, "split 2,gap push,sg");
			add(btClose, "sg");
		}

		private AbstractAction getOpenAction() {
			return new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent event) {
					try {
						Desktop.open(new File(ExportPrintDialog.ConfirmDialog.this.fileName));
						ExportPrintDialog.ConfirmDialog.this.getCloseAction().actionPerformed(event);
					} catch (DesktopException exc) {
						StorybookApp.error("ExportPrintDialog.getOpenAction()",exc);
					}
				}
			};
		}

		private AbstractAction getCloseAction() {
			return new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent event) {
					ExportPrintDialog.ConfirmDialog.this.getThat().dispose();
				}
			};
		}

		private JDialog getThat() {
			return this;
		}
	}
}
