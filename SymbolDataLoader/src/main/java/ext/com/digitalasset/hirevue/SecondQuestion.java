package ext.com.digitalasset.hirevue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

public class SecondQuestion {

	/**
	### INPUTS & OUTPUTS #############
	exit
	ERROR INPUT
	25
	XXV
	3999
	MMMCMXCIX
	3992
	MMMCMXCII
	256
	CCLVI
	265
	CCLXV
	### INPUTS & OUTPUTS #############
	**/
	
	private static final TreeMap<Integer, String> cardinalToRomanNumMap = createMap();

	private static TreeMap<Integer, String> createMap() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
		return map;
	}

	public static void main(String[] args) throws IOException {
		InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(reader);
		String line;
		while ((line = in.readLine()) != null) {
			// System.out.println(line);
			int input = 0;
			try {
				input = Integer.parseInt(line);
				System.out.println(SecondQuestion.toRoman(input));
			} catch (Exception e) {
				System.out.println("ERROR INPUT");
			}
		}
	}

	public final static String toRoman(int number) {
		int l = cardinalToRomanNumMap.floorKey(number);
		if (number == l) {
			return cardinalToRomanNumMap.get(number);
		}
		return cardinalToRomanNumMap.get(l) + toRoman(number - l);
	}
}
