package io.github.sspanak.tt9.languages;

public class LanguageKind {
	public static boolean isArabic(Language language) { return language != null && language.getId() == 502337; }
	public static boolean isBulgarian(Language language) { return language != null && language.getId() == 231650; }
	public static boolean isCyrillic(Language language) { return language != null && language.getKeyCharacters(2).contains("а"); }
	public static boolean isFrench(Language language) { return language != null && language.getId() == 596550; }
	public static boolean isGreek(Language language) { return language != null && language.getId() == 597381; }
	public static boolean isHebrew(Language language) { return language != null && (language.getId() == 305450 || language.getId() == 403177); }
	public static boolean isLatinBased(Language language) { return language != null && language.getKeyCharacters(2).contains("a"); }
	public static boolean isRTL(Language language) { return isArabic(language) || isHebrew(language); }
	public static boolean isUkrainian(Language language) { return language != null && language.getId() == 54645; }
}
