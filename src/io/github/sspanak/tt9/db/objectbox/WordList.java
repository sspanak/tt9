package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class WordList extends ArrayList<Word> {
	public WordList() {
		super();
	}


	public WordList(@NonNull List<Word> words) {
		addAll(words);
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size(); i++) {
			sb
				.append("word: ").append(get(i).word)
				.append(" | sequence: ").append(get(i).sequence)
				.append(" | priority: ").append(get(i).frequency)
				.append("\n");
		}

		return sb.toString();
	}


	@NonNull
	public WordList filter(int minLength, int minWords) {
		WordList filtered = new WordList();
		for (int i = 0; i < size(); i++) {
			if (get(i).length == minLength || filtered.size() < minWords) {
				filtered.add(get(i));
			}
		}
		return filtered;
	}


	@NonNull
	public ArrayList<String> toStringList() {
		ArrayList<String> strings = new ArrayList<>();
		for (int i = 0; i < size(); i++) {
			strings.add(get(i).word);
		}
		return strings;
	}
}
