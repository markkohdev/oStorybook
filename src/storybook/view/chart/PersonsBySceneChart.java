/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.chart;

import storybook.model.DocumentModel;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.toolkit.I18N;
import storybook.toolkit.html.HtmlUtil;
import storybook.toolkit.swing.ColorUtil;
import storybook.toolkit.swing.FontUtil;
import storybook.toolkit.swing.ReadOnlyTable;
import storybook.toolkit.swing.SwingUtil;
import storybook.toolkit.swing.table.ColorTableCellRenderer;
import storybook.toolkit.swing.table.FixedColumnScrollPane;
import storybook.toolkit.swing.table.HeaderTableCellRenderer;
import storybook.toolkit.swing.table.ToolTipHeader;
import storybook.view.MainFrame;
import storybook.view.chart.legend.StrandsLegendPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.hibernate.Session;

public class PersonsBySceneChart extends AbstractPersonsChart
	implements ChangeListener {

	private JTable table;
	private JSlider colSlider;
	private JCheckBox cbShowUnusedPersons;
	private int colWidth = 50;

	public PersonsBySceneChart(MainFrame paramMainFrame) {
		super(paramMainFrame, "msg.report.person.scene.title");
		this.partRelated = true;
	}

	protected void initChartUi() {
		JLabel localJLabel = new JLabel(this.chartTitle);
		localJLabel.setFont(FontUtil.getBoldFont());
		this.table = createTable();
		FixedColumnScrollPane localFixedColumnScrollPane = new FixedColumnScrollPane(this.table, 1);
		localFixedColumnScrollPane.getRowHeader().setPreferredSize(new Dimension(200, 20));
		this.panel.add(localJLabel, "center");
		this.panel.add(localFixedColumnScrollPane, "grow, h pref-20");
		this.panel.add(new StrandsLegendPanel(this.mainFrame), "gap push");
	}

	protected void initOptionsUi() {
		super.initOptionsUi();
		this.cbShowUnusedPersons = new JCheckBox();
		this.cbShowUnusedPersons.setSelected(true);
		this.cbShowUnusedPersons.setText(I18N.getMsg("msg.chart.common.unused.characters"));
		this.cbShowUnusedPersons.setOpaque(false);
		this.cbShowUnusedPersons.addActionListener(this);
		JLabel localJLabel = new JLabel(I18N.getIcon("icon.small.size"));
		this.colSlider = SwingUtil.createSafeSlider(0, 5, 200, this.colWidth);
		this.colSlider.setMinorTickSpacing(1);
		this.colSlider.setMajorTickSpacing(2);
		this.colSlider.setSnapToTicks(false);
		this.colSlider.addChangeListener(this);
		this.colSlider.setOpaque(false);
		this.optionsPanel.add(this.cbShowUnusedPersons, "right,gap push");
		this.optionsPanel.add(localJLabel, "gap 20");
		this.optionsPanel.add(this.colSlider);
	}

	public void refresh() {
		this.colWidth = this.colSlider.getValue();
		super.refresh();
		this.colSlider.setValue(this.colWidth);
		setTableColumnWidth();
	}

	private JTable createTable() {
		Part localPart = this.mainFrame.getCurrentPart();
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		PersonDAOImpl localPersonDAOImpl = new PersonDAOImpl(localSession);
		List localList1 = localPersonDAOImpl.findByCategories(this.selectedCategories);
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		List localList2 = localSceneDAOImpl.findByPart(localPart);
		localDocumentModel.commit();
		String[] arrayOfString = new String[localList2.size() + 1];
		arrayOfString[0] = "";
		int i = 1;
		/* Obfuscator ?
		Object localObject1 = localList2.iterator();
		while (((Iterator) localObject1).hasNext()) {
		Scene localObject2 = (Scene) ((Iterator) localObject1).next();
		arrayOfString[i] = ((Scene) localObject2).getChapterSceneNo(false);
		i++;
		}*/
		ArrayList localObject1 = new ArrayList();
		String[] localObject2 = new String[localList2.size() + 1];
		Object localObject3 = localList1.iterator();
		Object localObject6;
		while (((Iterator) localObject3).hasNext()) {
			Person localObject4 = (Person) ((Iterator) localObject3).next();
			int j = 0;
			Object[] arrayOfObject2 = new Object[localList2.size() + 1];
			arrayOfObject2[(j++)] = localObject4;
			int n = 0;
			localObject6 = localList2.iterator();
			while (((Iterator) localObject6).hasNext()) {
				Scene localScene = (Scene) ((Iterator) localObject6).next();
				if (localScene.getPersons().contains(localObject4)) {
					n = 1;
					arrayOfObject2[j] = ColorUtil.darker(localScene.getStrand().getJColor(), 0.05D);
				} else {
					arrayOfObject2[j] = null;
				}
				localObject2[j] = (String)HtmlUtil.wrapIntoTable(localScene.getTitleText(true, 500));
				j++;
			}
			if ((this.cbShowUnusedPersons == null) || (this.cbShowUnusedPersons.isSelected()) || (n != 0)) {
				((ArrayList) localObject1).add(arrayOfObject2);
			}
		}
		localObject3 = new Object[((ArrayList) localObject1).size()][];
		i = 0;
		/* Obfuscator ?
		 Object localObject4 = ((ArrayList)localObject1).iterator();
		 while (((Iterator)localObject4).hasNext())
		 {
		 Object[] arrayOfObject1 = (Object[])((Iterator)localObject4).next();
		 localObject3[(i++)] = arrayOfObject1;
		 }*/
		ReadOnlyTable localObject4 = new ReadOnlyTable((Object[][]) localObject3, arrayOfString);
		if (((JTable) localObject4).getModel().getRowCount() == 0) {
			return localObject4;
		}
		((JTable) localObject4).getColumnModel().getColumn(0).setPreferredWidth(200);
		((JTable) localObject4).getColumnModel().getColumn(0).setCellRenderer(new HeaderTableCellRenderer());
		for (int k = 1; k < ((JTable) localObject4).getColumnCount(); k++) {
			int m = ((JTable) localObject4).getColumnModel().getColumn(k).getModelIndex();
			Object localObject5 = ((JTable) localObject4).getModel().getValueAt(0, m);
			localObject6 = ((JTable) localObject4).getColumnModel().getColumn(k);
			if ((localObject5 == null) || ((localObject5 instanceof Color))) {
				((TableColumn) localObject6).setPreferredWidth(this.colWidth);
				((TableColumn) localObject6).setCellRenderer(new ColorTableCellRenderer(false));
			}
		}
		((JTable) localObject4).setAutoResizeMode(0);
		((JTable) localObject4).getTableHeader().setReorderingAllowed(false);
		ToolTipHeader localToolTipHeader = new ToolTipHeader(((JTable) localObject4).getColumnModel());
		localToolTipHeader.setToolTipStrings((String[]) localObject2);
		localToolTipHeader.setToolTipText("Default ToolTip TEXT");
		((JTable) localObject4).setTableHeader(localToolTipHeader);
		return localObject4;
	}

	@Override
	public void stateChanged(ChangeEvent paramChangeEvent) {
		setTableColumnWidth();
	}

	private void setTableColumnWidth() {
		this.colWidth = this.colSlider.getValue();
		for (int i = 0; i < this.table.getColumnCount(); i++) {
			TableColumn localTableColumn = this.table.getColumnModel().getColumn(i);
			localTableColumn.setPreferredWidth(this.colWidth);
		}
	}
}