/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.export;

import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import storybook.toolkit.I18N;

import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class BookExporter extends JDialog {
	protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public BookExporter() {
		this.mainFrame = null;
		this.parent = null;
	}

	public BookExporter(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public BookExporter(JComponent parent) {
		this.parent = parent;
		this.mainFrame = null;
	}
	public boolean setExportForOpenOffice(boolean b){
		JOptionPane.showMessageDialog(null,
			this.getClass().getName()+"\n\n"+I18N.getMsg("msg.common.not.available"),
			this.getClass().getName(), JOptionPane.ERROR_MESSAGE);
		return(false);
	}
	public void exportToFile(){
		JOptionPane.showMessageDialog(null,
			this.getClass().getName()+"\n\n"+I18N.getMsg("msg.common.not.available"),
			this.getClass().getName(), JOptionPane.ERROR_MESSAGE);
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
		return(x);
	}

}
