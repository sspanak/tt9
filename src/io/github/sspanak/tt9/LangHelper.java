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

}
