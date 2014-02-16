package org.nyanya.android.traditionalt9;

import java.util.Locale;

public class LangHelper {
    protected static final Locale RUSSIAN = new Locale("ru","RU");
    protected static final int EN = 0;
    protected static final int RU = 1;
    protected static final Locale[] LOCALES = {Locale.ENGLISH, RUSSIAN};
    protected static final String[] LANGS = {"EN", "RU"};

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
            }
    };
}
