package io.github.sspanak.tt9.db.entities;

import java.util.ArrayList;

public class WordList extends ArrayList<Word> {
	public void add(String word, int frequency, int position) {
		add(Word.create(word, frequency, position));
	}

	public ArrayList<String> toStringList() {
		ArrayList<String> list = new ArrayList<>(size());
		for (Word word : this) {
			list.add(word.word);
		}
		return list;
	}
}
