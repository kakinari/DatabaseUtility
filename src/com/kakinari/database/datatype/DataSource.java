/**
 * 
 */
package com.kakinari.database.datatype;

import com.kakinari.core.datatype.ListUnit;
import com.kakinari.core.datatype.interfaces.Param;

/**
 * @author Takashi Kakinari
 *
 */
public class DataSource extends DataSourceTable<DBListRow> {

	/**
	 * 
	 */
	public DataSource() {
		super();
	}

	/**
	 * @param list
	 */
	public DataSource(ListUnit<DBListRow> list) {
		super(list);
	}

	/**
	 * @param param
	 */
	public DataSource(Param param) {
		super(param);
	}

	/**
	 * @param table
	 */
	public DataSource(DBTableUnit table) {
		super(table);
	}

	/**
	 * @param table
	 * @param label
	 */
	public DataSource(DBTableUnit table, String[] label) {
		super(table, label);
	}

	@Override
	protected DBListRow getRowUnit() {
		return new DBListRow();
	}

}
