package com.kakinari.database.datatype;

import java.util.List;

import com.kakinari.core.datatype.ListUnit;
import com.kakinari.core.datatype.interfaces.Param;

public abstract class DataSourceTable<T extends DBListRow> extends ListUnit<T> {
	private String[] label = null;
	protected abstract  T getRowUnit();

	public DataSourceTable() {
		super();
	}

	public DataSourceTable(ListUnit<T> list) {
		super(list);
	}

	public DataSourceTable(Param param) {
		super(param);
	}

	public DataSourceTable(DBTableUnit table) {
		super();
		this.label = table.size()>0? table.get(0).keySet().toArray(new String[0]) : null;
		setdata(table);
	}
	
	public DataSourceTable(DBTableUnit table, String[] label) {
		super();
		this.label = label;
		setdata(table);
	}

	private void setdata(DBTableUnit table) {
		for(DBDataRow row : table) {
			T list =getRowUnit();
			for(String key : label) {
				list.add(row.get(key));
			}
			add(list);
		}		
	}

	public String[] getLabel() {
		return label;
	}

	protected void setLabel(String[] label) {
		this.label = label;
	}
	
	@Override
	protected void setListData(List<?> list) {
	}

}
