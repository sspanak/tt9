package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;

import io.github.sspanak.tt9.R;

public class SoftKeyTextEdit extends SoftNumberKey {
	public SoftKeyTextEdit(Context context) {
		super(context);
	}

	public SoftKeyTextEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyTextEdit(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

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

	private Drawable getIcon(int resId) {
		return getResources().getDrawable(resId);
	}

	@Override
	protected String getSubTitle() {
		return super.getSubTitle();
	}
}
