package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class KanaPredictions extends IdeogramPredictions {
	private final char SEQUENCE_PREFIX;
	@NonNull private final String STEM_PREFIX;
	private final int STEM_PREFIX_LENGTH;

	public KanaPredictions(SettingsStore settings, TextField textField, boolean isKatakana) {
		super(settings, textField);
		SEQUENCE_PREFIX = isKatakana ? '1' : '0';
		STEM_PREFIX = isKatakana ? "Qk" : "Qh";
		STEM_PREFIX_LENGTH = STEM_PREFIX.length();
	}

	@Override
	public Predictions setDigitSequence(@NonNull String digitSequence) {
		return super.setDigitSequence(SEQUENCE_PREFIX + digitSequence);
	}

	@Override
	public WordPredictions setStem(String stem) {
		return super.setStem(STEM_PREFIX + stem);
	}

	@Override
	protected String stripNativeWord(@NonNull String dbTranscription) {
		String transcription = super.stripNativeWord(dbTranscription);
		return transcription.length() > STEM_PREFIX_LENGTH ? transcription.substring(STEM_PREFIX_LENGTH) : transcription;
	}
}
