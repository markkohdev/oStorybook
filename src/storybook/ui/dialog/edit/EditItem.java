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
package storybook.ui.dialog.edit;

import java.awt.CardLayout;
import storybook.model.hbn.entity.Item;
import storybook.toolkit.swing.htmleditor.HtmlEditor;
import static storybook.ui.dialog.edit.CommonBox.loadCbCategories;
import static storybook.ui.dialog.edit.DlgErrorMessage.mandatoryField;

/**
 *
 * @author favdb
 */
public class EditItem extends javax.swing.JPanel {

	Editor parent;
	Item item;
	private final CardLayout cardDescription = new CardLayout(0, 0);
	private final CardLayout cardNotes = new CardLayout(0, 0);
	private final HtmlEditor description = new HtmlEditor();
	private final HtmlEditor notes = new HtmlEditor();

	/**
	 * Creates new form EditItem
	 */
	public EditItem() {
		initComponents();
	}

	EditItem(Editor m, Item i) {
		initComponents();
		parent = m;
		item = i;
		initUI();
	}

	private void initUI() {
		paneDescription.setLayout(cardDescription);
		paneDescription.add(description, "description");
		cardDescription.show(paneDescription, "description");
		paneNotes.setLayout(cardNotes);
		paneNotes.add(notes);
		cardNotes.show(paneNotes, "notes");
		if (item == null)
			item = createNewItem();
		txtId.setText(Long.toString(item.getId()));
		txName.setText(item.getName());
		loadCbCategories(parent.mainFrame, cbCategory, item);
		description.setText(item.getDescription());
		notes.setText(item.getNotes());
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        paneCommon = new javax.swing.JPanel();
        lbTitle = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        lbId = new javax.swing.JLabel();
        txName = new javax.swing.JTextField();
        btSuppressCategory = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cbCategory = new javax.swing.JComboBox();
        paneDescription = new javax.swing.JPanel();
        paneNotes = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("storybook/msg/messages"); // NOI18N
        lbTitle.setText(bundle.getString("msg.common.name")); // NOI18N

        txtId.setEditable(false);

        lbId.setText(bundle.getString("msg.common.id")); // NOI18N

        btSuppressCategory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/storybook/resources/icons/16x16/clear.png"))); // NOI18N
        btSuppressCategory.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setText(bundle.getString("msg.common.category")); // NOI18N

        paneDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("msg.common.description"))); // NOI18N

        javax.swing.GroupLayout paneDescriptionLayout = new javax.swing.GroupLayout(paneDescription);
        paneDescription.setLayout(paneDescriptionLayout);
        paneDescriptionLayout.setHorizontalGroup(
            paneDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        paneDescriptionLayout.setVerticalGroup(
            paneDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout paneCommonLayout = new javax.swing.GroupLayout(paneCommon);
        paneCommon.setLayout(paneCommonLayout);
        paneCommonLayout.setHorizontalGroup(
            paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paneCommonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paneCommonLayout.createSequentialGroup()
                        .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbTitle)
                            .addComponent(lbId))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(paneCommonLayout.createSequentialGroup()
                                .addComponent(txtId, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                .addGap(269, 269, 269))
                            .addComponent(txName))
                        .addGap(14, 14, 14))
                    .addGroup(paneCommonLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSuppressCategory)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addComponent(paneDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        paneCommonLayout.setVerticalGroup(
            paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paneCommonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbTitle)
                    .addComponent(txName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paneCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btSuppressCategory))
                    .addGroup(paneCommonLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paneDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab(bundle.getString("msg.common"), paneCommon); // NOI18N

        javax.swing.GroupLayout paneNotesLayout = new javax.swing.GroupLayout(paneNotes);
        paneNotes.setLayout(paneNotesLayout);
        paneNotesLayout.setHorizontalGroup(
            paneNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 392, Short.MAX_VALUE)
        );
        paneNotesLayout.setVerticalGroup(
            paneNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("msg.common.notes"), paneNotes); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btSuppressCategory;
    private javax.swing.JComboBox cbCategory;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbId;
    private javax.swing.JLabel lbTitle;
    private javax.swing.JPanel paneCommon;
    private javax.swing.JPanel paneDescription;
    private javax.swing.JPanel paneNotes;
    private javax.swing.JTextField txName;
    private javax.swing.JTextField txtId;
    // End of variables declaration//GEN-END:variables

	private Item createNewItem() {
		Item i = new Item();
		i.setId(-1L);
		i.setName("");
		i.setDescription("");
		i.setNotes("");
		i.setCategory("");
		return (i);
	}

	public boolean isModified() {
		if (!item.getName().equals(txName.getText()))
			return (true);
		if (!item.getDescription().equals(description.getText()))
			return (true);
		if (!item.getNotes().equals(notes.getText()))
			return (true);
		if (!item.getCategory().equals(cbCategory.getSelectedItem().toString()))
			return (true);
		return (false);
	}

	public String saveData() {
		String rt = ctrlData();
		if ("".equals(rt)) {
			item.setName(txName.getText());
			item.setDescription(description.getText());
			item.setNotes(notes.getText());
			item.setCategory(cbCategory.getSelectedItem().toString());
		}
		return (rt);
	}

	private String ctrlData() {
		if (!"".equals(txName.getText()))
			return (mandatoryField("msg.common.name"));
		if (!"".equals(description.getText()))
			return (mandatoryField("msg.common.description"));
		return ("");
	}
}
