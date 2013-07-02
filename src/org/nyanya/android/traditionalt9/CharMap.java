package org.nyanya.android.traditionalt9;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

public class CharMap {
    protected static final Map<Character, Integer> CHARTABLE;
    static {
        Map<Character, Integer> aMap = new HashMap<Character, Integer>();
        aMap.put('.', 1); aMap.put(',', 1); aMap.put('!', 1); aMap.put('?', 1); 
        aMap.put('-', 1); aMap.put('"', 1); aMap.put('\'', 1); aMap.put('@', 1);
        aMap.put('#', 1); aMap.put('$', 1); aMap.put('%', 1); aMap.put('&', 1);
        aMap.put('*', 1); aMap.put('(', 1); aMap.put(')', 1); aMap.put('1', 1);
        aMap.put('a', 2);      aMap.put('\u00e1', 2); aMap.put('\u00e4', 2);
    	aMap.put('\u00e2', 2); aMap.put('\u00e0', 2); aMap.put('\u00e5', 2);
    	aMap.put('b', 2);      aMap.put('c', 2);      aMap.put('\u00e7', 2); 
    	aMap.put('2', 2);      aMap.put('d', 3);      aMap.put('e', 3);
    	aMap.put('\u00e9', 3); aMap.put('\u00eb', 3); aMap.put('\u00e8', 3);
    	aMap.put('\u00ea', 3); aMap.put('f', 3);      aMap.put('3', 3); 
    	aMap.put('g', 4);      aMap.put('h', 4);      aMap.put('i', 4);
    	aMap.put('\u00ed', 4); aMap.put('\u00ef', 4); aMap.put('4', 4);
    	aMap.put('j', 5);      aMap.put('k', 5);      aMap.put('l', 5);
    	aMap.put('5', 5);      aMap.put('m', 6);      aMap.put('n', 6);
    	aMap.put('\u00f1', 6); aMap.put('o', 6);      aMap.put('\u00f3', 6);
    	aMap.put('\u00f6', 6); aMap.put('\u00f4', 6); aMap.put('\u00fb', 6);
    	aMap.put('6', 6);      aMap.put('p', 7);      aMap.put('q', 7);
    	aMap.put('r', 7);      aMap.put('s', 7);      aMap.put('7', 7);
    	aMap.put('t', 8);      aMap.put('u', 8);      aMap.put('\u00fc', 8);
    	aMap.put('v', 8);      aMap.put('8', 8);      aMap.put('w', 9);
    	aMap.put('x', 9);      aMap.put('y', 9);      aMap.put('z', 9);
    	aMap.put('9', 9);      aMap.put('+', 0);      aMap.put('0', 0);
    	CHARTABLE = Collections.unmodifiableMap(aMap);
    }
    protected static final char[][] T9TABLE = {
    	{'0', '+'}, {'.', ',', '!', '?', '-', '"', '\'', '@', '#', '$', '%', '&', '*', '(', ')', '1'}, 
    	{'a', 'b', 'c', 'A', 'B', 'C', '2'}, {'d', 'e', 'f', 'D', 'E', 'F', '3'}, 
    	{'g', 'h', 'i', 'G', 'H', 'I', '4'}, {'j', 'k', 'l', 'J', 'K', 'L', '5'}, 
    	{'m', 'n', 'o', 'M', 'N', 'O', '6'}, {'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7'}, 
    	{'t', 'u', 'v', 'T', 'U', 'V', '8'}, {'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9'},
    	{' ', '\n'}
    };
    
    protected static final int[] T9CAPSTART = {
    	0, 0, 3, 3, 3, 3, 3, 4, 3, 4, 0
    };
    
    protected static int[] getSequence(String word){
		int[] intseq = new int[word.length()];
		String tword = word.toLowerCase(Locale.ENGLISH);
		for (int i = 0; i < word.length(); i++){
			char c = tword.charAt(i);
		    Integer z = CharMap.CHARTABLE.get(c);
		    if (z == null){
		    	Log.e("getSequence", "ERROR: "+ (int)c + " NOT FOUND (" + Integer.toHexString((int)c) + ")");
		    	throw new NullPointerException();
		    }
		    intseq[i] = z;
		}
		return intseq;
	}
    
    protected static String getStringSequence(String word){
    	StringBuilder seq = new StringBuilder();
    	String tword = word.toLowerCase(Locale.ENGLISH);
		for (int i = 0; i < word.length(); i++){
			char c = tword.charAt(i);
		    Integer z = CharMap.CHARTABLE.get(c);
		    if (z == null){
		    	Log.e("getStringSequence", "ERROR: "+ (int)c + " NOT FOUND (" + Integer.toHexString((int)c) + ")");
		    	throw new NullPointerException();
		    }
		    seq.append(z.toString());
		}
    	return seq.toString();
    }
}
