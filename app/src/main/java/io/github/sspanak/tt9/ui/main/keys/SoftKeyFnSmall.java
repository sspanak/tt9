package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;

import io.github.sspanak.tt9.R;

public class SoftKeyFnSmall extends SoftKeyFnNumpad {
	public SoftKeyFnSmall(Context context) { super(context);}
	public SoftKeyFnSmall(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftKeyFnSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}

	@Override protected void handleHold() { preventRepeat(); }
	@Override protected String getTitle() { return getNumber() + ""; }
	@Override protected float getTitleScale() { return 1; }

	private boolean isVoiceInput() {
		return getId() == R.id.soft_key_3;
	}

	private boolean isTextEditing() {
		return getId() == R.id.soft_key_5;
	}

	private boolean isNOOP() {
		return getId() == R.id.soft_key_9 || getId() == R.id.soft_key_0;
	}

	protected boolean isVisible() {
		if (isNOOP()) {
			return false;
		} else if (getId() == R.id.soft_key_7) {
			return tt9 == null || tt9.isDeveloperCommandsEnabled();
		} else if (isVoiceInput()) {
			return tt9 == null || !tt9.isVoiceInputMissing();
		} else if (isTextEditing()) {
			return tt9 == null || !tt9.isInputLimited();
		} else {
			return true;
		}
	}


	protected int getBottomIconId() {
		final int keyId = getId();

		if (keyId == R.id.soft_key_1) return R.drawable.ic_fn_settings;
		if (keyId == R.id.soft_key_2) return R.drawable.ic_fn_add_word;
		if (keyId == R.id.soft_key_3) return R.drawable.ic_fn_voice;
		if (keyId == R.id.soft_key_4) return R.drawable.ic_fn_undo;
		if (keyId == R.id.soft_key_5) return R.drawable.ic_txt_cut;
		if (keyId == R.id.soft_key_6) return R.drawable.ic_fn_redo;
		if (keyId == R.id.soft_key_7) return R.drawable.ic_fn_developer;
		if (keyId == R.id.soft_key_8) return R.drawable.ic_fn_next_keyboard;

		return -1;
	}


	private void setBottomIcon() {
		final int iconId = getBottomIconId();
		final Drawable icon = iconId > 0 && tt9 != null ? AppCompatResources.getDrawable(tt9.getApplicationContext(), iconId) : null;
		setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
	}


	@Override
	public void render() {
		setVisibility(isVisible() ? VISIBLE : GONE);
		setBottomIcon();
		super.render();
	}
}
