package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.TextChangeWatcher;
import io.github.sspanak.tt9.util.colors.AccentSystemColor;
import io.github.sspanak.tt9.util.colors.ErrorSystemColor;

public class TextInputPreference extends ScreenPreference {
	@Nullable PreferenceViewHolder viewHolder;
	@NonNull protected String text = "";


	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public TextInputPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	public TextInputPreference(@NonNull Context context) {
		super(context);
	}


	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		this.viewHolder = holder;
		setTextField(getTextField());
	}


	@Nullable
	protected EditText getTextField() {
		if (viewHolder == null) {
			return null;
		}
		return viewHolder.itemView.findViewById(R.id.input_text_input_field);
	}


	@Override public int getDefaultLayout() { return R.layout.pref_input_text; }
	@Override public int getLargeLayout() { return R.layout.pref_input_text_large; }


	/**
	 * Called when the text in the EditText field changes. The new text can then be retrieved with
	 * getText(). Override if needed.
	 */
	protected void onTextChange() {
		// Override to handle text changes
	}


	/**
	 * Sets or clears an error message in the EditText field, if available.
	 * Also changes the text field border color to accent or error color.
	 */
	protected void setError(String error) {
		EditText textField = getTextField();
		if (textField == null) {
			return;
		}

		final boolean noError = error == null || error.isEmpty();
		textField.setError(noError ? null : error);

		int color = noError ? new AccentSystemColor(getContext()).get() : new ErrorSystemColor(getContext()).get();
		textField.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
	}


	public CharSequence getText() {
		return text;
	}


	/**
	 * Sets the text both internally and in the EditText field, if available.
	 * Does not trigger onTextChange().
	 */
	public void setText(@Nullable CharSequence newText) {
		if (newText != null && !text.equals(newText.toString())) {
			text = newText.toString();
		}

		EditText textView = getTextField();
		if (textView != null) {
			textView.setText(newText);
		}
	}


	/**
	 * Override this to set a custom icon. The default is no icon.
	 */
	protected int getIconResource() {
		return 0;
	}


	private void setTextField(EditText textField) {
		if (textField == null) {
			return;
		}

		setTextChangedListener(textField);
		ignoreEnter(textField);
		setTextFieldIcon(textField, getIconResource());

		// avoid unnecessary setText() to save resources, and because it moves the cursor to the start
		if (!text.contentEquals(textField.getText())) {
			textField.setText(text);
			textField.setSelection(text.length()); // move cursor to the end
		}
	}


	/**
	 * Sets an icon in the text field, if available. Note, that this is different from the preference icon.
	 */
	private void setTextFieldIcon(@NonNull EditText textField, int icon) {
		if (icon == 0) {
			return;
		}

		final Context context = getContext();
		final Drawable searchIcon = AppCompatResources.getDrawable(context, icon);
		if (searchIcon != null) {
			searchIcon.setTint(ContextCompat.getColor(context, R.color.keyboard_text));
		}

		if (LanguageKind.isRTL(LanguageCollection.getDefault())) {
			textField.setCompoundDrawablesWithIntrinsicBounds(null, null, searchIcon, null);
		} else {
			textField.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
		}
	}


	private void setTextChangedListener(@Nullable EditText textField) {
		if (textField == null) {
			return;
		}

		Object tag = textField.getTag();
		if (tag instanceof TextChangeWatcher) {
			textField.removeTextChangedListener((TextChangeWatcher) tag);
		}

		TextChangeWatcher watcher = new TextChangeWatcher(this::checkTextChange);
		textField.addTextChangedListener(watcher);
		textField.setTag(watcher);
	}


	/**
	 * Internal text change detector that calls the onTextChange() when needed.
	 */
	private void checkTextChange(Editable txt) {
		String newText = txt != null ? txt.toString() : "";
		if (!text.equals(newText)) {
			text = newText;
			onTextChange();
		}
	}


	/**
	 * This prevents IllegalStateException "focus search returned a view that wasn't able to take focus!",
	 * which is thrown when the EditText is focused and it receives a simulated ENTER key event.
	 */
	private void ignoreEnter(@NonNull EditText textField) {
		textField.setOnKeyListener((v, keyCode, e) -> keyCode == KeyEvent.KEYCODE_ENTER);
	}
}
