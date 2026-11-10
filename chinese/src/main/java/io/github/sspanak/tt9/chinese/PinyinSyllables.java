package io.github.sspanak.tt9.chinese;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** The dictionary's Mandarin syllables, using its V spelling for ü. */
public final class PinyinSyllables {
	private static final Set<String> SYLLABLES = new HashSet<>(Arrays.asList((
		"A AI AN ANG AO BA BAI BAN BANG BAO BEI BEN BENG BI BIAN BIANG BIAO BIE BIN BING BO BU " +
		"CA CAI CAN CANG CAO CE CEN CENG CHA CHAI CHAN CHANG CHAO CHE CHEN CHENG CHI CHONG CHOU CHU CHUA CHUAI CHUAN CHUANG CHUI CHUN CHUO CI CONG COU CU CUAN CUI CUN CUO " +
		"DA DAI DAN DANG DAO DE DEI DEN DENG DI DIA DIAN DIAO DIE DING DIU DONG DOU DU DUAN DUI DUN DUO E EI EN ENG ER " +
		"FA FAN FANG FEI FEN FENG FIAO FO FOU FU GA GAI GAN GANG GAO GE GEI GEN GENG GONG GOU GU GUA GUAI GUAN GUANG GUI GUN GUO " +
		"HA HAI HAN HANG HAO HE HEI HEN HENG HONG HOU HU HUA HUAI HUAN HUANG HUI HUN HUO " +
		"JI JIA JIAN JIANG JIAO JIE JIN JING JIONG JIU JU JUAN JUE JUN KA KAI KAN KANG KAO KE KEI KEN KENG KONG KOU KU KUA KUAI KUAN KUANG KUI KUN KUO " +
		"LA LAI LAN LANG LAO LE LEI LENG LI LIA LIAN LIANG LIAO LIE LIN LING LIU LO LONG LOU LU LUAN LUN LUO LV LVE " +
		"MA MAI MAN MANG MAO ME MEI MEN MENG MI MIAN MIAO MIE MIN MING MIU MO MOU MU " +
		"NA NAI NAN NANG NAO NE NEI NEN NENG NI NIAN NIANG NIAO NIE NIN NING NIU NONG NOU NU NUAN NUO NV NVE O OU " +
		"PA PAI PAN PANG PAO PEI PEN PENG PI PIAN PIAO PIE PIN PING PO POU PU QI QIA QIAN QIANG QIAO QIE QIN QING QIONG QIU QU QUAN QUE QUN " +
		"RAN RANG RAO RE REN RENG RI RONG ROU RU RUA RUAN RUI RUN RUO SA SAI SAN SANG SAO SE SEN SENG SHA SHAI SHAN SHANG SHAO SHE SHEI SHEN SHENG SHI SHOU SHU SHUA SHUAI SHUAN SHUANG SHUI SHUN SHUO SI SONG SOU SU SUAN SUI SUN SUO " +
		"TA TAI TAN TANG TAO TE TEI TENG TI TIAN TIAO TIE TING TONG TOU TU TUAN TUI TUN TUO WA WAI WAN WANG WEI WEN WENG WO WU " +
		"XI XIA XIAN XIANG XIAO XIE XIN XING XIONG XIU XU XUAN XUE XUN YA YAN YANG YAO YE YI YIN YING YO YONG YOU YU YUAN YUE YUN " +
		"ZA ZAI ZAN ZANG ZAO ZE ZEI ZEN ZENG ZHA ZHAI ZHAN ZHANG ZHAO ZHE ZHEN ZHENG ZHI ZHONG ZHOU ZHU ZHUA ZHUAI ZHUAN ZHUANG ZHUI ZHUN ZHUO ZI ZONG ZOU ZU ZUAN ZUI ZUN ZUO"
	).split(" ")));
	private static final int MAX_VARIANTS = 32;
	private static final int MAX_SYLLABLES = 32;

	private PinyinSyllables() {}

	public static List<String> all() {
		ArrayList<String> result = new ArrayList<>(SYLLABLES);
		result.sort(String::compareTo);
		return List.copyOf(result);
	}

	public static boolean isSyllable(String syllable) {
		return syllable != null && SYLLABLES.contains(syllable.toUpperCase(Locale.ROOT));
	}

	/** Normalizes display boundaries; rejects tones, digits and arbitrary punctuation. */
	private static String normalize(String reading) {
		if (reading == null) throw new IllegalArgumentException("Pinyin is required");
		String value = reading.trim().toUpperCase(Locale.ROOT).replace("U:", "V").replace('Ü', 'V').replace('’', '\'');
		value = value.replaceAll("\\s+", "'");
		if (value.isEmpty() || !value.matches("[A-Z]+(?:'[A-Z]+)*")) {
			throw new IllegalArgumentException("Use unaccented Pinyin syllables separated by spaces or apostrophes");
		}
		return value;
	}

	public static String compact(String reading) { return normalize(reading).replace("'", ""); }

	public static String getSequence(String reading) {
		String value = compact(reading);
		String keys = "22233344455566677778889999";
		StringBuilder result = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) result.append(keys.charAt(value.charAt(i) - 'A'));
		return result.toString();
	}

	/** One syllable per Han code point. Ambiguous concatenations require explicit boundaries. */
	public static String parseUserReading(String reading, int hanLength) {
		if (hanLength < 1 || hanLength > MAX_SYLLABLES) throw new IllegalArgumentException("Use 1–32 Chinese characters");
		String normalized = normalize(reading);
		if (normalized.indexOf('\'') >= 0) {
			String[] syllables = normalized.split("'");
			if (syllables.length != hanLength) throw new IllegalArgumentException("Provide one Pinyin syllable for each Chinese character");
			for (String syllable : syllables) if (!isSyllable(syllable)) throw new IllegalArgumentException("Invalid Pinyin syllable: " + syllable);
			return normalized;
		}
		List<String> variants = segment(normalized, hanLength);
		if (variants.size() != 1) throw new IllegalArgumentException("Separate the Pinyin syllables with spaces or apostrophes");
		return variants.get(0);
	}

	public static List<String> segment(String reading) { return segment(reading, -1); }

	/** Do not invent a boundary when the existing dictionary cannot distinguish equal-length splits. */
	public static List<String> choicesForStoredWord(String raw) {
		int latinEnd = 0;
		while (latinEnd < raw.length() && raw.charAt(latinEnd) >= 'A' && raw.charAt(latinEnd) <= 'Z') latinEnd++;
		if (latinEnd == 0 || latinEnd == raw.length()) return List.of();
		String reading = raw.substring(0, latinEnd);
		int length = raw.codePointCount(latinEnd, raw.length());
		List<String> variants = segment(reading, length);
		return variants.size() == 1 ? variants : List.of(reading);
	}

	/** Selected whole syllables and explicit separators constrain the Han length before completions. */
	public static boolean matchesSelectedReading(String raw, String selected) {
		if (selected == null || selected.isEmpty()) return true;
		String value;
		try { value = normalize(selected); } catch (IllegalArgumentException ignored) { return false; }
		if (value.indexOf('\'') < 0 && !isSyllable(value)) return true;
		int latinEnd = 0;
		while (latinEnd < raw.length() && raw.charAt(latinEnd) >= 'A' && raw.charAt(latinEnd) <= 'Z') latinEnd++;
		if (latinEnd == 0 || latinEnd == raw.length()) return false;
		int selectedLength = compact(value).length();
		int selectedSyllables = value.split("'").length;
		int nativeLength = raw.codePointCount(latinEnd, raw.length());
		if (latinEnd > selectedLength) {
			int remaining = nativeLength - selectedSyllables;
			return remaining > 0 && !segment(raw.substring(selectedLength, latinEnd), remaining).isEmpty();
		}
		return nativeLength == selectedSyllables;
	}

	/** Longest syllables first, bounded to avoid exponential work on long ambiguous input. */
	public static List<String> segment(String reading, int syllableCount) {
		String value;
		try { value = compact(reading); } catch (IllegalArgumentException ignored) { return List.of(); }
		if (value.length() > MAX_SYLLABLES * 6 || syllableCount == 0 || syllableCount > MAX_SYLLABLES) return List.of();
		ArrayList<String> result = new ArrayList<>();
		collect(value, 0, syllableCount, new ArrayList<>(), result, new HashSet<>());
		return result;
	}

	private static boolean collect(String value, int start, int remaining, ArrayList<String> path, ArrayList<String> result, Set<String> deadEnds) {
		if (result.size() >= MAX_VARIANTS || path.size() > MAX_SYLLABLES) return true;
		if (start == value.length()) {
			if (remaining <= 0) { result.add(String.join("'", path)); return true; }
			return false;
		}
		if (remaining == 0 || (remaining > 0 && (value.length() - start < remaining || value.length() - start > remaining * 6))) return false;
		String key = start + ":" + remaining;
		if (deadEnds.contains(key)) return false;
		boolean found = false;
		for (int end = Math.min(value.length(), start + 6); end > start; end--) {
			String syllable = value.substring(start, end);
			if (!SYLLABLES.contains(syllable)) continue;
			path.add(syllable);
			found |= collect(value, end, remaining < 0 ? -1 : remaining - 1, path, result, deadEnds);
			path.remove(path.size() - 1);
			if (result.size() >= MAX_VARIANTS) break;
		}
		if (!found) deadEnds.add(key);
		return found;
	}
}
