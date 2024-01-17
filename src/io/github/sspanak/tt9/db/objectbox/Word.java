package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;
import io.objectbox.annotation.ConflictStrategy;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

@Entity
public class Word {
	@Id public long id;
	public short frequency;
	public boolean isCustom;
	public short langId;
	@Index public int position;
	@Index public String word;

	public static Word create(@NonNull Language language, @NonNull String word, short frequency, int position) {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.langId = (short)language.getId();
		w.position = position;
		w.word = word;

		return w;
	}

	public static Word create(@NonNull Language language, @NonNull String word, short frequency, int position, boolean isCustom) {
		Word w = create(language, word, frequency, position);
		w.isCustom = isCustom;
		return w;
	}

	@NonNull
	@Override
	public String toString() {
		return word;
	}
}
