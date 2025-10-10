package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.colors.AccentSystemColor;
import io.github.sspanak.tt9.util.colors.ErrorSystemColor;

abstract public class TextInputPreference extends ScreenPreference implements TextWatcher {
	protected EditText textField;
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
		setTextField(holder);
		if (textField != null) {
			ignoreEnter();
			textField.addTextChangedListener(this);
		}
	}


	@Override protected int getDefaultLayout() { return R.layout.pref_input_text; }
	@Override protected int getLargeLayout() { return R.layout.pref_input_text_large; }


	/**
	 * Sets or clears an error message in the EditText field, if available.
	 * Also changes the text field border color to accent or error color.
	 */
	protected void setError(String error) {
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
	protected void setText(@Nullable CharSequence newText) {
		if (newText != null && !text.equals(newText.toString())) {
			text = newText.toString();
		}

		if (textField != null) {
			textField.setText(newText);
		}
	}


	/**
	 * Override this to set a custom icon. The default is no icon.
	 */
	protected int getIconResource() {
		return 0;
	}


	private void setTextField(@NonNull PreferenceViewHolder holder) {
		EditText editText = holder.itemView.findViewById(R.id.input_text_input_field);
		if (editText != null) {
			textField = editText;
			textField.setText(text);
			setTextFieldIcon(getIconResource());
		}
	}


	/**
	 * Sets an icon in the text field, if available. Note, that this is different from the preference icon.
	 */
	private void setTextFieldIcon(int icon) {
		if (icon == 0) {
			return;
		}

		final Context context = getContext();
		final Drawable searchIcon = AppCompatResources.getDrawable(context, icon);
		if (searchIcon != null) {
			searchIcon.setTint(context.getResources().getColor(R.color.keyboard_text));
		}

		if (LanguageKind.isRTL(LanguageCollection.getDefault())) {
			textField.setCompoundDrawablesWithIntrinsicBounds(null, null, searchIcon, null);
		} else {
			textField.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
		}
	}


	/**
	 * Internal text change detector that calls the onTextChange() when needed.
	 */
	@Override
	public void afterTextChanged(Editable txt) {
		String newText = txt != null ? txt.toString() : "";
		if (!text.equals(newText)) {
			text = newText;
			onTextChange();
		}
	}


	// unused
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


	/**
	 * This prevents IllegalStateException "focus search returned a view that wasn't able to take focus!",
	 * which is thrown when the EditText is focused and it receives a simulated ENTER key event.
	 */
	private void ignoreEnter() {
		textField.setOnKeyListener((v, keyCode, e) -> keyCode == KeyEvent.KEYCODE_ENTER);
	}


	abstract protected void onTextChange();
}
