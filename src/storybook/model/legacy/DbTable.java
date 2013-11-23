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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

@Deprecated
@SuppressWarnings("serial")
public abstract class DbTable implements Serializable {

    private static Logger logger = Logger.getLogger(DbTable.class);

    private static int volatileId = -100;
    boolean isVolatile = false;

    protected String tableName;
    protected int id = -1;
    protected int realId = -1;
    protected boolean isNew;
    protected boolean toStringUsedForList;

    public DbTable(String tableName) {
        this.tableName = tableName;
    }

	public DbTable(String tableName, boolean isVolatile) {
		this(tableName);
		this.isVolatile = isVolatile;
		this.id = DbTable.volatileId--;
	}

    public abstract boolean save() throws Exception;

    public abstract String getLabelText();

    public void setToStringUsedForList(boolean toStringUsedForList) {
        this.toStringUsedForList = toStringUsedForList;
    }

    public boolean isToStringUsedForList() {
        return toStringUsedForList;
    }

    public boolean isVolatile() {
    	return this.isVolatile;
    }

	public boolean isClone() {
		return id <= -1000;
	}

	public int getRealId() {
		return realId;
	}

    public void markAsExpired() {
        id = -1;
    }

	public boolean isMarkedAsExpired() {
		return (id == -1);
	}

    public int getId() {
        return id;
    }

    public boolean isNew() {
        return isNew;
    }

    public String getTablename() {
        return tableName;
    }

    public boolean changeId(int newId) {
        PreparedStatement stmt = null;
        boolean retour = false;
        try {
            if (newId == getId()) {
                retour = true;
            } else {
                int oldId = getId();
                this.id = newId;
                String sql = "update " + getTablename()
                        + " set id = ? where id = ?";
                stmt = PersistenceManager.getInstance().getConnection().prepareStatement(sql);
                stmt.setInt(1, newId);
                stmt.setInt(2, oldId);
                if (stmt.executeUpdate() != 1) {
                    this.id = oldId;
                    throw new SQLException("update failed, newId: " + newId);
                }
                logger.debug("ID manually changed: oldId=" + oldId + ", newId="
                        + getId() + " " + this);
                retour = true;
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage());
        } finally {
            PersistenceManager.getInstance().closePrepareStatement(stmt);
        }
        return retour;
    }

    @Override
    public String toString() {
        return "" + getId();
    }

    @Override
	public boolean equals(Object obj) {
		if (this == null || obj == null) {
			return false;
		}
		try {
			return this.getId() == ((DbTable) obj).getId();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}

    @Override
	public int hashCode() {
		// int hash = 1;
		// hash = hash * 31 + getTablename().hashCode() + getId();
		// hash = hash * 31 + new Integer(getId()).hashCode();

		int hash = 1;
		hash = hash * 31 + getTablename().hashCode();
		hash = hash * 31 + new Integer(getId()).hashCode();
		return hash;
	}
}
