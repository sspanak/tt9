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
			case "ca", "ga", "sw" -> language;
			case "en" -> "in".equals(country) ? "hn" : language; // en-IN = Hinglish
			case "fa" -> "ف";
			case "fi" -> "su";
			case "el" -> "ελ";
			case "gu" -> "ગુ";
			case "he", "iw" -> "אב";
			case "hi" -> "ह";
			case "hu" -> "mg";
			case "ji", "yi" -> "יי";
			case "ja" -> "漢";
			case "ko" -> "한";
			case "ru" -> "ру";
			case "sr" -> "rs".equals(country) ? "ср" : language;
			case "th" -> "ไท";
			case "uk" -> "ук";
			case "zgh" -> "dz".equals(country) ? "tm" : "ⵜⵎ";
			case "zh" -> "cn".equals(country) ? "拼" : language;
			default -> country;
		};
	}

	@NonNull
	@Override
	public String toString() {
		return (getLanguage() + getCountry()).toUpperCase();
	}
}
