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
	public WordList filter(int minLength, int minWords) {
		WordList filtered = new WordList();
		for (int i = 0; i < size(); i++) {
			if (get(i).word.length() == minLength || filtered.size() < minWords) {
				filtered.add(get(i));
			}
		}
		return filtered;
	}



	@NonNull
	@Override
	public String toString() {
		return toDebugString(20);

	}

	public String toDebugString(int MAX_ITEMS) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size() && i < MAX_ITEMS; i++) {
			sb
				.append("word: ").append(get(i).word)
				.append(" | priority: ").append(get(i).frequency)
				.append("\n");
		}

		if (size() > MAX_ITEMS) {
			sb.append("...\n(Total: ").append(size()).append(")");
		}

		return sb.toString();
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
