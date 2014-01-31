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
package storybook.controller;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JMenuBar;

import storybook.StorybookApp;
import storybook.model.AbstractModel;
import storybook.model.EntityUtil;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.ui.panel.AbstractPanel;

public abstract class AbstractController implements PropertyChangeListener {

	private final List<Component> attachedViews;
	private final ArrayList<AbstractModel> attachedModels;

	public AbstractController() {
		// attachedViews = new ArrayList<AbstractPanel>();

		attachedViews = new CopyOnWriteArrayList<>();
		attachedModels = new ArrayList<>();
	}

	public void attachModel(AbstractModel model) {
		attachedModels.add(model);
		model.addPropertyChangeListener(this);
		printAttachedModels();
	}

	public void detachModel(AbstractModel model) {
		attachedModels.remove(model);
		model.removePropertyChangeListener(this);
		printAttachedModels();
	}

	public void attachView(Component view) {
		StorybookApp.trace("AbstractController.attachView(" + view.getName()+")");
		synchronized (attachedViews) {
			attachedViews.add(view);
		}
		if (StorybookApp.getTrace()) {
			printNumberOfAttachedViews();
			printAttachedViews();
		}
	}

	public void detachView(Component view) {
		synchronized (attachedViews) {
			attachedViews.remove(view);
		}
		printAttachedViews();
	}

	public void printNumberOfAttachedViews() {
		StorybookApp.trace("AbstractController.printNumberOfAttachedViews():"
				+ " attached views size: " + attachedViews.size());
	}

	public void printAttachedViews() {
		StorybookApp.trace("AbstractController.printAttachedViews()");
	}

	public String getInfoAttachedViews() {
		return getInfoAttachedViews(false);
	}

	public String getInfoAttachedViews(boolean html) {
		StringBuilder buf = new StringBuilder();
		int i = 0;
		int size = attachedViews.size();
		for (Component view : attachedViews) {
			buf.append("attached view ")
				.append(i).append("/")
				.append(size).append(": ")
				.append(view.getClass().getSimpleName());
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
		if (StorybookApp.getTrace()) {
			System.out.println("AbstractController.printAttachedModels(): ");
			for (AbstractModel model : attachedModels) {
				System.out.println("AbstractController.printAttachedModels():" + " model:" + model);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		synchronized (attachedViews) {
			// must be in synchronized block
			for (Component comp : attachedViews) {
				if (comp instanceof AbstractPanel) {
					((AbstractPanel) comp).modelPropertyChange(evt);
					continue;
				}
				if (comp instanceof JMenuBar) {
					JMenuBar mb = (JMenuBar) comp;
					PropertyChangeListener[] pcls = mb.getPropertyChangeListeners();
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
	}

	public synchronized void fireAgain() {
		for (AbstractModel model : attachedModels) {
			model.fireAgain();
		}
	}

	protected synchronized void setModelProperty(String propertyName, Object newValue) {
		StorybookApp.trace("AbstractControler.setModelProperty(" + propertyName.toString() + "," + newValue.toString() + ")");
		for (AbstractModel model : attachedModels) {
			Method method = null;
			Class<?>[] classes = null;
			try {
				if (newValue instanceof AbstractEntity) {
					classes = new Class[]{EntityUtil.getEntityClass((AbstractEntity) newValue)};
				} else if (newValue != null) {
					classes = new Class[]{newValue.getClass()};
				}
				method = model.getClass().getMethod("set" + propertyName, classes);
				if (newValue != null) {
					method.invoke(model, newValue);
				} else {
					method.invoke(model);
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
				System.err
					.println("ERR AbstractController.setModelProperty() Exception: "+e.getMessage()
					+ "method:" + method + "\n"
					+ "classes:" + classes);
			}
		}
	}

	protected synchronized void setModelProperty(String propertyName) {
		StorybookApp.trace("AbstractController.setModelProperty(" + propertyName.toString() + ")");
		for (AbstractModel model : attachedModels) {
			try {
				Method method = model.getClass().getMethod("set" + propertyName);
				method.invoke(model);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException ex) {
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
