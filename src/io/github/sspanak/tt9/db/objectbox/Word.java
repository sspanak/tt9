package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;
import io.objectbox.annotation.ConflictStrategy;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Word {
	@Id public long id;
	public int frequency;
	public boolean isCustom;
	public int langId;
	@Unique int position;
	public String word;
	@Unique(onConflict = ConflictStrategy.REPLACE) public String uniqueId;

	public static Word create(@NonNull Language language, @NonNull String word, int frequency, int position) {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.langId = language.getId();
		w.uniqueId = (language.getId() + "-" + word);
		w.word = word;
		w.position = position;

		return w;
	}

	public static Word create(@NonNull Language language, @NonNull String word, int frequency, int position, boolean isCustom) {
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
