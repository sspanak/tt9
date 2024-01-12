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
		int listSize = size();
		if (listSize == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(get(0).toString().length());
		for (int i = 0; i < listSize; i++) {
			sb.append(get(i).toDebugString());
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
		int listSize = size();
		ArrayList<String> strings = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; i++) {
			strings.add(get(i).toString());
		}
		return strings;
	}
}
