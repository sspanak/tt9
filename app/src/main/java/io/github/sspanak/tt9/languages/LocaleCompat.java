package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Deals with inconsistencies between Java and Android language codes.
 */
class LocaleCompat {
	private final Locale locale;

	LocaleCompat(Locale locale) {
		this.locale = locale;
	}

	private String getCountry() {
		String country = locale != null ? locale.getCountry() : "";
		return country.equals("YI") ? "JI" : country;
	}

	private String getLanguage() {
		String language = locale != null ? locale.getLanguage() : "";
		return switch (language) {
			case "yi" -> "ji";
			case "he" -> "iw";
			case "id" -> "in";
			default -> language;
		};
	}


	@NonNull
	@Override
	public String toString() {
		return (getLanguage() + getCountry()).toUpperCase();
	}
}
