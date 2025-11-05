package io.github.sspanak.tt9.preferences.custom;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.Logger;

public class KeyboardPreviewSwitchPreference extends Preference {
	public static final String NAME = "pref_keyboard_preview_switch";
	private static final int PREVIEW_RESUME_DELAY = 500; //ms

	private SwitchCompat switchView;
	private EditText text1;
	private EditText text2;


	public KeyboardPreviewSwitchPreference(@NonNull Context context) {
		super(context);
		setKey(NAME);
		setTitle(R.string.pref_keyboard_preview);
		setWidgetLayoutResource(R.layout.widget_keyboard_preview_switch);
	}


	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		bindView(holder.itemView, true);
	}


	public void bindView(@Nullable View view) {
		if (view == null) {
			Logger.e(NAME, "Cannot bind '" + NAME + "' to a null view");
			return;
		}

		bindView(view, false);
	}


	private void bindView(@NonNull View view, boolean automatic) {
		text1 = view.findViewById(R.id.preview_text_field_1);
		text2 = view.findViewById(R.id.preview_text_field_2);
		switchView = view.findViewById(R.id.switchWidget);

		if (automatic) {
			return;
		}

		TextView titleView = view.findViewById(android.R.id.title);
		if (titleView != null) {
			titleView.setText(R.string.pref_keyboard_preview);
		}

		view.setOnClickListener((v) -> onClick());
		if (switchView != null) {
			switchView.setOnClickListener((v) -> toggleKeyboard());
		}
	}


	public void resume() {
		if (switchView != null && switchView.isChecked()) {
			// wait for any popups to close, then show the keyboard
			new Handler().postDelayed(this::showKeyboard, PREVIEW_RESUME_DELAY);
		}
	}


	public void pause() {
		hideKeyboard();
	}


	public void stop() {
		switchSwitch(false);
		toggleKeyboard();
	}


	@Override
	protected void onClick() {
		super.onClick();
		switchSwitch(switchView != null && !switchView.isChecked());
		toggleKeyboard();
	}


	private void toggleKeyboard() {
		if (switchView != null && switchView.isChecked()) {
			showKeyboard();
		} else {
			hideKeyboard();
		}
	}


	private void showKeyboard() {
		if (text1 == null || text2 == null) {
			return;
		}

		EditText targetField = text1.isFocused() ? text2 : text1;
		EditText otherField = targetField == text1 ? text2 : text1;

		otherField.clearFocus();

		targetField.post(() -> {
			targetField.requestFocus();
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(targetField, InputMethodManager.SHOW_FORCED);
			}
		});
	}

	private void hideKeyboard() {
		if (text1 == null) {
			return;
		}

		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(text1.getWindowToken(), 0);
		}
		text1.clearFocus();
		text2.clearFocus();
	}


	private void switchSwitch(boolean on) {
		if (switchView != null && switchView.isChecked() != on) {
			switchView.setChecked(on);
		}
	}
}
