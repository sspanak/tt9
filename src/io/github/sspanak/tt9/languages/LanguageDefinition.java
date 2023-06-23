package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
		} catch (IOException e) {
			Logger.e("tt9.LanguageDefinition", "Failed reading language definitions from: '" + definitionsDir + "'. " + e.getMessage());
		}

		return files;
	}

	public static LanguageDefinition fromFile(AssetManager assets, String definitionFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(definitionFile), StandardCharsets.UTF_8));
		return new Yaml().loadAs(reader, LanguageDefinition.class);
	}

	public String getDictionaryFile() {
		return languagesDir + "/dictionaries/" + dictionaryFile;
	}
}
