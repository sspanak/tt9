package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

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


	private static String load(AssetManager assetManager, String definitionFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(definitionFile), StandardCharsets.UTF_8));
		StringBuilder fileContents = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			fileContents.append(line).append("\n");
		}

		return fileContents.toString();
	}


	private static LanguageDefinition parse(String yaml) {
		LanguageDefinition definition = new LanguageDefinition();

		definition.abcString = getPropertyFromYaml(yaml, "absString");
		definition.dictionaryFile = getPropertyFromYaml(yaml, "dictionaryFile");
		definition.locale = getPropertyFromYaml(yaml, "locale");
		definition.name = getPropertyFromYaml(yaml, "name");
//		definition.layout = getLayoutFromYaml(yaml);
//
//		String hasUpperCase = getPropertyFromYaml(yaml, "hasUpperCase");
//		if (hasUpperCase != null) {
//			hasUpperCase = hasUpperCase.toLowerCase();
//			definition.hasUpperCase = hasUpperCase.equals("true") || hasUpperCase.equals("on") || hasUpperCase.equals("yes") || hasUpperCase.equals("y");
//		}

		return definition;
	}


	private static String getPropertyFromYaml(String yaml, String property) {
		try {
			String regex = property + ":\\s*([^\\n]+)";
			Pattern pattern = Pattern.compile(regex);
			return pattern.matcher(yaml).group(0);
		} catch (Exception e) {
			Logger.w("LanguageDefinition", "Property '" + property + "' not found or invalid. " + e.getMessage());
			return "";
		}
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
}
