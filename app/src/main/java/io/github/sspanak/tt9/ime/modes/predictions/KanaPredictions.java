package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class KanaPredictions extends IdeogramPredictions {
	private final char SEQUENCE_PREFIX;
	@NonNull private final String STEM_PREFIX;
	private final int STEM_PREFIX_LENGTH;

	public KanaPredictions(SettingsStore settings, TextField textField, Sequences sequences, boolean isKatakana) {
		super(settings, textField, sequences);

		SEQUENCE_PREFIX = isKatakana ? '1' : '0';
		STEM_PREFIX = isKatakana ? "Qk" : "Qh";
		STEM_PREFIX_LENGTH = STEM_PREFIX.length();

		// Avoid incorrect order of words like "か" and "じゃ". They have different char lengths,
		// but share the same sequence, "52", so we must consider them equivalent.
		orderWordsByLength = false;
	}

	@Override
	public Predictions setDigitSequence(@NonNull String newSequence) {
		super.setDigitSequence(newSequence);
		digitSequence = digitSequence.isEmpty() ? digitSequence : SEQUENCE_PREFIX + digitSequence;
		return this;
	}

	@Override
	public WordPredictions setStem(String stem) {
		return super.setStem(STEM_PREFIX + stem);
	}

	@Override
	protected String stripNativeWord(@NonNull String dbTranscription) {
		return stripStemPrefix(super.stripNativeWord(dbTranscription));
	}

	@NonNull
	private String stripStemPrefix(@NonNull String transcription) {
		return transcription.length() > STEM_PREFIX_LENGTH ? transcription.substring(STEM_PREFIX_LENGTH) : transcription;
	}

	@Override
	public void onAcceptIdeogram(String word) throws InvalidLanguageCharactersException {
		String transcription = getTranscription(word);
		String sequence = SEQUENCE_PREFIX + language.getDigitSequenceForWord(stripStemPrefix(transcription));
		super.onAccept(transcription + word, sequence);
	}

	@Override
	protected void onNoWords() {
		if (digitSequence.length() == 2) {
			transcriptions = generateWordVariations(null);
			words = new ArrayList<>(transcriptions);
		}
	}
}
