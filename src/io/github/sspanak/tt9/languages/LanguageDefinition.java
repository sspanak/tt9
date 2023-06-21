package io.github.sspanak.tt9.languages;

import android.content.res.AssetManager;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LanguageDefinition {
	public String locale = "";
	public String dictionaryFile = "";
	public String abcString = "";
	public String name = "";
	public boolean hasUpperCase = true;
	public ArrayList<ArrayList<String>> keys = new ArrayList<>();


	public static ArrayList<LanguageDefinition> getAll(AssetManager assets) throws Exception {
		final String definitionsDir = "languages/definitions";
		final ArrayList<LanguageDefinition> definitions = new ArrayList<>();

		try {
			for (String fileName : assets.list(definitionsDir)) {
				String filePath = definitionsDir + "/" + fileName;
				BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(filePath), StandardCharsets.UTF_8));
				definitions.add(new Yaml().loadAs(reader, LanguageDefinition.class));
			}
		} catch (IOException e) {
			throw new Exception("Failed reading language definitions from: '" + definitionsDir + "'. " + e.getMessage());
		}

		return definitions;
	}
}
