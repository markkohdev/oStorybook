package storybook.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.hibernate.Session;
import storybook.model.hbn.SbSessionFactory;

public abstract class AbstractModel {

	protected PropertyChangeSupport propertyChangeSupport;

	protected SbSessionFactory sessionFactory;

	public AbstractModel() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		sessionFactory = new SbSessionFactory();
	}

	public abstract void fireAgain();


	public void initSession(String dbName, String configFile) {
		sessionFactory.init(dbName, configFile);
	}

	public void initDefault() {
		fireAgain();
	}

	public Session beginTransaction() {
		Session session = sessionFactory.getSession();
		session.beginTransaction();
		return session;
	}

	public Session getSession() {
		return sessionFactory.getSession();
	}

	public void commit() {
		Session session = sessionFactory.getSession();
		session.getTransaction().commit();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}

	public SbSessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
