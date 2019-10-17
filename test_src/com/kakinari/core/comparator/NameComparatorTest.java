package com.kakinari.core.comparator;

import java.util.TreeMap;
import org.json.JSONObject;
import net.arnx.jsonic.JSON;

public class NameComparatorTest {

	public static void main(String[] args) {
		String[] list = {
			"First",
			"Second",
			"Third",
			"Another",
			"All",
			"Options"
		};
		TreeMap<String, Object> map = new TreeMap<String, Object>(new NameComparator(list));
		map.put("Another","4");
		map.put("All","5");
		map.put("First","1");
		map.put("Options","6");
		map.put("Second","2");
		map.put("Third","3");
		map.put("Foo","8");
		map.put("Bar","7");
		/*
		 * JSONObjectは内部でHashMapに変換されてしまうため、Comparatorの効果がない。
		 */
		JSONObject obj = new JSONObject(map);
		System.out.println(obj.toString(4));
		/*
		 * JSONIC encodeではTreeMapを評価しているので、Comparatorの効果が反映。
		 */
		System.out.println(JSON.encode(map, true));
		/*
		 * KeySet()は、Comparatorの効果が反映
		 */
		for(String key : map.keySet()) {
			System.out.println(String.format("\"%s\" : \"%s\",", key, map.get(key)));
		}
	}
}
