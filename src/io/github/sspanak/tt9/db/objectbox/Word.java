package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;
import io.objectbox.annotation.ConflictStrategy;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.IndexType;
import io.objectbox.annotation.Unique;

@Entity
public class Word {
	@Id public long id;
	public int frequency;
	public boolean isCustom;
	public int langId;
	public int length;
	@Index(type = IndexType.VALUE) public String sequence;
	@Index public byte sequenceShort; // up to 2 digits
	@Unique(onConflict = ConflictStrategy.REPLACE) public String uniqueId;
	public String word;

	public static Word create(@NonNull Language language, @NonNull String word, int frequency) throws InvalidLanguageCharactersException {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.langId = language.getId();
		w.length = word.length();
		w.sequence = language.getDigitSequenceForWord(word);
		w.sequenceShort = shrinkSequence(w.sequence);
		w.uniqueId = (language.getId() + "-" + word);
		w.word = word;

		return w;
	}

	public static Word create(@NonNull Language language, @NonNull String word, int frequency, boolean isCustom) throws InvalidLanguageCharactersException {
		Word w = create(language, word, frequency);
		w.isCustom = isCustom;
		return w;
	}

	public static Byte shrinkSequence(@NonNull String sequence) {
		return Byte.parseByte(sequence.substring(0, Math.min(2, sequence.length())));
	}
}
