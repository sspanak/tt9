package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;

public class SoftCommandKey extends SoftNumberKey {
	public SoftCommandKey(Context context) { super(context);}
	public SoftCommandKey(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftCommandKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}


	@Override protected void handleHold() {}


	@Override
	public void setDarkTheme(boolean darkEnabled) {
		super.setDarkTheme(darkEnabled);

		final int color = darkEnabled ? R.color.dark_button_text : R.color.button_text;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(getContext().getColor(color)));
		} else {
			setDarkThemeLegacy(color);
		}
	}


	private void setDarkThemeLegacy(int color) {
		Drawable[] icons = getCompoundDrawables();

		if (icons.length >= 4 && icons[3] != null) {
			Drawable icon = DrawableCompat.wrap(icons[3]);
			DrawableCompat.setTint(icon, getResources().getColor(color));
			setCompoundDrawables(null, null, null, icon);
		}
	}


	protected String getTextSubTitle(int resId) {
		setTextSize(SettingsStore.SOFT_KEY_TITLE_SIZE);
		return getContext().getString(resId);
	}


	@Override
	protected String getTitle() {
		return getNumber(getId()) + "";
	}


	@Override
	protected String getSubTitle() {
		boolean noIconSupport = Characters.noEmojiSupported();
		int keyId = getId();

		// command palette
		if (keyId == R.id.soft_key_1) return noIconSupport ? getTextSubTitle(R.string.virtual_key_settings) : "⚙";
		if (keyId == R.id.soft_key_2) return "＋";
		if (keyId == R.id.soft_key_8) return noIconSupport ? getTextSubTitle(R.string.virtual_key_change_keyboard) : "⌨";

		return null;
	}


	@Override
	protected int getNumber(int keyId) {
		if (keyId == R.id.soft_key_101) return 1;
		if (keyId == R.id.soft_key_102) return 2;
		if (keyId == R.id.soft_key_103) return 3;
		if (keyId == R.id.soft_key_104) return 4;
		if (keyId == R.id.soft_key_105) return 5;
		if (keyId == R.id.soft_key_106) return 6;
		if (keyId == R.id.soft_key_107) return 7;
		if (keyId == R.id.soft_key_108) return 8;
		if (keyId == R.id.soft_key_109) return 9;

		return super.getNumber(keyId);
	}
}
