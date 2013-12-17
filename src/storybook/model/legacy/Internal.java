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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import storybook.model.legacy.Constants.ProjectSetting;

@Deprecated
@SuppressWarnings("serial")
public class Internal extends DbTable {

	private static Logger logger = Logger.getLogger(Internal.class);

	public static final String TABLE_NAME = "internal";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_STRING_VALUE = "string_value";
	public static final String COLUMN_INTEGER_VALUE = "integer_value";
	public static final String COLUMN_BOOLEAN_VALUE = "boolean_value";
	public static final String COLUMN_OLD_VALUE = "value";

	private String key = null;
	private String stringValue = null;
	private Integer integerValue = null;
	private Boolean booleanValue = null;

	public Internal() {
		super(TABLE_NAME);
		isNew = true;
	}

	// This method must be packaged private! It is used
	// by {@link InternalPeer} only.
	//
	// @param id
	//            the id
	//
	Internal(int id) {
		super(TABLE_NAME);
		this.id = id;
		isNew = false;
	}

	@Override
	public boolean save() throws Exception {
		try {
			String sql;
			if (isNew) {
				// insert
				sql = "insert into "
					+ TABLE_NAME
					+ "(" + COLUMN_KEY
					+ ", " + COLUMN_STRING_VALUE
					+ ", " + COLUMN_INTEGER_VALUE
					+ ", " + COLUMN_BOOLEAN_VALUE
					+ ") values(?, ?, ?, ?)";
			} else {
				// update
				sql = "update " + TABLE_NAME
					+ " set "
					+ COLUMN_KEY + " = ?, "
					+ COLUMN_STRING_VALUE + " = ?, "
					+ COLUMN_INTEGER_VALUE + " = ?, "
					+ COLUMN_BOOLEAN_VALUE + " = ? "
					+ "where " + COLUMN_ID + " = ?";
			}
			PreparedStatement stmt = PersistenceManager.getInstance()
					.getConnection().prepareStatement(sql);
			// sets for insert & update
			stmt.setString(1, getKey());
			stmt.setString(2, getStringValue() == null ? "" : getStringValue());
			stmt.setInt(3, getIntegerValue() == null ? Integer.MIN_VALUE
					: getIntegerValue());
			stmt.setBoolean(4, getBooleanValue() == null ? false
					: getBooleanValue());
			if (!isNew) {
				// sets for update only
				stmt.setInt(5, getId());
			}
			if (stmt.executeUpdate() != 1) {
				throw new SQLException(isNew ? "insert" : "update" + " failed");
			}
			if (isNew) {
				ResultSet rs = stmt.getGeneratedKeys();
				int count = 0;
				while (rs.next()) {
					if (count > 0) {
						throw new SQLException("error: got more than one id");
					}
					this.id = rs.getInt(1);
					logger.debug("save (insert): " + this);
					++count;
				}
				isNew = false;
			} else {
				logger.debug("save (update): " + this);
			}
			return true;
		} catch (SQLException e) {
			throw e;
		}
	}

	@Override
	public String getLabelText(){
		return toString();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ID=" + getId());
		buf.append(", key=" + getKey());
		if(getStringValue() != null){
			buf.append(", stringValue=" + getStringValue());
		}
		if(getIntegerValue() != null){
			buf.append(", integerValue=" + getIntegerValue());
		}
		if(getBooleanValue() != null){
			buf.append(", booleanValue=" + getBooleanValue());
		}
		return buf.toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setKey(ProjectSetting ps){
		this.key = ps.toString();
	}

	public String getStringValue() {
		return stringValue;
	}

	public ProjectSetting getProjectSetting(){
		return ProjectSetting.valueOfText(stringValue);
	}

	public void setStringValue(ProjectSetting ps) {
		setStringValue(ps.toString());
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Integer getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Integer integerValue) {
		this.integerValue = integerValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}
}
/* */