package org.nyanya.android.traditionalt9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

public class CharMap {
    protected static final ArrayList<Map<Character, Integer>> CHARTABLE = new ArrayList<Map<Character, Integer>>(2);
    static {
        Map<Character, Integer> enMap = new HashMap<Character, Integer>();
        enMap.put('.', 1); enMap.put(',', 1); enMap.put('!', 1); enMap.put('?', 1);
        enMap.put('-', 1); enMap.put('"', 1); enMap.put('\'', 1); enMap.put('@', 1);
        enMap.put('#', 1); enMap.put('$', 1); enMap.put('%', 1); enMap.put('&', 1);
        enMap.put('*', 1); enMap.put('(', 1); enMap.put(')', 1); enMap.put('1', 1);
        enMap.put(':', 1); enMap.put(';', 1); enMap.put('/', 1); enMap.put('\\', 1);
        enMap.put('+', 1); enMap.put('=', 1); enMap.put('<', 1); enMap.put('>', 1);
        enMap.put('[', 1); enMap.put(']', 1); enMap.put('{', 1); enMap.put('}', 1);
        enMap.put('^', 1); enMap.put('|', 1); enMap.put('_', 1); enMap.put('~', 1);
        enMap.put('`', 1);
        enMap.put('a', 2);      enMap.put('\u00e1', 2); enMap.put('\u00e4', 2);
        enMap.put('\u00e2', 2); enMap.put('\u00e0', 2); enMap.put('\u00e5', 2);
        enMap.put('b', 2);      enMap.put('c', 2);      enMap.put('\u00e7', 2);
        enMap.put('2', 2);      enMap.put('d', 3);      enMap.put('e', 3);
        enMap.put('\u00e9', 3); enMap.put('\u00eb', 3); enMap.put('\u00e8', 3);
        enMap.put('\u00ea', 3); enMap.put('f', 3);      enMap.put('3', 3);
        enMap.put('g', 4);      enMap.put('h', 4);      enMap.put('i', 4);
        enMap.put('\u00ed', 4); enMap.put('\u00ef', 4); enMap.put('4', 4);
        enMap.put('j', 5);      enMap.put('k', 5);      enMap.put('l', 5);
        enMap.put('5', 5);      enMap.put('m', 6);      enMap.put('n', 6);
        enMap.put('\u00f1', 6); enMap.put('o', 6);      enMap.put('\u00f3', 6);
        enMap.put('\u00f6', 6); enMap.put('\u00f4', 6); enMap.put('\u00fb', 6);
        enMap.put('6', 6);      enMap.put('p', 7);      enMap.put('q', 7);
        enMap.put('r', 7);      enMap.put('s', 7);      enMap.put('7', 7);
        enMap.put('t', 8);      enMap.put('u', 8);      enMap.put('\u00fc', 8);
        enMap.put('v', 8);      enMap.put('8', 8);      enMap.put('w', 9);
        enMap.put('x', 9);      enMap.put('y', 9);      enMap.put('z', 9);
        enMap.put('9', 9);      enMap.put('+', 0);      enMap.put('0', 0);
        CHARTABLE.add(0, Collections.unmodifiableMap(enMap));

        Map<Character, Integer> ruMap = new HashMap<Character, Integer>();
        ruMap.put('.', 1); ruMap.put(',', 1); ruMap.put('!', 1); ruMap.put('?', 1);
        ruMap.put('-', 1); ruMap.put('"', 1); ruMap.put('\'', 1); ruMap.put('@', 1);
        ruMap.put('#', 1); ruMap.put('$', 1); ruMap.put('%', 1); ruMap.put('&', 1);
        ruMap.put('*', 1); ruMap.put('(', 1); ruMap.put(')', 1); ruMap.put('1', 1);
        ruMap.put(':', 1); ruMap.put(';', 1); ruMap.put('/', 1); ruMap.put('\\', 1);
        ruMap.put('+', 1); ruMap.put('=', 1); ruMap.put('<', 1); ruMap.put('>', 1);
        ruMap.put('[', 1); ruMap.put(']', 1); ruMap.put('{', 1); ruMap.put('}', 1);
        ruMap.put('^', 1); ruMap.put('|', 1); ruMap.put('_', 1); ruMap.put('~', 1);
        ruMap.put('`', 1); ruMap.put('1', 1);
        ruMap.put('а', 2); ruMap.put('б', 2); ruMap.put('в', 2); ruMap.put('г', 2);
        ruMap.put('2', 2);
        ruMap.put('д', 3); ruMap.put('е', 3); ruMap.put('ё', 3); ruMap.put('ж', 3);
        ruMap.put('з', 3); ruMap.put('3', 3);
        ruMap.put('и', 4); ruMap.put('й', 4); ruMap.put('к', 4); ruMap.put('л', 4);
        ruMap.put('4', 4);
        ruMap.put('м', 5); ruMap.put('н', 5); ruMap.put('о', 5); ruMap.put('п', 5);
        ruMap.put('5', 5);
        ruMap.put('р', 6); ruMap.put('с', 6); ruMap.put('т', 6); ruMap.put('у', 6);
        ruMap.put('6', 6);
        ruMap.put('ф', 7); ruMap.put('х', 7); ruMap.put('ц', 7); ruMap.put('ч', 7);
        ruMap.put('7', 7);
        ruMap.put('ш', 8); ruMap.put('щ', 8); ruMap.put('ъ', 8); ruMap.put('ы', 8);
        ruMap.put('8', 8);
        ruMap.put('ь', 9); ruMap.put('э', 9); ruMap.put('ю', 9); ruMap.put('я', 9);
        ruMap.put('9', 9);
        ruMap.put('+', 0);      ruMap.put('0', 0);
        CHARTABLE.add(1, Collections.unmodifiableMap(ruMap));
    }

    protected static final char[][] ENT9TABLE = { { '0', '+' },
            { '.', ',', '?', '!', '"', '\'', '-', '@', '$', '%', '&', '*', '(', ')', '_', '1' },
            { 'a', 'b', 'c', 'A', 'B', 'C', '2' }, { 'd', 'e', 'f', 'D', 'E', 'F', '3' },
            { 'g', 'h', 'i', 'G', 'H', 'I', '4' }, { 'j', 'k', 'l', 'J', 'K', 'L', '5' },
            { 'm', 'n', 'o', 'M', 'N', 'O', '6' }, { 'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7' },
            { 't', 'u', 'v', 'T', 'U', 'V', '8' }, { 'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9' },
            { ' ', '\n' } };
    protected static final char[][] RUT9TABLE = { { '0', '+' },
            { '.', ',', '?', '!', '"', '\'', '-', '@', '$', '%', '&', '*', '(', ')', '_', '1' },
            { 'а', 'б', 'в', 'г', 'А', 'Б', 'В', 'Г', '2' }, { 'д', 'е', 'ё', 'ж', 'з', 'Д', 'Е', 'Ё', 'Ж', 'З', '3' },
            { 'и', 'й', 'к', 'л', 'И', 'Й', 'К', 'Л', '4' }, { 'м', 'н', 'о', 'п', 'М', 'Н', 'О', 'П', '5' },
            { 'р', 'с', 'т', 'у', 'Р', 'С', 'Т', 'У', '6' }, { 'ф', 'х', 'ц', 'ч', 'Ф', 'Х', 'Ц', 'Ч', '7' },
            { 'ш', 'щ', 'ъ', 'ы', 'Ш', 'Щ', 'Ъ', 'Ы', '8' }, { 'ь', 'э', 'ю', 'я', 'Ь', 'Э', 'Ю', 'Я', '9' },
            { ' ', '\n' } };
    protected static final char[][][] T9TABLE = {ENT9TABLE, RUT9TABLE};

    protected static final int[] ENT9CAPSTART = { 0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0 };
    protected static final int [] RUT9CAPSTART = {0, 0, 4, 5, 4, 4, 4, 4, 4, 4, 0};
    protected static final int[][] T9CAPSTART = {ENT9CAPSTART, RUT9CAPSTART};

    protected static String getStringSequence(String word, int lang) {
        StringBuilder seq = new StringBuilder();
        String tword = word.toLowerCase(LangHelper.LOCALES[lang]);
        for (int i = 0; i < word.length(); i++) {
            char c = tword.charAt(i);
            Integer z = CharMap.CHARTABLE.get(lang).get(c);
            if (z == null) {
                Log.e("getStringSequence",
                        "ERROR: " + (int) c + " NOT FOUND FOR [" + lang + "] (" + Integer.toHexString((int) c) + ") Index: " + i);
                throw new NullPointerException();
            }
            seq.append(z.toString());
        }
        return seq.toString();
    }
}
