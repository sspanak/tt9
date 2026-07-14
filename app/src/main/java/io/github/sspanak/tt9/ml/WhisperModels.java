package io.github.sspanak.tt9.ml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Available Whisper models for offline speech recognition
 */
public class WhisperModels {

	public static final ModelData ENGLISH_TINY = new ModelData(
		"English-39 (default)",
		false,
		"tiny_en_acft_q8_0.bin",
		"4b5480aa1b14a7efc5b578ef176510970a898049671c3cd237285b3e3f6bfbfc"
	);

	public static final ModelData ENGLISH_BASE = new ModelData(
		"English-74 (slower, more accurate)",
		false,
		"base_en_acft_q8_0.bin",
		"e9b4b7b81b8a28769e8aa9962aa39bb9f21b622cf6a63982e93f065ed5caf1c8"
	);

public static final ModelData MULTILINGUAL_TINY = new ModelData(
                "Multilingual-39 (57 languages)",
                false,
                "tiny_acft_q8_0.bin",
                "07aa4d514144deacf5ffec5cacb36c93dee272fda9e64ac33a801f8cd5cbd953"
        );

        public static final ModelData[] ENGLISH_MODELS = {
                ENGLISH_TINY,
                ENGLISH_BASE
        };

        public static final ModelData[] MULTILINGUAL_MODELS = {
                MULTILINGUAL_TINY
        };

        /** Language codes with no Whisper support — Irish, Toki Pona, Tamazight */
        public static final Set<String> UNSUPPORTED_LANGUAGE_CODES = new HashSet<>(Arrays.asList("ga", "tp", "zgh"));

	/**
	 * Language codes supported by the multilingual model
	 * All 74+ languages from Whisper training dataset (excluding English - uses dedicated model)
	 */
	public static final String[] SUPPORTED_LANGUAGES = {
		// High-resource languages (>1000 hours)
		"zh", "de", "es", "ru", "fr", "pt", "ko", "ja", "tr", "pl", "it", "sv", "nl", "ca", "fi", "id", "in",
		// Medium-resource languages (100-1000 hours)
		"ar", "uk", "vi", "he", "el", "da", "ms", "hu", "ro", "no", "nb", "nn", "th", "cs", "ta", "ur", 
		// Lower-resource languages (10-100 hours)
		"hr", "sk", "bg", "tl", "cy", "lt", "lv", "az", "et", "sl", "sr", "fa", "eu", "is", "mk", "hy", "kk", "hi", "bs", "gl",
		// Low-resource languages (<10 hours)
		"sq", "si", "sw", "te", "af", "kn", "be", "km", "bn", "mt", "ht", "pa", "mr", "ne", "ka", "ml",
		// Very low-resource languages (<0.5 hours)
		"yi", "uz", "gu", "tg", "mg", "my", "su", "lo"
	};

	/**
	 * Map language code to Whisper language code
	 * Some languages use different codes in Whisper
	 */
	public static String mapLanguageCode(String languageCode) {
		// Whisper uses 2-letter language codes
		if (languageCode.contains("-") || languageCode.contains("_")) {
			languageCode = languageCode.substring(0, 2);
		}
		languageCode = languageCode.toLowerCase();
		
		// Map deprecated codes to current ISO 639-1 codes
		if ("in".equals(languageCode)) {
			return "id"; // Indonesian: 'in' (old) -> 'id' (current)
		}
		if ("iw".equals(languageCode)) {
			return "he"; // Hebrew: 'iw' (old) -> 'he' (current)
		}
		if ("ji".equals(languageCode)) {
			return "yi"; // Yiddish: 'ji' (old) -> 'yi' (current)
		}
		// Map Norwegian variants to generic Norwegian
		if ("nb".equals(languageCode) || "nn".equals(languageCode)) {
			return "no"; // Norwegian Bokmål/Nynorsk -> Norwegian
		}
		// Map Hinglish to Hindi (Whisper handles code-mixing naturally)
		if ("hn".equals(languageCode)) {
			return "hi"; // Hinglish -> Hindi
		}
return languageCode;
	}

	/**
	 * Check if a language is supported by the multilingual model
	 */
	public static boolean isLanguageSupported(String languageCode) {
		String mapped = mapLanguageCode(languageCode);
		for (String lang : SUPPORTED_LANGUAGES) {
			if (lang.equals(mapped)) {
				return true;
			}
		}
		return false;
	}

        /**
         * Returns true for languages with no Whisper support (Irish, Toki Pona, Tamazight)
         */
        public static boolean isLanguageUnsupported(String languageCode) {
                return UNSUPPORTED_LANGUAGE_CODES.contains(mapLanguageCode(languageCode));
        }

}
