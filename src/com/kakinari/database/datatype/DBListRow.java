package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.kakinari.core.comparator.NameComparator;
import com.kakinari.core.datatype.BaseUnit;
import com.kakinari.core.datatype.BoolUnit;
import com.kakinari.core.datatype.DateUnit;
import com.kakinari.core.datatype.ListDataUnit;
import com.kakinari.core.datatype.ListUnit;
import com.kakinari.core.datatype.NullUnit;
import com.kakinari.core.datatype.NumberUnit;
import com.kakinari.core.datatype.ObjectUnit;
import com.kakinari.core.datatype.StringUnit;

public class DBListRow extends ListDataUnit {
	private String[] label = null;

	public DBListRow() {
		super();
	}

	public DBListRow(List<?> list) {
		super(list);
	}

	public DBListRow(ListUnit<BaseUnit<?>> unit) {
		super(unit);
	}

	public DBListRow(ResultSet resultset) throws SQLException {
		super();
		ArrayList<String> label = new ArrayList<String>();
		ResultSetMetaData meta = resultset.getMetaData();
		for(int i=1; i<= meta.getColumnCount(); i++) {
			label.add(meta.getColumnLabel(i));
			Object val = resultset.getObject(i);
			if (val == null)
				add(new NullUnit());
			else if (val instanceof Boolean)
				add(new BoolUnit((Boolean) val));
			else if (val instanceof Number)
				add(new NumberUnit((Number) val));
			else if (val instanceof Date)
				add(new DateUnit((Date) val));
			else if (val instanceof Calendar)
				add(new DateUnit((Calendar) val));
			else if (val instanceof Timestamp)
				add(new DateUnit((Timestamp) val));
			else if (val instanceof String)
				add(new StringUnit((String) val));
			else
				add(new ObjectUnit(val));
		}
		this.label = label.toArray(new String[0]);
	}
	
	public Map<String, BaseUnit<?>> toMap() {
		TreeMap<String, BaseUnit<?>> ret = new TreeMap<String, BaseUnit<?>>(new NameComparator(label));
		for (int i=0; i<label.length;i++)
			ret.put(label[i], get(i));
		return ret;
	}
	
	protected String[] getLabel() {
		return this.label;
	}

	protected void setLabel(String[] label) {
		this.label = label;
	}


}
