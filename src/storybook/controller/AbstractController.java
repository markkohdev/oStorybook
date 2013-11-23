package storybook.controller;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JMenuBar;

import storybook.StorybookApp;
import storybook.model.AbstractModel;
import storybook.model.EntityUtil;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.view.AbstractPanel;

public abstract class AbstractController implements PropertyChangeListener {

	private List<Component> attachedViews;
	private ArrayList<AbstractModel> attachedModels;

	public AbstractController() {
		// attachedViews = new ArrayList<AbstractPanel>();

		attachedViews = new CopyOnWriteArrayList<Component>();
		attachedModels = new ArrayList<AbstractModel>();
	}

	public void attachModel(AbstractModel model) {
		attachedModels.add(model);
		model.addPropertyChangeListener(this);
		// printAttachedModels();
	}

	public void detachModel(AbstractModel model) {
		attachedModels.remove(model);
		model.removePropertyChangeListener(this);
		// printAttachedModels();
	}

	public void attachView(Component view) {
		System.out.println("AbstractController.attachView(): view cl:"
				+ view.getClass());
		synchronized (attachedViews) {
			attachedViews.add(view);
		}
		printNumberOfAttachedViews();
		// attachedViews.add(view);
		// printAttachedViews();
	}

	public void detachView(Component view) {
		synchronized (attachedViews) {
			attachedViews.remove(view);
		}
		// attachedViews.remove(view);
		// printAttachedViews();
	}

	public void printNumberOfAttachedViews() {
		System.out
				.println("AbstractController.printNumberOfAttachedViews(): attached views: "
						+ attachedViews.size());
	}

	public void printAttachedViews() {
		System.out.println(getInfoAttachedViews());
	}

	public String getInfoAttachedViews() {
		return getInfoAttachedViews(false);
	}

	public String getInfoAttachedViews(boolean html) {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		int size = attachedViews.size();
		for (Component view : attachedViews) {
			buf.append("attached view " + i + "/" + size + ": "
					+ view.getClass().getSimpleName());
			if (html) {
				buf.append("\n<br>");
			} else {
				buf.append("\n");
			}
			++i;
		}
		return buf.toString();
	}

	public void printAttachedModels() {
		System.out.println("AbstractController.printAttachedModels(): ");
		for (AbstractModel model : attachedModels) {
			System.out
					.println("AbstractController.printAttachedModels(): model:"
							+ model);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		synchronized (attachedViews) {
			// must be in synchronized block
			Iterator<Component> i = attachedViews.iterator();
			while (i.hasNext()) {
				Component comp = i.next();
				if (comp instanceof AbstractPanel) {
					((AbstractPanel) comp).modelPropertyChange(evt);
					continue;
				}
				// if (comp instanceof MainFrame) {
				// ((MainFrame) comp).modelPropertyChange(evt);
				// continue;
				// }
				if (comp instanceof JMenuBar) {
					JMenuBar mb = (JMenuBar) comp;
					PropertyChangeListener[] pcls = mb
							.getPropertyChangeListeners();
					for (PropertyChangeListener pcl : pcls) {
						pcl.propertyChange(evt);
					}
					continue;
				}
				if (comp instanceof StorybookApp) {
					((StorybookApp) comp).modelPropertyChange(evt);
					continue;
				}
			}
		}
		// for (AbstractPanel view : attachedViews) {
		// view.modelPropertyChange(evt);
		// }
	}

	public synchronized void fireAgain() {
		for (AbstractModel model : attachedModels) {
			model.fireAgain();
		}
	}

	protected synchronized void setModelProperty(String propertyName,
			Object newValue) {
		for (AbstractModel model : attachedModels) {
			Method method = null;
			Class<?>[] classes = null;
			try {
				if (newValue instanceof AbstractEntity) {
					classes = new Class[] { EntityUtil
							.getEntityClass((AbstractEntity) newValue) };
				} else if (newValue != null) {
					classes = new Class[] { newValue.getClass() };
				}
				// OLD: method = model.getClass().getMethod("set" +
				// propertyName, new Class[] { newValue.getClass() });
				// doesn't work in any case since hibernate returns
				// "entity.Tag_$$_javassist_2" which is not found as parameter
				// "Tag"
				method = model.getClass().getMethod("set" + propertyName,
						classes);
				if (newValue != null) {
					method.invoke(model, newValue);
				} else {
					method.invoke(model);
				}
			} catch (Exception e) {
				System.err
						.println("AbstractController.setModelProperty(): method:"
								+ method);
				System.err
						.println("AbstractController.setModelProperty(): classes:"
								+ classes);
				e.printStackTrace();
			}
		}
	}

	protected synchronized void setModelProperty(String propertyName) {
		for (AbstractModel model : attachedModels) {
			try {
				Method method = model.getClass()
						.getMethod("set" + propertyName);
				method.invoke(model);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public int getNumberOfAttachedViews() {
		return attachedViews.size();
	}

	public ArrayList<Component> getAttachedViews() {
		return (ArrayList<Component>) attachedViews;
	}

	public ArrayList<AbstractModel> getAttachedModels() {
		return attachedModels;
	}
}
