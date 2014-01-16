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

package storybook.model.hbn;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import storybook.model.hbn.entity.AbstractEntity;

import com.googlecode.genericdao.dao.hibernate.GenericDAOImpl;
import java.util.logging.Level;
import org.hibernate.HibernateException;
import storybook.StorybookApp;

public class SbSessionFactory {

	private boolean trace=false;

	private static final Log log = LogFactory.getLog(SbSessionFactory.class);

	private SessionFactory sessionFactory;

	public SbSessionFactory() {
	}

	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			log.fatal("Call init() first.");
		}
		return sessionFactory;
	}

	public Session getSession() {
		if (sessionFactory == null) {
			log.fatal("Call init() first.");
		}
		return sessionFactory.getCurrentSession();
	}

	public void init(String filename, String configFile) {
		if (StorybookApp.getTrace()) {
			System.out.println("SbSessionFactory.init()");
		}
		if (!StorybookApp.getTraceHibernate()) {
			java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
		}
		try {
			// create the SessionFactory from given config file
			// modif favdb remplacement du configFile par la programmation directe
			//System.out.println("filename="+filename);
			//System.out.println("configFile="+configFile);
			Configuration config = new Configuration()/*.configure(configFile)*/;
			config.setProperty("hibernate.show_sql", "false");
			config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
			config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
			config.setProperty("hibernate.connection.url", "jdbc:h2:" + filename);
			config.setProperty("hibernate.connection.username", "sa");
			config.setProperty("hibernate.connection.password", "");
			config.setProperty("hibernate.hbm2ddl.auto", "update");
			config.setProperty("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
			config.setProperty("hibernate.c3p0.min_size", "0");
			config.setProperty("hibernate.c3p0.max_size", "1");
			config.setProperty("hibernate.c3p0.timeout", "5000");
			config.setProperty("hibernate.c3p0.max_statements", "100");
			config.setProperty("hibernate.c3p0.idle_test_period", "300");
			config.setProperty("hibernate.c3p0.acquire_increment", "2");
			config.setProperty("current_session_context_class", "thread");
			config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
			config.setProperty("hibernate.current_session_context_class", "thread");
			if (configFile.contains("preference")) {
				config.addClass(storybook.model.hbn.entity.Preference.class);
			} else {
				config.addClass(storybook.model.hbn.entity.Part.class);
				config.addClass(storybook.model.hbn.entity.Chapter.class);
				config.addClass(storybook.model.hbn.entity.Scene.class);
				config.addClass(storybook.model.hbn.entity.Gender.class);
				config.addClass(storybook.model.hbn.entity.Person.class);
				config.addClass(storybook.model.hbn.entity.Location.class);
				config.addClass(storybook.model.hbn.entity.Strand.class);
				config.addClass(storybook.model.hbn.entity.AbstractTag.class);
				config.addClass(storybook.model.hbn.entity.AbstractTagLink.class);
				config.addClass(storybook.model.hbn.entity.Idea.class);
				config.addClass(storybook.model.hbn.entity.Internal.class);
				config.addClass(storybook.model.hbn.entity.Category.class);
				config.addClass(storybook.model.hbn.entity.Attribute.class);
			}
			sessionFactory = config.buildSessionFactory();
		} catch (SecurityException | HibernateException ex) {
			// make sure you log the exception, as it might be swallowed
			System.err.println("SbSessionFactory.init()");
			System.err.println("*** Initial SessionFactory creation failed: ");
			System.err.println("*** msg: " + ex.getMessage());
			throw new ExceptionInInitializerError(ex);
		}
	}

	public void query(GenericDAOImpl<? extends AbstractEntity, ?> dao) {
		if (trace) {
			System.out.println("SbSessionFactory.query(): "
				+ dao.getClass().getSimpleName());
			List<? extends AbstractEntity> entities = dao.findAll();
			for (AbstractEntity entity : entities) {
				String name = entity.getClass().getSimpleName();
				//System.out.println("  " + name + ": " + entity.toString());
			}
		}
	}
}
