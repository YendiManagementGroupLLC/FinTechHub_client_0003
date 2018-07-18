package ext.com.digitalasset.hirevue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FirstQuestion {
	
	/**
	### INPUTS & OUTPUTS #############
	 	(9,1), (6,7), (1,6), (2,7)
		false
		(4,5), (5,5), (5,6), (4,6)
		true
		(5,6), (5,5), (5,6), (4,6)
		false
		exit
		false
		bye
		false
		go
		false
		done
		false
	### INPUTS & OUTPUTS ############# 
	**/

	public static void main(String[] args) throws IOException {
		InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(reader);
		String line;
		while ((line = in.readLine()) != null) {
			boolean hasFailed = false;
			// System.out.println(line);
			String[] coordinates = String.valueOf(line).split(" ");
			if (coordinates.length != 4) { // if we don't have four points, return false as we have bad coordinates
											// anyway
				hasFailed = true;
			} else {
				Map<Integer, Integer> points = new HashMap<Integer, Integer>();
				for (String coordinate : coordinates) {
					coordinate = coordinate.replace("(", "");
					coordinate = coordinate.replace(")", "");
					String[] currPoints = coordinate.split(",");
					try { // test that the coordinates are valid integers on an x/y plane
						int lhs = Integer.parseInt(currPoints[0]);
						int rhs = Integer.parseInt(currPoints[1]);
						Integer lhsKey = new Integer(lhs);
						addPoint(points, lhsKey);
						Integer rhsKey = new Integer(rhs);
						addPoint(points, rhsKey);
					} catch (NumberFormatException ex) { // if any of these above fails, return false as we have bad
															// coordinates anyway
						hasFailed = true;
						break;
					}
				}
				for (Integer value : points.values()) {
					if (value.intValue() != 2 && value.intValue() != 4) {
						hasFailed = true;
						break;
					}
				}
			}
			System.out.println(hasFailed ? "false" : "true");
		}
	}

	private static void addPoint(Map<Integer, Integer> points, Integer key) {
		Integer oldCount = points.get(key);
		if (oldCount == null)
			points.put(key, 1);
		else
			points.put(key, (oldCount.intValue() + 1));
	}
}
