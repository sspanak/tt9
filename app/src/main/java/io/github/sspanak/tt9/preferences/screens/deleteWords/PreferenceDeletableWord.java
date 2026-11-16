package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceDeletableWord extends ScreenPreference {
	private DeletableWordsList parent;
	private Language language;
	private String word;


	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceDeletableWord(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceDeletableWord(@NonNull Context context) { super(context); }


	@Override public int getDefaultLayout() { return R.layout.pref_deletable_word; }
	@Override public int getLargeLayout() { return R.layout.pref_deletable_word_large; }


	void setParent(DeletableWordsList parent) {
		if (parent != null) {
			this.parent = parent;
		}
	}


	void setWord(CustomWord word) {
		setWord(word.language, word.word, word.sequence);
	}


	private void setWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		this.word = word;
		this.language = language;
		setTitle(
			Logger.isDebugLevel() ? word + " / " + sequence + " / " + language.getName() : word + " / " + language.getName()
		);
	}


	@Override
	protected void onClick() {
		super.onClick();
		showEditDialog();
	}


	private void showEditDialog() {
		Context context = getContext();

		EditText input = new EditText(context);
		input.setSingleLine(true);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		input.setText(word);
		input.setSelection(word.length());

		FrameLayout container = new FrameLayout(context);
		int padding = Math.round(20 * context.getResources().getDisplayMetrics().density);
		container.setPadding(padding, 0, padding, 0);
		container.addView(input);

		new PopupBuilder(context)
			.setCancelable(true)
			.setTitle(context.getString(R.string.edit_word_title))
			.setMessage(language.getName())
			.setView(container)
			.setNegativeButton(true, null)
			.setNeutralButton(context.getString(R.string.delete), this::confirmDeletion)
			.setPositiveButton(context.getString(R.string.edit_word_save), () -> onSave(input.getText().toString()))
			.show();

		input.requestFocus();
	}


	private void onSave(@NonNull String newWord) {
		final String oldWord = word;
		DataStore.editCustomWord(
			(result) -> ((Activity) getContext()).runOnUiThread(() -> onWordEdited(oldWord, result)),
			language,
			oldWord,
			newWord
		);
	}


	private void onWordEdited(@NonNull String oldWord, @NonNull AddWordResult result) {
		Context context = getContext();

		if (result.statusCode() != AddWordResult.CODE_SUCCESS) {
			UI.toastLong(context, result.toHumanFriendlyString(context));
			return;
		}

		if (oldWord.equals(result.word())) {
			return;
		}

		String sequence;
		try {
			sequence = language.getDigitSequenceForWord(result.word());
		} catch (Exception e) {
			sequence = "";
		}

		setWord(language, result.word(), sequence);
		UI.toast(context, context.getString(R.string.edit_word_success, oldWord, result.word()));
	}


	private void confirmDeletion() {
		Context context = getContext();
		new PopupBuilder(context)
			.setCancelable(true)
			.setTitle(context.getString(R.string.delete_words_deleted_confirm_deletion_title))
			.setMessage(context.getString(R.string.delete_words_deleted_confirm_deletion_question, word))
			.setNegativeButton(true, null)
			.setPositiveButton(context.getString(R.string.delete), this::onDeletionConfirmed)
			.show();
	}


	private void onDeletionConfirmed() {
		DataStore.deleteCustomWord(this::onWordDeleted, language, word);
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
