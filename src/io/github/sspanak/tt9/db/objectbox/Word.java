package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.IndexType;

@Entity
public class Word {
	@Id public long id;
	public int frequency;
	public boolean isCustom;
	public int langId;
	public int length;
	@Index(type = IndexType.VALUE) public String sequence;
	@Index public short sequenceShort; // up to 2 digits
	public String word;

	public static Word create(@NonNull Language language, @NonNull String word, int frequency) throws InvalidLanguageCharactersException {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.langId = language.getId();
		w.length = word.length();
		w.sequence = language.getDigitSequenceForWord(word);
		w.sequenceShort = shrinkSequence(w.sequence);
		w.word = word;

		return w;
	}

	public static Word create(@NonNull Language language, @NonNull String word, int frequency, boolean isCustom) throws InvalidLanguageCharactersException {
		Word w = create(language, word, frequency);
		w.isCustom = isCustom;
		return w;
	}

	public static short shrinkSequence(@NonNull String sequence) {
		int length = sequence.length();
		if (length == 0) {
			return 0;
		} else if (length == 1) {
			return (short) (sequence.charAt(0) - '0');
		}

		short shrunk = (short) (10 * (sequence.charAt(0) - '0') + (sequence.charAt(1) - '0'));
		return (length > 2) ? (short) -shrunk : shrunk;
	}

	@NonNull
	@Override
	public String toString() {
		return word;
	}

	public String toDebugString() {
		return "word: " + word +
			" | sequence: " + sequence +
			" | short: " + sequenceShort +
			" | priority: " + frequency +
			"\n";
	}
}
