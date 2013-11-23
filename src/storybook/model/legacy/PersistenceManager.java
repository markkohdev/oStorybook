/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

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
package storybook.model.legacy;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import storybook.SbConstants;
import storybook.model.DbFile;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.MainFrame;
import storybook.view.dialog.ExceptionDialog;

@Deprecated
public class PersistenceManager {

	private static Logger log = Logger.getLogger(PersistenceManager.class);
	private static PersistenceManager thePersistenceManager;

    private String databaseName;
    private boolean init;
    private boolean openOnlyIfExists;
    private Connection connection;
    private File file;
    private Statement stmt;
    private MainFrame mainFrame;
    private String oldDbVersion;
    private String newDbVersion;

    private PersistenceManager() {
        // make the constructor private
        init = false;
        connection = null;
        databaseName = null;
    }

    public void open(DbFile dbFile) {
    	this.file = dbFile.getFile();
        this.databaseName = dbFile.getDbName();
        this.openOnlyIfExists = true;
        this.init = true;
        try {
            getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("open db, databaseName=" + this.databaseName);
    }

//	public static String getDatabaseName(File file) {
//		String path = FilenameUtils.getFullPath(file.getPath());
//		String baseName = FilenameUtils.getBaseName(file.getName());
//		int i = baseName.lastIndexOf(".");
//		if (i == -1) {
//			return path + baseName;
//		}
//		String dbName = baseName.substring(0, i);
//		return path + dbName;
//	}

    public static PersistenceManager getInstance() {
        if (thePersistenceManager == null) {
            thePersistenceManager = new PersistenceManager();
        }
        return thePersistenceManager;
    }

    public Connection getConnection() {
        if (!init) {
            return null;
        }
        if (connection == null) {
            String connectionStr = "jdbc:h2:" + databaseName;
            if (openOnlyIfExists) {
                connectionStr = connectionStr + ";IFEXISTS=TRUE";
            }
            log.info("connect to: " + connectionStr);
            try {
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(
                        connectionStr, "sa", "");
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (!isConnectionOpen()) {
            return;
        }
        try {
            this.connection.close();
            this.init = false;
            this.connection = null;
            this.databaseName = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDatabaseName() {
        return databaseName;
    }

	public File getFile() {
		return file;
	}

    public boolean isConnectionOpen() {
        return connection != null;
    }

    /**
     * Closes the result set
     *
     * @param result The ResultSet that needs to close
     */
    public void closeResultSet(ResultSet result) {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException se) {
            log.error("### PersistenceManager, SQLException, closing result set", se);
        }
    }

    /**
     * Closes the prepare statement
     *
     * @param prepare The PreparedStatement that needs to close
     */
    public void closePrepareStatement(PreparedStatement prepare) {
        try {
            if (prepare != null) {
                prepare.close();
            }
        } catch (SQLException se) {
            log.error("### PersistenceManager, SQLException, closing prepare statement", se);
        }
    }

    /**
     * Closes the statement
     *
     * @param stmt The Statement that needs to close
     */
    public void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {
            log.error("### PersistenceManager, SQLException, closing prepare statement", se);
        }
    }

    public int getGeneratedId(PreparedStatement stmt) throws SQLException {
        int retour = -1;
        ResultSet rs = null;
        try {
            rs = stmt.getGeneratedKeys();
            int count = 0;
            while (rs.next()) {
                if (count > 0) {
                    throw new SQLException("error: got more than one id");
                }
                retour = rs.getInt(1);
                ++count;
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        } finally {
            this.closeResultSet(rs);
        }
        return retour;
    }

	/* SB5 suppress checking old version before 4.0
	public boolean checkAndAlterModel() throws Exception {
		oldDbVersion = InternalPeer.getDbModelVersion();
		if (oldDbVersion == null) {
			return true;
		}
		newDbVersion = SbConstants.Storybook.DB_VERSION.toString();

		if (oldDbVersion.equals(newDbVersion)) {
			// model matches, nothing to do
			return true;
		}

        // alter models
		stmt = PersistenceManager.getInstance().getConnection().createStatement();
        // old versions
		if (oldDbVersion.equals("0") || oldDbVersion.equals("0.1")
				|| oldDbVersion.equals("0.1") || oldDbVersion.equals("0.2")
				|| oldDbVersion.equals("0.3") || oldDbVersion.equals("0.4")
				|| oldDbVersion.equals("0.5") || oldDbVersion.equals("0.6")
				|| oldDbVersion.equals("0.7") || oldDbVersion.equals("0.8")
				|| oldDbVersion.equals("0.9") || oldDbVersion.equals("1.0")
				|| oldDbVersion.equals("1.1") || oldDbVersion.equals("1.2")
				|| oldDbVersion.equals("1.3") || oldDbVersion.equals("1.4")) {
			throw new Exception(
					"File version too old. Update to the latest version of Storybook 3 first.");
		}

		// UpdateDialog dlg = new UpdateDialog();
		// SwingTools.showDialog(dlg, MainFrame.getInstance());

		// backup current file
		String fn = FilenameUtils.removeExtension(file.getAbsolutePath());
		fn = fn + ".bak";
		File backupFile = new File(fn);
		try {
			if (backupFile.exists()) {
				backupFile.delete();
			}
			FileUtils.copyFile(file, backupFile);
		} catch (IOException e1) {
			int n = JOptionPane.showConfirmDialog(
					mainFrame,
					"Cannot make a backup file:\n"
							+ backupFile.getAbsolutePath()
							+ "\nDo you want to continue?", "Backup failed",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
				return false;
			}
		}

		if (oldDbVersion.equals("1.5")) {
			// 1.5 -> 4.0
			alterFrom1_5to4_0();
		}

		return true;

		// dlg.append();
		// dlg.append("File");
		// dlg.append(PersistenceManager.getInstance().getFile().toString());
		// dlg.append("was updated successfully.");
		// dlg.append();
		// dlg.append("Press 'Close' to continue.");

		// // unknown version
		// JOptionPane.showMessageDialog(
		// MainFrame.getInstance(),
		// I18N.getMsg("msg.error.wrong.version", projectDbVersion),
		// I18N.getMsg("msg.common.error"),
		// JOptionPane.ERROR_MESSAGE);
		return true;
    }
	*/

	/* SB5 checkin old version before 4.0
	private void alterFrom1_5to4_0() throws Exception {
		// dlg.append("Updating file version from 1.4 to 1.5 ...");

		String sql = "";

		// to test an exception
//		sql = "alter table dummy alter column name varchar(256)";
//		executeSQLStatement(sql, stmt);

		// location
		sql = "alter table location alter column name varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table location alter column city varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table location alter column country varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table location alter column address varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table location alter column description varchar(32768)";
		executeSQLStatement(sql, stmt);
		sql = "alter table location alter column notes varchar(32768)";
		executeSQLStatement(sql, stmt);

		// person
		sql = "ALTER TABLE PERSON ALTER COLUMN CATEGORY RENAME TO CATEGORY_ID;";
		executeSQLStatement(sql, stmt);
		sql = "ALTER TABLE PERSON ALTER COLUMN CATEGORY_ID long;";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column firstname varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column lastname varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column abbreviation varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column occupation varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column description varchar(32768)";
		executeSQLStatement(sql, stmt);
		sql = "alter table person alter column notes varchar(32768)";
		executeSQLStatement(sql, stmt);

		// scene
		sql = "update scene set date = null where id in(select id from scene where scene.RELATIVE_SCENE_ID!=-1)";
		executeSQLStatement(sql, stmt);
		sql = "alter table scene alter column title varchar(2048)";
		executeSQLStatement(sql, stmt);
		sql = "alter table scene alter column summary varchar(32768)";
		executeSQLStatement(sql, stmt);
		sql = "alter table scene alter column notes varchar(32768)";
		executeSQLStatement(sql, stmt);
		sql = "ALTER TABLE SCENE ALTER COLUMN RELATIVE_SCENE_ID long;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE SCENE SET chapter_id=NULL WHERE chapter_id=-1;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE SCENE SET RELATIVE_SCENE_ID=NULL WHERE RELATIVE_SCENE_ID=-1;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE SCENE SET RELATIVE_DATE_DIFFERENCE=NULL WHERE RELATIVE_DATE_DIFFERENCE=0;";
		executeSQLStatement(sql, stmt);
		sql = "ALTER TABLE SCENE ALTER COLUMN DATE RENAME TO SCENE_TS;";
		executeSQLStatement(sql, stmt);
		sql = "ALTER TABLE SCENE ALTER COLUMN SCENE_TS timestamp;";
		executeSQLStatement(sql, stmt);

		// chapter
		sql = "alter table chapter alter column title varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table chapter alter column description varchar(32768)";
		executeSQLStatement(sql, stmt);
		sql = "alter table chapter alter column notes varchar(32768)";
		executeSQLStatement(sql, stmt);

		// gender
		sql = "alter table gender alter column name varchar(256)";
		executeSQLStatement(sql, stmt);

		// idea
		sql = "alter table ideas alter column category varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table ideas alter column note varchar(32768)";
		executeSQLStatement(sql, stmt);

		// part
		sql = "alter table part alter column name varchar(256)";
		executeSQLStatement(sql, stmt);

		// strand
		sql = "alter table strand alter column name varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table strand alter column abbreviation varchar(256)";
		executeSQLStatement(sql, stmt);
		sql = "alter table strand alter column notes varchar(32768)";
		executeSQLStatement(sql, stmt);

		// tag link
		sql = "UPDATE TAG_LINK SET START_SCENE_ID=NULL WHERE START_SCENE_ID=0;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET START_SCENE_ID=NULL WHERE START_SCENE_ID=-1;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET END_SCENE_ID=NULL WHERE END_SCENE_ID=0;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET END_SCENE_ID=NULL WHERE END_SCENE_ID=-1;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET CHARACTER_ID=NULL WHERE CHARACTER_ID=0;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET CHARACTER_ID=NULL WHERE CHARACTER_ID=-1;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET LOCATION_ID=NULL WHERE LOCATION_ID=0;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK SET LOCATION_ID=NULL WHERE LOCATION_ID=-1;";
		executeSQLStatement(sql, stmt);
		sql = "ALTER TABLE TAG_LINK ADD TYPE integer;";
		executeSQLStatement(sql, stmt);
		sql = "UPDATE TAG_LINK AS TL SET TYPE=(SELECT T.TYPE FROM TAG AS T WHERE T.ID=TL.TAG_ID);";
		executeSQLStatement(sql, stmt);

		// category
		sql = "CREATE TABLE CATEGORY(ID bigint PRIMARY KEY NOT NULL,SORT integer,NAME varchar(256));";
		executeSQLStatement(sql, stmt);
		sql = "INSERT INTO CATEGORY (ID,SORT,NAME) VALUES (1, 1, 'major');";
		executeSQLStatement(sql, stmt);
		sql = "INSERT INTO CATEGORY (ID,SORT,NAME) VALUES (2, 2, 'minor');";
		executeSQLStatement(sql, stmt);

		// internal
		sql = "alter table internal alter column key varchar(512)";
		executeSQLStatement(sql, stmt);
		sql = "alter table internal alter column string_value varchar(4096)";
		executeSQLStatement(sql, stmt);

		InternalPeer.setDbModelVersion(SbConstants.Storybook.DB_VERSION
				.toString());
	}
	*/

	/* SB5 checkin old version before 4.0
	private void executeSQLStatement(String sql, Statement stmt) {
		try {
			log.debug(sql);
			stmt.execute(sql);
		} catch (SQLException e) {
			log.error(e);
			ExceptionDialog dlg = new ExceptionDialog(e);
			SwingUtil.showModalDialog(dlg, mainFrame);
		}
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public String getOldDbVersion() {
		return oldDbVersion;
	}

	public String getNewDbVersion() {
		return newDbVersion;
	}

	public boolean hasAlteredDbModel() {
		if (oldDbVersion == null) {
			return false;
		}
		return !oldDbVersion.equals(newDbVersion);
	}
	*/
}
