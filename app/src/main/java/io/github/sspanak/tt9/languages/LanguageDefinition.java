package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.AssetFile;
import io.github.sspanak.tt9.util.Logger;

public class LanguageDefinition {
	private static final String LOG_TAG = LanguageDefinition.class.getSimpleName();

	private static final String LANGUAGES_DIR = "languages";
	private static final String DEFINITIONS_PATH = LANGUAGES_DIR + "/definitions.yml";
	private static final String YAML_SEPARATOR = "---";

	public String abcString = "";
	public String currency = "";
	public String dictionaryFile = "";
	public boolean filterBySounds = false;
	public boolean hasABC = true;
	public boolean hasSpaceBetweenWords = true;
	public boolean hasUpperCase = true;
	public boolean isTranscribed = false;
	public final ArrayList<ArrayList<String>> layout = new ArrayList<>();
	public String locale = "";
	public String name = "";
	@NonNull public final HashMap<Integer, String> numerals = new HashMap<>();

	private boolean inLayout = false;



	/**
	 * Converts YAML definitions to a LanguageDefinition objects. All properties in the YAML are
	 * considered optional, so the LanguageDefinition defaults will be used when some property is omitted.
	 * Had to write all this, because the only usable library, SnakeYAML, works fine on Android 10+,
	 * but causes crashes on older devices.
	 */
	private LanguageDefinition() {}


	/**
	 * Returns a list of all language definitions contained in the asset at DEFINITIONS_PATH,
	 * or an empty list on error.
	 */
	public static ArrayList<LanguageDefinition> getAll(AssetManager assets) {
		String[] definitionLines = readDefinitions(assets);
		if (definitionLines.length == 0) {
			return new ArrayList<>();
		}

		ArrayList<LanguageDefinition> definitions = new ArrayList<>();

		LanguageDefinition definition = new LanguageDefinition();
		for (String line : definitionLines) {
			if (YAML_SEPARATOR.equals(line)) {
				definitions.add(definition);
				definition = new LanguageDefinition();
			} else if (!definition.setLayoutEntry(line)) {
				definition.setProperty(line);
			}
		}

		definitions.add(definition);

		Logger.d("tt9.LanguageCollection", "Found " + definitions.size() + " languages");
		return definitions;
	}


	/**
	 * Reads the language definitions from DEFINITIONS_PATH and returns them as an array of strings.
	 */
	private static String[] readDefinitions(AssetManager assets) {
		try {
			BufferedReader reader = new AssetFile(assets, DEFINITIONS_PATH).getReader();
			StringBuilder contents = new StringBuilder();
			char[] buffer = new char[10000];
			int read;

			while ((read = reader.read(buffer)) != -1) {
				contents.append(buffer, 0, read);
			}

			return contents.toString().split("\n");
		} catch (IOException e) {
			Logger.e(LOG_TAG, "Failed reading language definitions from: '" + DEFINITIONS_PATH + "'. " + e.getMessage());
			return new String[0];
		}
	}


	/**
	 * Normalizes a YAML boolean to a Java boolean.
	 */
	private boolean parseYamlBoolean(@Nullable String value) {
		if (value == null) {
			return false;
		}

		return switch (value.toLowerCase()) {
			case "true", "on", "yes", "y" -> true;
			default -> false;
		};
	}


	/**
	 * Sets a property based on the key-value pair in a YAML line. If the key does not match any
	 * property, the line is ignored.
	 */
	private void setProperty(@NonNull String line) {
		int colonIndex = line.indexOf(':');
		if (colonIndex == -1) {
			return;
		}

		String key = line.substring(0, colonIndex).trim();
		String value = (colonIndex + 1 < line.length()) ? line.substring(colonIndex + 1).trim() : "";

		switch (key) {
			case "abcString":
				abcString = value;
				return;
			case "currency":
				currency = value;
				return;
			case "dictionaryFile":
				dictionaryFile = value.replaceFirst("\\.\\w+$", "." + BuildConfig.DICTIONARY_EXTENSION);
				return;
			case "filterBySounds":
				filterBySounds = parseYamlBoolean(value);
				return;
			case "hasABC":
				hasABC = parseYamlBoolean(value);
				return;
			case "hasSpaceBetweenWords":
				hasSpaceBetweenWords = parseYamlBoolean(value);
				return;
			case "hasUpperCase":
				hasUpperCase = parseYamlBoolean(value);
				return;
			case "sounds":
				isTranscribed = true;
				return;
			case "locale":
				locale = value;
				return;
			case "name":
				name = value;
				return;
			case "numerals":
				setNumerals(value);
				return;
		}
	}


	/**
	 * Builds the key layout line by line. Returns true when a layout entry is successfully set.
	 */
	private boolean setLayoutEntry(@NonNull String line) {
		if (!inLayout) {
			return inLayout = "layout:\r".equals(line);
		}

		ArrayList<String> layoutEntry = parseList(line);
		if (layoutEntry == null) {
			inLayout = false;
		} else {
			layout.add(layoutEntry);
			return true;
		}

		return false;
	}


	private void setNumerals(@NonNull String yamlList) {
		ArrayList<String> numberList = parseList(yamlList);
		if (numberList == null || numberList.size() != 10) {
			return;
		}

		for (int i = 0; i < 10; i++) {
			numerals.put(i, numberList.get(i));
		}
	}



	/**
	 * Validates a YAML line as an array and returns the character list to be assigned to a given key (a layout entry).
	 * If the YAML line is invalid, NULL will be returned.
	 */
	@Nullable
	private ArrayList<String> parseList(@NonNull String yamlLine) {
		int start = yamlLine.indexOf('[');
		int end = yamlLine.indexOf(']');
		if (start == -1 || end == -1 || start >= end) {
			return null;
		}

		String entryTxt = yamlLine.substring(start + 1, end).replace(" ", "");

		ArrayList<String> entry = new ArrayList<>();
		int last = 0, len = entryTxt.length();

		for (int i = 0; i < len; i++) {
			if (entryTxt.charAt(i) == ',') {
				entry.add(entryTxt.substring(last, i));
				last = i + 1;
			}
		}

		if (last < len) {
			entry.add(entryTxt.substring(last));
		}

		return entry;
	}


	public String getDictionaryFile() {
		return LANGUAGES_DIR + "/dictionaries/" + dictionaryFile;
	}
}
