package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CommandCollection;

public class SoftKeyTextEditingSmall extends SoftKeyFnSmall {
	public SoftKeyTextEditingSmall(Context context) { super(context); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	private boolean isTextEditingOn() {
		return tt9 != null && tt9.isTextEditingActive();
	}

	@Override
	protected boolean isVisible() {
		return (isTextEditingOn() && getId() != R.id.soft_key_0) || super.isVisible();
	}

	@Override
	protected String getAccessibilityText() {
		return isTextEditingOn() ? CommandCollection.getBySoftKey(CommandCollection.COLLECTION_TEXT_EDITING, getId()).getName(tt9) : super.getAccessibilityText();
	}

	@Override
	protected int getBottomIconId() {
		return isTextEditingOn() ? CommandCollection.getBySoftKey(CommandCollection.COLLECTION_TEXT_EDITING, getId()).getIcon() : super.getBottomIconId();
	}
}
