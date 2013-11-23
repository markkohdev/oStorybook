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

package storybook.view.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;

import storybook.SbConstants;
import storybook.SbConstants.ClientPropertyName;
import storybook.SbConstants.ComponentName;
import storybook.SbConstants.InternalKey;
import storybook.controller.DocumentController;
import storybook.model.EntityUtil;
import storybook.model.handler.AbstractEntityHandler;
import storybook.model.handler.CategoryEntityHandler;
import storybook.model.handler.ChapterEntityHandler;
import storybook.model.handler.GenderEntityHandler;
import storybook.model.handler.IdeaEntityHandler;
import storybook.model.handler.InternalEntityHandler;
import storybook.model.handler.ItemEntityHandler;
import storybook.model.handler.ItemLinkEntityHandler;
import storybook.model.handler.LocationEntityHandler;
import storybook.model.handler.PartEntityHandler;
import storybook.model.handler.PersonEntityHandler;
import storybook.model.handler.SceneEntityHandler;
import storybook.model.handler.StrandEntityHandler;
import storybook.model.handler.TagEntityHandler;
import storybook.model.handler.TagLinkEntityHandler;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.AbstractTag;
import storybook.model.hbn.entity.Attribute;
import storybook.model.hbn.entity.Category;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Idea;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Item;
import storybook.model.hbn.entity.ItemLink;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.model.hbn.entity.Tag;
import storybook.model.hbn.entity.TagLink;
import storybook.model.state.IdeaState;
import storybook.model.state.SceneState;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.completer.AbbrCompleter;
import storybook.toolkit.swing.AutoCompleteComboBox;
import storybook.toolkit.swing.CleverColorChooser;
import storybook.toolkit.swing.ColorUtil;
import storybook.toolkit.swing.FontUtil;
import storybook.toolkit.swing.IconUtil;
import storybook.toolkit.swing.SwingUtil;
import storybook.toolkit.swing.htmleditor.HtmlEditor;
import storybook.toolkit.swing.panel.DateChooser;
import storybook.toolkit.swing.panel.PlainTextEditor;
import storybook.toolkit.swing.verifier.AbstractInputVerifier;
import storybook.toolkit.swing.verifier.DocumentSizeFilter;
import storybook.toolkit.swing.verifier.AbstractInputVerifier.ErrorState;
import storybook.view.AbstractPanel;
import storybook.view.MainFrame;
import storybook.view.RadioButtonGroup;
import storybook.view.SbColumn;
import storybook.view.SbColumn.InputType;
import storybook.view.attributes.AttributesPanel;
import storybook.view.combo.IRefreshableComboModel;

import net.miginfocom.swing.MigLayout;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class EntityEditor extends AbstractPanel implements ActionListener,
		ItemListener {

	public static Dimension MINIMUM_SIZE = new Dimension(440, 500);

	private static final String ERROR_LABEL = "error_label";

	private enum MsgState {
		ERRORS, WARNINGS, UPDATED, ADDED
	}

	private boolean leaveOpen;

	private AbstractEntityHandler entityHandler;

	private MainFrame mainFrame;
	private DocumentController ctrl;

	private AbstractEntity entity;

	private ErrorState errorState;

	private JTabbedPane tabbedPane;
	private TitlePanel titlePanel;
	private Vector<JComponent> inputComponents;
	private Vector<JPanel> containers;
	private JButton btAddOrUpdate;
	private JLabel lbMsgState;
	private HashMap<RadioButtonGroup, RadioButtonGroupPanel> rbgPanels;

	private ArrayList<Attribute> attributes;

	private AbstractEntity origEntity = null;

	public EntityEditor(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.ctrl = mainFrame.getDocumentController();
	}

	@Override
	public void init() {
		containers = new Vector<JPanel>();
		inputComponents = new Vector<JComponent>();
		rbgPanels = new HashMap<RadioButtonGroup, RadioButtonGroupPanel>();

		try {
			Internal internal = DocumentUtil.restoreInternal(mainFrame,
					InternalKey.LEAVE_EDITOR_OPEN,
					SbConstants.DEFAULT_LEAVE_EDITOR_OPEN);
			if (internal != null) {
				leaveOpen = internal.getBooleanValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
			leaveOpen = SbConstants.DEFAULT_LEAVE_EDITOR_OPEN;
		}
	}

	/**
	 * Minimum size: see {@link MainFrame#showView(ViewName))}
	 */
	@Override
	public void initUi() {
		setLayout(new MigLayout("fill,wrap"));
		setBackground(Color.white);
		setMinimumSize(MINIMUM_SIZE);

		removeAll();
		containers.removeAllElements();
		inputComponents.clear();
		rbgPanels.clear();

		if (entityHandler == null) {
			JLabel lb = new JLabel(I18N.getMsg("msg.editor.nothing.to.edit"),
					JLabel.CENTER);
			SwingUtil.setMaxPreferredSize(lb);
			add(lb, "top");
			SwingUtil.forceRevalidate(this);
			return;
		}

		titlePanel = new TitlePanel();
		titlePanel.refresh(entity);
		add(titlePanel, "growx");

		containers.add(new JPanel());
		JPanel container = containers.lastElement();
		container.setLayout(new MigLayout("wrap 2", "[][grow]", ""));
		container.putClientProperty(
				SbConstants.ClientPropertyName.COMPONENT_TITLE.toString(),
				I18N.getMsg("msg.common"));

		for (SbColumn col : entityHandler.getColumns()) {
			if (col.isShowInSeparateTab()) {
				containers.add(new JPanel());
				container = containers.lastElement();
				container.setLayout(new MigLayout("fill,wrap", "[grow]", ""));
				container.putClientProperty(
						SbConstants.ClientPropertyName.COMPONENT_TITLE
								.toString(), col.toString());
			}

			RadioButtonGroupPanel btgPanel = null;

			// skip label for components shown in a separate tab
			if (!col.isShowInSeparateTab()) {
				// label
				if (col.hasRadioButtonGroup()) {
					// radio button group
					RadioButtonGroup rbg = col.getRadioButtonGroup();
					btgPanel = rbgPanels.get(rbg);
					if (btgPanel == null) {
						btgPanel = new RadioButtonGroupPanel(rbg);
						rbgPanels.put(rbg, btgPanel);
					}
					container.add(btgPanel, "span");
				}
				if (col.getInputType() == InputType.NONE) {
					// skip input type "none"
					continue;
				}
				JLabel lb = new JLabel();
				lb.setText(col.toString() + ":");
				// make mandatory labels bold, but skip read-only fields
				if (col.hasVerifier()) {
					if (col.getVerifier().isMandatory() && !col.isReadOnly()) {
						lb.setFont(FontUtil.getBoldFont());
					}
				}
				if (col.hasRadioButtonGroup()) {
					btgPanel.getSubPanel(col.getRadioButtonIndex()).add(lb, "top");
				} else {
					container.add(lb, "top");
				}
			}

			// input field
			InputType inputType = col.getInputType();
			JComponent comp = null;
			if (inputType == InputType.NONE) {
				comp = new JLabel();
			} else if (inputType == InputType.TEXTFIELD) {
				comp = new JTextField(20);
				if (col.hasVerifier()) {
					if (col.getVerifier().isNumber()) {
						((JTextField) comp).setColumns(4);
					}
				}
				if (col.isAutoComplete()) {
					comp = new AutoCompleteComboBox();
				} else if (col.hasMaxLength()) {
					AbstractDocument doc = (AbstractDocument) ((JTextField) comp)
							.getDocument();
					doc.setDocumentFilter(new DocumentSizeFilter(col
							.getMaxLength()));
				}
			} else if (inputType == InputType.TEXTAREA) {
				if (col.getMethodName().equals("Description")
						|| col.getMethodName().equals("Notes")) {
					if (DocumentUtil.isUseHtmlDescr(mainFrame)) {
						comp = new HtmlEditor();
						((HtmlEditor) comp).setMaxLength(col.getMaxLength());
					}
				} else {
					if (DocumentUtil.isUseHtmlScenes(mainFrame)) {
						comp = new HtmlEditor(
								DocumentUtil.isEditorFullToolbar(mainFrame));
						((HtmlEditor) comp).setMaxLength(col.getMaxLength());
					}
				}
				if (comp == null) {
					comp = new PlainTextEditor();
					((PlainTextEditor) comp).setMaxLength(col.getMaxLength());
				}
			} else if (inputType == InputType.CHECKBOX) {
				comp = new JCheckBox();
			} else if (inputType == InputType.COMBOBOX) {
				comp = new JComboBox();
			} else if (inputType == InputType.DATE) {
				comp = new DateChooser(mainFrame, col.hasDateTime());
				comp.setPreferredSize(new Dimension(150, 20));
			} else if (inputType == InputType.COLOR) {
				comp = new CleverColorChooser(
						I18N.getMsg("msg.dlg.strand.choose.color"), null,
						ColorUtil.getNiceColors(), col.isAllowNoColor());
			} else if (inputType == InputType.ICON) {
				comp = new JLabel();
			} else if (inputType == InputType.LIST) {
				comp = new CheckBoxPanel(mainFrame);
			} else if (inputType == InputType.ATTRIBUTES) {
				comp = new AttributesPanel(mainFrame);
			}

			comp.setName(col.getMethodName());
			comp.putClientProperty(
					ClientPropertyName.DOCUMENT_MODEL.toString(),
					mainFrame.getDocumentModel());
			if (col.isReadOnly()) {
				comp.setEnabled(false);
			}
			if (col.hasVerifier()) {
				comp.setInputVerifier(col.getVerifier());
			}
			if (inputType == InputType.TEXTAREA
					|| inputType == InputType.ATTRIBUTES) {
				if (comp instanceof JTextArea
						|| comp instanceof AttributesPanel) {
					JScrollPane scroller = new JScrollPane(comp);
					SwingUtil.setUnitIncrement(scroller);
					SwingUtil.setMaxPreferredSize(scroller);
					container.add(scroller, "grow,id " + comp.getName());
				} else {
					SwingUtil.setMaxPreferredSize(comp);
					if (comp instanceof HtmlEditor) {
						container.add(new JLabel());
						container.add(comp, "span,grow,id " + comp.getName());
					} else {
						container.add(comp, "grow,id " + comp.getName());
					}
				}
			} else if (inputType == InputType.LIST) {
				JScrollPane scroller = new JScrollPane(comp);
				SwingUtil.setUnitIncrement(scroller);
				SwingUtil.setMaxPreferredSize(scroller);
				container.add(scroller, "grow");
			} else {
				if (col.hasRadioButtonGroup()) {
					btgPanel.getSubPanel(col.getRadioButtonIndex()).add(comp,
							"id " + comp.getName());
				} else {
					String growx = "";
					if (col.isGrowX()) {
						growx = "growx,";
					}
					container.add(comp, growx + "id " + comp.getName());
				}
			}
			inputComponents.add(comp);
		}

		// handle completer
		for (SbColumn column : entityHandler.getColumns()) {
			if (column.hasCompleter()) {
				if (column.getCompleter() instanceof AbbrCompleter) {
					AbbrCompleter abbrCompleter = (AbbrCompleter) column
							.getCompleter();
					Iterator<JComponent> it = inputComponents.iterator();
					while (it.hasNext()) {
						JComponent comp = it.next();
						if (comp.getName() == column.getMethodName()) {
							abbrCompleter.setComp((JTextComponent) comp);
						}
						if (comp.getName().equals(abbrCompleter.getCompName1())) {
							comp.addKeyListener(abbrCompleter);
							abbrCompleter.setSourceComp1((JTextComponent) comp);
						}
						if (comp.getName().equals(abbrCompleter.getCompName2())) {
							comp.addKeyListener(abbrCompleter);
							abbrCompleter.setSourceComp2((JTextComponent) comp);
						}
					}
				}
			}
		}

		tabbedPane = new JTabbedPane();
		int i = 0;
		for (JPanel container2 : containers) {
			String title = (String) container2
					.getClientProperty(SbConstants.ClientPropertyName.COMPONENT_TITLE
							.toString());
			if (i == 0) {
				// put the first panel into a scroller (for small screens)
				container2.setPreferredSize(new Dimension(400, 520));
				JScrollPane scroller = new JScrollPane(container2);
				SwingUtil.setUnitIncrement(scroller);
				tabbedPane.addTab(title, scroller);
			} else {
				tabbedPane.addTab(title, container2);
			}
			++i;
		}
		SwingUtil.setMaxPreferredSize(tabbedPane);
		add(tabbedPane);

		lbMsgState = new JLabel();

		JButton btOk = new JButton(I18N.getMsg("msg.common.ok"));
		btOk.setName(ComponentName.BT_OK.toString());
		btOk.setIcon(I18N.getIcon("icon.small.ok"));
		btOk.addActionListener(this);

		btAddOrUpdate = new JButton(I18N.getMsg("msg.editor.update"));
		btAddOrUpdate.setName(ComponentName.BT_ADD_OR_UPDATE.toString());
		if (I18N.isEnglish()) {
			btAddOrUpdate.setIcon(I18N.getIcon("icon.small.refresh"));
		}
		btAddOrUpdate.addActionListener(this);

		JButton btCancel = new JButton(I18N.getMsg("msg.common.cancel"));
		btCancel.setName(ComponentName.BT_CANCEL.toString());
		if (I18N.isEnglish()) {
			btCancel.setIcon(I18N.getIcon("icon.small.cancel"));
		}
		btCancel.addActionListener(this);
		SwingUtil.addEscAction(btCancel, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abandonEntityChanges();
				refresh();
				if (!leaveOpen) {
					mainFrame.hideEditor();
				}
			}
		});

		JCheckBox cbLeaveOpen = new JCheckBox();
		cbLeaveOpen.setText(I18N.getMsg("msg.editor.leave.open"));
		cbLeaveOpen.setOpaque(false);
		cbLeaveOpen.addItemListener(this);
		cbLeaveOpen.setSelected(leaveOpen);

		add(lbMsgState, "growx");
		add(btOk, "span,split 4,sg");
		add(btAddOrUpdate, "sg");
		add(btCancel, "sg");
		add(cbLeaveOpen, "gap push");

		SwingUtil.forceRevalidate(this);
	}

	private void setMsgState(MsgState state) {
		String text = "";
		Icon icon = null;
		switch (state) {
		case ERRORS:
			text = I18N.getMsg("msg.common.error");
			icon = IconUtil.StateIcon.ERROR.getIcon();
			break;
		case WARNINGS:
			text = I18N.getMsg("msg.common.warning");
			icon = IconUtil.StateIcon.WARNING.getIcon();
			break;
		case ADDED:
			text = I18N.getMsg("msg.editor.added");
			icon = IconUtil.StateIcon.OK.getIcon();
			break;
		case UPDATED:
			text = I18N.getMsg("msg.editor.updated");
			icon = IconUtil.StateIcon.OK.getIcon();
			break;
		}
		lbMsgState.setVisible(true);
		lbMsgState.setText(text);
		lbMsgState.setIcon(icon);

		if (state == MsgState.ADDED || state == MsgState.UPDATED) {
			Timer timer = new Timer(1500, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					lbMsgState.setText("");
					lbMsgState.setIcon(null);
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();

		// not used
//		if (CommonProps.UNLOAD_EDITOR.check(propName)) {
//			if (entity != null) {
//				final View editorView = mainFrame.getView(ViewName.EDITOR);
//				boolean showing = editorView.isShowing();
//				if (showing) {
//					int n = showConfirmation();
//					if (n == 0) {
//						addOrUpdateEntity();
//					} else if (n == 1) {
//						abandonEntityChanges();
//					} else if (n == 2) {
//						SwingUtilities.invokeLater(new Runnable() {
//							@Override
//							public void run() {
//								editorView.restoreFocus();
//							}
//						});
//						return;
//					}
//				}
//			}
//			entity = null;
//			entityHandler = null;
//			initUi();
//			return;
//		}

		if (propName.startsWith("Delete")) {
			if (entity != null
					&& entity.equals((AbstractEntity) evt.getOldValue())) {
				entityHandler = null;
				initUi();
			}
			return;
		}

		if (!propName.startsWith("Edit")) {
			return;
		}

		if (entity != null) {
			updateEntityFromInputComponents();
			verifyInput();
		}

		final AbstractEntity newEntity = (AbstractEntity) evt.getNewValue();
		if (hasEntityChanged()) {
			if (errorState == ErrorState.ERROR) {
				showHasErrorWarning();
				return;
			}
			if (newEntity.getClass().equals(entity.getClass())
					&& newEntity.getId().equals(entity.getId())) {
				return;
			}
			int n = showConfirmation();
			if (n == 2) {
				return;
			}
			if (n == 0) {
				addOrUpdateEntity();
			} else if (n == 1) {
				abandonEntityChanges();
			}
		}

		if (DocumentController.SceneProps.EDIT.check(propName)) {
			entityHandler = new SceneEntityHandler(mainFrame);
		} else if (DocumentController.ChapterProps.EDIT.check(propName)) {
			entityHandler = new ChapterEntityHandler(mainFrame);
		} else if (DocumentController.PartProps.EDIT.check(propName)) {
			entityHandler = new PartEntityHandler(mainFrame);
		} else if (DocumentController.LocationProps.EDIT.check(propName)) {
			entityHandler = new LocationEntityHandler(mainFrame);
		} else if (DocumentController.PersonProps.EDIT.check(propName)) {
			entityHandler = new PersonEntityHandler(mainFrame);
		} else if (DocumentController.GenderProps.EDIT.check(propName)) {
			entityHandler = new GenderEntityHandler(mainFrame);
		} else if (DocumentController.CategoryProps.EDIT.check(propName)) {
			entityHandler = new CategoryEntityHandler(mainFrame);
		} else if (DocumentController.StrandProps.EDIT.check(propName)) {
			entityHandler = new StrandEntityHandler(mainFrame);
		} else if (DocumentController.IdeaProps.EDIT.check(propName)) {
			entityHandler = new IdeaEntityHandler(mainFrame);
		} else if (DocumentController.TagProps.EDIT.check(propName)) {
			entityHandler = new TagEntityHandler(mainFrame);
		} else if (DocumentController.ItemProps.EDIT.check(propName)) {
			entityHandler = new ItemEntityHandler(mainFrame);
		} else if (DocumentController.TagLinkProps.EDIT.check(propName)) {
			entityHandler = new TagLinkEntityHandler(mainFrame);
		} else if (DocumentController.ItemLinkProps.EDIT.check(propName)) {
			entityHandler = new ItemLinkEntityHandler(mainFrame);
		} else if (DocumentController.InternalProps.EDIT.check(propName)) {
			entityHandler = new InternalEntityHandler(mainFrame);
		}

		entity = (AbstractEntity) evt.getNewValue();
		if (entity.isTransient()) {
			// new entity
			entity = entityHandler.newEntity(entity);
		}

		// keep origEntity up-to-date
		origEntity = entityHandler.createNewEntity();
		EntityUtil.copyEntityProperties(mainFrame, entity, origEntity);

		initUi();

		// set entity and DAO on input components
		for (JComponent comp : inputComponents) {
			comp.putClientProperty(ClientPropertyName.ENTITY.toString(), entity);
			comp.putClientProperty(ClientPropertyName.DAO.toString(),
					entityHandler.createDAO());
		}

		if (entity.isTransient()) {
			// new
			btAddOrUpdate.setText(I18N.getMsg("msg.common.add"));
		} else {
			// update
			btAddOrUpdate.setText(I18N.getMsg("msg.editor.update"));
		}

		editEntity(evt);

		if (!entity.isTransient()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					verifyInput();
				}
			});
		}
	}

	private void showHasErrorWarning() {
		mainFrame.showEditor();
		JOptionPane.showMessageDialog(this,
				I18N.getMsg("msg.common.editor.has.error"),
				I18N.getMsg("msg.common.warning"), JOptionPane.WARNING_MESSAGE);
	}

	private int showConfirmation() {
		mainFrame.showEditor();
		final Object[] options = { I18N.getMsg("msg.common.save.changes"),
				I18N.getMsg("msg.common.discard.changes"),
				I18N.getMsg("msg.common.cancel") };
		int n = JOptionPane.showOptionDialog(
				mainFrame,
				I18N.getMsg("msg.common.save.or.discard.changes") + "\n\n"
						+ EntityUtil.getEntityTitle(entity) + ": "
						+ entity.toString() + "\n\n",
				I18N.getMsg("msg.common.save.changes.title"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[2]);
		return n;
	}

	private void editEntity(PropertyChangeEvent evt) {
		for (SbColumn col : entityHandler.getColumns()) {
			try {
				String methodName = "get" + col.getMethodName();
				Method method = entity.getClass().getMethod(methodName);
				final Object ret = method.invoke(entity);
				for (JComponent comp : inputComponents) {
					if (col.getMethodName().equals(comp.getName())) {
						if (comp instanceof JTextComponent) {
							if (ret == null) {
								continue;
							}
							JTextComponent tf = (JTextComponent) comp;
							tf.setText(ret.toString());
							tf.setCaretPosition(0);
						} else if (comp instanceof PlainTextEditor) {
							if (ret == null) {
								continue;
							}
							PlainTextEditor editor = (PlainTextEditor) comp;
							editor.setText(ret.toString());
						} else if (comp instanceof HtmlEditor) {
							if (ret == null) {
								continue;
							}
							HtmlEditor editor = (HtmlEditor) comp;
							editor.setText(ret.toString());
							editor.setCaretPosition(0);
						} else if (comp instanceof JCheckBox) {
							if (ret == null) {
								continue;
							}
							((JCheckBox) comp).setSelected((Boolean)ret);
						} else if (comp instanceof AutoCompleteComboBox) {
							AbstractEntityHandler entityHandler = EntityUtil
									.getEntityHandler(mainFrame, ret, method,
											entity);
							AutoCompleteComboBox autoCombo = (AutoCompleteComboBox) comp;
							EntityUtil.fillAutoCombo(mainFrame, autoCombo,
									entityHandler, (String) ret,
									col.getAutoCompleteDaoMethod());
						} else if (comp instanceof JComboBox) {
							boolean isNew = (ret == null);
							final JComboBox combo = (JComboBox) comp;
							if (col.hasComboModel()) {
								final ComboBoxModel model = col.getComboModel();
								if (model instanceof IRefreshableComboModel) {
									IRefreshableComboModel refModel = (IRefreshableComboModel) model;
									refModel.setMainFrame(mainFrame);
									refModel.refresh();
								}
								combo.setModel(model);
								combo.revalidate();
								if (ret == null && combo.getItemCount() > 0) {
									combo.setSelectedIndex(0);
								} else {
									model.setSelectedItem(ret);
								}
								if (col.hasListCellRenderer()) {
									combo.setRenderer(col.getListCellRenderer());
								}
							} else {
								AbstractEntityHandler entityHandler2 = EntityUtil
										.getEntityHandler(mainFrame, ret,
												method, entity);
								EntityUtil.fillEntityCombo(mainFrame, combo,
										entityHandler2, (AbstractEntity) ret,
										isNew, col.isEmptyComboItem());
							}
						} else if (comp instanceof CheckBoxPanel) {
							CheckBoxPanel cbPanel = (CheckBoxPanel) comp;
							if (col.hasDecorator()) {
								CbPanelDecorator decorator = col.getDecorator();
								decorator.setPanel(cbPanel);
								cbPanel.setDecorator(decorator);
							}
							cbPanel.setEntity(entity);
							AbstractEntityHandler entityHandler2 = EntityUtil
									.getEntityHandler(mainFrame, ret, method,
											entity);
							cbPanel.setEntityHandler(entityHandler2);
							cbPanel.setEntity(entity);
							cbPanel.setEntityList((List)ret);
							cbPanel.setSearch(col.getSearch());
							cbPanel.initAll();
						} else if (comp instanceof DateChooser) {
							DateChooser dateChooser = (DateChooser) comp;
							dateChooser.setDate((Date) ret);
						} else if (comp instanceof CleverColorChooser) {
							if (ret != null) {
								CleverColorChooser colorChooser = (CleverColorChooser) comp;
								colorChooser.setColor((Color) ret);
							}
						} else if (comp instanceof JLabel) {
							JLabel lb = (JLabel) comp;
							lb.setIcon((Icon) ret);
						} else if (comp instanceof AttributesPanel) {
							AttributesPanel attrPanel = (AttributesPanel) comp;
							attrPanel.setAttributes(EntityUtil
									.getEntityAttributes(mainFrame, entity));
							attrPanel.initAll();
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (col.hasRadioButtonGroup()) {
				// enable or disable depending on the selection radio button
				RadioButtonGroup rbGroup = col.getRadioButtonGroup();
				RadioButtonGroupPanel rbgPanel = rbgPanels.get(rbGroup);
				int key = col.getRadioButtonIndex();
				AbstractButton bt = rbgPanel.getButton(key);
				if (rbGroup.hasAttr(entity, key)) {
					bt.setSelected(true);
					rbgPanel.enableSubPanel(key);
				} else {
					bt.setSelected(false);
					rbgPanel.disableSubPanel(key);
				}
			}
		}
	}

	private void updateEntityFromInputComponents() {
		for (SbColumn col : entityHandler.getColumns()) {
			try {
				if (col.isReadOnly()) {
					continue;
				}

				Object objVal = null;
				for (JComponent comp : inputComponents) {
					if (!col.getMethodName().equals(comp.getName())) {
						continue;
					}
					if (comp instanceof JTextComponent) {
						JTextComponent tf = (JTextComponent) comp;
						objVal = tf.getText();
						break;
					}
					if (comp instanceof PlainTextEditor) {
						PlainTextEditor editor = (PlainTextEditor) comp;
						objVal = editor.getText();
						break;
					}
					if (comp instanceof HtmlEditor) {
						HtmlEditor editor = (HtmlEditor) comp;
						objVal = editor.getText();
						break;
					}
					if (comp instanceof JCheckBox) {
						JCheckBox combo = (JCheckBox) comp;
						objVal = combo.isSelected();
						break;
					}
					if (comp instanceof JComboBox) {
						JComboBox combo = (JComboBox) comp;
						objVal = combo.getSelectedItem();
						break;
					}
					if (comp instanceof AutoCompleteComboBox) {
						AutoCompleteComboBox autoCombo = (AutoCompleteComboBox) comp;
						objVal = autoCombo.getJComboBox().getSelectedItem();
						break;
					}
					if (comp instanceof AttributesPanel) {
						AttributesPanel propPanel = (AttributesPanel) comp;
						objVal = propPanel.getAttributes();
						break;
					}
					if (comp instanceof DateChooser) {
						DateChooser dateChooser = (DateChooser) comp;
						objVal = dateChooser.getTimestamp();
						break;
					}
					if (comp instanceof CleverColorChooser) {
						CleverColorChooser colorChooser = (CleverColorChooser) comp;
						if (colorChooser.getColor() != null) {
							objVal = colorChooser.getColor();
						}
						break;
					}
					if (comp instanceof CheckBoxPanel) {
						CheckBoxPanel cbPanel = (CheckBoxPanel) comp;
						objVal = cbPanel.getSelectedEntities();
						break;
					}
					if (comp instanceof JLabel) {
						JLabel lb = (JLabel) comp;
						objVal = lb.getIcon();
						break;
					}
				}

				// get type from getter
				String methodName = "get" + col.getMethodName();
				Method method = entity.getClass().getMethod(methodName);
				Type type = method.getReturnType();

				Object val = null;
				Class<?>[] types = null;
				if (type == Long.class) {
					// scene ID
					val = (Scene) objVal;
					types = new Class[] { Scene.class };
				} else if (type == Integer.class) {
					if (objVal != null) {
						if (objVal.toString().isEmpty()) {
							val = null;
						} else {
							val = Integer.parseInt(objVal.toString());
						}
					}
					types = new Class[] { Integer.class };
				} else if (type == String.class) {
					val = objVal;
					types = new Class[] { String.class };
				} else if (type == Boolean.class) {
					val = objVal;
					types = new Class[] { Boolean.class };
				} else if (type == Person.class) {
					if (objVal instanceof String) {
						if (((String) objVal).length() == 0) {
							val = null;
						}
					} else {
						val = (Person) objVal;
					}
					types = new Class[] { Person.class };
				} else if (type == Location.class) {
					if (objVal instanceof String) {
						if (((String) objVal).length() == 0) {
							val = null;
						}
					} else {
						val = (Location) objVal;
					}
					types = new Class[] { Location.class };
				} else if (type == Scene.class) {
					if (objVal instanceof String) {
						if (((String) objVal).length() == 0) {
							val = null;
						}
					} else {
						val = (Scene) objVal;
					}
					types = new Class[] { Scene.class };
				} else if (type == Chapter.class) {
					if (objVal instanceof String) {
						if (((String) objVal).length() == 0) {
							val = null;
						}
					} else {
						val = (Chapter) objVal;
					}
					types = new Class[] { Chapter.class };
				} else if (type == Part.class) {
					val = (Part) objVal;
					types = new Class[] { Part.class };
				} else if (type == Gender.class) {
					val = (Gender) objVal;
					types = new Class[] { Gender.class };
				} else if (type == Category.class) {
					val = (Category) objVal;
					types = new Class[] { Category.class };
				} else if (type == Strand.class) {
					val = (Strand) objVal;
					types = new Class[] { Strand.class };
				} else if (type == Idea.class) {
					val = (Idea) objVal;
					types = new Class[] { Idea.class };
				} else if (type == Tag.class) {
					val = (Tag) objVal;
					types = new Class[] { Tag.class };
				} else if (type == AbstractTag.class) {
					val = (AbstractTag) objVal;
					types = new Class[] { AbstractTag.class };
				} else if (type == Item.class) {
					val = (Item) objVal;
					types = new Class[] { Item.class };
				} else if (type == TagLink.class) {
					val = (TagLink) objVal;
					types = new Class[] { TagLink.class };
				} else if (type == ItemLink.class) {
					val = (ItemLink) objVal;
					types = new Class[] { ItemLink.class };
				} else if (type == Date.class) {
					val = (Date) objVal;
					types = new Class[] { Date.class };
				} else if (type == Timestamp.class) {
					val = (Timestamp) objVal;
					types = new Class[] { Timestamp.class };
				} else if (type == Color.class) {
					val = (Color) objVal;
					types = new Class[] { Color.class };
				} else if (type == SceneState.class) {
					val = (SceneState) objVal;
					types = new Class[] { SceneState.class };
				} else if (type == IdeaState.class) {
					val = (IdeaState) objVal;
					types = new Class[] { IdeaState.class };
				} else if (type == List.class) {
					val = (List<?>) objVal;
					types = new Class[] { List.class };
				} else if (type == Icon.class) {
					val = (Icon) objVal;
					types = new Class[] { Icon.class };
				}

				// invoke setter
				if (col.getInputType() != InputType.ATTRIBUTES
						&& col.getInputType() != InputType.NONE) {
					methodName = "set" + col.getMethodName();
					method = entity.getClass().getMethod(methodName, types);
					method.invoke(entity, val);
				}
				if (col.getInputType() == InputType.ATTRIBUTES) {
					if (entity instanceof Person) {
						// save attributes for later usage
						attributes = (ArrayList<Attribute>) val;
						// ((Person)entity).setAttributes((ArrayList<Attribute>)
						// val);
					}
				}
			} catch (NumberFormatException ex1) {
				// ignore
			} catch (NullPointerException ex2) {
				// ignore;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// on a radio button panel, remove (set to null) attributes that are not
		// selected
		for (Entry<RadioButtonGroup, RadioButtonGroupPanel> e : rbgPanels
				.entrySet()) {
			RadioButtonGroup rbg = e.getKey();
			RadioButtonGroupPanel panel = e.getValue();
			ArrayList<AbstractButton> buttons = panel.getNotSelectedButtons();
			for (AbstractButton bt : buttons) {
				Integer key = Integer.parseInt(bt.getName());
				rbg.removeAttr(entity, key);
			}
		}
	}

	private void verifyInput() {
		errorState = ErrorState.OK;
		try {
			// remove error labels
			for (Container container : containers) {
				ArrayList<Component> components = new ArrayList<Component>();
				SwingUtil.findComponentsNameStartsWith(container, ERROR_LABEL,
						components);
				for (Component comp : components) {
					Container cont = comp.getParent();
					cont.remove(comp);
				}
			}

			int i = 0;
			for (JComponent comp : inputComponents) {
				AbstractInputVerifier verifier = (AbstractInputVerifier) comp
						.getInputVerifier();
				if (verifier == null) {
					continue;
				}
				if (!comp.isEnabled()) {
					// don't check disabled components
					continue;
				}
				if (!verifier.verify(comp)) {
					Icon icon = IconUtil.StateIcon.WARNING.getIcon();
					if (errorState == ErrorState.OK) {
						errorState = ErrorState.WARNING;
					}
					if (verifier.getErrorState() == ErrorState.ERROR) {
						errorState = ErrorState.ERROR;
						icon = IconUtil.StateIcon.ERROR.getIcon();
					}
					JLabel lb = new JLabel(icon);
					lb.setToolTipText(verifier.getErrorText());
					lb.setName(ERROR_LABEL + "_" + i);
					String cn = comp.getName();
					Container container = comp.getParent();
					int x = comp.getWidth() - 18;
					int y = 1;
					if (comp instanceof DateChooser) {
						x = comp.getWidth() - 203;
					}
					if (comp instanceof JComboBox) {
						x = comp.getWidth() - 40;
						y = 3;
					}
					if (comp instanceof HtmlEditor) {
						x = comp.getWidth() - 44;
						y = comp.getHeight() - 70;
					}
					if (comp instanceof PlainTextEditor) {
						x = comp.getWidth() - 44;
						y = comp.getHeight() - 70;
					}
					if (x < 0) {
						x = 0;
					}
					if (y < 0) {
						y = 0;
					}
					container.add(lb, "pos (" + cn + ".x+" + x + ") (" + cn
							+ ".y+" + y + ")");
					int zo = container.getComponentZOrder(comp);
					container.setComponentZOrder(lb, zo > 0 ? zo - 1 : 0);

					++i;
				}
			}
			if (errorState != ErrorState.OK) {
				if (errorState == ErrorState.ERROR) {
					setMsgState(MsgState.ERRORS);
				} else {
					setMsgState(MsgState.WARNINGS);
				}
			}

			for (JPanel container : containers) {
				SwingUtil.forceRevalidate(container);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	private void addOrUpdateEntity() {
		try {
			updateEntityFromInputComponents();
			if (entity.isTransient()) {
				// new entity
				verifyInput();
				if (errorState == ErrorState.ERROR) {
					return;
				}
				ctrl.newEntity(entity);
				if (errorState == ErrorState.OK) {
					setMsgState(MsgState.ADDED);
				}
				titlePanel.refresh(entity);
				btAddOrUpdate.setText(I18N.getMsg("msg.editor.update"));
			} else {
				// update entity
				verifyInput();
				if (errorState == ErrorState.ERROR) {
					return;
				}
				ctrl.updateEntity(entity);
				if (errorState == ErrorState.OK) {
					setMsgState(MsgState.UPDATED);
				}
			}

			// save attributes
			if (entity instanceof Person) {
				EntityUtil.setEntityAttributes(mainFrame, entity, attributes);
			}

			// keep origEntity up-to-date
			origEntity = entityHandler.createNewEntity();
			EntityUtil.copyEntityProperties(mainFrame, entity, origEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void abandonEntityChanges() {
		EntityUtil.abandonEntityChanges(mainFrame, entity);
		unloadEntity();
	}

	private void unloadEntity() {
		entity = null;
		entityHandler = null;
		containers.clear();
		inputComponents.clear();
		rbgPanels.clear();
	}

	public AbstractEntity getEntity() {
		return entity;
	}

	public boolean isEntityLoaded() {
		return entity != null;
	}

	public boolean hasEntityChanged() {
		return (entity != null && (!entity.equals(origEntity)));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String compName = ((Component) e.getSource()).getName();
		if (ComponentName.BT_OK.check(compName)) {
			addOrUpdateEntity();
			if (errorState == ErrorState.ERROR) {
				return;
			}
			unloadEntity();
			refresh();
			if (!leaveOpen) {
				mainFrame.hideEditor();
			}
		} else if (ComponentName.BT_ADD_OR_UPDATE.check(compName)) {
			addOrUpdateEntity();
			if (errorState == ErrorState.ERROR) {
				return;
			}
		} else if (ComponentName.BT_CANCEL.check(compName)) {
			abandonEntityChanges();
			refresh();
			if (!leaveOpen) {
				mainFrame.hideEditor();
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox cb = (JCheckBox) e.getSource();
		leaveOpen = cb.isSelected();
		DocumentUtil.storeInternal(mainFrame, InternalKey.LEAVE_EDITOR_OPEN,
				leaveOpen);
	}
}
