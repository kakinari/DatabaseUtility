package com.kakinari.database.datatype;

import java.util.Comparator;
import java.util.Map;

import com.kakinari.core.comparator.NameComparator;
import com.kakinari.core.datatype.TreeUnit;

/*
 * Table class for converting from DBTableUnit to MappedTable format
 * this is for changing row and columns orientation.
 */
public class MappedTable extends TreeUnit<DBListRow> {

	public MappedTable() {
		super();
	}

	public MappedTable(Comparator<Object> comparator) {
		super(comparator);
	}

	public MappedTable(TreeUnit<DBListRow> tree) {
		super(tree);
	}

	public MappedTable(DBTableUnit table) {
		super(new NameComparator(table.size()>0?table.get(0).keySet() : null));
		for(DBDataRow row : table) {
			for(Object key : row.keySet()) {
				if (get(key) == null)
					put(key, new DBListRow());
				get(key).add(row.get(key));
			}
		}
	}

	@Override
	protected void setMapValue(Map<?, ?> mapdata) {
	}
	
}
