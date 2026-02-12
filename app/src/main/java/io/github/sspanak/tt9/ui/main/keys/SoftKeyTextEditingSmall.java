package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CommandCollection;

public class SoftKeyTextEditingSmall extends SoftKeyFnSmall {
	public SoftKeyTextEditingSmall(Context context) { super(context); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean isVisible() {
		return (tt9 != null && tt9.isTextEditingActive() && getId() != R.id.soft_key_0) || super.isVisible();
	}

	@Override
	protected int getBottomIconId() {
		if (tt9 == null || !tt9.isTextEditingActive()) {
			return super.getBottomIconId();
		}

		return CommandCollection.getBySoftKey(CommandCollection.COLLECTION_TEXT_EDITING, getId()).getIcon();
	}
}
