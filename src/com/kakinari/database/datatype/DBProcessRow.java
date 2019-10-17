package com.kakinari.database.datatype;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.kakinari.core.datatype.BoolUnit;
import com.kakinari.core.datatype.DateUnit;
import com.kakinari.core.datatype.MapDataUnit;
import com.kakinari.core.datatype.NullUnit;
import com.kakinari.core.datatype.NumberUnit;
import com.kakinari.core.datatype.ObjectUnit;
import com.kakinari.core.datatype.StringUnit;

public class DBProcessRow extends MapDataUnit {

	public DBProcessRow() {
		super();
	}

	public DBProcessRow(MapDataUnit tree) {
		super(tree);
	}

	public DBProcessRow(ResultSet resultset) throws SQLException {
		super();
		 ResultSetMetaData meta = resultset.getMetaData();
		for(int i=1; i<=meta .getColumnCount(); i++) {
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
