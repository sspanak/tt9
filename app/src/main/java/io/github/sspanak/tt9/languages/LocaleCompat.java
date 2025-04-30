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
			case "zgh" -> "zg";
			default -> language;
		};
	}

	public String getUniqueLanguageCode() {
		if (locale == null) {
			return "";
		}

		String country = locale.getCountry().toLowerCase();
		String language = locale.getLanguage().toLowerCase();

		return switch (language) {
			case "ar" -> "ع";
			case "bg" -> "бг";
			case "en" -> ("in".equals(country)) ? "hn" : language; // en-IN = Hinglish
			case "fa" -> "ف";
			case "fi" -> "su";
			case "he", "iw" -> "אב";
			case "hu" -> "mg";
			case "ji", "yi" -> "יי";
			case "ru" -> "ру";
			case "uk" -> "ук";
			case "zgh" -> "tm";
			case "ca", "ga", "sw" -> language;
			default -> country;
		};
	}

	@NonNull
	@Override
	public String toString() {
		return (getLanguage() + getCountry()).toUpperCase();
	}
}
