package io.github.sspanak.tt9.chinese;

import android.app.Dialog;
import android.os.LocaleList;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.ViewGroup;

import java.util.Locale;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.PopupBuilder;

/** Explicit word and reading input; editing never changes the host application's composing text. */
final class ChineseUserWordDialog {
	private ChineseUserWordDialog() {}

	static void show(PreferencesActivity activity, CustomWord previous, Runnable onChanged) {
		LinearLayout content = new LinearLayout(activity);
		content.setOrientation(LinearLayout.VERTICAL);
		int padding = (int) (20 * activity.getResources().getDisplayMetrics().density);
		content.setPadding(padding, 0, padding, 0);
		TextView help = new TextView(activity);
		help.setText(R.string.chinese_user_reading_help);
		help.setPadding(0, 0, 0, padding / 2);
		content.addView(help);
		EditText word = new EditText(activity);
		word.setId(R.id.chinese_user_word_input);
		word.setHint(R.string.chinese_user_word_hint);
		word.setSingleLine(true);
		word.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
		word.setImeHintLocales(new LocaleList(Locale.SIMPLIFIED_CHINESE));
		EditText reading = new EditText(activity);
		reading.setId(R.id.chinese_user_reading_input);
		reading.setHint(R.string.chinese_user_reading_hint);
		reading.setSingleLine(true);
		reading.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		reading.setImeHintLocales(new LocaleList(Locale.ENGLISH));
		reading.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
		if (previous != null) {
			word.setText(previous.getDisplayWord());
			reading.setText(previous.getReading().replace('\'', ' '));
		}
		content.addView(word);
		content.addView(reading);
		Button delete = new Button(activity);
		delete.setId(R.id.chinese_user_delete);
		delete.setText(R.string.chinese_user_delete);
		if (previous != null) content.addView(delete);
		LinearLayout actions = new LinearLayout(activity);
		Button cancel = new Button(activity);
		cancel.setId(android.R.id.button2);
		cancel.setText(android.R.string.cancel);
		Button save = new Button(activity);
		save.setId(android.R.id.button1);
		save.setText(R.string.chinese_user_save);
		actions.addView(cancel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		actions.addView(save, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		content.addView(actions);
		ScrollView scroll = new ScrollView(activity);
		scroll.addView(content);
		Dialog dialog = new PopupBuilder(activity)
			.setTitle(activity.getString(previous == null ? R.string.chinese_user_add : R.string.chinese_user_edit))
			.setView(scroll)
			.show();
		cancel.setOnClickListener(view -> dialog.dismiss());
		save.setOnClickListener(view -> {
			CustomWord entry;
			try { entry = CustomWord.chinese(word.getText().toString(), reading.getText().toString()); }
			catch (IllegalArgumentException invalid) { reading.setError(activity.getString(R.string.chinese_user_invalid)); return; }
			save.setEnabled(false);
			DataStore.replaceCustom(result -> activity.runOnUiThread(() -> {
				if (activity.isFinishing() || activity.isDestroyed()) return;
				if (result.statusCode() == AddWordResult.CODE_SUCCESS) { dialog.dismiss(); onChanged.run(); }
				else { save.setEnabled(true); reading.setError(result.toHumanFriendlyString(activity)); }
			}), previous, entry, activity.getSettings().getPredictTopWords());
		});
		delete.setOnClickListener(view -> new PopupBuilder(activity)
			.setTitle(activity.getString(R.string.chinese_user_delete))
			.setMessage(activity.getString(R.string.chinese_user_delete_confirm, previous.getDisplayWord(), previous.getReading()))
			.setNegativeButton(true, null)
			.setPositiveButton(activity.getString(R.string.chinese_user_delete), () -> DataStore.deleteCustomWord(
				() -> activity.runOnUiThread(() -> { if (!activity.isDestroyed()) { dialog.dismiss(); onChanged.run(); } }), previous.language, previous.word))
			.show());
	}
}
