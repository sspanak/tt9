package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;

import io.github.sspanak.tt9.R;

public class SoftKeyDeveloperSmall extends SoftKeyFnSmall {
	public SoftKeyDeveloperSmall(Context context) { super(context); }
	public SoftKeyDeveloperSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyDeveloperSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean isVisible() {
		return tt9 != null && tt9.isDeveloperCommandsActive();
	}

	@Override
	protected String getTitle() {
		return switch (getId()) {
			case R.id.soft_key_1 -> "1\nCtrl";
			case R.id.soft_key_2 -> "2\nAlt";
			case R.id.soft_key_3 -> "3\nFn";
			case R.id.soft_key_4 -> "4\nMeta";
			case R.id.soft_key_5 -> "5\nShift";
			case R.id.soft_key_6 -> "6\nCtrlL";
			case R.id.soft_key_7 -> "7\nAltL";
			case R.id.soft_key_8 -> "8\nClr";
			case R.id.soft_key_9 -> "9\nCaps";
			default -> super.getTitle();
		};
	}

	@Override
	protected float getTitleScale() {
		return 0.8f;
	}

	@Override
	protected int getBottomIconId() {
		return switch (getId()) {
			case R.id.soft_key_1, R.id.soft_key_6 -> R.drawable.ic_dev_ctrl;
			case R.id.soft_key_2, R.id.soft_key_7 -> R.drawable.ic_dev_alt;
			case R.id.soft_key_3 -> R.drawable.ic_dev_fn;
			case R.id.soft_key_4 -> R.drawable.ic_dev_meta;
			case R.id.soft_key_5 -> R.drawable.ic_fn_shift_up;
			case R.id.soft_key_8 -> R.drawable.ic_txt_select_none;
			case R.id.soft_key_9 -> R.drawable.ic_dev_caps;
			default -> -1;
		};
	}

	@Override
	public void render() {
		final int iconId = getBottomIconId();
		final Drawable icon = iconId > 0 && tt9 != null ? AppCompatResources.getDrawable(tt9.getApplicationContext(), iconId) : null;
		setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
		super.render();
		setTextColor(tt9 != null && tt9.isDeveloperModifierHeld(getNumber()) ? Color.RED : getTextColors().getDefaultColor());
	}
}
