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
package storybook.toolkit;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JOptionPane;
import storybook.model.hbn.entity.Scene;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class CallLibreOffice {

	String pathToLO = "/home/favdb/";

	public void execLibreOffice() {
	}

	public void openFile(MainFrame mainFrame, Scene scene) {
		if (pathToLO.equals("")) {
			JOptionPane.showMessageDialog(null,
				"You must select your word processor before.\nPlease see the application preference",
				"Lauching word processor",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		String name=getFilePath(mainFrame,scene);
		File file=new File(name);
		if (!file.exists()) {
			if (JOptionPane.showConfirmDialog(null,
				"Scene text file does not exist.\nDo you want to create?",
				"Lauching word processor",
				JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
				createFile(mainFrame,scene);
			}
		}
		/*
		String[] command = {pathToLO+"libreoffice",name};
		 try {
			Runtime.getRuntime().exec(command);
		 } catch(IOException e) {
			System.err.println("execLibreOffice("+str+")");
		 }
		 */
	}

	public void createFile(MainFrame mainFrame,Scene scene) {
	}

	private String getFilePath(MainFrame mainFrame, Scene scene) {
		String path = mainFrame.getDbFile().getPath();
		String str1 = scene.getChapterSceneNo();
		if (str1.length() < 2) {str1 = "0" + str1;}
		String str2 = scene.getChapterSceneNo();
		if (str2.length() < 2) {str2 = "0" + str2;}
		String str = path
			+ I18N.getMsg("msg.common.chapter") + " " + str1 + " "
			+ I18N.getMsg("msg.common.scene") + " " + str2
			+ ".odt";
		System.out.println("Scene odt file=" + str);
		return(str);
	}

	public void setPathToLO(String p) {
	}

	public void findPathToLO(String p) {
	}
}
