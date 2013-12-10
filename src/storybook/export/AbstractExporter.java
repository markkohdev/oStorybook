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

/**
 *
 * @author favdb
 */
import storybook.model.hbn.entity.Internal;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.filefilter.HtmlFileFilter;
import storybook.toolkit.filefilter.TextFileFilter;
import storybook.view.MainFrame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import storybook.SbConstants;

public abstract class AbstractExporter {

	private String fileName;
	private boolean onlyHtmlExport;
	protected MainFrame mainFrame;

	public abstract StringBuffer getContent();

	public AbstractExporter(MainFrame m) {
		this(m, false);
	}

	public AbstractExporter(MainFrame m, boolean b) {
		this.mainFrame = m;
		this.onlyHtmlExport = b;
		this.fileName = "";
	}

	public boolean exportToFile() {
		boolean bool = DocumentUtil.isUseHtmlScenes(this.mainFrame);
		if (this.onlyHtmlExport) {
			bool = true;
		}
		Internal localInternal = DocumentUtil.restoreInternal(this.mainFrame,
			SbConstants.InternalKey.EXPORT_DIRECTORY,
			EnvUtil.getDefaultExportDir(this.mainFrame));
		File localFile1 = new File(localInternal.getStringValue());
		JFileChooser localJFileChooser = new JFileChooser(localFile1);
		localJFileChooser.setApproveButtonText(I18N.getMsg("msg.common.export"));
		localJFileChooser.setSelectedFile(new File(getFileName()));
		if (bool) {
			localJFileChooser.setFileFilter(new HtmlFileFilter());
		} else {
			localJFileChooser.setFileFilter(new TextFileFilter());
		}
		int i = localJFileChooser.showOpenDialog(this.mainFrame);
		if (i == 1) {
			return false;
		}
		File localFile2 = localJFileChooser.getSelectedFile();
		if (bool) {
			if ((!localFile2.getName().endsWith(".html")) || (localFile2.getName().endsWith(".htm"))) {
				localFile2 = new File(localFile2.getPath() + ".html");
			}
		} else if (!localFile2.getName().endsWith(".txt")) {
			localFile2 = new File(localFile2.getPath() + ".txt");
		}
		StringBuffer localStringBuffer = getContent();
		try {
			try (BufferedWriter localBufferedWriter = new BufferedWriter(new FileWriter(localFile2))) {
				String str = localStringBuffer.toString();
				localBufferedWriter.write(str);
			}
		} catch (IOException localIOException) {
			return false;
		}
		JOptionPane.showMessageDialog(this.mainFrame,
			I18N.getMsg("msg.common.export.success")
			+ "\n"
			+ localFile2.getAbsolutePath(),
			I18N.getMsg("msg.common.export"), 1);
		return true;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String s) {
		this.fileName = s;
	}
}