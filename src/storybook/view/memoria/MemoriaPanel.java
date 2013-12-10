package storybook.view.memoria;

import storybook.SbConstants.ComponentName;
import storybook.SbConstants.IconSize;
import storybook.SbConstants.InternalKey;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController.CommonProps;
import storybook.controller.DocumentController.MemoriaViewProps;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.ItemDAOImpl;
import storybook.model.hbn.dao.ItemLinkDAOImpl;
import storybook.model.hbn.dao.LocationDAOImpl;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.dao.TagDAOImpl;
import storybook.model.hbn.dao.TagLinkDAOImpl;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.AbstractTag;
import storybook.model.hbn.entity.Attribute;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Item;
import storybook.model.hbn.entity.ItemLink;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.model.hbn.entity.Tag;
import storybook.model.hbn.entity.TagLink;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.IOUtil;
import storybook.toolkit.Period;
import storybook.toolkit.filefilter.PngFileFilter;
import storybook.toolkit.swing.IconButton;
import storybook.toolkit.swing.ScreenImage;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.AbstractPanel;
import storybook.view.MainFrame;
import storybook.view.combo.EntityTypeListCellRenderer;
import storybook.view.interfaces.IRefreshable;
import storybook.view.options.MemoriaOptionsDialog;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.Line;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections15.Transformer;
import org.hibernate.Session;
import org.jdesktop.swingx.icon.EmptyIcon;
import storybook.SbConstants;
import storybook.controller.DocumentController;

public class MemoriaPanel extends AbstractPanel
	implements ActionListener, IRefreshable {

	private DelegateForest<AbstractEntity, Long> graph;
	private VisualizationViewer<AbstractEntity, Long> vv;
	private TreeLayout<AbstractEntity, Long> treeLayout;
	private BalloonLayout<AbstractEntity, Long> balloonLayout;
	private GraphZoomScrollPane graphPanel;
	private Map<AbstractEntity, String> labelMap;
	private Map<AbstractEntity, Icon> iconMap;
	private String entitySourceName;
	private long entityId;
	private AbstractEntity shownEntity;
	private long graphIndex;
	private ScalingControl scaler;
	private Scene sceneVertex;
	private String sceneVertexTitle;
	private Person characterVertex;
	private Location locationVertex;
	private String locationVertexTitle;
	private Tag tagVertex;
	private boolean showTagVertex = true;
	private Tag involvedTagVertex;
	private List<Long> sceneIds;
	private Set<AbstractTag> involvedTags;
	boolean showBalloonLayout = true;
	private JPanel controlPanel;
	private JPanel datePanel;
	private JComboBox entityTypeCombo;
	private JComboBox entityCombo;
	private Date chosenDate;
	private JComboBox dateCombo;
	private JCheckBox cbAutoRefresh;
	private Icon womanIconMedium = I18N.getIcon("icon.medium.woman");
	private Icon womanIconLarge = I18N.getIcon("icon.large.woman");
	private Icon manIconMedium = I18N.getIcon("icon.medium.man");
	private Icon manIconLarge = I18N.getIcon("icon.large.man");
	private Icon personIconMedium = I18N.getIcon("icon.medium.person");
	private Icon personIconLarge = I18N.getIcon("icon.large.person");
	private Icon locationIconMedium = I18N.getIcon("icon.medium.location");
	private Icon locationIconLarge = I18N.getIcon("icon.large.location");
	private Icon sceneIconMedium = I18N.getIcon("icon.medium.scene");
	private Icon sceneIconLarge = I18N.getIcon("icon.large.scene");
	private Icon itemIconMedium = I18N.getIcon("icon.medium.item");
	private Icon itemIconLarge = I18N.getIcon("icon.large.item");
	private Icon tagIconMedium = I18N.getIcon("icon.medium.tag");
	private Icon tagIconLarge = I18N.getIcon("icon.large.tag");
	private Icon emptyIcon = new EmptyIcon();
	private boolean processActionListener = true;

	public MemoriaPanel(MainFrame paramMainFrame) {
		super(paramMainFrame);
	}

	public void modelPropertyChange(PropertyChangeEvent paramPropertyChangeEvent) {
		try {
			Object localObject1 = paramPropertyChangeEvent.getNewValue();
			String str = paramPropertyChangeEvent.getPropertyName();
			if (str == null) {
				return;
			}
			Object localObject2;
			Object localObject3;
			if (DocumentController.CommonProps.REFRESH.check(str)) {
				localObject2 = (View) localObject1;
				localObject3 = (View) getParent().getParent();
				if (localObject3 == localObject2) {
					refresh();
				}
				return;
			}
			if (DocumentController.CommonProps.SHOW_IN_MEMORIA.check(str)) {
				localObject2 = (AbstractEntity) localObject1;
				refresh((AbstractEntity) localObject2);
				return;
			}
			if (DocumentController.CommonProps.SHOW_OPTIONS.check(str)) {
				localObject2 = (View) paramPropertyChangeEvent.getNewValue();
				if (!((View) localObject2).getName().equals(SbConstants.ViewName.MEMORIA.toString())) {
					return;
				}
				localObject3 = new MemoriaOptionsDialog(this.mainFrame);
				SwingUtil.showModalDialog((JDialog) localObject3, this);
				return;
			}
			if (DocumentController.MemoriaViewProps.BALLOON.check(str)) {
				this.showBalloonLayout = ((Boolean) localObject1).booleanValue();
				makeLayoutTransition();
				return;
			}
			if ((str.startsWith("Update")) || (str.startsWith("Delete")) || (str.startsWith("New"))) {
				refresh();
				return;
			}
			if (DocumentController.CommonProps.EXPORT.check(str)) {
				localObject2 = (View) localObject1;
				localObject3 = (View) getParent().getParent();
				if (localObject3 == localObject2) {
					export();
				}
				return;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void init() {
		try {
			this.chosenDate = new Date(0L);
			this.entitySourceName = "";
			this.sceneIds = new ArrayList();
			this.involvedTags = new HashSet();
			this.scaler = new CrossoverScalingControl();
			try {
				Internal localInternal = DocumentUtil.restoreInternal(this.mainFrame, SbConstants.InternalKey.MEMORIA_BALLOON, Boolean.valueOf(true));
				this.showBalloonLayout = localInternal.getBooleanValue().booleanValue();
			} catch (Exception localException1) {
				this.showBalloonLayout = true;
			}
		} catch (Exception localException2) {
			localException2.printStackTrace();
		}
	}

	public void initUi() {
		try {
			MigLayout localMigLayout1 = new MigLayout("wrap,fill", "[]", "[][grow]");
			setLayout(localMigLayout1);
			setBackground(SwingUtil.getBackgroundColor());
			this.controlPanel = new JPanel();
			MigLayout localMigLayout2 = new MigLayout("flowx", "", "");
			this.controlPanel.setLayout(localMigLayout2);
			this.controlPanel.setOpaque(false);
			refreshControlPanel();
			initGraph();
			add(this.controlPanel, "alignx center");
			add(this.graphPanel, "grow");
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void refreshEntityCombo(EntityTypeCbItem.Type paramType) {
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		Object localObject;
		List localList;
		if (paramType == EntityTypeCbItem.Type.SCENE) {
			localObject = new SceneDAOImpl(localSession);
			localList = ((SceneDAOImpl) localObject).findAll();
			refreshCombo(new Scene(), localList, false);
			this.datePanel.setVisible(false);
		} else if (paramType == EntityTypeCbItem.Type.PERSON) {
			localObject = new PersonDAOImpl(localSession);
			localList = ((PersonDAOImpl) localObject).findAll();
			Person localPerson = new Person();
			ArrayList localArrayList = new ArrayList();
			localArrayList.add(new Attribute("fd", "fds"));
			localPerson.setAttributes(localArrayList);
			refreshCombo(localPerson, localList, false);
			this.datePanel.setVisible(true);
		} else if (paramType == EntityTypeCbItem.Type.LOCATION) {
			localObject = new LocationDAOImpl(localSession);
			localList = ((LocationDAOImpl) localObject).findAll();
			refreshCombo(new Location(), localList, false);
			this.datePanel.setVisible(true);
		} else if (paramType == EntityTypeCbItem.Type.TAG) {
			localObject = new TagDAOImpl(localSession);
			localList = ((TagDAOImpl) localObject).findAll();
			refreshCombo(new Tag(), localList, false);
			this.datePanel.setVisible(true);
		} else if (paramType == EntityTypeCbItem.Type.ITEM) {
			localObject = new ItemDAOImpl(localSession);
			localList = ((ItemDAOImpl) localObject).findAll();
			refreshCombo(new Item(), localList, false);
			this.datePanel.setVisible(true);
		}
		localDocumentModel.commit();
	}

	private void refreshControlPanel() {
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		List localList1 = localSceneDAOImpl.findAll();
		List localList2 = localSceneDAOImpl.findDistinctDates();
		localList2.removeAll(Collections.singletonList(null));
		localDocumentModel.commit();
		Object localObject1 = null;
		if (this.entityTypeCombo != null) {
			localObject1 = this.entityTypeCombo.getSelectedItem();
		}
		Object localObject2 = null;
		if (this.entityCombo != null) {
			localObject2 = this.entityCombo.getSelectedItem();
		}
		Object localObject3 = null;
		if (this.dateCombo != null) {
			localObject3 = this.dateCombo.getSelectedItem();
		}
		this.dateCombo = new JComboBox();
		this.dateCombo.setPreferredSize(new Dimension(100, 20));
		Object localObject4 = localList2.iterator();
		while (((Iterator) localObject4).hasNext()) {
			this.dateCombo.addItem((Date) ((Iterator) localObject4).next());
		}
		this.dateCombo.setName(SbConstants.ComponentName.COMBO_DATES.toString());
		this.dateCombo.setMaximumRowCount(15);
		if (localObject3 != null) {
			this.dateCombo.setSelectedItem(localObject3);
		}
		this.datePanel = new JPanel(new MigLayout("flowx,ins 0"));
		this.datePanel.setOpaque(false);
		this.datePanel.setVisible(false);
		localObject4 = new IconButton("icon.small.first");
		((IconButton) localObject4).setSize32x20();
		((IconButton) localObject4).setName(SbConstants.ComponentName.BT_FIRST.toString());
		((IconButton) localObject4).addActionListener(this);
		Object localObject5 = new IconButton("icon.small.next");
		((IconButton) localObject5).setSize32x20();
		((IconButton) localObject5).setName(SbConstants.ComponentName.BT_NEXT.toString());
		((IconButton) localObject5).addActionListener(this);
		IconButton localIconButton1 = new IconButton("icon.small.previous");
		localIconButton1.setSize32x20();
		localIconButton1.setName(SbConstants.ComponentName.BT_PREVIOUS.toString());
		localIconButton1.addActionListener(this);
		IconButton localIconButton2 = new IconButton("icon.small.last");
		localIconButton2.setSize32x20();
		localIconButton2.setName(SbConstants.ComponentName.BT_LAST.toString());
		localIconButton2.addActionListener(this);
		this.datePanel.add(this.dateCombo);
		this.datePanel.add((Component) localObject4);
		this.datePanel.add(localIconButton1);
		this.datePanel.add((Component) localObject5);
		this.datePanel.add(localIconButton2);
		this.entityTypeCombo = new JComboBox();
		this.entityTypeCombo.setPreferredSize(new Dimension(120, 20));
		this.entityTypeCombo.setName(SbConstants.ComponentName.COMBO_ENTITY_TYPES.toString());
		this.entityTypeCombo.setRenderer(new EntityTypeListCellRenderer());
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.SCENE));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.PERSON));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.LOCATION));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.TAG));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.ITEM));
		if (localObject1 != null) {
			this.entityTypeCombo.setSelectedItem(localObject1);
		}
		this.entityCombo = new JComboBox();
		this.entityCombo.setName(SbConstants.ComponentName.COMBO_ENTITIES.toString());
		this.entityCombo.setMaximumRowCount(15);
		if (localObject2 != null) {
			EntityTypeCbItem localEntityTypeCbItem = (EntityTypeCbItem) localObject1;
			refreshEntityCombo(localEntityTypeCbItem.getType());
			this.entityCombo.setSelectedItem(localObject2);
		} else {
			refreshCombo(new Scene(), localList1, false);
		}
		this.controlPanel.removeAll();
		this.controlPanel.add(this.entityTypeCombo);
		this.controlPanel.add(this.entityCombo, "gapafter 32");
		this.controlPanel.add(this.datePanel);
		this.controlPanel.revalidate();
		this.controlPanel.repaint();
		this.entityTypeCombo.addActionListener(this);
		this.entityCombo.addActionListener(this);
		this.dateCombo.addActionListener(this);
	}

	private JPanel getThis() {
		return this;
	}

	private void makeLayoutTransition() {
		if (this.vv == null) {
			return;
		}
		LayoutTransition localLayoutTransition;
		if (this.showBalloonLayout) {
			localLayoutTransition = new LayoutTransition(this.vv, this.treeLayout, this.balloonLayout);
		} else {
			localLayoutTransition = new LayoutTransition(this.vv, this.balloonLayout, this.treeLayout);
		}
		Animator localAnimator = new Animator(localLayoutTransition);
		localAnimator.start();
		this.vv.repaint();
	}

	private void clearGraph() {
		try {
			if (this.graph == null) {
				this.graph = new DelegateForest();
				return;
			}
			Collection localCollection = this.graph.getRoots();
			Iterator localIterator = localCollection.iterator();
			while (localIterator.hasNext()) {
				AbstractEntity localAbstractEntity = (AbstractEntity) localIterator.next();
				if (localAbstractEntity != null) {
					this.graph.removeVertex(localAbstractEntity);
				}
			}
		} catch (Exception localException) {
			this.graph = new DelegateForest();
		}
	}

	public void zoomIn() {
		this.scaler.scale(this.vv, 1.1F, this.vv.getCenter());
	}

	public void zoomOut() {
		this.scaler.scale(this.vv, 0.9090909F, this.vv.getCenter());
	}

	public void export() {
		try {
			if (this.shownEntity == null) {
				return;
			}
			Internal localInternal = DocumentUtil.restoreInternal(this.mainFrame, SbConstants.InternalKey.EXPORT_DIRECTORY, EnvUtil.getDefaultExportDir(this.mainFrame));
			File localFile1 = new File(localInternal.getStringValue());
			JFileChooser localJFileChooser = new JFileChooser(localFile1);
			localJFileChooser.setFileFilter(new PngFileFilter());
			localJFileChooser.setApproveButtonText(I18N.getMsg("msg.common.export"));
			String str = IOUtil.getEntityFileNameForExport(this.mainFrame, "Memoria", this.shownEntity);
			localJFileChooser.setSelectedFile(new File(str));
			int i = localJFileChooser.showDialog(getThis(), I18N.getMsg("msg.common.export"));
			if (i == 1) {
				return;
			}
			File localFile2 = localJFileChooser.getSelectedFile();
			if (!localFile2.getName().endsWith(".png")) {
				localFile2 = new File(localFile2.getPath() + ".png");
			}
			ScreenImage.createImage(this.graphPanel, localFile2.toString());
			JOptionPane.showMessageDialog(getThis(), I18N.getMsg("msg.common.export.success") + "\n" + localFile2.getAbsolutePath(), I18N.getMsg("msg.common.export"), 1);
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}

	private void refreshCombo(AbstractEntity paramAbstractEntity, List<? extends AbstractEntity> paramList, boolean paramBoolean) {
		try {
			this.processActionListener = false;
			DefaultComboBoxModel localDefaultComboBoxModel = (DefaultComboBoxModel) this.entityCombo.getModel();
			localDefaultComboBoxModel.removeAllElements();
			localDefaultComboBoxModel.addElement(paramAbstractEntity);
			Iterator localIterator = paramList.iterator();
			while (localIterator.hasNext()) {
				AbstractEntity localAbstractEntity = (AbstractEntity) localIterator.next();
				localDefaultComboBoxModel.addElement(localAbstractEntity);
			}
			this.processActionListener = true;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void initGraph() {
		try {
			this.labelMap = new HashMap();
			this.iconMap = new HashMap();
			this.graph = new DelegateForest();
			this.treeLayout = new TreeLayout(this.graph);
			this.balloonLayout = new BalloonLayout(this.graph);
			this.vv = new VisualizationViewer(this.balloonLayout);
			this.vv.setSize(new Dimension(800, 800));
			refreshGraph();
			this.vv.setBackground(Color.white);
			this.vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
			this.vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
			this.vv.setVertexToolTipTransformer(new EntityTransformer());
			this.graphPanel = new GraphZoomScrollPane(this.vv);
			DefaultModalGraphMouse localDefaultModalGraphMouse = new DefaultModalGraphMouse();
			this.vv.setGraphMouse(localDefaultModalGraphMouse);
			localDefaultModalGraphMouse.add(new MemoriaGraphMouse(this));
			/* TODO MemoriaPanel compile error
			VertexStringerImpl localVertexStringerImpl = new VertexStringerImpl(this.labelMap);
			this.vv.getRenderContext().setVertexLabelTransformer(new VertexStringerImpl(localVertexStringerImpl));
			*/
			VertexIconShapeTransformer localVertexIconShapeTransformer = new VertexIconShapeTransformer(new EllipseVertexShapeTransformer());
			DefaultVertexIconTransformer localDefaultVertexIconTransformer = new DefaultVertexIconTransformer();
			localVertexIconShapeTransformer.setIconMap(this.iconMap);
			localDefaultVertexIconTransformer.setIconMap(this.iconMap);
			this.vv.getRenderContext().setVertexShapeTransformer(localVertexIconShapeTransformer);
			this.vv.getRenderContext().setVertexIconTransformer(localDefaultVertexIconTransformer);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void refreshGraph() {
		refreshGraph(null);
	}

	private void refreshGraph(AbstractEntity paramAbstractEntity) {
		try {
			clearGraph();
			if (paramAbstractEntity == null) {
				paramAbstractEntity = (AbstractEntity) this.entityCombo.getItemAt(0);
			}
			if ((!(paramAbstractEntity instanceof Scene)) && (this.chosenDate == null)) {
				return;
			}
			if ((paramAbstractEntity instanceof Scene)) {
				createSceneGraph();
			} else if ((paramAbstractEntity instanceof Person)) {
				createPersonGraph();
			} else if ((paramAbstractEntity instanceof Location)) {
				createLocationGraph();
			} else if ((paramAbstractEntity instanceof Tag)) {
				createTagGraph();
			} else if ((paramAbstractEntity instanceof Item)) {
				createItemGraph();
			}
			this.shownEntity = paramAbstractEntity;
			this.treeLayout = new TreeLayout(this.graph);
			this.balloonLayout = new BalloonLayout(this.graph);
			Dimension localDimension = this.mainFrame.getSize();
			this.balloonLayout.setSize(new Dimension(localDimension.width / 2, localDimension.height / 2));
			this.balloonLayout.setGraph(this.graph);
			if (this.showBalloonLayout) {
				this.vv.setGraphLayout(this.balloonLayout);
			} else {
				this.vv.setGraphLayout(this.treeLayout);
			}
			this.vv.repaint();
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private boolean isNothingSelected() {
		return this.entityId <= -1L;
	}

	private void showMessage(String paramString) {
		Graphics2D localGraphics2D = (Graphics2D) this.vv.getGraphics();
		if (localGraphics2D == null) {
			return;
		}
		Rectangle localRectangle = this.vv.getBounds();
		int i = (int) localRectangle.getCenterX();
		int j = (int) localRectangle.getCenterY();
		localGraphics2D.setColor(Color.lightGray);
		localGraphics2D.fillRect(i - 200, j - 20, 400, 40);
		localGraphics2D.setColor(Color.black);
		localGraphics2D.drawString(paramString, i - 180, j + 5);
	}

	private void scaleToLayout(ScalingControl paramScalingControl) {
		Dimension localDimension1 = this.vv.getPreferredSize();
		if (this.vv.isShowing()) {
			localDimension1 = this.vv.getSize();
		}
		Dimension localDimension2 = this.vv.getGraphLayout().getSize();
		if (!localDimension1.equals(localDimension2)) {
			paramScalingControl.scale(this.vv, (float) (localDimension1.getWidth() / localDimension2.getWidth()), new Point2D.Double());
		}
	}

	private void createSceneGraph() {
		this.graphIndex = 0L;
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		Scene localScene = (Scene) localSceneDAOImpl.find(Long.valueOf(this.entityId));
		if (localScene == null) {
			localDocumentModel.commit();
			return;
		}
		this.graph.addVertex(localScene);
		this.labelMap.put(localScene, localScene.toString());
		this.iconMap.put(localScene, this.sceneIconLarge);
		this.sceneVertexTitle = I18N.getMsg("msg.graph.scenes.same.date");
		initVertices(localScene);
		HashSet localHashSet1 = new HashSet();
		HashSet localHashSet2 = new HashSet();
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(localSession);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(localSession);
		Date localDate = null;
		if (!localScene.hasNoSceneTs()) {
			localDate = new Date(localScene.getSceneTs().getTime());
		}
		Object localObject6;
		Object localObject7;
		Iterator localObject5;
		Person localObject2;
		if (!localScene.hasNoSceneTs()) {
			Iterator<Scene> lo2 = (localSceneDAOImpl.findByDate(localDate)).iterator();
			while (((Iterator) lo2).hasNext()) {
				Scene localObject3 = (Scene) ((Iterator) lo2).next();
				if ((!((Scene) localObject3).getId().equals(localScene.getId())) && (((Scene) localObject3).getStrand().getId() == localScene.getStrand().getId())) {
					this.graph.addVertex(localObject3);
					this.labelMap.put(localObject3, ((Scene) localObject3).getFullTitle(true));
					this.iconMap.put(localObject3, this.sceneIconMedium);
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.sceneVertex, localObject3);
					List<TagLink> localObject4 = localTagLinkDAOImpl.findByScene((Scene) localObject3);
					if (!((List) localObject4).isEmpty()) {
						localObject5 = ((List) localObject4).iterator();
						while (((Iterator) localObject5).hasNext()) {
							localObject6 = (TagLink) ((Iterator) localObject5).next();
							if (((TagLink) localObject6).hasOnlyScene()) {
								this.involvedTags.add(((TagLink) localObject6).getTag());
							}
						}
					}
					List<ItemLink> lo5 = localItemLinkDAOImpl.findByScene((Scene) localObject3);
					if (!((List) lo5).isEmpty()) {
						localObject6 = ((List) lo5).iterator();
						while (((Iterator) localObject6).hasNext()) {
							localObject7 = (ItemLink) ((Iterator) localObject6).next();
							if (((ItemLink) localObject7).hasOnlyScene()) {
								this.involvedTags.add(((ItemLink) localObject7).getItem());
							}
						}
					}
				}
			}
		}
		Object localObject1 = localScene.getPersons().iterator();
		while (((Iterator) localObject1).hasNext()) {
			Person person = (Person) ((Iterator) localObject1).next();
			this.graph.addVertex(person);
			this.labelMap.put(person, ((Person) person).toString());
			this.iconMap.put(person, getPersonIcon((Person) person, SbConstants.IconSize.MEDIUM));
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.characterVertex, person);
		}
		localObject1 = localScene.getLocations().iterator();
		while (((Iterator) localObject1).hasNext()) {
			Location location = (Location) ((Iterator) localObject1).next();
			this.graph.addVertex(location);
			this.labelMap.put(location, ((Location) location).toString());
			this.iconMap.put(location, this.locationIconMedium);
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.locationVertex, location);
		}
		localObject1 = localTagLinkDAOImpl.findByScene(localScene);
		Object lo22 = ((List) localObject1).iterator();
		while (((Iterator) lo22).hasNext()) {
			TagLink lo33 = (TagLink) ((Iterator) lo22).next();
			if (((TagLink) lo33).hasOnlyScene()) {
				Tag lo44 = ((TagLink) lo33).getTag();
				localHashSet1.add(lo44);
			}
		}
		List<ItemLink> lo222 = localItemLinkDAOImpl.findByScene(localScene);
		Object localObject3 = ((List) lo222).iterator();
		while (((Iterator) localObject3).hasNext()) {
			ItemLink lo444 = (ItemLink) ((Iterator) localObject3).next();
			if (((ItemLink) lo444).hasOnlyScene()) {
				Item lo55 = ((ItemLink) lo444).getItem();
				localHashSet2.add(lo55);
			}
		}
		localObject3 = localTagLinkDAOImpl.findAll();
		Object localObject4 = ((List) localObject3).iterator();
		while (((Iterator) localObject4).hasNext()) {
			TagLink lo55 = (TagLink) ((Iterator) localObject4).next();
			if (((TagLink) lo55).hasPerson()) {
				localObject6 = ((TagLink) lo55).getPerson();
				if (!localScene.getPersons().contains(localObject6));
			} else if (((TagLink) lo55).hasLocation()) {
				if ((((TagLink) lo55).hasStartScene()) && (!((TagLink) lo55).hasEndScene())) {
					if (((TagLink) lo55).getStartScene().getId() == localScene.getId());
				} else {
					localObject6 = ((TagLink) lo55).getPeriod();
					if ((localObject6 == null) || (localDate == null) || (((Period) localObject6).isInside(localDate))) {
						if (((TagLink) lo55).hasLocationOrPerson()) {
							localObject6 = ((TagLink) lo55).getLocation();
							if (!localScene.getLocations().contains(localObject6)) {
								continue;
							}
						}
					}
				}
			} else if (((TagLink) lo55).hasOnlyScene()) {
				if (((TagLink) lo55).hasEndScene()) {
					localObject6 = ((TagLink) lo55).getStartScene();
					if (((Scene) localObject6).getStrand().getId() == localScene.getStrand().getId()) {
						localObject7 = ((TagLink) lo55).getPeriod();
						if ((localObject7 != null) && (localDate != null) && (!((Period) localObject7).isInside(localDate)));
					}
				}
			} else {
				if (((TagLink) lo55).getTag() != null) {
					this.involvedTags.add(((TagLink) lo55).getTag());
				}
			}
		}
		localObject4 = localItemLinkDAOImpl.findAll();
		Object lo50 = ((List) localObject4).iterator();
		while (((Iterator) lo50).hasNext()) {
			localObject6 = (ItemLink) ((Iterator) lo50).next();
			if (((ItemLink) localObject6).hasPerson()) {
				Person lo17 = ((ItemLink) localObject6).getPerson();
				if (!localScene.getPersons().contains(lo17));
			} else if (((ItemLink) localObject6).hasLocation()) {
				if ((((ItemLink) localObject6).hasStartScene()) && (!((ItemLink) localObject6).hasEndScene())) {
					if (((ItemLink) localObject6).getStartScene().getId() == localScene.getId());
				} else {
					Period lo27 = ((ItemLink) localObject6).getPeriod();
					if ((lo27 == null) || (localDate == null) || (((Period) lo27).isInside(localDate))) {
						if (((ItemLink) localObject6).hasLocationOrPerson()) {
							Location lo37 = ((ItemLink) localObject6).getLocation();
							if (!localScene.getLocations().contains(lo37)) {
								continue;
							}
						}
					}
				}
			} else if (((ItemLink) localObject6).hasOnlyScene()) {
				if (((ItemLink) localObject6).hasEndScene()) {
					Scene lo37 = ((ItemLink) localObject6).getStartScene();
					if (((Scene) lo37).getStrand().getId() == localScene.getStrand().getId()) {
						Period localPeriod = ((ItemLink) localObject6).getPeriod();
						if ((localPeriod != null) && (localDate != null) && (!localPeriod.isInside(localDate)));
					}
				}
			} else {
				Item lo47 = ((ItemLink) localObject6).getItem();
				if (lo47 != null) {
					this.involvedTags.add(lo47);
				}
			}
		}
		removeDoublesFromInvolvedTags(localHashSet1, localHashSet2);
		localObject5 = localHashSet1.iterator();
		while (((Iterator) localObject5).hasNext()) {
			Tag lo16 = (Tag) ((Iterator) localObject5).next();
			if (lo16 != null) {
				this.graph.addVertex(lo16);
				this.labelMap.put(lo16, ((Tag) lo16).toString());
				this.iconMap.put(lo16, this.tagIconMedium);
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo16);
			}
		}
		localObject5 = localHashSet2.iterator();
		while (((Iterator) localObject5).hasNext()) {
			Item lo26 = (Item) ((Iterator) localObject5).next();
			if (lo26 != null) {
				this.graph.addVertex(lo26);
				this.labelMap.put(lo26, ((Item) lo26).toString());
				this.iconMap.put(lo26, this.itemIconMedium);
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo26);
			}
		}
		addToVertexInvolvedTags();
		localDocumentModel.commit();
	}

	private void removeDoublesFromInvolvedTags(Set<Tag> paramSet, Set<Item> paramSet1) {
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator1 = this.involvedTags.iterator();
		AbstractTag localAbstractTag;
		while (localIterator1.hasNext()) {
			localAbstractTag = (AbstractTag) localIterator1.next();
			Iterator localIterator2 = paramSet.iterator();
			Object localObject;
			while (localIterator2.hasNext()) {
				localObject = (Tag) localIterator2.next();
				if (((Tag) localObject).getId().equals(localAbstractTag.getId())) {
					localArrayList.add(localAbstractTag);
				}
			}
			localIterator2 = paramSet1.iterator();
			while (localIterator2.hasNext()) {
				localObject = (Item) localIterator2.next();
				if (((Item) localObject).getId().equals(localAbstractTag.getId())) {
					localArrayList.add(localAbstractTag);
				}
			}
		}
		localIterator1 = localArrayList.iterator();
		while (localIterator1.hasNext()) {
			localAbstractTag = (AbstractTag) localIterator1.next();
			this.involvedTags.remove(localAbstractTag);
		}
	}

	private void createTagGraph() {
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		TagDAOImpl localTagDAOImpl = new TagDAOImpl(localSession);
		Tag localTag = (Tag) localTagDAOImpl.find(Long.valueOf(this.entityId));
		if (localTag == null) {
			localDocumentModel.commit();
			return;
		}
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(localSession);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(localSession);
		this.graphIndex = 0L;
		this.graph.addVertex(localTag);
		this.labelMap.put(localTag, localTag.toString());
		this.iconMap.put(localTag, this.tagIconLarge);
		this.showTagVertex = false;
		initVertices(localTag);
		HashSet localHashSet1 = new HashSet();
		HashSet localHashSet2 = new HashSet();
		HashSet localHashSet3 = new HashSet();
		List localList = localTagLinkDAOImpl.findByTag(localTag);
		Iterator localIterator = localList.iterator();
		while (localIterator.hasNext()) {
			TagLink localTagLink = (TagLink) localIterator.next();
			Period localPeriod = localTagLink.getPeriod();
			if ((localPeriod == null) || (localPeriod.isInside(this.chosenDate))) {
				Object localObject1;
				Object localObject2;
				Object localObject3;
				Object localObject4;
				Object localObject5;
				if (localTagLink.hasLocationOrPerson()) {
					if (localTagLink.hasPerson()) {
						localHashSet2.add(localTagLink.getPerson());
						localObject1 = localTagLinkDAOImpl.findByPerson(localTagLink.getPerson());
						localObject2 = ((List) localObject1).iterator();
						while (((Iterator) localObject2).hasNext()) {
							localObject3 = (TagLink) ((Iterator) localObject2).next();
							if (((TagLink) localObject3).getTag().getId() != localTag.getId()) {
								localObject4 = ((TagLink) localObject3).getTag();
								if (localObject4 != null) {
									if (!isTagInGraph((Tag) localObject4)) {
										this.involvedTags.add(((TagLink) localObject3).getTag());
									}
								}
							}
						}
						localObject2 = localItemLinkDAOImpl.findByPerson(localTagLink.getPerson());
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							ItemLink lo14 = (ItemLink) ((Iterator) localObject3).next();
							Item lo15 = ((ItemLink) lo14).getItem();
							if (lo15 != null) {
								if (!isItemInGraph((Item) lo15)) {
									this.involvedTags.add(lo15);
								}
							}
						}
					}
					if (localTagLink.hasLocation()) {
						localHashSet1.add(localTagLink.getLocation());
						localObject1 = localTagLinkDAOImpl.findByLocation(localTagLink.getLocation());
						localObject2 = ((List) localObject1).iterator();
						while (((Iterator) localObject2).hasNext()) {
							localObject3 = (TagLink) ((Iterator) localObject2).next();
							if (((TagLink) localObject3).getTag().getId() != localTag.getId()) {
								localObject4 = ((TagLink) localObject3).getTag();
								if (localObject4 != null) {
									if (!isTagInGraph((Tag) localObject4)) {
										this.involvedTags.add(((TagLink) localObject3).getTag());
									}
								}
							}
						}
						localObject2 = localItemLinkDAOImpl.findByLocation(localTagLink.getLocation());
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							localObject4 = (ItemLink) ((Iterator) localObject3).next();
							localObject5 = ((ItemLink) localObject4).getItem();
							if (localObject5 != null) {
								if (!isItemInGraph((Item) localObject5)) {
									this.involvedTags.add(((ItemLink) localObject4).getItem());
								}
							}
						}
					}
				} else {
					localObject1 = localTagLink.getStartScene();
					if (localObject1 != null) {
						localObject2 = localSceneDAOImpl.findByDate(this.chosenDate);
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							localObject4 = (Scene) ((Iterator) localObject3).next();
							if (((localTagLink.hasEndScene()) || (((Scene) localObject4).getId() == ((Scene) localObject1).getId())) && (((Scene) localObject4).getStrand().getId() == ((Scene) localObject1).getStrand().getId())) {
								localHashSet3.add(localObject4);
								localObject5 = localTagLinkDAOImpl.findByScene((Scene) localObject4);
								Object localObject6 = ((List) localObject5).iterator();
								Object localObject8;
								while (((Iterator) localObject6).hasNext()) {
									TagLink lo777 = (TagLink) ((Iterator) localObject6).next();
									Tag lo18 = ((TagLink) lo777).getTag();
									if (lo18 != null) {
										if ((localTag.getId() != ((Tag) lo18).getId()) && (!isTagInGraph((Tag) lo18))) {
											this.involvedTags.add(lo18);
										}
									}
								}
								localObject6 = localItemLinkDAOImpl.findByScene((Scene) localObject4);
								Object localObject7 = ((List) localObject6).iterator();
								while (((Iterator) localObject7).hasNext()) {
									localObject8 = (ItemLink) ((Iterator) localObject7).next();
									Item localItem = ((ItemLink) localObject8).getItem();
									if (localItem != null) {
										if (!isItemInGraph(localItem)) {
											this.involvedTags.add(localItem);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		localDocumentModel.commit();
		addToVertexScenes(localHashSet3);
		addToVertexPersons(localHashSet2);
		addToVertexLocations(localHashSet1);
		addToVertexInvolvedTags();
	}

	private void createItemGraph() {
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		ItemDAOImpl localItemDAOImpl = new ItemDAOImpl(localSession);
		Item localItem = (Item) localItemDAOImpl.find(Long.valueOf(this.entityId));
		if (localItem == null) {
			localDocumentModel.commit();
			return;
		}
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(localSession);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(localSession);
		this.graphIndex = 0L;
		this.graph.addVertex(localItem);
		this.labelMap.put(localItem, localItem.toString());
		this.iconMap.put(localItem, this.itemIconLarge);
		this.showTagVertex = false;
		initVertices(localItem);
		HashSet localHashSet1 = new HashSet();
		HashSet localHashSet2 = new HashSet();
		HashSet localHashSet3 = new HashSet();
		List localList = localItemLinkDAOImpl.findByItem(localItem);
		Iterator localIterator = localList.iterator();
		while (localIterator.hasNext()) {
			ItemLink localItemLink = (ItemLink) localIterator.next();
			Period localPeriod = localItemLink.getPeriod();
			if ((localPeriod == null) || (localPeriod.isInside(this.chosenDate))) {
				Object localObject1;
				Object localObject2;
				Object localObject3;
				Object localObject4;
				Object localObject5;
				if (localItemLink.hasLocationOrPerson()) {
					if (localItemLink.hasPerson()) {
						localHashSet2.add(localItemLink.getPerson());
						localObject1 = localItemLinkDAOImpl.findByPerson(localItemLink.getPerson());
						localObject2 = ((List) localObject1).iterator();
						while (((Iterator) localObject2).hasNext()) {
							localObject3 = (ItemLink) ((Iterator) localObject2).next();
							if (((ItemLink) localObject3).getItem().getId() != localItem.getId()) {
								localObject4 = ((ItemLink) localObject3).getItem();
								if (localObject4 != null) {
									if (!isItemInGraph((Item) localObject4)) {
										this.involvedTags.add(((ItemLink) localObject3).getItem());
									}
								}
							}
						}
						localObject2 = localTagLinkDAOImpl.findByPerson(localItemLink.getPerson());
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							TagLink lo14 = (TagLink) ((Iterator) localObject3).next();
							Tag lo15 = ((TagLink) lo14).getTag();
							if (lo15 != null) {
								if (!isTagInGraph((Tag) lo15)) {
									this.involvedTags.add(lo15);
								}
							}
						}
					}
					if (localItemLink.hasLocation()) {
						localHashSet1.add(localItemLink.getLocation());
						localObject1 = localItemLinkDAOImpl.findByLocation(localItemLink.getLocation());
						localObject2 = ((List) localObject1).iterator();
						while (((Iterator) localObject2).hasNext()) {
							localObject3 = (ItemLink) ((Iterator) localObject2).next();
							if (((ItemLink) localObject3).getItem().getId() != localItem.getId()) {
								localObject4 = ((ItemLink) localObject3).getItem();
								if (localObject4 != null) {
									if (!isItemInGraph((Item) localObject4)) {
										this.involvedTags.add(((ItemLink) localObject3).getItem());
									}
								}
							}
						}
						localObject2 = localTagLinkDAOImpl.findByLocation(localItemLink.getLocation());
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							localObject4 = (TagLink) ((Iterator) localObject3).next();
							localObject5 = ((TagLink) localObject4).getTag();
							if (localObject5 != null) {
								if (!isTagInGraph((Tag) localObject5)) {
									this.involvedTags.add(((TagLink) localObject4).getTag());
								}
							}
						}
					}
				} else {
					localObject1 = localItemLink.getStartScene();
					if (localObject1 != null) {
						localObject2 = localSceneDAOImpl.findByDate(this.chosenDate);
						localObject3 = ((List) localObject2).iterator();
						while (((Iterator) localObject3).hasNext()) {
							localObject4 = (Scene) ((Iterator) localObject3).next();
							if (((localItemLink.hasEndScene()) || (((Scene) localObject4).getId() == ((Scene) localObject1).getId())) && (((Scene) localObject4).getStrand().getId() == ((Scene) localObject1).getStrand().getId())) {
								localHashSet3.add(localObject4);
								localObject5 = localItemLinkDAOImpl.findByScene((Scene) localObject4);
								Object localObject6 = ((List) localObject5).iterator();
								Object localObject8;
								while (((Iterator) localObject6).hasNext()) {
									ItemLink lo777 = (ItemLink) ((Iterator) localObject6).next();
									Item lo18 = ((ItemLink) lo777).getItem();
									if (lo18 != null) {
										if ((localItem.getId() != ((Item) lo18).getId()) && (!isItemInGraph((Item) lo18))) {
											this.involvedTags.add(lo18);
										}
									}
								}
								localObject6 = localTagLinkDAOImpl.findByScene((Scene) localObject4);
								Object localObject7 = ((List) localObject6).iterator();
								while (((Iterator) localObject7).hasNext()) {
									TagLink lo18 = (TagLink) ((Iterator) localObject7).next();
									Tag localTag = ((TagLink) lo18).getTag();
									if (localTag != null) {
										if (!isTagInGraph(localTag)) {
											this.involvedTags.add(localTag);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		localDocumentModel.commit();
		addToVertexScenes(localHashSet3);
		addToVertexPersons(localHashSet2);
		addToVertexLocations(localHashSet1);
		addToVertexInvolvedTags();
	}

	private void addToVertexScenes(Set<Scene> paramSet) {
		Iterator localIterator = paramSet.iterator();
		while (localIterator.hasNext()) {
			Scene localScene = (Scene) localIterator.next();
			this.graph.addVertex(localScene);
			this.labelMap.put(localScene, localScene.toString());
			this.iconMap.put(localScene, this.sceneIconMedium);
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.sceneVertex, localScene);
		}
	}

	private void addToVertexPersons(Set<Person> paramSet) {
		Iterator localIterator = paramSet.iterator();
		while (localIterator.hasNext()) {
			Person localPerson = (Person) localIterator.next();
			this.graph.addVertex(localPerson);
			this.labelMap.put(localPerson, localPerson.toString());
			this.iconMap.put(localPerson, getPersonIcon(localPerson, SbConstants.IconSize.MEDIUM));
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.characterVertex, localPerson);
		}
	}

	private void addToVertexLocations(Set<Location> paramSet) {
		Iterator localIterator = paramSet.iterator();
		while (localIterator.hasNext()) {
			Location localLocation = (Location) localIterator.next();
			this.graph.addVertex(localLocation);
			this.labelMap.put(localLocation, localLocation.toString());
			this.iconMap.put(localLocation, this.locationIconMedium);
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.locationVertex, localLocation);
		}
	}

	private void createLocationGraph() {
		try {
			DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
			Session localSession = localDocumentModel.beginTransaction();
			LocationDAOImpl localLocationDAOImpl = new LocationDAOImpl(localSession);
			Location localLocation = (Location) localLocationDAOImpl.find(Long.valueOf(this.entityId));
			if (localLocation == null) {
				localDocumentModel.commit();
				return;
			}
			SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
			List localList1 = localSceneDAOImpl.findAll();
			if ((localList1 == null) || (localList1.isEmpty())) {
				return;
			}
			TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(localSession);
			ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(localSession);
			this.graphIndex = 0L;
			this.graph.addVertex(localLocation);
			this.labelMap.put(localLocation, localLocation.toString());
			this.iconMap.put(localLocation, this.locationIconLarge);
			this.locationVertexTitle = I18N.getMsg("msg.graph.involved.locations");
			initVertices(localLocation);
			List localList2 = localTagLinkDAOImpl.findByLocation(localLocation);
			Object localObject1 = localList2.iterator();
			Object localObject3;
			Object localObject4;
			while (((Iterator) localObject1).hasNext()) {
				TagLink lo22 = (TagLink) ((Iterator) localObject1).next();
				localObject3 = ((TagLink) lo22).getPeriod();
				if ((localObject3 == null) || (((Period) localObject3).isInside(this.chosenDate))) {
					Tag lo14 = ((TagLink) lo22).getTag();
					if (lo14 != null) {
						this.graph.addVertex(lo14);
						this.labelMap.put(lo14, ((Tag) lo14).toString());
						this.iconMap.put(lo14, this.tagIconMedium);
						this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo14);
					}
				}
			}
			localObject1 = localItemLinkDAOImpl.findByLocation(localLocation);
			Object localObject2 = ((List) localObject1).iterator();
			Object localObject5;
			while (((Iterator) localObject2).hasNext()) {
				localObject3 = (ItemLink) ((Iterator) localObject2).next();
				localObject4 = ((ItemLink) localObject3).getPeriod();
				if ((localObject4 == null) || (((Period) localObject4).isInside(this.chosenDate))) {
					Item lo15 = ((ItemLink) localObject3).getItem();
					if (lo15 != null) {
						this.graph.addVertex(lo15);
						this.labelMap.put(lo15, ((Item) lo15).toString());
						this.iconMap.put(lo15, this.tagIconMedium);
						this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo15);
					}
				}
			}
			localObject2 = localList1.iterator();
			Object localObject6;
			Object localObject7;
			Object localObject8;
			Object localObject9;
			Object localObject10;
			while (((Iterator) localObject2).hasNext()) {
				localObject3 = (Scene) ((Iterator) localObject2).next();
				if ((!((Scene) localObject3).hasNoSceneTs()) && (this.chosenDate.compareTo(((Scene) localObject3).getDate()) == 0)) {
					localObject4 = ((Scene) localObject3).getLocations().iterator();
					while (((Iterator) localObject4).hasNext()) {
						localObject5 = (Location) ((Iterator) localObject4).next();
						if (((Location) localObject5).getId().equals(localLocation.getId())) {
							this.graph.addVertex((Scene)localObject3);
							this.labelMap.put((Scene)localObject3, ((Scene) localObject3).toString());
							this.iconMap.put((Scene)localObject3, this.sceneIconMedium);
							this.graph.addEdge(Long.valueOf(this.graphIndex++), this.sceneVertex, (Scene)localObject3);
							this.sceneIds.add(((Scene) localObject3).getId());
							localObject6 = localTagLinkDAOImpl.findByScene((Scene) localObject3);
							if (!((List) localObject6).isEmpty()) {
								localObject7 = ((List) localObject6).iterator();
								while (((Iterator) localObject7).hasNext()) {
									localObject8 = (TagLink) ((Iterator) localObject7).next();
									Tag lo19 = ((TagLink) localObject8).getTag();
									if (lo19 != null) {
										if (!isTagInGraph((Tag) lo19)) {
											this.involvedTags.add(lo19);
										}
									}
								}
							}
							localObject7 = localItemLinkDAOImpl.findByScene((Scene) localObject3);
							if (!((List) localObject7).isEmpty()) {
								localObject8 = ((List) localObject7).iterator();
								while (((Iterator) localObject8).hasNext()) {
									ItemLink lo19 = (ItemLink) ((Iterator) localObject8).next();
									Item lo110 = ((ItemLink) lo19).getItem();
									if (lo110 != null) {
										if (!isItemInGraph((Item) lo110)) {
											this.involvedTags.add(lo110);
										}
									}
								}
							}
						}
					}
				}
			}
			localObject2 = localList1.iterator();
			while (((Iterator) localObject2).hasNext()) {
				localObject3 = (Scene) ((Iterator) localObject2).next();
				if ((!((Scene) localObject3).hasNoSceneTs()) && (this.chosenDate.compareTo(((Scene) localObject3).getDate()) == 0)) {
					localObject4 = ((Scene) localObject3).getLocations().iterator();
					while (((Iterator) localObject4).hasNext()) {
						localObject5 = (Location) ((Iterator) localObject4).next();
						if (((Location) localObject5).getId() == localLocation.getId()) {
							localObject6 = ((Scene) localObject3).getPersons().iterator();
							while (((Iterator) localObject6).hasNext()) {
								Person person = (Person) ((Iterator) localObject6).next();
								this.graph.addVertex(person);
								this.labelMap.put(person, ((Person) person).toString());
								this.iconMap.put(person, getPersonIcon((Person) person, SbConstants.IconSize.MEDIUM));
								this.graph.addEdge(Long.valueOf(this.graphIndex++), this.characterVertex, person);
								localObject8 = localTagLinkDAOImpl.findByPerson((Person) person);
								localObject9 = ((List) localObject8).iterator();
								while (((Iterator) localObject9).hasNext()) {
									localObject10 = (TagLink) ((Iterator) localObject9).next();
									Tag localObject11 = ((TagLink) localObject10).getTag();
									if (localObject11 != null) {
										if (!isTagInGraph((Tag) localObject11)) {
											this.involvedTags.add(localObject11);
										}
									}
								}
								localObject9 = localItemLinkDAOImpl.findByPerson((Person) person);
								localObject10 = ((List) localObject9).iterator();
								while (((Iterator) localObject10).hasNext()) {
									ItemLink localObject11 = (ItemLink) ((Iterator) localObject10).next();
									Item localItem = ((ItemLink) localObject11).getItem();
									if (localItem != null) {
										if (!isItemInGraph(localItem)) {
											this.involvedTags.add(localItem);
										}
									}
								}
							}
						}
					}
				}
			}
			localObject2 = localList1.iterator();
			while (((Iterator) localObject2).hasNext()) {
				localObject3 = (Scene) ((Iterator) localObject2).next();
				localObject4 = ((Scene) localObject3).getLocations().iterator();
				while (((Iterator) localObject4).hasNext()) {
					Location lo15 = (Location) ((Iterator) localObject4).next();
					if ((this.sceneIds.contains(((Scene) localObject3).getId())) && (((Location) lo15).getId() != localLocation.getId())) {
						this.graph.addVertex(lo15);
						this.labelMap.put(lo15, ((Location) lo15).toString());
						this.iconMap.put(lo15, this.locationIconMedium);
						this.graph.addEdge(Long.valueOf(this.graphIndex++), this.locationVertex, lo15);
						localObject6 = localTagLinkDAOImpl.findByLocation((Location) lo15);
						if (!((List) localObject6).isEmpty()) {
							localObject7 = ((List) localObject6).iterator();
							while (((Iterator) localObject7).hasNext()) {
								localObject8 = (TagLink) ((Iterator) localObject7).next();
								Tag lo19 = ((TagLink) localObject8).getTag();
								if (lo19 != null) {
									if (!isTagInGraph((Tag) lo19)) {
										this.involvedTags.add(lo19);
									}
								}
							}
						}
						localObject7 = localItemLinkDAOImpl.findByLocation((Location) lo15);
						if (!((List) localObject7).isEmpty()) {
							localObject8 = ((List) localObject7).iterator();
							while (((Iterator) localObject8).hasNext()) {
								localObject9 = (ItemLink) ((Iterator) localObject8).next();
								Item lo110 = ((ItemLink) localObject9).getItem();
								if (lo110 != null) {
									if (!isItemInGraph((Item) lo110)) {
										this.involvedTags.add(lo110);
									}
								}
							}
						}
					}
				}
			}
			localDocumentModel.commit();
			addToVertexInvolvedTags();
		} catch (Exception localException) {
		}
	}

	private void createPersonGraph() {
		DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
		Session localSession = localDocumentModel.beginTransaction();
		PersonDAOImpl localPersonDAOImpl = new PersonDAOImpl(localSession);
		Person localPerson = (Person) localPersonDAOImpl.find(Long.valueOf(this.entityId));
		if (localPerson == null) {
			localDocumentModel.commit();
			return;
		}
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
		List localList1 = localSceneDAOImpl.findAll();
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(localSession);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(localSession);
		this.graphIndex = 0L;
		this.graph.addVertex(localPerson);
		this.labelMap.put(localPerson, localPerson.toString());
		this.iconMap.put(localPerson, getPersonIcon(localPerson, SbConstants.IconSize.LARGE));
		initVertices(localPerson);
		List localList2 = localTagLinkDAOImpl.findByPerson(localPerson);
		Object localObject1 = localList2.iterator();
		Object localObject3;
		while (((Iterator) localObject1).hasNext()) {
			TagLink lo22 = (TagLink) ((Iterator) localObject1).next();
			if (((TagLink) lo22).hasPeriod()) {
				localObject3 = ((TagLink) lo22).getPeriod();
				if ((localObject3 != null) && (!((Period) localObject3).isInside(this.chosenDate)));
			} else {
				Tag lo13 = ((TagLink) lo22).getTag();
				if (lo13 != null) {
					this.graph.addVertex(lo13);
					this.labelMap.put(lo13, ((Tag) lo13).toString());
					this.iconMap.put(lo13, this.tagIconMedium);
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo13);
				}
			}
		}
		localObject1 = localItemLinkDAOImpl.findByPerson(localPerson);
		Object localObject2 = ((List) localObject1).iterator();
		Object localObject4;
		while (((Iterator) localObject2).hasNext()) {
			localObject3 = (ItemLink) ((Iterator) localObject2).next();
			if (((ItemLink) localObject3).hasPeriod()) {
				localObject4 = ((ItemLink) localObject3).getPeriod();
				if ((localObject4 != null) && (!((Period) localObject4).isInside(this.chosenDate)));
			} else {
				Item lo14 = ((ItemLink) localObject3).getItem();
				if (lo14 != null) {
					this.graph.addVertex(lo14);
					this.labelMap.put(lo14, ((Item) lo14).toString());
					this.iconMap.put(lo14, this.itemIconMedium);
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo14);
				}
			}
		}
		localObject2 = localList1.iterator();
		Object localObject5;
		Object localObject6;
		Object localObject7;
		Object localObject8;
		while (((Iterator) localObject2).hasNext()) {
			Scene lo13 = (Scene) ((Iterator) localObject2).next();
			if ((!((Scene) lo13).hasNoSceneTs()) && (this.chosenDate.compareTo(((Scene) lo13).getDate()) == 0) && (((Scene) lo13).getPersons().contains(localPerson))) {
				this.graph.addVertex(lo13);
				this.labelMap.put(lo13, ((Scene) lo13).toString());
				this.iconMap.put(lo13, this.sceneIconMedium);
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.sceneVertex, lo13);
				this.sceneIds.add(((Scene) lo13).getId());
				localObject4 = localTagLinkDAOImpl.findByScene((Scene) lo13);
				localObject5 = ((List) localObject4).iterator();
				while (((Iterator) localObject5).hasNext()) {
					localObject6 = (TagLink) ((Iterator) localObject5).next();
					Tag lo17 = ((TagLink) localObject6).getTag();
					if (lo17 != null) {
						if (!isTagInGraph((Tag) lo17)) {
							this.involvedTags.add(lo17);
						}
					}
				}
				localObject5 = localItemLinkDAOImpl.findByScene((Scene) lo13);
				localObject6 = ((List) localObject5).iterator();
				while (((Iterator) localObject6).hasNext()) {
					localObject7 = (ItemLink) ((Iterator) localObject6).next();
					Item lo18 = ((ItemLink) localObject7).getItem();
					if (lo18 != null) {
						if (!isItemInGraph((Item) lo18)) {
							this.involvedTags.add(lo18);
						}
					}
				}
			}
		}
		localObject2 = localList1.iterator();
		Object localObject9;
		Item localItem;
		while (((Iterator) localObject2).hasNext()) {
			localObject3 = (Scene) ((Iterator) localObject2).next();
			if ((!((Scene) localObject3).hasNoSceneTs()) && (this.chosenDate.compareTo(((Scene) localObject3).getDate()) == 0) && (((Scene) localObject3).getPersons().contains(localPerson))) {
				localObject4 = ((Scene) localObject3).getLocations().iterator();
				while (((Iterator) localObject4).hasNext()) {
					Location lo15 = (Location) ((Iterator) localObject4).next();
					this.graph.addVertex(lo15);
					this.labelMap.put(lo15, ((Location) lo15).toString());
					this.iconMap.put(lo15, this.locationIconMedium);
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.locationVertex, lo15);
					localObject6 = localTagLinkDAOImpl.findByLocation((Location) lo15);
					localObject7 = ((List) localObject6).iterator();
					while (((Iterator) localObject7).hasNext()) {
						localObject8 = (TagLink) ((Iterator) localObject7).next();
						Tag lo19 = ((TagLink) localObject8).getTag();
						if (lo19 != null) {
							if (!isTagInGraph((Tag) lo19)) {
								this.involvedTags.add(lo19);
							}
						}
					}
					localObject7 = localItemLinkDAOImpl.findByLocation((Location) lo15);
					localObject8 = ((List) localObject7).iterator();
					while (((Iterator) localObject8).hasNext()) {
						localObject9 = (ItemLink) ((Iterator) localObject8).next();
						localItem = ((ItemLink) localObject9).getItem();
						if (localItem != null) {
							if (!isItemInGraph(localItem)) {
								this.involvedTags.add(localItem);
							}
						}
					}
				}
			}
		}
		localObject2 = localList1.iterator();
		while (((Iterator) localObject2).hasNext()) {
			localObject3 = (Scene) ((Iterator) localObject2).next();
			localObject4 = ((Scene) localObject3).getPersons().iterator();
			while (((Iterator) localObject4).hasNext()) {
				Person person = (Person) ((Iterator) localObject4).next();
				if ((((Person) person).getId() != localPerson.getId()) && (this.sceneIds.contains(((Scene) localObject3).getId()))) {
					this.graph.addVertex(person);
					this.labelMap.put(person, ((Person) person).toString());
					this.iconMap.put(person, getPersonIcon((Person) person, SbConstants.IconSize.MEDIUM));
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.characterVertex, person);
					localObject6 = localTagLinkDAOImpl.findByPerson((Person) person);
					localObject7 = ((List) localObject6).iterator();
					while (((Iterator) localObject7).hasNext()) {
						localObject8 = (TagLink) ((Iterator) localObject7).next();
						Tag tag = ((TagLink) localObject8).getTag();
						if (tag != null) {
							if (!isTagInGraph((Tag) tag)) {
								this.involvedTags.add(tag);
							}
						}
					}
					localObject7 = localItemLinkDAOImpl.findByPerson((Person) person);
					localObject8 = ((List) localObject7).iterator();
					while (((Iterator) localObject8).hasNext()) {
						localObject9 = (ItemLink) ((Iterator) localObject8).next();
						localItem = ((ItemLink) localObject9).getItem();
						if (localItem != null) {
							if (!isItemInGraph(localItem)) {
								this.involvedTags.add(localItem);
							}
						}
					}
				}
			}
		}
		localDocumentModel.commit();
		addToVertexInvolvedTags();
	}

	private boolean isTagInGraph(Tag paramTag) {
		if (paramTag == null) {
			return false;
		}
		Collection localCollection = this.graph.getVertices();
		Iterator localIterator = localCollection.iterator();
		while (localIterator.hasNext()) {
			AbstractEntity localAbstractEntity = (AbstractEntity) localIterator.next();
			if (((localAbstractEntity instanceof Tag)) && (localAbstractEntity.getId().equals(paramTag.getId()))) {
				return true;
			}
		}
		return false;
	}

	private boolean isItemInGraph(Item paramItem) {
		if (paramItem == null) {
			return false;
		}
		Collection localCollection = this.graph.getVertices();
		Iterator localIterator = localCollection.iterator();
		while (localIterator.hasNext()) {
			AbstractEntity localAbstractEntity = (AbstractEntity) localIterator.next();
			if (((localAbstractEntity instanceof Item)) && (localAbstractEntity.getId().equals(paramItem.getId()))) {
				return true;
			}
		}
		return false;
	}

	private void initVertices(AbstractEntity paramAbstractEntity) {
		addVertexScene(paramAbstractEntity);
		addVertexCharachter(paramAbstractEntity);
		addVertexLocation(paramAbstractEntity);
		if (this.showTagVertex) {
			addVertexTag(paramAbstractEntity);
		}
		addVertexInvoldedTag(paramAbstractEntity);
		this.sceneIds = new ArrayList();
		this.involvedTags = new HashSet();
		this.sceneVertexTitle = null;
		this.locationVertexTitle = null;
		this.showTagVertex = true;
	}

	private void addVertexTag(AbstractEntity paramAbstractEntity) {
		this.tagVertex = new Tag();
		this.tagVertex.setName(I18N.getMsg("msg.tags") + " & " + I18N.getMsg("msg.items"));
		this.graph.addVertex(this.tagVertex);
		this.labelMap.put(this.tagVertex, this.tagVertex.getName());
		this.iconMap.put(this.tagVertex, this.emptyIcon);
		this.graph.addEdge(Long.valueOf(this.graphIndex++), paramAbstractEntity, this.tagVertex);
	}

	private void addVertexInvoldedTag(AbstractEntity paramAbstractEntity) {
		this.involvedTagVertex = new Tag();
		this.involvedTagVertex.setName(I18N.getMsg("msg.graph.involved.tags_items"));
		this.graph.addVertex(this.involvedTagVertex);
		this.labelMap.put(this.involvedTagVertex, this.involvedTagVertex.getName());
		this.iconMap.put(this.involvedTagVertex, this.emptyIcon);
		this.graph.addEdge(Long.valueOf(this.graphIndex++), paramAbstractEntity, this.involvedTagVertex);
	}

	private void addToVertexInvolvedTags() {
		Iterator localIterator = this.involvedTags.iterator();
		while (localIterator.hasNext()) {
			AbstractTag localAbstractTag = (AbstractTag) localIterator.next();
			if (localAbstractTag != null) {
				this.graph.addVertex(localAbstractTag);
				this.labelMap.put(localAbstractTag, localAbstractTag.toString());
				if ((localAbstractTag instanceof Item)) {
					this.iconMap.put(localAbstractTag, this.itemIconMedium);
				} else if ((localAbstractTag instanceof Tag)) {
					this.iconMap.put(localAbstractTag, this.tagIconMedium);
				}
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.involvedTagVertex, localAbstractTag);
			}
		}
	}

	private void addVertexScene(AbstractEntity paramAbstractEntity) {
		this.sceneVertex = new Scene();
		if (this.sceneVertexTitle != null) {
			this.sceneVertex.setTitle(this.sceneVertexTitle);
		} else {
			this.sceneVertex.setTitle(I18N.getMsg("msg.common.scenes"));
		}
		this.graph.addVertex(this.sceneVertex);
		this.labelMap.put(this.sceneVertex, this.sceneVertex.toString());
		this.iconMap.put(this.sceneVertex, this.emptyIcon);
		this.graph.addEdge(Long.valueOf(this.graphIndex++), paramAbstractEntity, this.sceneVertex);
	}

	private void addVertexCharachter(AbstractEntity paramAbstractEntity) {
		this.characterVertex = new Person();
		this.characterVertex.setFirstname(I18N.getMsg("msg.common.persons"));
		this.graph.addVertex(this.characterVertex);
		this.labelMap.put(this.characterVertex, this.characterVertex.getFullName());
		this.iconMap.put(this.characterVertex, this.emptyIcon);
		this.graph.addEdge(Long.valueOf(this.graphIndex++), paramAbstractEntity, this.characterVertex);
	}

	private void addVertexLocation(AbstractEntity paramAbstractEntity) {
		this.locationVertex = new Location();
		if (this.locationVertexTitle != null) {
			this.locationVertex.setName(this.locationVertexTitle);
		} else {
			this.locationVertex.setName(I18N.getMsg("msg.menu.locations"));
		}
		this.graph.addVertex(this.locationVertex);
		this.labelMap.put(this.locationVertex, this.locationVertex.toString());
		this.iconMap.put(this.locationVertex, this.emptyIcon);
		this.graph.addEdge(Long.valueOf(this.graphIndex++), paramAbstractEntity, this.locationVertex);
	}

	private Icon getPersonIcon(Person paramPerson, SbConstants.IconSize paramIconSize) {
		if (paramIconSize == SbConstants.IconSize.MEDIUM) {
			if (paramPerson.getGender().isMale()) {
				return this.manIconMedium;
			}
			if (paramPerson.getGender().isFemale()) {
				return this.womanIconMedium;
			}
			return this.personIconMedium;
		}
		if (paramIconSize == SbConstants.IconSize.LARGE) {
			if (paramPerson.getGender().isMale()) {
				return this.manIconLarge;
			}
			if (paramPerson.getGender().isFemale()) {
				return this.womanIconLarge;
			}
			return this.personIconLarge;
		}
		return this.emptyIcon;
	}

	public void setCharacterId(int paramInt) {
		this.entityId = paramInt;
	}

	public void refresh(AbstractEntity paramAbstractEntity) {
		try {
			if (paramAbstractEntity == null) {
				return;
			}
			this.entityId = paramAbstractEntity.getId().longValue();
			refreshGraph(paramAbstractEntity);
			updateControlPanel(paramAbstractEntity);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void updateControlPanel(AbstractEntity paramAbstractEntity) {
		int i = 0;
		Object localObject;
		for (int j = 0; j < this.entityTypeCombo.getItemCount(); j++) {
			localObject = (EntityTypeCbItem) this.entityTypeCombo.getItemAt(j);
			if ((((EntityTypeCbItem) localObject).getType() == EntityTypeCbItem.Type.PERSON) && ((paramAbstractEntity instanceof Person))) {
				i = j;
				break;
			}
			if ((((EntityTypeCbItem) localObject).getType() == EntityTypeCbItem.Type.LOCATION) && ((paramAbstractEntity instanceof Location))) {
				i = j;
				break;
			}
			if ((((EntityTypeCbItem) localObject).getType() == EntityTypeCbItem.Type.SCENE) && ((paramAbstractEntity instanceof Scene))) {
				i = j;
				break;
			}
			if ((((EntityTypeCbItem) localObject).getType() == EntityTypeCbItem.Type.TAG) && ((paramAbstractEntity instanceof Tag))) {
				i = j;
				break;
			}
			if ((((EntityTypeCbItem) localObject).getType() == EntityTypeCbItem.Type.ITEM) && ((paramAbstractEntity instanceof Item))) {
				i = j;
				break;
			}
		}
		this.entityTypeCombo.setSelectedIndex(i);
		int j;
		if ((paramAbstractEntity instanceof Scene)) {
			for (j = 0; j < this.entityCombo.getItemCount(); j++) {
				localObject = (Scene) this.entityCombo.getItemAt(j);
				if (((Scene) localObject).getId().equals(paramAbstractEntity.getId())) {
					this.entityCombo.setSelectedIndex(j);
					break;
				}
			}
		} else {
			this.entityCombo.setSelectedItem(paramAbstractEntity);
		}
	}

	public boolean hasAutoRefresh() {
		return this.cbAutoRefresh.isSelected();
	}

	public void actionPerformed(ActionEvent paramActionEvent) {
		Object localObject1 = paramActionEvent.getSource();
		if ((localObject1 == null) || (!this.processActionListener)) {
			return;
		}
		if ((localObject1 instanceof JButton)) {
			String localObject2 = ((JButton) localObject1).getName();
			int i = this.dateCombo.getSelectedIndex();
			if (SbConstants.ComponentName.BT_PREVIOUS.check((String) localObject2)) {
				i--;
				if (i >= 0);
			} else if (SbConstants.ComponentName.BT_NEXT.check((String) localObject2)) {
				i++;
				if (i <= this.dateCombo.getItemCount() - 1);
			} else if (SbConstants.ComponentName.BT_FIRST.check((String) localObject2)) {
				i = 0;
			} else if (SbConstants.ComponentName.BT_LAST.check((String) localObject2)) {
				i = this.dateCombo.getItemCount() - 1;
			}
			this.dateCombo.setSelectedIndex(i);
			return;
		}
		this.entitySourceName = ((JComponent) localObject1).getName();
		if (this.entitySourceName.equals(SbConstants.ComponentName.COMBO_ENTITY_TYPES.toString())) {
			EntityTypeCbItem localObject2 = (EntityTypeCbItem) this.entityTypeCombo.getSelectedItem();
			refreshEntityCombo(((EntityTypeCbItem) localObject2).getType());
			return;
		}
		this.chosenDate = ((Date) this.dateCombo.getSelectedItem());
		Object localObject2 = (AbstractEntity) this.entityCombo.getSelectedItem();
		refresh((AbstractEntity) localObject2);
	}

	public void refresh() {
		refreshControlPanel();
		refreshGraph();
	}

	public MainFrame getMainFrame() {
		return this.mainFrame;
	}

	class VertexStringerImpl<V> implements Transformer<V, String> {

		Map<V, String> map = new HashMap();
		boolean enabled = true;

		public VertexStringerImpl() {
			Object localObject = null;
			this.map = (Map<V, String>) localObject;
		}

		public String transform(V paramV) {
			if (isEnabled()) {
				return "<html><table width='100'><tr><td>" + (String) this.map.get(paramV) + "</td></tr></table>";
			}
			return "";
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean paramBoolean) {
			this.enabled = paramBoolean;
		}
	}
}