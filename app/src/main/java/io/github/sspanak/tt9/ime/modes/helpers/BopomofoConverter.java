package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.HashMap;

public class BopomofoConverter {

	private final HashMap<String, String> bopomofoToLatin = new HashMap<>();
	private final HashMap<String, String> latinToBopomofo = new HashMap<>();

	public BopomofoConverter() {
		// because I was too lazy to parse ChineseBopomofo.yml...
		// the YAML is never going to change, so this is fine
		latinToBopomofo.put("I", "ㄧ");
		latinToBopomofo.put("U", "ㄨ");
		latinToBopomofo.put("Yu", "ㄩ");
		latinToBopomofo.put("B", "ㄅ");
		latinToBopomofo.put("P", "ㄆ");
		latinToBopomofo.put("M", "ㄇ");
		latinToBopomofo.put("F", "ㄈ");
		latinToBopomofo.put("D", "ㄉ");
		latinToBopomofo.put("T", "ㄊ");
		latinToBopomofo.put("N", "ㄋ");
		latinToBopomofo.put("L", "ㄌ");
		latinToBopomofo.put("G", "ㄍ");
		latinToBopomofo.put("K", "ㄎ");
		latinToBopomofo.put("H", "ㄏ");
		latinToBopomofo.put("J", "ㄐ");
		latinToBopomofo.put("Q", "ㄑ");
		latinToBopomofo.put("X", "ㄒ");
		latinToBopomofo.put("Zh", "ㄓ");
		latinToBopomofo.put("Ch", "ㄔ");
		latinToBopomofo.put("Sh", "ㄕ");
		latinToBopomofo.put("R", "ㄖ");
		latinToBopomofo.put("Z", "ㄗ");
		latinToBopomofo.put("C", "ㄘ");
		latinToBopomofo.put("S", "ㄙ");
		latinToBopomofo.put("A", "ㄚ");
		latinToBopomofo.put("O", "ㄛ");
		latinToBopomofo.put("E", "ㄜ");
		latinToBopomofo.put("Ie", "ㄝ");
		latinToBopomofo.put("Ai", "ㄞ");
		latinToBopomofo.put("Ei", "ㄟ");
		latinToBopomofo.put("Ao", "ㄠ");
		latinToBopomofo.put("Ou", "ㄡ");
		latinToBopomofo.put("An", "ㄢ");
		latinToBopomofo.put("En", "ㄣ");
		latinToBopomofo.put("Ang", "ㄤ");
		latinToBopomofo.put("Eng", "ㄥ");
		latinToBopomofo.put("Er", "ㄦ");

		for (HashMap.Entry<String, String> entry : latinToBopomofo.entrySet()) {
			bopomofoToLatin.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Converts a Bopomofo string to its Latin transcription using the predefined mapping.
	 * If a character is not found in the mapping, it is left unchanged.
	 */
	public String toLatin(String bopomofo) {
		StringBuilder result = new StringBuilder();
		for (char c : bopomofo.toCharArray()) {
			if (bopomofoToLatin.containsKey(String.valueOf(c))) {
				result.append(bopomofoToLatin.get(String.valueOf(c)));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Converts a Latin string to its Bopomofo transcription using the predefined mapping.
	 * If a character or sequence is not found in the mapping, it is left unchanged.
	 */
	public String toBopomofo(String latin) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < latin.length()) {
			if (i + 3 <= latin.length()) {
				String three = latin.substring(i, i + 3);
				if (latinToBopomofo.containsKey(three)) {
					result.append(latinToBopomofo.get(three));
					i += 3;
					continue;
				}
			}

			if (i + 2 <= latin.length()) {
				String two = latin.substring(i, i + 2);
				if (latinToBopomofo.containsKey(two)) {
					result.append(latinToBopomofo.get(two));
					i += 2;
					continue;
				}
			}

			String one = latin.substring(i, i + 1);
			if (latinToBopomofo.containsKey(one)) {
				result.append(latinToBopomofo.get(one));
			} else {
				result.append(one);
			}
			i++;
		}
		return result.toString();
	}
}
