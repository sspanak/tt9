package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

public class Word {
	public short frequency;
	public boolean isCustom;
	public int position;
	public String word;


	public static Word create(@NonNull String word, short frequency, int position) {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.position = position;
		w.word = word;

		return w;
	}


	public static Word create(@NonNull String word, short frequency, int position, boolean isCustom) {
		Word w = create(word, frequency, position);
		w.isCustom = isCustom;
		return w;
	}
}
