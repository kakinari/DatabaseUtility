package com.kakinari.core.datatype;

import java.util.ArrayList;
import java.util.Calendar;

import com.kakinari.database.datatype.DBListRow;

public class DatatypeCheck {

	public static void main(String[] args) {
		BaseUnit<?> a = new StringUnit("hello");
		BaseUnit<?> b = new NumberUnit(123);
		BaseUnit<?> c = new DateUnit(Calendar.getInstance());
		BaseUnit<?> d = new ObjectUnit(Calendar.getInstance());
		DBListRow list = new DBListRow();
		list.add(a);
		list.add(b);
		list.add(c);
		MapDataUnit map = new MapDataUnit();
		ArrayList<Object> itemlist = new ArrayList<Object>();
		itemlist.add("mapval_c");
		itemlist.add("mapval_b");
		itemlist.add("mapval_a");
		
		map.setItemOrder(itemlist);
		map.put("mapval_a", a);
		map.put("mapval_b", b);
		map.put("mapval_c", c);
		a.setTag("p");
		b.setTag("i");
		c.setTag("b");
		d.setTag("u");
		a.setXmlTag("avalue");
		b.setXmlTag("bvalue");
		c.setXmlTag("cvalue");
		d.setXmlTag("dvalue");
		a.setIdString("avalue");
		b.setIdString("bvalue");
		c.setIdString("cvalue");
		d.setIdString("dvalue");
		a.setClassName("value");
		b.setClassName("value");
		c.setClassName("value");
		d.setClassName("value");
		a.setComment("a value");
		b.setComment("b value");
		c.setComment("c value");
		d.setComment("d value");
		System.out.println(a.toString());
		System.out.println(b.toString());
		System.out.println(c.toString());
		System.out.println(((ObjectUnit)d).toDateUnit().toString());
		System.out.println(a.toJSONString());
		System.out.println(b.toJSONString());
		System.out.println(c.toJSONString());
		System.out.println(((ObjectUnit)d).toDateUnit().toJSONString());
		System.out.println(a.toXMLString(2,true));
		System.out.println(b.toXMLString(3,true));
		System.out.println(c.toXMLString(4,true));
		System.out.println(((ObjectUnit)d).toDateUnit().toXMLString(4,true));
		System.out.println(a.toHtmlString(2,true));
		System.out.println(b.toHtmlString(3,true));
		System.out.println(c.toHtmlString(4,true));
		System.out.println(((ObjectUnit)d).toDateUnit().toHtmlString(4,true));
		System.out.println(list.toHtmlString(1,true));
		System.out.println(list.toString());
		System.out.println(list.toJSONString(1, true));
		System.out.println(list.toXMLString(1,true));
		System.out.println(map.toHtmlString(1,true));
		System.out.println(map.toString());
		System.out.println(map.toJSONString(1, true));
		System.out.println(map.toXMLString(1,true));
	}

}
