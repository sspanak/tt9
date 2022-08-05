package io.github.sspanak.tt9;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LangHelper {
    protected static final Locale BULGARIAN = new Locale("bg", "BG");
	protected static final Locale RUSSIAN = new Locale("ru","RU");
    protected static final Locale UKRAINIAN = new Locale("uk","UA");


	public enum LANGUAGE {
		// MAKE SURE THESE MATCH WITH values/const.xml
		// (index, id) Where index is index in arrays like LOCALES and MUST increment and MUST be in
		// the same order as arrays.xml/pref_lang_values, and id is the identifier used in
		// the database and such. id should never change unless database update is done.
		// id MUST increment in doubles (as the enabled languages are stored as an integer)
		NONE(-1, -1), EN(0,1), RU(1,2), DE(2,4), FR(3,8), IT(4,16), UK(5,32), BG(6, 64);
		public final int index;
		public final int id;
		// lookup map
		private static final Map<Integer, LANGUAGE> lookup = new HashMap<Integer, LANGUAGE>();
		private static final LANGUAGE[] ids = LANGUAGE.values();
		static { for (LANGUAGE l : ids) lookup.put(l.id, l); }

		private LANGUAGE(int index, int id) { this.index = index; this.id = id; }

		public static LANGUAGE get(int i) { return lookup.get(i);}
	}

	public static final Locale[] LOCALES = {Locale.ENGLISH, RUSSIAN, Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN, UKRAINIAN, BULGARIAN};

	public static final int LANG_DEFAULT = LANGUAGE.EN.id;

	protected static final int NLANGS = LANGUAGE.lookup.size();

	protected static String getString(int lang) {
        return LANGUAGE.get(lang).name();
    }

	protected static int getIndex(LANGUAGE l) {
		return l.index;
	}

	    //[LANG][MODE][CAPSMODE] = iconref
	    // first group en, first line LANG, second line TEXT, last line NUM
	    protected static final int[][][] ICONMAP = {
			{
					//English resources
					{R.drawable.ime_lang_en_lower, R.drawable.ime_lang_en_single, R.drawable.ime_lang_en_upper},
					{R.drawable.ime_lang_latin_lower, R.drawable.ime_lang_latin_single, R.drawable.ime_lang_latin_upper},
					{R.drawable.ime_number},
			},
			{
					// Russian resources
					{R.drawable.ime_lang_ru_lower, R.drawable.ime_lang_ru_single, R.drawable.ime_lang_ru_upper}, //LANG
					{R.drawable.ime_lang_cyrillic_lower, R.drawable.ime_lang_cyrillic_single, R.drawable.ime_lang_cyrillic_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
			{
					// German resources
					{R.drawable.ime_lang_de_lower, R.drawable.ime_lang_de_single, R.drawable.ime_lang_de_upper}, //LANG
					{R.drawable.ime_lang_latin_lower, R.drawable.ime_lang_latin_single, R.drawable.ime_lang_latin_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
			{
					// French resources
					{R.drawable.ime_lang_fr_lower, R.drawable.ime_lang_fr_single, R.drawable.ime_lang_fr_upper}, //LANG
					{R.drawable.ime_lang_latin_lower, R.drawable.ime_lang_latin_single, R.drawable.ime_lang_latin_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
			{
					// Italian resources
					{R.drawable.ime_lang_it_lower, R.drawable.ime_lang_it_single, R.drawable.ime_lang_it_upper}, //LANG
					{R.drawable.ime_lang_latin_lower, R.drawable.ime_lang_latin_single, R.drawable.ime_lang_latin_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
			{
					// Ukrainian resources
					{R.drawable.ime_lang_uk_lower, R.drawable.ime_lang_uk_single, R.drawable.ime_lang_uk_upper}, //LANG
					{R.drawable.ime_lang_cyrillic_lower, R.drawable.ime_lang_cyrillic_single, R.drawable.ime_lang_cyrillic_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
			{
					// Bulgarian resources
					{R.drawable.ime_lang_bg_lower, R.drawable.ime_lang_bg_single, R.drawable.ime_lang_bg_upper}, //LANG
					{R.drawable.ime_lang_cyrillic_lower, R.drawable.ime_lang_cyrillic_single, R.drawable.ime_lang_cyrillic_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},
	    };

	public static LANGUAGE[] buildLangs(int i) {
		int num = 0;
		//calc size of filtered array
		for (LANGUAGE l : LANGUAGE.ids) {
			if ((i & l.id) == l.id) {
				num++;
			}
		}
		LANGUAGE[] la = new LANGUAGE[num];
		int lai = 0;
		for (LANGUAGE l : LANGUAGE.ids) {
			if ((i & l.id) == l.id) {
				la[lai] = l;
				lai++;
			}
		}
		return la;
	}

	public static int shrinkLangs(LANGUAGE[] langs) {
		int i = 0;
		for (LANGUAGE l : langs)
			i = i | l.id;
		return i;
	}
	public static int shrinkLangs(int[] langs) {
		int i = 0;
		for (int l : langs)
			i = i | l;
		return i;
	}

	protected static int findIndex(LANGUAGE[] ia, LANGUAGE target) {
		for (int x=0; x<ia.length; x++) {
			if (ia[x] == target)
				return x;
		}
		return 0;
	}
}
