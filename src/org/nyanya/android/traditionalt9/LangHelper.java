package org.nyanya.android.traditionalt9;

import java.util.Locale;

import pl.wavesoftware.widget.MultiSelectListPreference;

public class LangHelper {
    protected static final Locale RUSSIAN = new Locale("ru","RU");
    protected static final int EN = 0;
    protected static final int RU = 1;
	protected static final int DE = 1;
    protected static final Locale[] LOCALES = {Locale.ENGLISH, RUSSIAN, Locale.GERMAN};
    protected static final String[] LANGS = {"EN", "RU", "DE"};

    protected static final int NLANGS = LANGS.length;

	protected static String getString(int lang) {
        return LANGS[lang];
    }

    //[LANG][MODE][CAPSMODE] = iconref
    // first group en, first line LANG, second line TEXT, last line NUM
    protected static final int[][][] ICONMAP = {
            {
                    //English resources
					{R.drawable.ime_en_lang_lower, R.drawable.ime_en_lang_single, R.drawable.ime_en_lang_upper},
                    {R.drawable.ime_en_text_lower, R.drawable.ime_en_text_single, R.drawable.ime_en_text_upper},
                    {R.drawable.ime_number},
            },
            {
                    // Russian resources
					{R.drawable.ime_ru_lang_lower, R.drawable.ime_ru_lang_single, R.drawable.ime_ru_lang_upper}, //LANG
                    {R.drawable.ime_ru_text_lower, R.drawable.ime_ru_text_single, R.drawable.ime_ru_text_upper}, //TEXT
                    {R.drawable.ime_number}, //NUM
            },
			{
					// German resources
					{R.drawable.ime_de_lang_lower, R.drawable.ime_de_lang_single, R.drawable.ime_de_lang_upper}, //LANG
					{R.drawable.ime_en_text_lower, R.drawable.ime_en_text_single, R.drawable.ime_en_text_upper}, //TEXT
					{R.drawable.ime_number}, //NUM
			},

    };

	protected static int[] buildLangs(CharSequence s) {
		int[] ia = MultiSelectListPreference.defaultunpack2Int(s);
		int num = 0;
		//calc size of filtered array
		for (int i : ia) {
			if (i >= 0 && i < LangHelper.NLANGS) {
				num++;
			}
		}
		int[] ian = new int[num];
		int iansize = 0;
		for (int i : ia) {
			if (i >= 0 && i < LangHelper.NLANGS) {
				ian[iansize] = i;
				iansize++;
			}
		}
		return ian;
	}

	protected static int findIndex(int[] ia, int target) {
		for (int x=0; x<ia.length; x++) {
			if (ia[x] == target)
				return x;
		}
		return -1;
	}
}
