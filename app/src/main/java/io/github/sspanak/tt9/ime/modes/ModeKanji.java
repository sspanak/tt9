package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeKanji extends ModePinyin {
	protected ModeKanji(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
		NAME = language.getName().replace(" / ローマ字", "");
	}


	@NonNull
	@Override
	public ArrayList<String> getSuggestions() {
		ArrayList<String> newSuggestions = new ArrayList<>();
		for (String s : suggestions) {
			// "Ql" is the transcription of "—" in the database, as defined in Japanese.yml. However, this
			// has only technical meaning. When displaying the suggestions, we want to show "—" for better
			// readability.
			newSuggestions.add(s.replaceAll("Ql", "—"));
		}

		return newSuggestions;
	}


	@Override
	public boolean onReplaceSuggestion(@NonNull String word) {
		// revert to the transcription, so that filtering works correctly
		return super.onReplaceSuggestion(word.replaceAll("—", "Ql"));
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		if (digitSequence.isEmpty()) {
			return false;
		}

		String nextSequence = digitSequence + (char)(nextKey + '0');
		if (nextSequence.endsWith(seq.CHARS_1_SEQUENCE) && !predictions.noDbWords()) {
			return false;
		}

		return super.shouldAcceptPreviousSuggestion(nextKey, hold);
	}


	@Override
	public boolean validateLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isJapanese(newLanguage);
	}
}
