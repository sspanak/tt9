package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceDeletableWord extends ScreenPreference {
	private DeletableWordsList parent;
	private String word;


	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceDeletableWord(@NonNull Context context) { super(context); }


	@Override protected int getDefaultLayout() { return R.layout.pref_deletable_word; }
	@Override protected int getLargeLayout() { return R.layout.pref_deletable_word_large; }


	void setParent(DeletableWordsList parent) {
		if (parent != null) {
			this.parent = parent;
		}
	}


	void setWord(String word) {
		this.word = word;
		setTitle(word);
	}


	@Override
	protected void onClick() {
		super.onClick();

		Context context = getContext();
		new PopupBuilder(context)
			.setCancelable(true)
			.setTitle(context.getString(R.string.delete_words_deleted_confirm_deletion_title))
			.setMessage(context.getString(R.string.delete_words_deleted_confirm_deletion_question, word))
			.setNegativeButton(true, null)
			.setPositiveButton(context.getString(R.string.delete_words_delete), this::onDeletionConfirmed)
			.show();
	}


	private void onDeletionConfirmed() {
		SettingsStore settings = new SettingsStore(getContext());
		DataStore.deleteCustomWord(
			this::onWordDeleted,
			LanguageCollection.getLanguage(settings.getInputLanguage()),
			Logger.isDebugLevel() ? word.replaceFirst(" / .+$", "") : word // strip debug info, if any
		);
	}


	private void deleteSelf() {
		if (parent != null) {
			parent.delete(this);
		} else if (getParent() instanceof PreferenceCategory) {
			getParent().removePreference(this);
		}

		UI.toastFromAsync(getContext(), getContext().getString(R.string.delete_words_deleted_x, word));
	}


	private void onWordDeleted() {
		((Activity) getContext()).runOnUiThread(this::deleteSelf);
	}
}
