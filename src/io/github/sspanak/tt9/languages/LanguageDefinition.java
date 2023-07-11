package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.Logger;

public class LanguageDefinition {
	private static final String languagesDir = "languages";
	private static final String definitionsDir = languagesDir + "/definitions";

	public String abcString = "";
	public String dictionaryFile = "";
	public boolean hasUpperCase = true;
	public ArrayList<ArrayList<String>> layout = new ArrayList<>();
	public String locale = "";
	public String name = "";

	public static ArrayList<String> getAllFiles(AssetManager assets) {
		ArrayList<String> files = new ArrayList<>();
		try {
			for (String file : assets.list(definitionsDir)) {
				files.add(definitionsDir + "/" + file);
			}

			Logger.d("LanguageDefinition", "Found: " + files.size() + " languages.");
		} catch (IOException e) {
			Logger.e("tt9.LanguageDefinition", "Failed reading language definitions from: '" + definitionsDir + "'. " + e.getMessage());
		}

		return files;
	}


	public static LanguageDefinition fromFile(AssetManager assetManager, String definitionFile) throws IOException {
		return parse(load(assetManager, definitionFile));
	}


	private static ArrayList<String> load(AssetManager assetManager, String definitionFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(definitionFile), StandardCharsets.UTF_8));
		ArrayList<String> fileContents = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			fileContents.add(line);
		}

		return fileContents;
	}

	/**
	 * Converts "yaml" to a LanguageDefinition object. All properties in the YAML are considered optional,
	 * so the LanguageDefinition defaults will be used when no value is found in the YAML.
	 */
	@NonNull
	private static LanguageDefinition parse(ArrayList<String> yaml) {
		LanguageDefinition definition = new LanguageDefinition();
		String value;

		value = getPropertyFromYaml(yaml, "absString");
		definition.abcString = value != null ? value : definition.abcString;

		value = getPropertyFromYaml(yaml, "dictionaryFile");
		definition.dictionaryFile = value != null ? value : definition.dictionaryFile;

		value = getPropertyFromYaml(yaml, "locale");
		definition.locale = value != null ? value : definition.locale;

		value = getPropertyFromYaml(yaml, "name");
		definition.name = value != null ? value : definition.name;

		//		definition.layout = getLayoutFromYaml(yaml);

		value = getPropertyFromYaml(yaml, "hasUpperCase");
		if (value != null) {
			value = value.toLowerCase();
			definition.hasUpperCase = value.equals("true") || value.equals("on") || value.equals("yes") || value.equals("y");
		}

		return definition;
	}


	/**
	 * getPropertyFromYaml
	 * Finds "property" in the "yaml" and returns its value.
	 * Optional properties are allowed. NULL will be returned when they are missing.
	 */
	@Nullable
	private static String getPropertyFromYaml(ArrayList<String> yaml, String property) {
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

		return null;
	}


	private static ArrayList<ArrayList<String>> getLayoutFromYaml(String yaml) {
		ArrayList<ArrayList<String>> layout = new ArrayList<>();

		Pattern entireLayoutRegex = Pattern.compile("layout:((?:[^\\-]+?-\\s*\\[[^\\]]+\\]){0,10})", Pattern.DOTALL);
		String rawLayout = entireLayoutRegex.matcher(yaml).group(0);
		if (rawLayout == null) {
			return layout;
		}

		rawLayout = rawLayout.trim();
		String[] layoutLines = rawLayout.split("\\s+-");
		if (layoutLines.length < 1) {
			return layout;
		}

		for (String line : layoutLines) {
			line = line
				.trim()
				.replaceAll("^\\s*\\[\\s*", "")
				.replaceAll("#.+$", "")
				.replaceAll("\\s*\\]\\s*$", "");

			ArrayList<String> lineChars = new ArrayList<>();
			for (String lineChar : line.split(",")) {
				lineChar = lineChar.trim();
				if (!lineChar.isEmpty()) {
					lineChars.add(lineChar);
				}
			}

			layout.add(lineChars);
		}

		return layout;
	}


	public String getDictionaryFile() {
		return languagesDir + "/dictionaries/" + dictionaryFile;
	}


	@NonNull
	@Override
	public String toString() {
		return getClass().getSimpleName()
			+ ": { abcString: '" + abcString
			+ "', dictionaryFile: '" + dictionaryFile
			+ "', locale: '" + locale
			+ "', name: '" + name
			+ "', hasUpperCase: " + hasUpperCase
			+ ", layout: " + layout.toString() + " }";
	}
}
