package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.AssetFile;
import io.github.sspanak.tt9.util.Logger;

public class LanguageDefinition extends AssetFile {
	private static final String LOG_TAG = LanguageDefinition.class.getSimpleName();

	private static final String languagesDir = "languages";
	private static final String definitionsDir = languagesDir + "/definitions";

	public String abcString = "";
	public String dictionaryFile = "";
	public boolean hasSpaceBetweenWords = true;
	public boolean hasUpperCase = true;
	public boolean isSyllabary = false;
	public ArrayList<ArrayList<String>> layout = new ArrayList<>();
	public String locale = "";
	public String name = "";


	public LanguageDefinition(AssetManager assets, String name) {
		super(assets, definitionsDir + "/" + name);
	}


	/**
	 * getAllFiles
	 * Returns a list of the paths of all language definition files in the assets folder or an empty list on error.
	 */
	public static ArrayList<String> getAllFiles(AssetManager assets) {
		ArrayList<String> files = new ArrayList<>();
		try {
			files.addAll(Arrays.asList(assets.list(definitionsDir)));
			Logger.d(LOG_TAG, "Found: " + files.size() + " languages.");
		} catch (IOException | NullPointerException e) {
			Logger.e(LOG_TAG, "Failed reading language definitions from: '" + definitionsDir + "'. " + e.getMessage());
		}

		return files;
	}


	/**
	 * fromFile
	 * Takes the path to a language definition in the assets folder and parses that file into a LanguageDefinition
	 * or throws an IOException on error.
	 */
	public static LanguageDefinition fromFile(AssetManager assetManager, String definitionFile) throws IOException {
		LanguageDefinition definition = new LanguageDefinition(assetManager, definitionFile);
		definition.parse(definition.load(definition));
		return definition;
	}


	/**
	 * load
	 * Loads a language definition file from the assets folder into a String or throws an IOException on error.
	 */
	private ArrayList<String> load(LanguageDefinition definitionFile) throws IOException {
		BufferedReader reader = definitionFile.getReader();
		ArrayList<String> fileContents = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			fileContents.add(line);
		}

		return fileContents;
	}


	/**
	 * parse
	 * Converts "yaml" to a LanguageDefinition object. All properties in the YAML are considered optional,
	 * so the LanguageDefinition defaults will be used when some property is omitted.
	 * Had to write all this, because the only usable library, SnakeYAML, works fine on Android 10+,
	 * but causes crashes on older devices.
	 */

	private void parse(ArrayList<String> yaml) {
		abcString = getPropertyFromYaml(yaml, "abcString", abcString);

		dictionaryFile = getPropertyFromYaml(yaml, "dictionaryFile", dictionaryFile);
		if (dictionaryFile != null) {
			dictionaryFile = dictionaryFile.replaceFirst("\\.\\w+$", "." + BuildConfig.DICTIONARY_EXTENSION);
		}

		hasSpaceBetweenWords = getPropertyFromYaml(yaml, "hasSpaceBetweenWords", hasSpaceBetweenWords);
		hasUpperCase = getPropertyFromYaml(yaml, "hasUpperCase", hasUpperCase);
		isSyllabary = hasYamlProperty(yaml, "sounds");
		layout = getLayoutFromYaml(yaml);
		locale = getPropertyFromYaml(yaml, "locale", locale);
		name = getPropertyFromYaml(yaml, "name", name);


	}


	/**
	 * getPropertyFromYaml
	 * Finds "property" in the "yaml" and returns its value.
	 * Optional properties are allowed. If the property is not found, "defaultValue" will be returned.
	 */
	@Nullable
	private String getPropertyFromYaml(ArrayList<String> yaml, String property, String defaultValue) {
		for (String line : yaml) {
			line = line.replaceAll("#.+$", "").trim();
			String[] parts = line.split(":");
			if (parts.length < 2) {
				continue;
			}

			if (property.equals(parts[0].trim())) {
				return parts[1].trim();
			}
		}

		return defaultValue;
	}


	private boolean hasYamlProperty(ArrayList<String> yaml, String property) {
		final String yamlProperty = property + ":";

		for (String line : yaml) {
			if (line.startsWith(yamlProperty)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * The boolean variant of getPropertyFromYaml. It returns true if the property is found and is:
	 * "true", "on", "yes" or "y".
	 */
	private boolean getPropertyFromYaml(ArrayList<String> yaml, String property, boolean defaultValue) {
		String value = getPropertyFromYaml(yaml, property, null);
		if (value == null) {
			return defaultValue;
		}

		value = value.toLowerCase();
		return value.equals("true") || value.equals("on") || value.equals("yes") || value.equals("y");
	}


	/**
	 * getLayoutFromYaml
	 * Finds and extracts the keypad layout. Less than 10 keys are accepted allowed leaving the ones up to 9-key empty.
	 */
	@NonNull
	private ArrayList<ArrayList<String>> getLayoutFromYaml(ArrayList<String> yaml) {
		ArrayList<ArrayList<String>> layout = new ArrayList<>();

		boolean inLayout = false;
		for (int i = 0; i < yaml.size(); i++) {
			if (yaml.get(i).contains("layout")) {
				inLayout = true;
				continue;
			}

			if (inLayout) {
				ArrayList<String> lineChars = getLayoutEntryFromYamlLine(yaml.get(i));
				if (lineChars != null) {
					layout.add(lineChars);
				} else {
					break;
				}
			}
		}

		return layout;
	}


	/**
	 * getLayoutEntryFromYamlLine
	 * Validates a YAML line as an array and returns the character list to be assigned to a given key (a layout entry).
	 * If the YAML line is invalid, NULL will be returned.
	 */
	@Nullable
	private ArrayList<String> getLayoutEntryFromYamlLine(String yamlLine) {
		if (!yamlLine.contains("[") || !yamlLine.contains("]")) {
			return null;
		}

		String line = yamlLine
			.replaceAll("#.+$", "")
			.replace('-', ' ')
			.replace('[', ' ')
			.replace(']', ' ')
			.replace(" ", "");

		return new ArrayList<>(Arrays.asList(line.split(",")));
	}


	public String getDictionaryFile() {
		return languagesDir + "/dictionaries/" + dictionaryFile;
	}
}
