package io.github.sspanak.tt9.ime.modes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

/** Shared ideogram behavior and the optional Chinese editor's host contract. */
public class ModePinyin extends ModeIdeograms {
	protected ModePinyin(SettingsStore settings, Language language, InputType inputType, TextField textField) {
		super(settings, language, inputType, textField);
	}

	@Override public boolean validateLanguage(@Nullable Language language) { return LanguageKind.isChinesePinyin(language); }
	public boolean isPinyinSequenceEditing() { return false; }
	public String getPinyinSequencePreview() { return ""; }
	public int getPinyinSequenceCursor() { return -1; }
	public boolean beginPinyinSequenceEdit(String reading) { return false; }
	public boolean movePinyinSequenceCursor(int direction) { return false; }
	public boolean setPinyinSequenceCursor(int offset) { return false; }
	public boolean finishPinyinSequenceEdit() { return false; }
	public boolean cancelPinyinSequenceEdit() { return false; }
	public boolean isPinyinEditPreview(@Nullable String word) { return false; }
	public boolean isPinyinSequenceTransitionPending() { return false; }
	public long getPinyinSuggestionsRevision() { return -1; }
	public void onPinyinSuggestionsShown(long revision) {}

	@Override protected String getPreferredChar() { return Characters.getChar(language, settings.getDoubleZeroChar()); }
	@NonNull @Override public String toAccessibilityString(@NonNull Context context) { return language.getName(); }
}
