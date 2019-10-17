package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

import com.kakinari.core.comparator.NameComparator;
import com.kakinari.core.datatype.BaseUnit;
import com.kakinari.core.datatype.BoolUnit;
import com.kakinari.core.datatype.DateUnit;
import com.kakinari.core.datatype.NullUnit;
import com.kakinari.core.datatype.NumberUnit;
import com.kakinari.core.datatype.ObjectUnit;
import com.kakinari.core.datatype.StringUnit;
import com.kakinari.core.datatype.TreeDataUnit;
import com.kakinari.core.datatype.TreeUnit;

public class DBDataRow extends TreeDataUnit {

	public DBDataRow() {
		super();
	}

	public DBDataRow(Comparator<Object> comparator) {
		super(comparator);
	}

	public DBDataRow(TreeUnit<BaseUnit<?>> tree) {
		super(tree);
	}

	public DBDataRow(TreeUnit<BaseUnit<?>> row, String[] label) {
		super(new NameComparator(label));
		for(String lbl : label) {
			put(lbl, row.get(lbl));
		}
	}

	public DBDataRow(TreeMap<Object, ? extends Object> map) {
		super(map);
	}

	public DBDataRow(DBListRow row, String[] label) {
		super(new NameComparator(label));
		for(int i=0;i<label.length;i++) {
			if (label[i] == null || label[i].length() == 0)
				continue;
			put(label[i], row.get(i));
		}
	}

	public DBDataRow(ResultSet resultset) throws SQLException {
		super(new NameComparator(resultset));
		 ResultSetMetaData meta = resultset.getMetaData();
		for(int i=1; i<=meta.getColumnCount(); i++) {
			String name =meta.getColumnLabel(i);
			Object val = resultset.getObject(i);
			if (val == null)
				put(name,new NullUnit());
			else if (val instanceof Boolean)
				put(name,new BoolUnit((Boolean) val));
			else if (val instanceof Number)
				put(name,new NumberUnit((Number) val));
			else if (val instanceof Date)
				put(name,new DateUnit((Date) val));
			else if (val instanceof Calendar)
				put(name,new DateUnit((Calendar) val));
			else if (val instanceof Timestamp)
				put(name,new DateUnit((Timestamp) val));
			else if (val instanceof String)
				put(name,new StringUnit((String) val));
			else
				put(name,new ObjectUnit(val));
		}
	}

}
