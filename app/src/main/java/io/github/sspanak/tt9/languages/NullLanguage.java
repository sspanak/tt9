package io.github.sspanak.tt9.languages;

import android.content.Context;

import java.util.Locale;

import io.github.sspanak.tt9.R;

public class NullLanguage extends Language {
	public NullLanguage(Context context) {
		locale = Locale.ROOT;
		name = context.getString(R.string.no_language);
		abcString = "abc";
	}
}
