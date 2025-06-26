package io.github.sspanak.tt9.languages;

import java.util.Locale;

public class LanguageKind {
	public static final int KOREAN = 601579;

	public static boolean isArabicBased(Language language) { return isArabic(language) || isFarsi(language); }
	public static boolean isCJK(Language language) { return isChinese(language) || isJapanese(language) || isKorean(language); }
	public static boolean isCyrillic(Language language) { return language != null && language.getKeyCharacters(2).contains("а"); }
	public static boolean isLatinBased(Language language) { return language != null && language.getKeyCharacters(2).contains("a") && !language.isTranscribed(); }
	public static boolean isRTL(Language language) { return isArabic(language) || isFarsi(language) || isHebrew(language); }

	public static boolean isArabic(Language language) { return language != null && language.getId() == 502337; }
	public static boolean isEnglish(Language language) { return language != null && language.getLocale().equals(Locale.ENGLISH); }
	public static boolean isChinese(Language language) { return isChineseBopomofo(language) || isChinesePinyin(language); }
	public static boolean isChineseBopomofo(Language language) { return language != null && (language.getId() == 774426 || language.getId() == 368922); }
	public static boolean isChinesePinyin(Language language) { return language != null && language.getId() == 462106; }
	public static boolean isFarsi(Language language) { return language != null && language.getId() == 599078; }
	public static boolean isFrench(Language language) { return language != null && language.getId() == 596550; }
	public static boolean isJapanese(Language language) { return language != null && language.getId() == 534570; }
	public static boolean isGreek(Language language) { return language != null && language.getId() == 597381; }
	public static boolean isGujarati(Language language) { return language != null && language.getId() == 468647; }
	public static boolean isHebrew(Language language) { return language != null && (language.getId() == 305450 || language.getId() == 403177); }
	public static boolean isHindi(Language language) { return language != null && language.getId() == 468264; }
	public static boolean isHinglish(Language language) { return language != null && language.getId() == 468421; }
	public static boolean isIndic(Language language) { return isGujarati(language) || isHindi(language); }
	public static boolean isKorean(Language language) { return language != null && language.getId() == KOREAN; }
	public static boolean isUkrainian(Language language) { return language != null && language.getId() == 54645; }
	public static boolean isVietnamese(Language language) { return language != null && language.getId() == 481590; }
}
