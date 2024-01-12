package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
	@Nullable
	@Index
	public String sequenceMedium;
	@Index public byte sequenceShort; // up to 2 digits
	public String word;

	public static Word create(@NonNull Language language, @NonNull String word, int frequency) throws InvalidLanguageCharactersException {
		Word w = new Word();
		w.frequency = frequency;
		w.isCustom = false;
		w.langId = language.getId();
		w.length = word.length();
		w.sequence = language.getDigitSequenceForWord(word);
		w.sequenceMedium = getMediumSequence(w.sequence);
		w.sequenceShort = getShortSequence(w.sequence);
		w.word = word;

		return w;
	}

	public static Word create(@NonNull Language language, @NonNull String word, int frequency, boolean isCustom) throws InvalidLanguageCharactersException {
		Word w = create(language, word, frequency);
		w.isCustom = isCustom;
		return w;
	}

	public static byte getShortSequence(@NonNull String sequence) {
		return sequence.length() == 0 ? 0 : Byte.parseByte(sequence.substring(0, Math.min(2, sequence.length())));
	}

	public static String getMediumSequence(@NonNull String sequence) {
		return sequence.length() < 2 ? "" : sequence.substring(0, Math.min(5, sequence.length()));
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
