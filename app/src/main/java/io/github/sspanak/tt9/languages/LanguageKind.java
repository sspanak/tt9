package io.github.sspanak.tt9.languages;

public class LanguageKind {
	public static boolean isArabic(Language language) { return language != null && language.getKeyCharacters(3).contains("ا"); }
	public static boolean isCyrillic(Language language) { return language != null && language.getKeyCharacters(2).contains("а"); }
	public static boolean isHebrew(Language language) { return language != null && language.getKeyCharacters(3).contains("א"); }
	public static boolean isGreek(Language language) { return language != null && language.getKeyCharacters(2).contains("α"); }
	public static boolean isLatinBased(Language language) { return language != null && language.getKeyCharacters(2).contains("a"); }
	public static boolean isRTL(Language language) { return isArabic(language) || isHebrew(language); }
	public static boolean isUkrainian(Language language) { return language != null && language.getKeyCharacters(3).contains("є"); }
}
