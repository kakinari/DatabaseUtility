package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.kakinari.database.dao.AbstractTableAccess;
import com.kakinari.core.datatype.BaseUnit;
import com.kakinari.core.datatype.ListUnit;
import com.kakinari.core.datatype.interfaces.Param;

public abstract class AbstractDBTable<T extends BaseUnit<?>> extends ListUnit<T> {
	private String context = null;
	protected abstract T getRowObject(ResultSet rs) throws SQLException;
	protected abstract String getQueryString();

	public AbstractDBTable() {
		super();
		if (getConnectionName() != null && getQueryString() != null)
			execute(null);
	}

	public AbstractDBTable(String context) {
		super();
		setConnectionName(context);
		if (getQueryString() != null)
			execute(null);
	}

	public AbstractDBTable(Param param) {
		super(param);
		if (getConnectionName() != null && getQueryString() != null)
			execute(null);
	}

	public AbstractDBTable(String[] condition) {
		super();
		if (getConnectionName() != null && getQueryString() != null)
			execute(condition);
	}

	public AbstractDBTable(String context, String[] condition) {
		super();
		setConnectionName(context);
		if (getQueryString() != null)
			execute(condition);
	}

	public AbstractDBTable(Param param, String[] condition) {
		super(param);
		if (getConnectionName() != null && getQueryString() != null)
			execute(condition);
	}

	/*
	 * Copy Constracter
	 */
	public AbstractDBTable(ListUnit<T> list) {
		super(list);
	}

	protected void setConnectionName(String context) {
		this.context  = context;
	}

	protected  String getConnectionName() {
		return this.context;
	}

	protected void execute(String[] condition) {
		new AbstractTableAccess(getConnectionName()) {
			@Override
			protected Object getAccessResult(ResultSet rs) throws SQLException {
				while(rs.next())
					add(getRowObject(rs));
				return this;
			}
		}.execute(getQueryString(condition));
	}
	
	protected String getQueryString(String[] condition) {
		return getQueryString() + 
				AbstractTableAccess.getCondition(
										getConditionTerget(),
										condition,
										getExtraCondition(),
										getGroupCondition(),
										getOrderCondition());
	}
	
	protected  String getInsertQueryString(String tableName, String dupexpress[],  Map<String, String> colmap) {
		return null;
	}
	
	protected  String getUpdateQueryString(String tableName, String primary, Map<String, String> colmap) {
		return null;
	}

	protected String getConditionTerget() {
		return null;
	}
	protected String getExtraCondition() {
		return null;
	}
	protected String getGroupCondition() {
		return null;
	}
	protected String getOrderCondition() {
		return null;
	}
	protected  int executeUpdate(String context, String query) {
		if (context == null || query == null)
			return -1;
		return new AbstractTableAccess(context) {
			@Override
			protected Object getAccessResult(ResultSet rs) throws SQLException {
				return this;
			}
		}.executeUpdate(query);
	}

	protected int InsertData(String tableName) {
		return executeUpdate( getConnectionName(),  getInsertQueryString(tableName, null,  null));
	}
	
	protected int InsertData(String context, String tableName) {
		return  executeUpdate(context, getInsertQueryString(tableName, null,  null));
	}

	protected int InsertData(String tableName, String[] dupexpress) {
		return executeUpdate( getConnectionName(),  getInsertQueryString(tableName, dupexpress,  null));
	}
	
	protected int InsertData(String context, String tableName, String[] dupexpress) {
		return  executeUpdate(context, getInsertQueryString(tableName, dupexpress,  null));
	}

	protected int InsertData(String tableName, Map<String,String> colmap) {
		return executeUpdate( getConnectionName(),  getInsertQueryString(tableName, null,  colmap));
	}
	
	protected int InsertData(String context, String tableName, Map<String,String> colmap) {
		return  executeUpdate(context, getInsertQueryString(tableName, null,  colmap));
	}

	protected int InsertData(String tableName, String[] dupexpress,  Map<String,String> colmap) {
		return executeUpdate( getConnectionName(),  getInsertQueryString(tableName, dupexpress,  colmap));
	}
	
	protected int InsertData(String context, String tableName, String[] dupexpress,  Map<String,String> colmap) {
		return  executeUpdate(context, getInsertQueryString(tableName, dupexpress,  colmap));
	}

	protected int updateData(String tableName, String primary, Map<String,String> colmap) {
		return executeUpdate( getConnectionName() ,  getUpdateQueryString(tableName, primary,  colmap));
	}

	protected int updateData(String context, String tableName, String primary, Map<String,String> colmap) {
		return executeUpdate(context ,  getUpdateQueryString(tableName, primary,  colmap));
	}
	
	@Override
	protected void setListData(List<?> list) {
	}

}
