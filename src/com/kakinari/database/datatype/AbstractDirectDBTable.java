package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.kakinari.database.dao.AbstractDirectTableAccess;
import com.kakinari.core.datatype.BaseUnit;
import com.kakinari.core.datatype.ListUnit;
import com.kakinari.core.datatype.interfaces.Param;

public abstract class AbstractDirectDBTable<T extends BaseUnit<?>> extends AbstractDBTable<T> {
	protected abstract String getDriverName();
	protected abstract String getConnectURL();
	protected abstract Properties getProperties();
	protected String getConnectionName() {return null;}	// this function is not used in this class
	
	public AbstractDirectDBTable() {
		super();
	}

	public AbstractDirectDBTable(Param param) {
		super(param);
	}

	public AbstractDirectDBTable(String[] condition) {
		super(condition);
	}

	public AbstractDirectDBTable(Param param, String[] condition) {
		super(param, condition);
	}

	public AbstractDirectDBTable(ListUnit<T> list) {
		super(list);
	}
	
	protected void execute(String[] condition) {
		new AbstractDirectTableAccess(getDriverName(),getConnectURL(), getProperties()) {
			@Override
			protected Object getAccessResult(ResultSet rs) throws SQLException {
				while(rs.next())
					add(getRowObject(rs));
				return this;
			}
		}.execute(getQueryString(condition));
	}

	protected  int executeUpdate(String query) {
		if (query == null)
			return -1;
		return new AbstractDirectTableAccess(getDriverName(),getConnectURL(), getProperties()) {
			@Override
			protected Object getAccessResult(ResultSet rs) throws SQLException {
				return this;
			}
		}.executeUpdate(query);
	}

}
