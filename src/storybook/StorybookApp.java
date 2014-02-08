/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package storybook;

import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.io.FileUtils;

import storybook.SbConstants.InternalKey;
import storybook.SbConstants.PreferenceKey;
import storybook.action.OpenFileAction;
import storybook.controller.PreferenceController;
import storybook.model.DbFile;
import storybook.model.PreferenceModel;
import storybook.model.hbn.entity.Preference;
import storybook.model.oldModel.ModelMigration;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.PrefUtil;
import storybook.toolkit.net.Updater;
import storybook.toolkit.swing.SwingUtil;
import storybook.toolkit.swing.splash.HourglassSplash;
import storybook.ui.MainFrame;
import storybook.ui.dialog.ExceptionDialog;
import storybook.ui.dialog.FirstStartDialog;
import storybook.ui.dialog.PostModelUpdateDialog;
import storybook.ui.dialog.file.NewFileDialog;

/**
 * Classe principale de l'application de type Component
 * @author martin
 */

public class StorybookApp extends Component {

	private static boolean bTrace=false;
	private static boolean bTraceHibernate=false;
	private static boolean bNewUi=false;

	private static StorybookApp instance;

	private PreferenceModel preferenceModel;
	private PreferenceController preferenceController;
	private final List<MainFrame> mainFrames;
	private Font defaultFont;

	private StorybookApp() {
		mainFrames = new ArrayList<>();
	}

/**
 * init()
 * Initialisation de la classe StorybookApp
 * - composant "préférence" (Controleur, Model)
 * - internationalisation
 * - look & feel
 * - détection si première activation
 * - interface utilisateur
 */
	private void init() {
		trace("StorybookApp.init()");
		try {
			// preference model and controller
			preferenceController = new PreferenceController();
			preferenceModel = new PreferenceModel();
			preferenceController.attachModel(preferenceModel);

			preferenceController.attachView(this);

			initI18N();

			SwingUtil.setLookAndFeel();
			restoreDefaultFont();
			// first start dialog
			Preference prefFirstStart = PrefUtil.get(
					PreferenceKey.FIRST_START_4, true);
			if (prefFirstStart.getBooleanValue()) {
				FirstStartDialog dlg = new FirstStartDialog();
				SwingUtil.showModalDialog(dlg, null);
				PrefUtil.set(PreferenceKey.FIRST_START_4, false);
			}

			Preference pref = PrefUtil.get(PreferenceKey.OPEN_LAST_FILE, false);
			boolean fileHasBeenOpened = false;
			if (pref.getBooleanValue()) {
				Preference pref2 = PrefUtil.get(PreferenceKey.LAST_OPEN_FILE,
						"");
				DbFile dbFile = new DbFile(pref2.getStringValue());
				trace("StorybookApp.init(): loading... " + dbFile);
				fileHasBeenOpened = openFile(dbFile);
			}
			if (fileHasBeenOpened) {
				// check for updates
				Updater.checkForUpdate();
				return;
			}
			MainFrame mainFrame = new MainFrame();
			mainFrame.init();
			mainFrame.initBlankUi();
			addMainFrame(mainFrame);

			// check for updates
			Updater.checkForUpdate();
/* abandon de l'appel au garbarge collector, utilisation non recommandée
			Timer timer = new Timer(10000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runGC();
				}
			});
			timer.start();
			*/

		} catch (Exception e) {
			error("StorybookApp.init()",e);
			ExceptionDialog dlg = new ExceptionDialog(e);
			SwingUtil.showModalDialog(dlg, null);
		}
	}

	/**
	 * initI18N
	 * initialisation de l'internationalisation
	 */
	public void initI18N() {
		trace("StorybookApp.initI18N()");
		String localeStr = PrefUtil.get(PreferenceKey.LANG,
				SbConstants.DEFAULT_LANG).getStringValue();
		SbConstants.Language lang = SbConstants.Language.valueOf(localeStr);
		Locale locale = lang.getLocale();
		setLocale(locale);
		I18N.initResourceBundles(getLocale());
	}

	/**
	 * getPreferenceModel
	 * obtenir le preferenceModel actuel
	 * @return le preferenceModel actuel
	 */
	public PreferenceModel getPreferenceModel() {
		return preferenceModel;
	}

	/**
	 * getPreferenceControler
	 * obtenir le preferenceControler actuel
	 * @return  le preferenceControler actuel
	 */
	public PreferenceController getPreferenceController() {
		return preferenceController;
	}

	/**
	 * getMainFrames
	 * Obtenir la liste des mainFrames existants
	 * @return la liste des mainFrames
	 */
	public List<MainFrame> getMainFrames() {
		return mainFrames;
	}

	/**
	 * addMainFrame()
	 * ajout d'un mainFrame à la liste des mainFrames
	 * @param mainFrame : mainFrame cible
	 */
	public void addMainFrame(MainFrame mainFrame) {
		trace("StorybookApp.addMainFrame("+mainFrame.getName()+")");
		mainFrames.add(mainFrame);
	}

	/**
	 * removeMainFrame
	 * @param mainFrame
	 */
	public void removeMainFrame(MainFrame mainFrame) {
		trace("StorybookApp.removeMainFrame("+mainFrame.getName()+")");
		mainFrames.remove(mainFrame);
	}

	/**
	 * closeBlank
	 * suppression de mainFrame vide
	 */
	public void closeBlank() {
		trace("StorybookApp.closeBlank()");
		for (MainFrame mainFrame : mainFrames) {
			if (mainFrame.isBlank()) {
				mainFrames.remove(mainFrame);
				mainFrame.dispose();
			}
		}
	}
/* suppression de l'appel du garbage collector
	public void runGC(){
		System.gc();
	}
*/
	/**
	 * getInstance
	 * obtenir l'instance courante
	 * @return l'objet StorybookApp actuel ou crée l'instance
	 */
	public static StorybookApp getInstance() {
		if (instance == null) {
			instance = new StorybookApp();
		}
		return instance;
	}

	/**
	 * createNewFile
	 * créer un nouveau fichier
	 * si le nom de fichier est correct :
	 * - réinitialisation de l'interface utilisateur
	 * - fermeture des mainFrames vide
	 * - actualisation de la liste des derniers fichiers ouverts
	 */
	public void createNewFile() {
		trace("StorybookApp.createNewFile()");
		try {
			NewFileDialog dlg = new NewFileDialog();
			SwingUtil.showModalDialog(dlg, null);
			if (dlg.isCanceled()) {
				return;
			}
			DbFile dbFile = new DbFile(dlg.getFile());
			String dbName = dbFile.getDbName();
			if (dbName == null) {
				return;
			}
			final MainFrame newMainFrame = new MainFrame();
			newMainFrame.init(dbFile);
			newMainFrame.getDocumentModel().initEntites();
			DocumentUtil.storeInternal(newMainFrame, InternalKey.USE_HTML_SCENES, dlg.getUseHtmlScenes());
			DocumentUtil.storeInternal(newMainFrame, InternalKey.USE_HTML_DESCR, dlg.getUseHtmlDescr());
			newMainFrame.initUi();
			newMainFrame.getDocumentController().fireAgain();

			addMainFrame(newMainFrame);
			closeBlank();

			updateFilePref(dbFile);

			setDefaultCursor();
		} catch (Exception e) {
			error("StorybookApp.createNewFile()",e);
		}
	}

	/**
	 * renameFile
	 * modification du nom du fichier par copie dans un nouveau
	 * fichier
	 * @param mainFrame le mainFrame cible
	 * @param file nouveau nom du fichier
	 * si la transformation du nom du fichier :
	 * - fermeture du mainFrame courant (désynchronisé de 1 seconde)
	 * - suppression de l'ancien fichier (désynchronisé de 4 secondes)
	 */
	public void renameFile(final MainFrame mainFrame, File file) {
		trace("StorybookApp.renameFile("+mainFrame.getName()+","+file.getAbsolutePath()+")");
		try {
			FileUtils.copyFile(mainFrame.getDbFile().getFile(), file);
			DbFile dbFile = new DbFile(file);
			OpenFileAction act = new OpenFileAction("", dbFile);
			act.actionPerformed(null);
			Timer timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainFrame.close();
				}
			});
			timer.setRepeats(false);
			timer.start();
			Timer timer2 = new Timer(4000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mainFrame.getDbFile().getFile().delete();
				}
			});
			timer2.setRepeats(false);
			timer2.start();
		} catch (IOException e) {
			error("StorybookApp.renameFile("+mainFrame.getName()+","+file.getName()+")", e);
		}
	}

	/**
	 * openFile
	 * ouverture d'un fichier avec dialogue de choix
	 * @return booléen vrai si le fichier est bien ouvert
	 */
	public boolean openFile() {
		trace("StorybookApp.openFile()");
		final DbFile dbFile = DocumentUtil.openDocumentDialog();
		if (dbFile == null) {
			return false;
		}
		return openFile(dbFile);
	}

	/**
	 * openFile
	 * ouverture d'un fichier avec vérification
	 * @param dbFile fichier à ouvrir
	 * @return booléen vrai si ouverture effectuée
	 * Si l'ouverture est correcte :
	 * - vérification que le fichier existe, si non retourne faux
	 * - vérification si le fichier peut être écrit, si non retourne faux
	 * - vérification que le fichier n'est pas déjà ouvert, si déjà ouvert
	 *		retourne vrai
	 */
	public boolean openFile(final DbFile dbFile) {
		trace("StorybookApp.openFile("+dbFile.getDbName()+")");
		try {
			// file doesn't exist
			if (!dbFile.getFile().exists()) {
				String txt = I18N.getMsg("msg.dlg.project.not.exits.text",
						dbFile.getFile().getPath());
				JOptionPane.showMessageDialog(null, txt,
						I18N.getMsg("msg.dlg.project.not.exits.title"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			// file is read-only
			if (!dbFile.getFile().canWrite()) {
				String txt = I18N.getMsg("msg.error.db.read.only", dbFile
						.getFile().getPath());
				JOptionPane.showMessageDialog(null, txt,
						I18N.getMsg("msg.common.warning"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			// file already opened
			String dbName = dbFile.getDbName();
			if (checkIfAlreadyOpened(dbName)) {
				return true;
			}

			// model update from Storybook 3.x to 4.0
			final ModelMigration oldPersMngr = ModelMigration.getInstance();
			oldPersMngr.open(dbFile);
			try {
				if (!oldPersMngr.checkAndAlterModel()) {
					oldPersMngr.closeConnection();
					return false;
				}
			} catch (Exception e) {
				oldPersMngr.closeConnection();
				StorybookApp.error("StorybookApp.openFile("+dbFile.getDbName()+")",e);
				ExceptionDialog dlg = new ExceptionDialog(e);
				SwingUtil.showModalDialog(dlg, null);
				return false;
			}

			oldPersMngr.closeConnection();
			setWaitCursor();
			String text = I18N.getMsg("msg.common.loading", dbFile.getName());
			final HourglassSplash dlg = new HourglassSplash(text);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						MainFrame newMainFrame = new MainFrame();
						newMainFrame.init(dbFile);
						newMainFrame.initUi();
						addMainFrame(newMainFrame);
						closeBlank();
						dlg.dispose();

						updateFilePref(dbFile);
						reloadMenuBars();
						setDefaultCursor();
						if (oldPersMngr.hasAlteredDbModel()) {
							PostModelUpdateDialog dlg2 = new PostModelUpdateDialog(newMainFrame);
							SwingUtil.showModalDialog(dlg2, newMainFrame);
						}
					} catch (Exception e) {
					}
				}
			});
		} catch (HeadlessException e) {
		}
		return true;
	}

	private boolean checkIfAlreadyOpened(String dbName) {
		trace("StorybookApp.checkIfAlreadyOpened("+dbName+")");
		for (MainFrame mainFrame : mainFrames) {
			if (mainFrame.isBlank()) {
				continue;
			}
			if (mainFrame.getDbFile().getDbName().equals(dbName)) {
				mainFrame.setVisible(true);
				return true;
			}
		}
		return false;
	}

	private void updateFilePref(DbFile dbFile) {
		trace("StorybookApp.updateFilePref("+dbFile.getDbName()+")");
		// save last open directory and file
		File file = dbFile.getFile();
		PrefUtil.set(PreferenceKey.LAST_OPEN_DIR, file.getParent());
		PrefUtil.set(PreferenceKey.LAST_OPEN_FILE, file.getPath());
		// save recent files
		List<DbFile> list = PrefUtil.getDbFileList();
		if (!list.contains(dbFile)) {
			list.add(dbFile);
		}
		// check recent files and remove non-existing entries
		Iterator<DbFile> it = list.iterator();
		while (it.hasNext()) {
			DbFile dbFile2 = it.next();
			if (!dbFile2.getFile().exists()) {
				it.remove();
			}
		}
		PrefUtil.setDbFileList(list);
		reloadMenuBars();
	}

	public void clearRecentFiles() {
		trace("StorybookApp.clearRecentFiles()");
		PrefUtil.setDbFileList(new ArrayList<DbFile>());
		reloadMenuBars();
	}

	public void exit() {
		trace("StorybookApp.exit()");
		if (mainFrames.size() > 0) {
			Preference pref = PrefUtil.get(PreferenceKey.CONFIRM_EXIT, true);
			if (pref.getBooleanValue()) {
				int n = JOptionPane.showConfirmDialog(null,
						I18N.getMsg("msg.mainframe.want.exit"),
						I18N.getMsg("msg.common.exit"),
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION
						|| n == JOptionPane.CLOSED_OPTION) {
					return;
				}
			}
			saveAll();
		}
		System.exit(0);
	}

	public void resetUiFont() {
		if (defaultFont == null) {
			return;
		}
		SwingUtil.setUIFont(new javax.swing.plaf.FontUIResource(defaultFont
				.getName(), defaultFont.getStyle(), defaultFont.getSize()));
	}

	public void setDefaultFont(Font font) {
		if (font == null) {
			return;
		}
		defaultFont = font;
		resetUiFont();
		PrefUtil.set(PreferenceKey.DEFAULT_FONT_NAME, font.getName());
		PrefUtil.set(PreferenceKey.DEFAULT_FONT_SIZE, font.getSize());
		PrefUtil.set(PreferenceKey.DEFAULT_FONT_STYLE, font.getStyle());
	}

	public Font getDefaultFont() {
		return this.defaultFont;
	}

	public void restoreDefaultFont() {
		Preference pref = PrefUtil.get(PreferenceKey.DEFAULT_FONT_NAME, SbConstants.DEFAULT_FONT_NAME);
		String name = SbConstants.DEFAULT_FONT_NAME;
		if (pref != null && !pref.getStringValue().isEmpty()) {
			name = pref.getStringValue();
		}

		pref = PrefUtil.get(PreferenceKey.DEFAULT_FONT_STYLE, SbConstants.DEFAULT_FONT_STYLE);
		int style = 0;
		if (pref != null) {
			style = pref.getIntegerValue();
		}

		pref = PrefUtil.get(PreferenceKey.DEFAULT_FONT_SIZE, SbConstants.DEFAULT_FONT_SIZE);
		int size = 0;
		if (pref != null) {
			size = pref.getIntegerValue();
		}

		// set default font
		setDefaultFont(new Font(name, style, size));
	}

	public void refresh() {
		trace("StorybookApp.refresh()");
		for (MainFrame mainFrame : mainFrames) {
			int width = mainFrame.getWidth();
			int height = mainFrame.getHeight();
			boolean maximized = mainFrame.isMaximized();
			mainFrame.getSbActionManager().reloadMenuToolbar();
			mainFrame.setSize(width, height);
			if (maximized) {
				mainFrame.setMaximized();
			}
			mainFrame.getActionController().handleRefresh();
		}
	}

	public void reloadMenuBars() {
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.getSbActionManager().reloadMenuToolbar();
		}
	}

	public void reloadStatusBars() {
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.refreshStatusBar();
		}
	}

	public void setWaitCursor() {
		for (MainFrame mainFrame : mainFrames) {
			SwingUtil.setWaitingCursor(mainFrame);
		}
	}

	public void setDefaultCursor() {
		for (MainFrame mainFrame : mainFrames) {
			SwingUtil.setDefaultCursor(mainFrame);
		}
	}

	public void saveAll() {
		trace("StorybookApp.saveAll()");
		for (MainFrame mainFrame : mainFrames) {
			mainFrame.getSbActionManager().getActionHandler().handleSave();
		}
	}

	public void modelPropertyChange(PropertyChangeEvent evt) {
		// works, but currently not used
		// may be used for entity copying between files
		// String propName = evt.getPropertyName();
		// Object newValue = evt.getNewValue();
		// Object oldValue = evt.getOldValue();
	}

	public static void main(String[] args) {
		if ("true".equals(SbConstants.Storybook.IS_BETA_VERSION.toString())) {
			// month is 0-based
			Calendar expireDate = new GregorianCalendar(2012, 5, 14);
			Calendar now = Calendar.getInstance();
			if (now.after(expireDate)) {
				JOptionPane.showMessageDialog(null,
						"This beta version of Storbyook has expired.",
						"Version has expired",
						JOptionPane.WARNING_MESSAGE);
				System.exit(-1);
			} else {
				DateFormat format = I18N.getLongDateFormatter();
				String dateStr = format.format(expireDate.getTime());
				JOptionPane.showMessageDialog(null,
						"This is a beta version of Storybook 4." +
						"\n\n- FOR TESTING ONLY." +
						"\n- Don't use it for productive use." +
						"\n- Work only with copies of your Stroybook 3.x files." +
						"\n\nThis beta version will expire on "
								+ dateStr +"\n\n", "Beta Version",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		String property = "java.io.tmpdir";
		String tempDir = System.getProperty(property);
		String fn = tempDir + File.separator + "storybook.lck";
		if (!lockInstance(fn)) {
			Object[] options = { I18N.getMsg("msg.running.remove"),
					I18N.getMsg("msg.common.cancel") };
			int n = JOptionPane.showOptionDialog(null,
					I18N.getMsg("msg.running.msg"),
					I18N.getMsg("msg.running.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				File file = new File(fn);
				if (file.exists() && file.canWrite()) {
					if (!file.delete()) {
						JOptionPane.showMessageDialog(null,
								"Delete failed",
								"File\n" + file.getAbsolutePath()
										+ "\ncould not be deleted.",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			return;
		}
		if (args.length>0) {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("--trace")) {
					StorybookApp.bTrace=true;
					StorybookApp.trace("Storybook execution in trace mode");
				}
				if (arg.equalsIgnoreCase("--hibernate")) {
					StorybookApp.bTraceHibernate=true;
					StorybookApp.trace("Hibernate in trace mode");
				}
				if (arg.equalsIgnoreCase("--newUI")) {
					StorybookApp.bNewUi=true;
					StorybookApp.trace("Application with new UI");
				}
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StorybookApp app=StorybookApp.getInstance();
				app.init();
			}
		});
	}

	private static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(
					file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} catch (IOException e) {
							System.err.println("Unable to remove lock file: "+ lockFile+"->"+e.getMessage());
						}
					}
				});
				return true;
			}
		} catch (IOException e) {
			System.err.println("Unable to create and/or lock file: " + lockFile+"->"+e.getMessage());
		}
		return false;
	}

	public static void error(String txt, Exception e) {
		System.err.println(txt+" Exception:"+e.getMessage());
	}

	public static void error() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static void trace(String msg) {
		if (bTrace) {
			System.out.println(msg);
		}
	}

	public static boolean getTrace() {
		return(bTrace);
	}

	public static boolean getTraceHibernate() {
		return(bTraceHibernate);
	}

	public static void logErr(String str, Exception ex) {
		System.err.println(str+ex.getMessage());
	}

	public static void logErr(String str, Error er) {
		System.err.println(str+er.getMessage());
	}

	public static boolean isNewUi() {
		return(bNewUi);
	}

}
