package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;

public class PreferenceDeletableWord extends Preference {
	private String word;


	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceDeletableWord(@NonNull Context context) { super(context); }


	public void setWord(String word) {
		this.word = word;
		setTitle(word);
	}


	@Override
	protected void onClick() {
		super.onClick();

		Context context = getContext();

		UI.confirm(
			context,
			context.getString(R.string.delete_words_deleted_confirm_deletion_title),
			context.getString(R.string.delete_words_deleted_confirm_deletion_question, word),
			context.getString(R.string.delete_words_delete),
			this::onDeletionConfirmed,
			null
		);
	}


	private void onDeletionConfirmed() {
		SettingsStore settings = new SettingsStore(getContext());
		WordStoreAsync.deleteCustomWord(
			this::onWordDeleted,
			LanguageCollection.getLanguage(getContext(), settings.getInputLanguage()),
			word
		);
	}


	private void onWordDeleted() {
		if (getParent() instanceof PreferenceCategory) {
			getParent().removePreference(this);
		}

		Activity activity = (Activity) getContext();
		activity.runOnUiThread(
			() -> UI.toastFromAsync(getContext(), activity.getString(R.string.delete_words_deleted_x, word))
		);
	}
}
