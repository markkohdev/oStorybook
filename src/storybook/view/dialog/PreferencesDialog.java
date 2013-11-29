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

package storybook.view.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import storybook.SbConstants;
import storybook.StorybookApp;
import storybook.SbConstants.Language;
import storybook.SbConstants.LookAndFeel;
import storybook.SbConstants.PreferenceKey;
import storybook.SbConstants.Spelling;
import storybook.model.hbn.entity.Preference;
import storybook.toolkit.I18N;
import storybook.toolkit.PrefUtil;
import storybook.toolkit.SpellCheckerUtil;
import storybook.toolkit.net.NetUtil;
import storybook.toolkit.swing.SwingUtil;

import net.miginfocom.swing.MigLayout;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends AbstractDialog implements
		ActionListener, CaretListener {

	private static final String CN_CHECK = "check";
	private static final String CN_PW_FORGOTTEN = "pwforgotten";
	private static final String CN_GO_PRO = "gopro";

	private AbstractAction fontChooserAction;

	private JComboBox languageCombo;
	private JComboBox spellingCombo;
	private JCheckBox cbLoadFileOnStart;
	private JCheckBox cbConfirmExit;
	// private JCheckBox cbUpdate;
	private JLabel lbShowFont;
	private Font font;
	private JComboBox lafCombo;
	private JTabbedPane tabbedPane;
	private JTextField tfGoogleMapsUrl;
	private JCheckBox cbTranslatorMode;
	/* SB5 suppress Pro
	private JTextField tfEmail;
	private JPasswordField pfPassword;
	private JButton btCheck;
	private JButton btPwForgotten;
	private JButton btGoPro;
	*/
	private JTextArea taResult;

	public PreferencesDialog() {
		super();
		initAll();
	}

	@Override
	public void init() {
		font = StorybookApp.getInstance().getDefaultFont();
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout("wrap,fill", "", "[grow][]"));
		setTitle(I18N.getMsg("msg.dlg.preference.title"));
		setIconImage(I18N.getIconImage("icon.sb"));
		Dimension dim = new Dimension(600, 530);
		setPreferredSize(dim);
		setMinimumSize(dim);

		JPanel panel = new JPanel(new MigLayout("flowy,fill"));
		panel.add(createCommonPanel(), "growx");
		panel.add(createAppearancePanel(), "growx");
		panel.add(createInternetPanel(), "growx");
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(I18N.getMsg("msg.dlg.preference.global"), panel);
		/* SB5 suppress Pro
		tabbedPane.addTab("Storybook Pro", createProPanel());
		*/
		tabbedPane.addTab("Translators", createTranslatorsPanel());

		// layout
		add(tabbedPane, "grow");
		add(getOkButton(), "split 2,sg,right");
		add(getCancelButton(), "sg");
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySettings();
				canceled = false;
				dispose();
			}
		};
	}

	private void applySettings() {
		StorybookApp app = StorybookApp.getInstance();
		SwingUtil.setWaitingCursor(this);

		PrefUtil.set(PreferenceKey.OPEN_LAST_FILE,
				cbLoadFileOnStart.isSelected());
		PrefUtil.set(PreferenceKey.CONFIRM_EXIT, cbConfirmExit.isSelected());

		// language
		int i = languageCombo.getSelectedIndex();
		Language lang = Language.values()[i];
		Locale locale = lang.getLocale();
		PrefUtil.set(PreferenceKey.LANG, I18N.getCountryLanguage(locale));
		I18N.initResourceBundles(locale);

		// spell checker
		String ix = (String) spellingCombo.getSelectedItem();
		//Spelling spelling = Spelling.values()[i];
		for (Spelling spelling : Spelling.values()) {
			if (spelling.getI18N().equals(ix)) {
				PrefUtil.set(PreferenceKey.SPELLING, spelling.name());
			}
		}
		SpellCheckerUtil.registerDictionaries();

		// look and feel
		i = lafCombo.getSelectedIndex();
		LookAndFeel laf = LookAndFeel.values()[i];
		SwingUtil.setLookAndFeel(laf);

		// default font
		app.setDefaultFont(font);

		// Google Maps
		PrefUtil.set(PreferenceKey.GOOGLE_MAPS_URL, tfGoogleMapsUrl.getText());
		NetUtil.setGoogleMapUrl(tfGoogleMapsUrl.getText());

		// translator mode
		PrefUtil.set(PreferenceKey.TRANSLATOR_MODE,
				cbTranslatorMode.isSelected());

		// email, password
		/*
		PrefUtil.set(PreferenceKey.EMAIL, tfEmail.getText());
		String pw = new String(pfPassword.getPassword());
		PrefUtil.set(PreferenceKey.PASSWORD, pw);
		Guardian guardian = Guardian.getInstance();
		guardian.setEmail(tfEmail.getText());
		guardian.setPassword(pw);
		*/
		// refresh
		app.refresh();

		SwingUtil.setDefaultCursor(this);
	}

	private JPanel createCommonPanel() {
		MigLayout layout = new MigLayout("wrap 2", "", "[]10");
		JPanel panel = new JPanel(layout);
		panel.setBorder(BorderFactory.createTitledBorder(I18N
				.getMsg("msg.common")));

		// language
		JLabel lbLanguage = new JLabel(I18N.getMsgColon("msg.common.language"));
		languageCombo = SwingUtil.createLanguageCombo();
		String currentLangStr = I18N.getCountryLanguage(Locale.getDefault());
		Language lang = Language.valueOf(currentLangStr);
		languageCombo.setSelectedIndex(lang.ordinal());

		// spelling
		JLabel lbSpelling = new JLabel(I18N.getMsgColon("msg.pref.spelling"));
		spellingCombo = SwingUtil.createSpellingCombo();
		Preference pref = PrefUtil.get(PreferenceKey.SPELLING,
				Spelling.none.toString());
		Spelling spelling = Spelling.valueOf(pref.getStringValue());
		spellingCombo.setSelectedItem(pref.getStringValue());//setSelectedIndex(spelling.ordinal());

		// start options
		JLabel lbStart = new JLabel(I18N.getMsg("msg.pref.start"));
		cbLoadFileOnStart = new JCheckBox(
				I18N.getMsg("msg.pref.start.openproject"));
		pref = PrefUtil.get(PreferenceKey.OPEN_LAST_FILE, false);
		cbLoadFileOnStart.setSelected(pref.getBooleanValue());

		// confirm exit
		JLabel lbConfirmExit = new JLabel(I18N.getMsg("msg.pref.exit"));
		cbConfirmExit = new JCheckBox(I18N.getMsg("msg.pref.exit.chb"));
		pref = PrefUtil.get(PreferenceKey.CONFIRM_EXIT, true);
		cbConfirmExit.setSelected(pref.getBooleanValue());

		// check for updates
		/* TODO check for update
		JLabel lbUpdate = new JLabel(I18N.getMsg("msg.pref.update"));
		cbUpdate = new JCheckBox(I18N.getMsg("msg.pref.update.chb"));
		Boolean updates = PrefManager.getInstance().getBooleanValue(
		Constants.Preference.CHECK_UPDATES);
		chbUpdate.getModel().setSelected(updates);
		*/

		// layout
		panel.add(lbLanguage);
		panel.add(languageCombo);
		panel.add(lbSpelling);
		panel.add(spellingCombo);
		panel.add(lbStart);
		panel.add(cbLoadFileOnStart);
		panel.add(lbConfirmExit);
		panel.add(cbConfirmExit);
		/* TODO check for update
		panel.add(lbUpdate);
		panel.add(cbUpdate);
		* */

		return panel;
	}

	/* SB5 suppress Pro
	private JPanel createProPanel() {
		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("wrap 2", "[][fill,grow]", "");
		panel.setLayout(layout);

		Guardian guardian = Guardian.getInstance();

		JLabel lbEmail = new JLabel(I18N.getMsgColon("msg.pro.email"));
		tfEmail = new JTextField();
		tfEmail.setText(guardian.getEmail());
		tfEmail.addCaretListener(this);

		JLabel lbPasswort = new JLabel(I18N.getMsgColon("msg.pro.password"));
		pfPassword = new JPasswordField();
		pfPassword.setText(guardian.getPassword());
		pfPassword.addCaretListener(this);

		btCheck = new JButton();
		btCheck.setName(CN_CHECK);
		btCheck.setEnabled(false);
		btCheck.setText(I18N.getMsg("msg.pro.check"));
		btCheck.addActionListener(this);
		checkButton();

		btPwForgotten = new JButton();
		btPwForgotten.setName(CN_PW_FORGOTTEN);
		btPwForgotten.setText(I18N.getMsg("msg.pro.pw.forgotten"));
		btPwForgotten.addActionListener(this);

		btGoPro = new JButton();
		btGoPro.setName(CN_GO_PRO);
		btGoPro.setText(I18N.getMsg("msg.common.go.pro"));
		btGoPro.addActionListener(this);

		taResult = new JTextArea(5, 40);
		taResult.setEditable(false);
		taResult.setBorder(SwingUtil.getBorderEtched());
		taResult.setLineWrap(true);
		taResult.setWrapStyleWord(true);

		// layout
		panel.add(lbEmail);
		panel.add(tfEmail);
		panel.add(lbPasswort);
		panel.add(pfPassword);
		panel.add(btCheck, "gaptop 10,span,split 3,sg");
		panel.add(btPwForgotten, "sg");
		panel.add(btGoPro, "sg");
		panel.add(new JScrollPane(taResult), "gaptop 10,span,growx");

		return panel;
	}
	*/

	private JPanel createAppearancePanel() {
		MigLayout layout = new MigLayout("wrap 2", "", "[]20[][]");
		JPanel panel = new JPanel(layout);
		panel.setBorder(BorderFactory.createTitledBorder(I18N
				.getMsg("msg.dlg.preference.appearance")));

		// standard font
		JLabel lbFont = new JLabel(I18N.getMsgColon("msg.pref.font.standard"));
		JButton btFont = new JButton();
		btFont.setAction(getFontChooserAction());
		btFont.setText(I18N.getMsg("msg.pref.font.standard.bt"));
		lbShowFont = new JLabel();
		lbShowFont.setText(SwingUtil.getNiceFontName(font));
		JLabel lbCurrentFont = new JLabel(
				I18N.getMsgColon("msg.pref.font.standard.current"));

		// look and feel
		JLabel lbLaf = new JLabel(I18N.getMsg("msg.pref.laf") + ": ");
		DefaultComboBoxModel lafModel = new DefaultComboBoxModel();
		for (SbConstants.LookAndFeel laf : SbConstants.LookAndFeel.values()) {
			lafModel.addElement(laf.getI18N());
		}
		lafCombo = new JComboBox(lafModel);
		Preference pref = PrefUtil.get(PreferenceKey.LAF,
				LookAndFeel.cross.name());
		LookAndFeel laf = LookAndFeel.valueOf(pref.getStringValue());
		lafCombo.setSelectedIndex(laf.ordinal());

		// layout
		panel.add(lbCurrentFont);
		panel.add(lbShowFont);
		panel.add(lbFont);
		panel.add(btFont, "gap bottom 16");
		panel.add(lbLaf);
		panel.add(lafCombo);

		return panel;
	}

	private JPanel createInternetPanel() {
		MigLayout layout = new MigLayout("wrap 2", "[][fill,grow]", "");
		JPanel panel = new JPanel(layout);
		panel.setBorder(BorderFactory.createTitledBorder("Internet"));

		// Google Maps URL
		JLabel lbGoogleMapsUrl = new JLabel("Google Maps URL:");
		tfGoogleMapsUrl = new JTextField();
		tfGoogleMapsUrl.setText(NetUtil.getGoogleMapsUrl());

		// layout
		panel.add(lbGoogleMapsUrl);
		panel.add(tfGoogleMapsUrl);

		return panel;
	}

	private JPanel createTranslatorsPanel() {
		MigLayout layout = new MigLayout("wrap 2", "[][fill,grow]", "");
		JPanel panel = new JPanel(layout);

		// translator mode
		JLabel lbEnableTranslatorMode = new JLabel("Translator Mode:");
		cbTranslatorMode = new JCheckBox("Enable Translator Mode");
		Preference pref = PrefUtil.get(PreferenceKey.TRANSLATOR_MODE, false);
		if (pref != null) {
			cbTranslatorMode.setSelected(pref.getBooleanValue());
		}

		// layout
		panel.add(lbEnableTranslatorMode);
		panel.add(cbTranslatorMode);

		return panel;
	}

	public AbstractAction getFontChooserAction() {
		if (fontChooserAction == null) {
			fontChooserAction = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					Font newFont = FontChooserDialog.showDialog(null, null,
							font);
					if (newFont == null) {
						return;
					}
					lbShowFont.setFont(newFont);
					lbShowFont.setText(SwingUtil.getNiceFontName(newFont));
					font = newFont;
				}
			};
		}
		return fontChooserAction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cn = ((JComponent) e.getSource()).getName();
		if (CN_CHECK.equals(cn)) {
			SwingUtil.setWaitingCursor(this);
			/*
			String pw = new String(pfPassword.getPassword());
			Guardian guardian = Guardian.getInstance();
			guardian.setEmail(tfEmail.getText());
			guardian.setPassword(pw);
			if (guardian.check()) {
				taResult.setText(I18N.getMsg("msg.pro.ok"));
			} else {
				taResult.setText(guardian.getErrorMessage());
			}
			*/
			SwingUtil.setDefaultCursor(this);
			//return;
		}

		/* SB5 suppress Pro
		if (CN_PW_FORGOTTEN.equals(cn)) {
			NetUtil.openBrowser(SbConstants.URL.PW_FORGOTTEN.toString());
			return;
		}
		if (CN_GO_PRO.equals(cn)) {
			if (I18N.isGerman()) {
				NetUtil.openBrowser(SbConstants.URL.GO_PRO_DE.toString());
			} else {
				NetUtil.openBrowser(SbConstants.URL.GO_PRO_EN.toString());
			}
			return;
		}
		*/
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		/* SB5 suppress Pro
		checkButton();
		*/
	}
	/* SB5 suppress Pro
	private void checkButton() {
		if (tfEmail.getText().length() > 0
				&& pfPassword.getPassword().length > 0) {
			btCheck.setEnabled(true);
		} else {
			btCheck.setEnabled(false);
		}
	}
	*/
}
