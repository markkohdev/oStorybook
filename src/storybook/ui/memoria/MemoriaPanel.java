package storybook.ui.memoria;

import storybook.model.BookModel;
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
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Item;
import storybook.model.hbn.entity.ItemLink;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Tag;
import storybook.model.hbn.entity.TagLink;
import storybook.toolkit.BookUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.IOUtil;
import storybook.toolkit.Period;
import storybook.toolkit.filefilter.PngFileFilter;
import storybook.toolkit.swing.IconButton;
import storybook.toolkit.swing.ScreenImage;
import storybook.toolkit.swing.SwingUtil;
import storybook.ui.panel.AbstractPanel;
import storybook.ui.MainFrame;
import storybook.ui.combo.EntityTypeListCellRenderer;
import storybook.ui.interfaces.IRefreshable;
import storybook.ui.options.MemoriaOptionsDialog;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
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
import storybook.SbApp;
import storybook.controller.BookController;

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

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		try {
			Object localObject1 = evt.getNewValue();
			String str = evt.getPropertyName();
			if (str == null) {
				return;
			}
			Object localObject2;
			Object localObject3;
			if (BookController.CommonProps.REFRESH.check(str)) {
				localObject2 = (View) localObject1;
				localObject3 = (View) getParent().getParent();
				if (localObject3 == localObject2) {
					refresh();
				}
				return;
			}
			if (BookController.CommonProps.SHOW_IN_MEMORIA.check(str)) {
				localObject2 = (AbstractEntity) localObject1;
				refresh((AbstractEntity) localObject2);
				return;
			}
			if (BookController.CommonProps.SHOW_OPTIONS.check(str)) {
				localObject2 = (View) evt.getNewValue();
				if (!((View) localObject2).getName().equals(SbConstants.ViewName.MEMORIA.toString())) {
					return;
				}
				localObject3 = new MemoriaOptionsDialog(this.mainFrame);
				SwingUtil.showModalDialog((JDialog) localObject3, this);
				return;
			}
			if (BookController.MemoriaViewProps.BALLOON.check(str)) {
				this.showBalloonLayout = ((Boolean) localObject1).booleanValue();
				makeLayoutTransition();
				return;
			}
			if ((str.startsWith("Update")) || (str.startsWith("Delete")) || (str.startsWith("New"))) {
				refresh();
				return;
			}
			if (BookController.CommonProps.EXPORT.check(str)) {
				localObject2 = (View) localObject1;
				localObject3 = (View) getParent().getParent();
				if (localObject3 == localObject2) {
					export();
				}
			}
		} catch (Exception exc) {
			System.err.println("MemoriaPanel.modelPropertyChange("+evt.toString()+") Exception"+exc.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void init() {
		try {
			this.chosenDate = new Date(0L);
			this.entitySourceName = "";
			this.sceneIds = new ArrayList();
			this.involvedTags = new HashSet();
			this.scaler = new CrossoverScalingControl();
			try {
				Internal internal = BookUtil.get(this.mainFrame, SbConstants.BookKey.MEMORIA_BALLOON, Boolean.valueOf(true));
				this.showBalloonLayout = internal.getBooleanValue().booleanValue();
			} catch (Exception exc) {
				this.showBalloonLayout = true;
			}
		} catch (Exception exc2) {
			SbApp.error("MemoriaPanel.init()",exc2);
		}
	}

	@Override
	public void initUi() {
		try {
			MigLayout migLayout1 = new MigLayout("wrap,fill", "[]", "[][grow]");
			setLayout(migLayout1);
			setBackground(SwingUtil.getBackgroundColor());
			this.controlPanel = new JPanel();
			MigLayout migLayout2 = new MigLayout("flowx", "", "");
			this.controlPanel.setLayout(migLayout2);
			this.controlPanel.setOpaque(false);
			refreshControlPanel();
			initGraph();
			add(this.controlPanel, "alignx center");
			add(this.graphPanel, "grow");
		} catch (Exception exc) {
			SbApp.error("MemoriaPanel.modelPropertyChange()",exc);
		}
	}

	@SuppressWarnings("unchecked")
	private void refreshEntityCombo(EntityTypeCbItem.Type type) {
		BookModel model = this.mainFrame.getBookModel();
		Session session = model.beginTransaction();
		Object object;
		List list;
		if (type == EntityTypeCbItem.Type.SCENE) {
			object = new SceneDAOImpl(session);
			list = ((SceneDAOImpl) object).findAll();
			refreshCombo(new Scene(), list, false);
			this.datePanel.setVisible(false);
		} else if (type == EntityTypeCbItem.Type.PERSON) {
			object = new PersonDAOImpl(session);
			list = ((PersonDAOImpl) object).findAll();
			Person person = new Person();
			ArrayList array = new ArrayList();
			array.add(new Attribute("fd", "fds"));
			person.setAttributes(array);
			refreshCombo(person, list, false);
			this.datePanel.setVisible(true);
		} else if (type == EntityTypeCbItem.Type.LOCATION) {
			object = new LocationDAOImpl(session);
			list = ((LocationDAOImpl) object).findAll();
			refreshCombo(new Location(), list, false);
			this.datePanel.setVisible(true);
		} else if (type == EntityTypeCbItem.Type.TAG) {
			object = new TagDAOImpl(session);
			list = ((TagDAOImpl) object).findAll();
			refreshCombo(new Tag(), list, false);
			this.datePanel.setVisible(true);
		} else if (type == EntityTypeCbItem.Type.ITEM) {
			object = new ItemDAOImpl(session);
			list = ((ItemDAOImpl) object).findAll();
			refreshCombo(new Item(), list, false);
			this.datePanel.setVisible(true);
		}
		model.commit();
	}

	@SuppressWarnings("unchecked")
	private void refreshControlPanel() {
		BookModel model = this.mainFrame.getBookModel();
		Session session = model.beginTransaction();
		SceneDAOImpl dao = new SceneDAOImpl(session);
		List scenes = dao.findAll();
		List dates = dao.findDistinctDates();
		dates.removeAll(Collections.singletonList(null));
		model.commit();
		Object object = null;
		if (this.entityTypeCombo != null) {
			object = this.entityTypeCombo.getSelectedItem();
		}
		Object object2 = null;
		if (this.entityCombo != null) {
			object2 = this.entityCombo.getSelectedItem();
		}
		Object object3 = null;
		if (this.dateCombo != null) {
			object3 = this.dateCombo.getSelectedItem();
		}
		this.dateCombo = new JComboBox();
		this.dateCombo.setPreferredSize(new Dimension(100, 20));
		Object object4 = dates.iterator();
		while (((Iterator) object4).hasNext()) {
			this.dateCombo.addItem((Date) ((Iterator) object4).next());
		}
		this.dateCombo.setName(SbConstants.ComponentName.COMBO_DATES.toString());
		this.dateCombo.setMaximumRowCount(15);
		if (object3 != null) {
			this.dateCombo.setSelectedItem(object3);
		}
		this.datePanel = new JPanel(new MigLayout("flowx,ins 0"));
		this.datePanel.setOpaque(false);
		this.datePanel.setVisible(false);
		object4 = new IconButton("icon.small.first");
		((IconButton) object4).setSize32x20();
		((IconButton) object4).setName(SbConstants.ComponentName.BT_FIRST.toString());
		((IconButton) object4).addActionListener(this);
		Object object5 = new IconButton("icon.small.next");
		((IconButton) object5).setSize32x20();
		((IconButton) object5).setName(SbConstants.ComponentName.BT_NEXT.toString());
		((IconButton) object5).addActionListener(this);
		IconButton iconButton1 = new IconButton("icon.small.previous");
		iconButton1.setSize32x20();
		iconButton1.setName(SbConstants.ComponentName.BT_PREVIOUS.toString());
		iconButton1.addActionListener(this);
		IconButton iconButton2 = new IconButton("icon.small.last");
		iconButton2.setSize32x20();
		iconButton2.setName(SbConstants.ComponentName.BT_LAST.toString());
		iconButton2.addActionListener(this);
		this.datePanel.add(this.dateCombo);
		this.datePanel.add((Component) object4);
		this.datePanel.add(iconButton1);
		this.datePanel.add((Component) object5);
		this.datePanel.add(iconButton2);
		this.entityTypeCombo = new JComboBox();
		this.entityTypeCombo.setPreferredSize(new Dimension(120, 20));
		this.entityTypeCombo.setName(SbConstants.ComponentName.COMBO_ENTITY_TYPES.toString());
		this.entityTypeCombo.setRenderer(new EntityTypeListCellRenderer());
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.SCENE));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.PERSON));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.LOCATION));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.TAG));
		this.entityTypeCombo.addItem(new EntityTypeCbItem(EntityTypeCbItem.Type.ITEM));
		if (object != null) {
			this.entityTypeCombo.setSelectedItem(object);
		}
		this.entityCombo = new JComboBox();
		this.entityCombo.setName(SbConstants.ComponentName.COMBO_ENTITIES.toString());
		this.entityCombo.setMaximumRowCount(15);
		if (object2 != null) {
			EntityTypeCbItem cbbItem = (EntityTypeCbItem) object;
			refreshEntityCombo(cbbItem.getType());
			this.entityCombo.setSelectedItem(object2);
		} else {
			refreshCombo(new Scene(), scenes, false);
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

	@SuppressWarnings("unchecked")
	private void makeLayoutTransition() {
		if (this.vv == null) {
			return;
		}
		LayoutTransition layout;
		if (this.showBalloonLayout) {
			layout = new LayoutTransition(this.vv, this.treeLayout, this.balloonLayout);
		} else {
			layout = new LayoutTransition(this.vv, this.balloonLayout, this.treeLayout);
		}
		Animator animator = new Animator(layout);
		animator.start();
		this.vv.repaint();
	}

	@SuppressWarnings("unchecked")
	private void clearGraph() {
		try {
			if (this.graph == null) {
				this.graph = new DelegateForest();
				return;
			}
			Collection collection = this.graph.getRoots();
			Iterator iCollection = collection.iterator();
			while (iCollection.hasNext()) {
				AbstractEntity entity = (AbstractEntity) iCollection.next();
				if (entity != null) {
					this.graph.removeVertex(entity);
				}
			}
		} catch (Exception exc) {
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
			Internal internal = BookUtil.get(this.mainFrame, SbConstants.BookKey.EXPORT_DIRECTORY, EnvUtil.getDefaultExportDir(this.mainFrame));
			File file1 = new File(internal.getStringValue());
			JFileChooser chooser = new JFileChooser(file1);
			chooser.setFileFilter(new PngFileFilter());
			chooser.setApproveButtonText(I18N.getMsg("msg.common.export"));
			String str = IOUtil.getEntityFileNameForExport(this.mainFrame, "Memoria", this.shownEntity);
			chooser.setSelectedFile(new File(str));
			int i = chooser.showDialog(getThis(), I18N.getMsg("msg.common.export"));
			if (i == 1) {
				return;
			}
			File file2 = chooser.getSelectedFile();
			if (!file2.getName().endsWith(".png")) {
				file2 = new File(file2.getPath() + ".png");
			}
			ScreenImage.createImage(this.graphPanel, file2.toString());
			JOptionPane.showMessageDialog(getThis(), I18N.getMsg("msg.common.export.success") + "\n" + file2.getAbsolutePath(), I18N.getMsg("msg.common.export"), 1);
		} catch (IOException exc) {
			SbApp.error("MemoriaPanel.export()",exc);
		}
	}

	@SuppressWarnings("unchecked")
	private void refreshCombo(AbstractEntity pEntity, List<? extends AbstractEntity> pList, boolean b) {
		try {
			this.processActionListener = false;
			DefaultComboBoxModel combo = (DefaultComboBoxModel) this.entityCombo.getModel();
			combo.removeAllElements();
			combo.addElement(pEntity);
			Iterator iterator = pList.iterator();
			while (iterator.hasNext()) {
				AbstractEntity entity = (AbstractEntity) iterator.next();
				combo.addElement(entity);
			}
			this.processActionListener = true;
		} catch (Exception exc) {
			SbApp.error("MemoriaPanel.refreshCombo("
				+pEntity.toString()+", list"+", "+b +")",exc);
		}
	}

	@SuppressWarnings("unchecked")
	private void initGraph() {
		try {
			labelMap = new HashMap();
			iconMap = new HashMap();
			graph = new DelegateForest();
			treeLayout = new TreeLayout(graph);
			balloonLayout = new BalloonLayout(graph);
			vv = new VisualizationViewer(balloonLayout);
			vv.setSize(new Dimension(800, 800));
			refreshGraph();
			vv.setBackground(Color.white);
			vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
			vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
			vv.setVertexToolTipTransformer(new EntityTransformer());
			graphPanel = new GraphZoomScrollPane(vv);
			DefaultModalGraphMouse mouse = new DefaultModalGraphMouse();
			vv.setGraphMouse(mouse);
			mouse.add(new MemoriaGraphMouse(this));
			// T O D O  MemoriaPanel compile error suppress 2 lines
			//VertexStringerImpl localVertexStringerImpl = new VertexStringerImpl(/*this.labelMap*/);
			//this.vv.getRenderContext().setVertexLabelTransformer(new VertexStringerImpl(/*localVertexStringerImpl*/));
			VertexIconShapeTransformer transformer = new VertexIconShapeTransformer(new EllipseVertexShapeTransformer());
			DefaultVertexIconTransformer iconTransformer = new DefaultVertexIconTransformer();
			transformer.setIconMap(iconMap);
			iconTransformer.setIconMap(iconMap);
			vv.getRenderContext().setVertexShapeTransformer(transformer);
			vv.getRenderContext().setVertexIconTransformer(iconTransformer);
		} catch (Exception exc) {
			SbApp.error("MemoriaPanel.initGraph()",exc);
		}
	}

	private void refreshGraph() {
		refreshGraph(null);
	}

	@SuppressWarnings("unchecked")
	private void refreshGraph(AbstractEntity entity) {
		try {
			clearGraph();
			if (entity == null) {
				entity = (AbstractEntity) this.entityCombo.getItemAt(0);
			}
			if ((!(entity instanceof Scene)) && (this.chosenDate == null)) {
				return;
			}
			if ((entity instanceof Scene)) {
				createSceneGraph();
			} else if ((entity instanceof Person)) {
				createPersonGraph();
			} else if ((entity instanceof Location)) {
				createLocationGraph();
			} else if ((entity instanceof Tag)) {
				createTagGraph();
			} else if ((entity instanceof Item)) {
				createItemGraph();
			}
			this.shownEntity = entity;
			this.treeLayout = new TreeLayout(this.graph);
			this.balloonLayout = new BalloonLayout(this.graph);
			Dimension dimension = this.mainFrame.getSize();
			this.balloonLayout.setSize(new Dimension(dimension.width / 2, dimension.height / 2));
			this.balloonLayout.setGraph(this.graph);
			if (this.showBalloonLayout) {
				this.vv.setGraphLayout(this.balloonLayout);
			} else {
				this.vv.setGraphLayout(this.treeLayout);
			}
			this.vv.repaint();
		} catch (Exception exc) {
			System.err.println("MemoriaPanel.refreshGraph() Exception : "+exc.getMessage());
		}
	}

	private boolean isNothingSelected() {
		return this.entityId <= -1L;
	}

	private void showMessage(String paramString) {
		Graphics2D graphics2D = (Graphics2D) this.vv.getGraphics();
		if (graphics2D == null) {
			return;
		}
		Rectangle rectangle = this.vv.getBounds();
		int i = (int) rectangle.getCenterX();
		int j = (int) rectangle.getCenterY();
		graphics2D.setColor(Color.lightGray);
		graphics2D.fillRect(i - 200, j - 20, 400, 40);
		graphics2D.setColor(Color.black);
		graphics2D.drawString(paramString, i - 180, j + 5);
	}

	private void scaleToLayout(ScalingControl paramScalingControl) {
		Dimension dimension1 = this.vv.getPreferredSize();
		if (this.vv.isShowing()) {
			dimension1 = this.vv.getSize();
		}
		Dimension dimension2 = this.vv.getGraphLayout().getSize();
		if (!dimension1.equals(dimension2)) {
			paramScalingControl.scale(this.vv, (float) (dimension1.getWidth() / dimension2.getWidth()), new Point2D.Double());
		}
	}

	@SuppressWarnings("unchecked")
	private void createSceneGraph() {
		this.graphIndex = 0L;
		BookModel model = this.mainFrame.getBookModel();
		Session session = model.beginTransaction();
		SceneDAOImpl daoScene = new SceneDAOImpl(session);
		Scene scene = (Scene) daoScene.find(Long.valueOf(this.entityId));
		if (scene == null) {
			model.commit();
			return;
		}
		this.graph.addVertex(scene);
		this.labelMap.put(scene, scene.toString());
		this.iconMap.put(scene, this.sceneIconLarge);
		this.sceneVertexTitle = I18N.getMsg("msg.graph.scenes.same.date");
		initVertices(scene);
		HashSet hashSet1 = new HashSet();
		HashSet hashSet2 = new HashSet();
		TagLinkDAOImpl daoTagLink = new TagLinkDAOImpl(session);
		ItemLinkDAOImpl daoItemLink = new ItemLinkDAOImpl(session);
		Date date = null;
		if (!scene.hasNoSceneTs()) {
			date = new Date(scene.getSceneTs().getTime());
		}
		Object object6;
		Object object7;
		Iterator object5;
		Person localObject2;
		if (!scene.hasNoSceneTs()) {
			Iterator<Scene> lo2 = (daoScene.findByDate(date)).iterator();
			while (((Iterator) lo2).hasNext()) {
				Scene localObject3 = (Scene) ((Iterator) lo2).next();
				if ((!((Scene) localObject3).getId().equals(scene.getId())) && (((Scene) localObject3).getStrand().getId() == scene.getStrand().getId())) {
					this.graph.addVertex(localObject3);
					this.labelMap.put(localObject3, ((Scene) localObject3).getFullTitle(true));
					this.iconMap.put(localObject3, this.sceneIconMedium);
					this.graph.addEdge(Long.valueOf(this.graphIndex++), this.sceneVertex, localObject3);
					List<TagLink> localObject4 = daoTagLink.findByScene((Scene) localObject3);
					if (!((List) localObject4).isEmpty()) {
						object5 = ((List) localObject4).iterator();
						while (((Iterator) object5).hasNext()) {
							object6 = (TagLink) ((Iterator) object5).next();
							if (((TagLink) object6).hasOnlyScene()) {
								this.involvedTags.add(((TagLink) object6).getTag());
							}
						}
					}
					List<ItemLink> lo5 = daoItemLink.findByScene((Scene) localObject3);
					if (!((List) lo5).isEmpty()) {
						object6 = ((List) lo5).iterator();
						while (((Iterator) object6).hasNext()) {
							object7 = (ItemLink) ((Iterator) object6).next();
							if (((ItemLink) object7).hasOnlyScene()) {
								this.involvedTags.add(((ItemLink) object7).getItem());
							}
						}
					}
				}
			}
		}
		Object localObject1 = scene.getPersons().iterator();
		while (((Iterator) localObject1).hasNext()) {
			Person person = (Person) ((Iterator) localObject1).next();
			this.graph.addVertex(person);
			this.labelMap.put(person, ((Person) person).toString());
			this.iconMap.put(person, getPersonIcon((Person) person, SbConstants.IconSize.MEDIUM));
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.characterVertex, person);
		}
		localObject1 = scene.getLocations().iterator();
		while (((Iterator) localObject1).hasNext()) {
			Location location = (Location) ((Iterator) localObject1).next();
			this.graph.addVertex(location);
			this.labelMap.put(location, ((Location) location).toString());
			this.iconMap.put(location, this.locationIconMedium);
			this.graph.addEdge(Long.valueOf(this.graphIndex++), this.locationVertex, location);
		}
		localObject1 = daoTagLink.findByScene(scene);
		Object lo22 = ((List) localObject1).iterator();
		while (((Iterator) lo22).hasNext()) {
			TagLink lo33 = (TagLink) ((Iterator) lo22).next();
			if (((TagLink) lo33).hasOnlyScene()) {
				Tag lo44 = ((TagLink) lo33).getTag();
				hashSet1.add(lo44);
			}
		}
		List<ItemLink> lo222 = daoItemLink.findByScene(scene);
		Object localObject3 = ((List) lo222).iterator();
		while (((Iterator) localObject3).hasNext()) {
			ItemLink lo444 = (ItemLink) ((Iterator) localObject3).next();
			if (((ItemLink) lo444).hasOnlyScene()) {
				Item lo55 = ((ItemLink) lo444).getItem();
				hashSet2.add(lo55);
			}
		}
		localObject3 = daoTagLink.findAll();
		Object localObject4 = ((List) localObject3).iterator();
		while (((Iterator) localObject4).hasNext()) {
			TagLink lo55 = (TagLink) ((Iterator) localObject4).next();
			if (((TagLink) lo55).hasPerson()) {
				object6 = ((TagLink) lo55).getPerson();
				if (!scene.getPersons().contains(object6));
			} else if (((TagLink) lo55).hasLocation()) {
				if ((((TagLink) lo55).hasStartScene()) && (!((TagLink) lo55).hasEndScene())) {
					if (((TagLink) lo55).getStartScene().getId() == scene.getId());
				} else {
					object6 = ((TagLink) lo55).getPeriod();
					if ((object6 == null) || (date == null) || (((Period) object6).isInside(date))) {
						if (((TagLink) lo55).hasLocationOrPerson()) {
							object6 = ((TagLink) lo55).getLocation();
							if (!scene.getLocations().contains(object6)) {
								continue;
							}
						}
					}
				}
			} else if (((TagLink) lo55).hasOnlyScene()) {
				if (((TagLink) lo55).hasEndScene()) {
					object6 = ((TagLink) lo55).getStartScene();
					if (((Scene) object6).getStrand().getId() == scene.getStrand().getId()) {
						object7 = ((TagLink) lo55).getPeriod();
						if ((object7 != null) && (date != null) && (!((Period) object7).isInside(date)));
					}
				}
			} else {
				if (((TagLink) lo55).getTag() != null) {
					this.involvedTags.add(((TagLink) lo55).getTag());
				}
			}
		}
		localObject4 = daoItemLink.findAll();
		Object lo50 = ((List) localObject4).iterator();
		while (((Iterator) lo50).hasNext()) {
			object6 = (ItemLink) ((Iterator) lo50).next();
			if (((ItemLink) object6).hasPerson()) {
				Person lo17 = ((ItemLink) object6).getPerson();
				if (!scene.getPersons().contains(lo17));
			} else if (((ItemLink) object6).hasLocation()) {
				if ((((ItemLink) object6).hasStartScene()) && (!((ItemLink) object6).hasEndScene())) {
					if (((ItemLink) object6).getStartScene().getId() == scene.getId());
				} else {
					Period lo27 = ((ItemLink) object6).getPeriod();
					if ((lo27 == null) || (date == null) || (((Period) lo27).isInside(date))) {
						if (((ItemLink) object6).hasLocationOrPerson()) {
							Location lo37 = ((ItemLink) object6).getLocation();
							if (!scene.getLocations().contains(lo37)) {
								continue;
							}
						}
					}
				}
			} else if (((ItemLink) object6).hasOnlyScene()) {
				if (((ItemLink) object6).hasEndScene()) {
					Scene lo37 = ((ItemLink) object6).getStartScene();
					if (((Scene) lo37).getStrand().getId() == scene.getStrand().getId()) {
						Period localPeriod = ((ItemLink) object6).getPeriod();
						if ((localPeriod != null) && (date != null) && (!localPeriod.isInside(date)));
					}
				}
			} else {
				Item lo47 = ((ItemLink) object6).getItem();
				if (lo47 != null) {
					this.involvedTags.add(lo47);
				}
			}
		}
		removeDoublesFromInvolvedTags(hashSet1, hashSet2);
		object5 = hashSet1.iterator();
		while (((Iterator) object5).hasNext()) {
			Tag lo16 = (Tag) ((Iterator) object5).next();
			if (lo16 != null) {
				this.graph.addVertex(lo16);
				this.labelMap.put(lo16, ((Tag) lo16).toString());
				this.iconMap.put(lo16, this.tagIconMedium);
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo16);
			}
		}
		object5 = hashSet2.iterator();
		while (((Iterator) object5).hasNext()) {
			Item lo26 = (Item) ((Iterator) object5).next();
			if (lo26 != null) {
				this.graph.addVertex(lo26);
				this.labelMap.put(lo26, ((Item) lo26).toString());
				this.iconMap.put(lo26, this.itemIconMedium);
				this.graph.addEdge(Long.valueOf(this.graphIndex++), this.tagVertex, lo26);
			}
		}
		addToVertexInvolvedTags();
		model.commit();
	}

	@SuppressWarnings("unchecked")
	private void removeDoublesFromInvolvedTags(Set<Tag> paramSet, Set<Item> paramSet1) {
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator1 = this.involvedTags.iterator();
		AbstractTag tag;
		while (localIterator1.hasNext()) {
			tag = (AbstractTag) localIterator1.next();
			Iterator localIterator2 = paramSet.iterator();
			Object localObject;
			while (localIterator2.hasNext()) {
				localObject = (Tag) localIterator2.next();
				if (((Tag) localObject).getId().equals(tag.getId())) {
					localArrayList.add(tag);
				}
			}
			localIterator2 = paramSet1.iterator();
			while (localIterator2.hasNext()) {
				localObject = (Item) localIterator2.next();
				if (((Item) localObject).getId().equals(tag.getId())) {
					localArrayList.add(tag);
				}
			}
		}
		localIterator1 = localArrayList.iterator();
		while (localIterator1.hasNext()) {
			tag = (AbstractTag) localIterator1.next();
			this.involvedTags.remove(tag);
		}
	}

	@SuppressWarnings("unchecked")
	private void createTagGraph() {
		BookModel model = this.mainFrame.getBookModel();
		Session session = model.beginTransaction();
		TagDAOImpl localTagDAOImpl = new TagDAOImpl(session);
		Tag tag = (Tag) localTagDAOImpl.find(Long.valueOf(this.entityId));
		if (tag == null) {
			model.commit();
			return;
		}
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(session);
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(session);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(session);
		this.graphIndex = 0L;
		this.graph.addVertex(tag);
		this.labelMap.put(tag, tag.toString());
		this.iconMap.put(tag, this.tagIconLarge);
		this.showTagVertex = false;
		initVertices(tag);
		HashSet localHashSet1 = new HashSet();
		HashSet localHashSet2 = new HashSet();
		HashSet localHashSet3 = new HashSet();
		List localList = localTagLinkDAOImpl.findByTag(tag);
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
							if (((TagLink) localObject3).getTag().getId() != tag.getId()) {
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
							if (((TagLink) localObject3).getTag().getId() != tag.getId()) {
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
										if ((tag.getId() != ((Tag) lo18).getId()) && (!isTagInGraph((Tag) lo18))) {
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
		model.commit();
		addToVertexScenes(localHashSet3);
		addToVertexPersons(localHashSet2);
		addToVertexLocations(localHashSet1);
		addToVertexInvolvedTags();
	}

	@SuppressWarnings("unchecked")
	private void createItemGraph() {
		BookModel model = this.mainFrame.getBookModel();
		Session session = model.beginTransaction();
		ItemDAOImpl localItemDAOImpl = new ItemDAOImpl(session);
		Item localItem = (Item) localItemDAOImpl.find(Long.valueOf(this.entityId));
		if (localItem == null) {
			model.commit();
			return;
		}
		SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(session);
		TagLinkDAOImpl localTagLinkDAOImpl = new TagLinkDAOImpl(session);
		ItemLinkDAOImpl localItemLinkDAOImpl = new ItemLinkDAOImpl(session);
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
		model.commit();
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
			BookModel localDocumentModel = this.mainFrame.getBookModel();
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
		BookModel localDocumentModel = this.mainFrame.getBookModel();
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

	@SuppressWarnings("unchecked")
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

	public void refresh(AbstractEntity entity) {
		try {
			if (entity == null) {
				return;
			}
			this.entityId = entity.getId().longValue();
			refreshGraph(entity);
			updateControlPanel(entity);
		} catch (Exception exc) {
			System.err.println("MemoriaPanel.refresh("+entity+") Exception : "+exc.getMessage());
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

	@Override
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
				if (i < 0) return;
			} else if (SbConstants.ComponentName.BT_NEXT.check((String) localObject2)) {
				i++;
				if (i > this.dateCombo.getItemCount() - 1) return;
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

	@Override
	public void refresh() {
		refreshControlPanel();
		refreshGraph();
	}

	@Override
	public MainFrame getMainFrame() {
		return this.mainFrame;
	}

	@SuppressWarnings("unchecked")
	class VertexStringerImpl<V> implements Transformer<V, String> {

		Map<V, String> map = new HashMap();
		boolean enabled = true;

		public VertexStringerImpl() {
			Object localObject = null;
			this.map = (Map<V, String>) localObject;
		}

		@Override
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