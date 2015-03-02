package storybook.importer;

import org.apache.commons.io.FileUtils;

import storybook.SbConstants;
import storybook.export.ExportData;
import storybook.export.ParamExport;
import storybook.model.hbn.entity.Internal;
import storybook.toolkit.BookUtil;
import storybook.toolkit.EnvUtil;

/**
 * Utility class used for importing characters from a text file.
 * Goal is to extract as many characters, matched with their
 * genders, parsed from the text.
 * @author Mark Koh
 *
 */
public class DlgImport extends javax.swing.JDialog {
	
	/**
	 * Creates new form dlgImport
	 * @param parent : parent frame
	 * @param modal : true or false
	 */
	public DlgImport(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		//Ethan, this is where changes will go.
	}

}
