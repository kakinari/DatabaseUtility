package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.kakinari.core.comparator.NameComparator;
import com.kakinari.core.datatype.BaseUnit;
import com.kakinari.core.datatype.NumberUnit;
import com.kakinari.core.datatype.interfaces.Param;

public abstract class DBTableUnit extends AbstractDBTable<DBDataRow> {
	TreeMap<String,Integer> index = null;
	private String[] idxlabel = null;
	public DBTableUnit() {
		super();
	}

	public DBTableUnit(Param param, String[] condition) {
		super(param, condition);
	}

	public DBTableUnit(Param param) {
		super(param);
	}

	public DBTableUnit(String[] condition) {
		super(condition);
	}

	public DBTableUnit(DBTableUnit table) {
		super(table);
	}

	public DBTableUnit(DBTableUnit table, String[] label) {
		super();
		for (DBDataRow row : table) {
			add(new DBDataRow(row, label));
		}
	}

	public DBTableUnit(DataSourceTable<?> source) {
		super();
		String[] label = source.getLabel();
		if (label == null && source.get(0) != null) {  // generate Number labels
			ArrayList<String> alabel = new ArrayList<String>();
			int sz = source.get(0).size();
			for (int i=0;i<sz;i++)
				alabel.add("" + (i+1));	// Start from 1
			label = alabel.toArray(new String[0]);
		}
		setSourceData(source, label);
	}

	public DBTableUnit(DataSourceTable<?> source, String[] label) {
		super();
		setSourceData(source, label);
	}

	@Override
	protected DBDataRow getRowObject(ResultSet rs) throws SQLException {
		return new DBDataRow(rs);
	}

	protected void setSourceData(DataSourceTable<?> source, String[] label) {
		for (DBListRow row : source) {
			add(new DBDataRow(row, label));
		}
	}

	public void setUniqueIndex(String label) throws Exception {
		String[] labels = {label};
		setUniqueIndex(labels);
	}
	
	public void setUniqueIndex(String[] labels) throws Exception {
		String[] codeOrder = null;
		this.index = new TreeMap<String,Integer>(new NameComparator(codeOrder));
		this.idxlabel = labels;
		for(int i=0;i<size();i++) {
			DBDataRow row = get(i);
			StringBuffer key = new StringBuffer();
			for (String k : labels) {
				if (row.get(k) == null)
					throw new Exception("LabelNotFoundException:" + k);
				if (key.length()>0) key.append("\n");
				key.append(row.get(k).toString());
			}
			if (this.index.get(key.toString()) != null)
				throw new Exception("DuplicateValueException:" + key);
			this.index.put(key.toString(), Integer.valueOf(i));
		}
	}

	public DBDataRow get(String value) {
		return this.index == null || this.index.get(value) == null ? null : get(this.index.get(value));
	}
	
	public String[] getIndexLabels() {
		return this.idxlabel;
	}
	
	public DBDataRow get(String[] values) {
		String key = null;
		for(String val : values)
			key = key != null ? key + "\n" + val : val;
		return get(key);
	}
	
	@Override
	protected String getInsertQueryString(String tableName, String[] dupexpress, Map<String, String> colmap) {
		if (size() == 0)
			return null;
		StringBuffer sb = new StringBuffer("INSERT INTO `" + tableName + "`( ");
		DBDataRow top = get(0);
		boolean flag = false;
		for(Object key : top.keySet()) {
			if (colmap != null && colmap.containsKey(key))
				key = colmap.get(key);
			if (flag) sb.append(",");
			sb.append(String.format("`%s`", key));
			flag = true;
		}
		sb.append(") \n VALUES \n");
		boolean rflag = false;
		for(DBDataRow row :this) {
			if (rflag)
				sb.append(",\n");
			flag = false;
			for(Object key : row.keySet()) {
				sb.append(flag ? "," : "(");
				sb.append(setValueStr( row.get(key)));
				flag = true;
				rflag=true;
			}
			sb.append(")");
		}
		if (dupexpress != null) {
			sb.append("\nON DUPLICATE KEY UPDATE ");
			flag = false;
			for (String exp : dupexpress) {
				if (flag)
					sb.append(",\n");
				flag = true;
				sb.append(exp);
			}
		}
		sb.append(";");
		return sb.toString();
	}

	@Override
	protected String getUpdateQueryString(String tableName, String primary, Map<String, String> colmap) {
		if (size() == 0 || tableName == null || primary == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for ( DBDataRow row : this) {
			sb.append("UPDATE `" + tableName + "` SET ");
			boolean flag = false;
			for(Object key : row.keySet()) {
				String name = key.toString();
				if (colmap != null && colmap.containsKey(key))
					name = colmap.get(key);
				if (primary.contains(name))
					continue;
				if (flag) sb.append(",");
				sb.append(setKeyValue(name, row.get(key)));
				flag = true;
			}
			flag=false;
			for(String key : primary.split(",")) {
				String name = key;
				if (colmap != null && colmap.containsKey(key))
					name = colmap.get(key);
				sb.append(flag ? " AND " : " WHERE ");
				sb.append(setKeyValue(name, row.get(key)));
				flag = true;
			}
			sb.append(";\n");
		}
		return sb.toString();
	}

	private Object setKeyValue(String key, BaseUnit<?> cell) {
		return String.format("`%s` =%s%s%s",
				key,
				cell instanceof NumberUnit ? "" : "'",
				cell.toString(),
				cell instanceof NumberUnit ? "" : "'"
			);
	}

	private Object setValueStr( BaseUnit<?> cell) {
		return String.format("%s%s%s",
				cell instanceof NumberUnit ? "" : "'",
				cell.toString(),
				cell instanceof NumberUnit ? "" : "'"
			);
	}
}
