package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.util.Characters;
import io.github.sspanak.tt9.util.Text;


public class NaturalLanguage extends Language implements Comparable<NaturalLanguage> {
	final public static String SPECIAL_CHARS_KEY = "0";
	final public static String PUNCTUATION_KEY = "1";
	final public static String PREFERRED_CHAR_SEQUENCE = "00";


	protected final ArrayList<ArrayList<String>> layout = new ArrayList<>();
	private final HashMap<Character, String> characterKeyMap = new HashMap<>();


	public static NaturalLanguage fromDefinition(LanguageDefinition definition) throws Exception {
		if (definition.dictionaryFile.isEmpty()) {
			throw new Exception("Invalid definition. Dictionary file must be set.");
		}

		NaturalLanguage lang = new NaturalLanguage();
		lang.abcString = definition.abcString.isEmpty() ? null : definition.abcString;
		lang.dictionaryFile = definition.getDictionaryFile();
		lang.hasUpperCase = definition.hasUpperCase;
		lang.name = definition.name.isEmpty() ? lang.name : definition.name;
		lang.setLocale(definition);
		lang.setLayout(definition);

		return lang;
	}


	private void setLocale(LanguageDefinition definition) throws Exception {
		if (definition.locale.isEmpty()) {
			throw new Exception("Invalid definition. Locale cannot be empty.");
		}

		if (definition.locale.equals("en")) {
			locale = Locale.ENGLISH;
		} else {
			String[] parts = definition.locale.split("-", 2);
			if (parts.length == 2) {
				locale = new Locale(parts[0], parts[1]);
			} else if (parts.length == 1) {
				locale = new Locale(parts[0]);
			} else {
				throw new Exception("Unrecognized locale format: '" + definition.locale + "'.");
			}
		}
	}


	private void setLayout(LanguageDefinition definition) {
		for (int key = 0; key <= 9 && key < definition.layout.size(); key++) {
				layout.add(
					key,
					key > 1 ? definition.layout.get(key) : generateSpecialChars(definition.layout.get(key))
				);
		}

		generateCharacterKeyMap();
	}


	private ArrayList<String> generateSpecialChars(ArrayList<String> definitionChars) {
		final String SPECIAL_CHARS_PLACEHOLDER = "SPECIAL";
		final String PUNCTUATION_PLACEHOLDER = "PUNCTUATION";
		final String ARABIC_PUNCTUATION_STYLE = PUNCTUATION_PLACEHOLDER + "_AR";
		final String FRENCH_PUNCTUATION_STYLE = PUNCTUATION_PLACEHOLDER + "_FR";
		final String GERMAN_PUNCTUATION_STYLE = PUNCTUATION_PLACEHOLDER + "_DE";
		final String GREEK_PUNCTUATION_STYLE = PUNCTUATION_PLACEHOLDER + "_GR";

		ArrayList<String> keyChars = new ArrayList<>();
		for (String defChar : definitionChars) {
			switch (defChar) {
				case SPECIAL_CHARS_PLACEHOLDER:
					keyChars.addAll(Characters.Special);
					break;
				case PUNCTUATION_PLACEHOLDER:
					keyChars.addAll(Characters.PunctuationEnglish);
					break;
				case ARABIC_PUNCTUATION_STYLE:
					keyChars.addAll(Characters.PunctuationArabic);
					break;
				case FRENCH_PUNCTUATION_STYLE:
					keyChars.addAll(Characters.PunctuationFrench);
					break;
				case GERMAN_PUNCTUATION_STYLE:
					keyChars.addAll(Characters.PunctuationGerman);
					break;
				case GREEK_PUNCTUATION_STYLE:
					keyChars.addAll(Characters.PunctuationGreek);
					break;
				default:
					keyChars.add(defChar);
					break;
			}
		}

		return keyChars;
	}


	/**
	 * generateId
	 * Uses the letters of the Locale to generate an ID for the language.
	 * Each letter is converted to uppercase and used as a 5-bit integer. Then the 5-bits
	 * are packed to form a 10-bit or a 20-bit integer, depending on the Locale length.
	 *
	 * Example (2-letter Locale)
	 * 	"en"
	 * 	-> "E" | "N"
	 * 	-> 5 | 448 (shift the 2nd number by 5 bits, so its bits would not overlap with the 1st one)
	 *	-> 543
	 *
	 * Example (4-letter Locale)
	 * 	"bg-BG"
	 * 	-> "B" | "G" | "B" | "G"
	 * 	-> 2 | 224 | 2048 | 229376 (shift each 5-bit number, not overlap with the previous ones)
	 *	-> 231650
	 *
	 * Minimum ID: "aa" -> 33
	 * Maximum ID: "zz-ZZ" -> 879450
	 */
	@Override
	public int getId() {
		if (id == 0) {
			String idString = (locale.getLanguage() + locale.getCountry()).toUpperCase();
			for (int i = 0; i < idString.length(); i++) {
				id |= (idString.codePointAt(i) & 31) << (i * 5);
			}
		}

		return id;
	}


	private String getSortingId() {
		switch (getLocale().getLanguage()) {
			case "fi":
				return "su";
			case "sw":
				return "ki";
			default:
				return getLocale().toString();
		}
	}


	@NonNull
	@Override
	public String getAbcString() {
		if (abcString == null) {
			ArrayList<String> lettersList = getKeyCharacters(2);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < lettersList.size() && i < 3; i++) {
				sb.append(lettersList.get(i));
			}

			abcString = sb.toString();
		}

		return abcString;
	}

	@NonNull
	@Override
	public String getName() {
		if (name == null) {
			name = new Text(this, locale.getDisplayLanguage(locale)).capitalize();
		}

		return name;
	}


	private void generateCharacterKeyMap() {
		characterKeyMap.clear();
		for (int digit = 0; digit <= 9; digit++) {
			for (String keyChar : getKeyCharacters(digit)) {
				characterKeyMap.put(keyChar.charAt(0), String.valueOf(digit));
			}
		}
	}


	@NonNull
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		if (key < 0 || key >= layout.size()) {
			return new ArrayList<>();
		}

		ArrayList<String> chars = layout.get(key);
		if (key == 0) {
			if (characterGroup > 1) {
				chars = new ArrayList<>();
			} else if (characterGroup == 1) {
				chars = new ArrayList<>(Characters.Currency);
			}
		}

		return chars;
	}


	@NonNull
	public String getKeyNumber(int key) {
		return key >= 0 && key < 10 && LanguageKind.isArabic(this) ? Characters.ArabicNumbers.get(key) : super.getKeyNumber(key);
	}


	@NonNull
	public String getDigitSequenceForWord(String word) throws InvalidLanguageCharactersException {
		StringBuilder sequence = new StringBuilder();
		String lowerCaseWord = word.toLowerCase(locale);

		for (int i = 0; i < lowerCaseWord.length(); i++) {
			char letter = lowerCaseWord.charAt(i);
			if (!characterKeyMap.containsKey(letter)) {
				throw new InvalidLanguageCharactersException(this, "Failed generating digit sequence for word: '" + word);
			}

			sequence.append(characterKeyMap.get(letter));
		}

		return sequence.toString();
	}



	public boolean isValidWord(String word) {
		if (word == null || word.isEmpty() || (word.length() == 1 && Character.isDigit(word.charAt(0)))) {
			return true;
		}

		String lowerCaseWord = word.toLowerCase(locale);

		for (int i = 0; i < lowerCaseWord.length(); i++) {
			if (!characterKeyMap.containsKey(lowerCaseWord.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	@Override
	public int compareTo(NaturalLanguage other) {
		return getSortingId().compareTo(other.getSortingId());
	}
}
