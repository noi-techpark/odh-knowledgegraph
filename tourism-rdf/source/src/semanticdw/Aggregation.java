package semanticdw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aggregation {

	private Map<List<String>, Integer> groupings;

	public Aggregation(List<List<String>> input) throws Exception {
		groupings = new HashMap<List<String>, Integer>();
		List<String> lastGroup = input.get(0);
		List<String> group = null;
		int c = 0;
		for (List<String> r : input) {
			group = r;
			if (different(lastGroup, group)) {
				if (groupings.containsKey(lastGroup)) {
					throw new Exception("Key already exists. Input must be sorted!");
				}
				groupings.put(lastGroup, new Integer(c));
				c = 0;
				lastGroup = group;
			}
			c++;
		}
		if (groupings.containsKey(lastGroup)) {
			throw new Exception("Key already exists. Input must be sorted!");
		}
		groupings.put(lastGroup, new Integer(c));
	}

	public Map<List<String>, Integer> getResult() {
		return groupings;
	}

	private static boolean different(List<String> g1, List<String> g2) {
		for (int i = 0; i < g1.size(); i++) {
			if (! g1.get(i).equals(g2.get(i))) {
				return true;
			}
		}
		return false;
	}
}
