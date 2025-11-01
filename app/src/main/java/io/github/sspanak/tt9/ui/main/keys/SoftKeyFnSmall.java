package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;

import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.CommandCollection;

public class SoftKeyFnSmall extends SoftKeyFnNumpad {
	public SoftKeyFnSmall(Context context) { super(context);}
	public SoftKeyFnSmall(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftKeyFnSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}

	@Override protected void handleHold() { preventRepeat(); }
	@Override protected String getTitle() { return String.valueOf(getNumber()); }
	@Override protected float getTitleScale() { return 1; }

	private boolean isVoiceInput() {
		return CommandCollection.indexOf(CommandCollection.COLLECTION_PALETTE, CmdVoiceInput.ID) == getId();
	}

	private boolean isTextEditing() {
		return CommandCollection.indexOf(CommandCollection.COLLECTION_PALETTE, CmdEditText.ID) == getId();
	}

	private boolean isNOOP() {
		return !CommandCollection.getAll(CommandCollection.COLLECTION_PALETTE).containsKey(getId());
	}

	protected boolean isVisible() {
		if (isNOOP()) {
			return false;
		} else if (isVoiceInput()) {
			return tt9 == null || !tt9.isVoiceInputMissing();
		} else if (isTextEditing()) {
			return tt9 == null || !tt9.isInputLimited();
		} else {
			return true;
		}
	}

	protected int getBottomIconId() {
		return CommandCollection.getByKeyId(CommandCollection.COLLECTION_PALETTE, getId()).getIcon();
	}

	private void setBottomIcon() {
		final int iconId = getBottomIconId();
		final Drawable icon = iconId > 0 && tt9 != null ? AppCompatResources.getDrawable(tt9.getApplicationContext(), iconId) : null;
		setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
		if (icon != null) {
			TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(centralIconColor));
		}
	}

	@Override
	public void render() {
		setVisibility(isVisible() ? VISIBLE : GONE);
		setBottomIcon();
		super.render();
	}
}
