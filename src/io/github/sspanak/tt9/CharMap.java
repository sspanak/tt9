package io.github.sspanak.tt9;

import android.util.Log;

import io.github.sspanak.tt9.LangHelper.LANGUAGE;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CharMap {
	protected static final AbstractList<Map<Character, Integer>> CHARTABLE = new ArrayList<Map<Character, Integer>>(LangHelper.NLANGS);
	static {
		// Punctuation
		Map<Character, Integer> commonMap = new HashMap<Character, Integer>();
		commonMap.put('.', 1); commonMap.put(',', 1); commonMap.put('!', 1); commonMap.put('?', 1);
		commonMap.put('-', 1); commonMap.put('"', 1); commonMap.put('\'', 1); commonMap.put('@', 1);
		commonMap.put('#', 1); commonMap.put('$', 1); commonMap.put('%', 1); commonMap.put('&', 1);
		commonMap.put('*', 1); commonMap.put('(', 1); commonMap.put(')', 1); commonMap.put(':', 1);
		commonMap.put(';', 1); commonMap.put('/', 1); commonMap.put('+', 1); commonMap.put('=', 1);
		commonMap.put('<', 1); commonMap.put('>', 1); commonMap.put('^', 1); commonMap.put('_', 1);
		commonMap.put('~', 1);
		commonMap.put('1', 1); commonMap.put('2', 2); commonMap.put('3', 3);
		commonMap.put('4', 4); commonMap.put('5', 5); commonMap.put('6', 6);
		commonMap.put('7', 7); commonMap.put('8', 8); commonMap.put('9', 9);
		commonMap.put('+', 0); commonMap.put('0', 0); // not sure why "+" is both on 1 on 0, but kept it anyway

		/*** Latin Scripts ***/

		// English
		// the English dictionary contains foreign words with their original spelling,
		// so non-English characters must be inside the map
		Map<Character, Integer> enMap = new HashMap<Character, Integer>(commonMap);
		enMap.put('a', 2); enMap.put('á', 2); enMap.put('ä', 2); enMap.put('â', 2);
		enMap.put('à', 2); enMap.put('å', 2); enMap.put('b', 2); enMap.put('c', 2);
		enMap.put('ç', 2);
		enMap.put('d', 3); enMap.put('e', 3); enMap.put('é', 3); enMap.put('ë', 3);
		enMap.put('è', 3); enMap.put('ê', 3); enMap.put('f', 3);
		enMap.put('g', 4); enMap.put('h', 4); enMap.put('i', 4); enMap.put('í', 4);
		enMap.put('ï', 4);
		enMap.put('j', 5); enMap.put('k', 5); enMap.put('l', 5); enMap.put('5', 5);
		enMap.put('m', 6); enMap.put('n', 6); enMap.put('ñ', 6); enMap.put('o', 6);
		enMap.put('ó', 6); enMap.put('ö', 6); enMap.put('ô', 6);
		enMap.put('p', 7); enMap.put('q', 7); enMap.put('r', 7); enMap.put('s', 7);
		enMap.put('t', 8); enMap.put('u', 8); enMap.put('û', 6); enMap.put('ü', 8);
		enMap.put('v', 8);
		enMap.put('w', 9); enMap.put('x', 9); enMap.put('y', 9); enMap.put('z', 9);

		// add extra characters for German and French maps.
		enMap.put('€', 1); enMap.put('ß', 7); // German chars
		enMap.put('æ', 1); enMap.put('î', 4); enMap.put('ù', 8); enMap.put('œ', 6);	// French chars
		enMap.put('ì', 4); enMap.put('ò', 8); // Italian chars

		/*** Cyrillic Scripts ***/
		Map<Character, Integer> cyrillicMap = new HashMap<Character, Integer>(commonMap);
		cyrillicMap.put('а', 2); cyrillicMap.put('б', 2); cyrillicMap.put('в', 2); cyrillicMap.put('г', 2);
		cyrillicMap.put('д', 3); cyrillicMap.put('е', 3); cyrillicMap.put('ж', 3); cyrillicMap.put('з', 3);
		cyrillicMap.put('и', 4); cyrillicMap.put('й', 4); cyrillicMap.put('к', 4); cyrillicMap.put('л', 4);
		cyrillicMap.put('м', 5); cyrillicMap.put('н', 5); cyrillicMap.put('о', 5); cyrillicMap.put('п', 5);
		cyrillicMap.put('р', 6); cyrillicMap.put('с', 6); cyrillicMap.put('т', 6); cyrillicMap.put('у', 6);
		cyrillicMap.put('ф', 7); cyrillicMap.put('х', 7); cyrillicMap.put('ц', 7); cyrillicMap.put('ч', 7);
		cyrillicMap.put('ш', 8); cyrillicMap.put('щ', 8);
		cyrillicMap.put('ь', 9); cyrillicMap.put('ю', 9); cyrillicMap.put('я', 9);

		// Bulgarian
		Map<Character, Integer> bgMap = new HashMap<Character, Integer>(cyrillicMap);
		bgMap.put('ъ', 8);

		// Russian
		Map<Character, Integer> ruMap = new HashMap<Character, Integer>(bgMap);
		ruMap.put('ё', 3); ruMap.put('ы', 8); ruMap.put('э', 9);

		// Ukrainian
		Map<Character, Integer> ukMap = new HashMap<Character, Integer>(cyrillicMap);
		ukMap.put('ґ', 2); ukMap.put('є', 3); ukMap.put('і', 4); ukMap.put('ї', 4);// Ukrainian chars

		CHARTABLE.add(0, Collections.unmodifiableMap(enMap));
		CHARTABLE.add(1, Collections.unmodifiableMap(ruMap));
		CHARTABLE.add(2, Collections.unmodifiableMap(enMap));
		CHARTABLE.add(3, Collections.unmodifiableMap(enMap));
		CHARTABLE.add(4, Collections.unmodifiableMap(enMap));
		CHARTABLE.add(5, Collections.unmodifiableMap(ukMap));
		CHARTABLE.add(6, Collections.unmodifiableMap(bgMap));
	}

	protected static final char[][] ENT9TABLE = { { '0', '+' },
		{ '.', ',', '?', '!', '"', '/', '-', '@', '$', '%', '&', '*', '#', '(', ')', '_', '1' },
		{ 'a', 'b', 'c', 'A', 'B', 'C', '2' }, { 'd', 'e', 'f', 'D', 'E', 'F', '3' },
		{ 'g', 'h', 'i', 'G', 'H', 'I', '4' }, { 'j', 'k', 'l', 'J', 'K', 'L', '5' },
		{ 'm', 'n', 'o', 'M', 'N', 'O', '6' }, { 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7' },
		{ 't', 'u', 'v', 'T', 'U', 'V', '8' }, { 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0
	protected static final char[][] RUT9TABLE = { { '0', '+' },
		{ '.', ',', '?', '!', '"', '/', '-', '@', '$', '%', '&', '*', '#', '(', ')', '_', '1' },
		{ 'а', 'б', 'в', 'г', 'А', 'Б', 'В', 'Г', '2' }, { 'д', 'е', 'ё', 'ж', 'з', 'Д', 'Е', 'Ё', 'Ж', 'З', '3' },
		{ 'и', 'й', 'к', 'л', 'И', 'Й', 'К', 'Л', '4' }, { 'м', 'н', 'о', 'п', 'М', 'Н', 'О', 'П', '5' },
		{ 'р', 'с', 'т', 'у', 'Р', 'С', 'Т', 'У', '6' }, { 'ф', 'х', 'ц', 'ч', 'Ф', 'Х', 'Ц', 'Ч', '7' },
		{ 'ш', 'щ', 'ъ', 'ы', 'Ш', 'Щ', 'Ъ', 'Ы', '8' }, { 'ь', 'э', 'ю', 'я', 'Ь', 'Э', 'Ю', 'Я', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][] DET9TABLE = {
		{ '0', '+' },
		{ '.', ',', '?', '!', ':', ';', '"', '\'', '-', '@', '^', '€', '$', '%', '&', '*', '#', '(', ')', '_', '1' },
		{ 'a', 'b', 'c', 'A', 'B', 'C', 'ä', 'Ä','á', 'â', 'à', 'å', 'ç', 'Á', 'Â', 'À', 'Å', 'Ç', '2' },
		{ 'd', 'e', 'f', 'D', 'E', 'F', 'é','ë','è','ê', 'É', 'Ë', 'È', 'Ê', '3' },
		{ 'g', 'h', 'i', 'G', 'H', 'I', 'í', 'ï', 'Í', 'Ï', '4' },
		{ 'j', 'k', 'l', 'J', 'K', 'L', '5' },
		{ 'm', 'n', 'o', 'M', 'N', 'O', 'ö', 'Ö', 'ñ','ó','ô', 'Ñ', 'Ó', 'Ô', '6' },
		{ 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', 'ß', '7' },
		{ 't', 'u', 'v', 'T', 'U', 'V', 'ü', 'Ü', 'û', 'Û', '8' },
		{ 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][] FRT9TABLE = {
		{ '0', '+' },
		{ '.', ',', '?', '!', ':', ';', '"', '/', '-', '@', '^', '€', '$', '%', '&', '*', '#', '(', ')', '_', '1' },
		{ 'a', 'b', 'c', 'A', 'B', 'C', '2', 'â', 'à', 'æ', 'ç', 'Â', 'À', 'Æ', 'Ç'},
		{ 'd', 'e', 'f', 'D', 'E', 'F', '3', 'é', 'è','ê', 'ë', 'É', 'È', 'Ê', 'Ë' },
		{ 'g', 'h', 'i', 'G', 'H', 'I', '4', 'î', 'ï', 'Î', 'Ï' },
		{ 'j', 'k', 'l', 'J', 'K', 'L', '5' },
		{ 'm', 'n', 'o', 'M', 'N', 'O', '6', 'ô', 'œ', 'Ô', 'Œ'},
		{ 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7' },
		{ 't', 'u', 'v', 'T', 'U', 'V', '8', 'û', 'Û', 'ù', 'Ù', 'ü', 'Ü'},
		{ 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][] ITT9TABLE = {
		{ '+', '0' },
		{ '.', ',', '?', '!', ':', ';', '"', '/', '-', '@', '^', '€', '$', '%', '&', '*', '#', '(', ')', '_', '1' },
		{ 'a', 'b', 'c', 'A', 'B', 'C', 'à', 'À', '2' }, { 'd', 'e', 'f', 'D', 'E', 'F', 'é', 'è', 'É', 'È', '3' },
		{ 'g', 'h', 'i', 'G', 'H', 'I', 'ì', 'Ì', '4' }, { 'j', 'k', 'l', 'J', 'K', 'L', '5' },
		{ 'm', 'n', 'o', 'M', 'N', 'O', 'ò', 'Ò', '6' }, { 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7' },
		{ 't', 'u', 'v', 'T', 'U', 'V', 'ù', 'Ù', '8' }, { 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][] UKT9TABLE = { { '0', '+' },
		{ '.', ',', '?', '!', '\'', '"', '/', '-', '@', '$', '%', '&', '*', '#',  '(', ')', '_', '1' },
		{ 'а', 'б', 'в', 'г', 'ґ', 'А', 'Б', 'В', 'Г', 'Ґ', '2' }, { 'д', 'е', 'є', 'ж', 'з', 'Д', 'Е', 'Є', 'Ж', 'З', '3' },
		{ 'и', 'і', 'ї', 'й', 'к', 'л', 'И', 'І', 'Ї', 'Й', 'К', 'Л', '4' }, { 'м', 'н', 'о', 'п', 'М', 'Н', 'О', 'П', '5' },
		{ 'р', 'с', 'т', 'у', 'Р', 'С', 'Т', 'У', '6' }, { 'ф', 'х', 'ц', 'ч', 'Ф', 'Х', 'Ц', 'Ч', '7' },
		{ 'ш', 'щ', 'Ш', 'Щ', '8' }, { 'ь', 'ю', 'я', 'Ь', 'Ю', 'Я', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][] BGT9TABLE = { { '0', '+' },
		{ '.', ',', '?', '!', '\'', '"', '/', '-', '@', '$', '%', '&', '*', '#',  '(', ')', '_', '1' },
		{ 'а', 'б', 'в', 'г', 'А', 'Б', 'В', 'Г', '2' }, { 'д', 'е', 'ж', 'з', 'Д', 'Е', 'Ж', 'З', '3' },
		{ 'и', 'й', 'к', 'л', 'И', 'Й', 'К', 'Л', '4' }, { 'м', 'н', 'о', 'п', 'М', 'Н', 'О', 'П', '5' },
		{ 'р', 'с', 'т', 'у', 'Р', 'С', 'Т', 'У', '6' }, { 'ф', 'х', 'ц', 'ч', 'Ф', 'Х', 'Ц', 'Ч', '7' },
		{ 'ш', 'щ', 'ъ', 'Ш', 'Щ', 'Ъ', '8' }, { 'ь', 'ю', 'я', 'Ь', 'Ю', 'Я', '9' },
		{ ' ', '\n' }, { ' ', '0', '+' }, { '\n' } }; // LAST TWO SPACE ON 0

	protected static final char[][][] T9TABLE = { ENT9TABLE, RUT9TABLE, DET9TABLE, FRT9TABLE, ITT9TABLE, UKT9TABLE, BGT9TABLE };

	// last 2 don't matter, are for spaceOnZero extra 'slots' 0 position, and 10 position
	protected static final int[] ENT9CAPSTART = { 0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0,	 0, 0 };
	protected static final int[] RUT9CAPSTART = { 0, 0, 4, 5, 4, 4, 4, 4, 4, 4, 0,	 0, 0 };
	protected static final int[] DET9CAPSTART = { 0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0,	 0, 0 };
	protected static final int[] FRT9CAPSTART = { 0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0,	 0, 0 };
	protected static final int[] ITT9CAPSTART = { 0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0,	 0, 0 };
	protected static final int[] UKT9CAPSTART = { 0, 0, 5, 5, 6, 4, 4, 4, 2, 3, 0,	 0, 0 };
	protected static final int[] BGT9CAPSTART = { 0, 0, 4, 4, 4, 4, 4, 4, 3, 3, 0,	 0, 0 };
	protected static final int[][] T9CAPSTART = {ENT9CAPSTART, RUT9CAPSTART, DET9CAPSTART, FRT9CAPSTART, ITT9CAPSTART, UKT9CAPSTART, BGT9CAPSTART};

	protected static String getStringSequence(String word, LANGUAGE lang) {
		StringBuilder seq = new StringBuilder();
		String tword = word.toLowerCase(LangHelper.LOCALES[lang.index]);
		for (int i = 0; i < word.length(); i++) {
			char c = tword.charAt(i);
			Integer z = CharMap.CHARTABLE.get(lang.index).get(c);
			if (z == null) {
				Log.e("getStringSequence",
						"ERROR: " + (int) c + " NOT FOUND FOR [" + lang.name() + "] (" + Integer.toHexString((int) c) + ") Index: " + i);
				throw new NullPointerException();
			}
			seq.append(z.toString());
		}
		return seq.toString();
	}
}
