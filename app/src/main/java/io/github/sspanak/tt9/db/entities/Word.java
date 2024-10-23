package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Word {
	public int frequency;
	public int position;
	public String word;

	public static Word create(@NonNull String word, int frequency, int position) {
		Word w = new Word();
		w.frequency = frequency;
		w.position = position;
		w.word = word;

		return w;
	}

	public static ArrayList<Word> create(WordFileLine line, int position) {
		final int wordsCount = line.words.size();
		ArrayList<Word> words = new ArrayList<>(wordsCount);
		for (int i = 0; i < wordsCount; i++) {
			words.add(create(line.words.get(i), wordsCount - i, position + i));
		}

		return words;
	}
}
