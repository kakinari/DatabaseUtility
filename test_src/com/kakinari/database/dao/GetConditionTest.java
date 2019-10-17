package com.kakinari.database.dao;

import java.util.ArrayList;

public class GetConditionTest {

	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.add("BO_");
		list.add("PH");
		list.add("%000");
		list.add("!NULL&%000");
		list.add("NULL");
		list.add("!NULL");
		list.add("=XA000100");
		list.add("<>XB000100");
		list.add("LH000100-LH003000");
		list.add("AA000000, BB000000,CC000000,DD000000");
		System.out.println(AbstractTableAccess.getCondition("SUBSTRING(`item_number`,1,8)", list.toArray(new String[0]), "foo = bar", "foo, bar", "foo"));
	}

}
