package io.github.sspanak.tt9.preferences.custom;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.preferences.items.TextInputPreference;
import io.github.sspanak.tt9.util.Logger;

public class KeyboardPreview {
	@Nullable	private final TextInputPreference text1;
	@Nullable private final TextInputPreference text2;


	public KeyboardPreview(@Nullable PreferenceCategory parent, int titleResId) {
		if (parent == null) {
			text1 = null;
			text2 = null;
		} else {
			text1 = new TextInputPreference(parent.getContext());
			text2 = new TextInputPreference(parent.getContext());
			parent.addPreference(text1);
			parent.addPreference(text2);

			text1.setTitle(titleResId);
			text2.setTitle(titleResId);
			text2.setVisible(false);
		}
	}


	public KeyboardPreview(@Nullable Preference text1, @Nullable Preference text2, int titleResId) {
		if (text1 instanceof TextInputPreference && text2 instanceof TextInputPreference) {
			this.text1 = (TextInputPreference) text1;
			this.text1.setTitle(titleResId);

			this.text2 = (TextInputPreference) text2;
			this.text2.setTitle(titleResId);
			this.text2.setVisible(false);
		} else {
			Logger.w(KeyboardPreview.class.getSimpleName(), "Cannot create preview: one or more TextInputPreference is null");
			this.text1 = null;
			this.text2 = null;
		}
	}


	public void preview() {
		boolean is1Visible = text1 != null && text1.isVisible();
		boolean is2Visible = text2 != null && text2.isVisible();
		boolean isNoneVisible = !is1Visible && !is2Visible;

		if (text1 != null) {
			text1.setVisible(is2Visible || isNoneVisible);
			text1.setText("");
		}

		if (text2 != null) {
			text2.setVisible(is1Visible || text1 == null);
			text2.setText("");
		}
	}


	public void hide() {
		if (text1 != null) {
			text1.setVisible(false);
			text1.setText("");
		}

		if (text2 != null) {
			text2.setVisible(false);
			text2.setText("");
		}
	}
}
