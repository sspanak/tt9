package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdBack;
import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdTxtCut;

public class SoftKeyEditText extends BaseSoftKeyWithIcons {
	private final CmdBack back = new CmdBack();
	private final CmdEditText editText = new CmdEditText();

	public SoftKeyEditText(Context context) { super(context); }
	public SoftKeyEditText(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyEditText(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isKeySmall() {
		return getTT9Height() < 0.8f && getTT9Width() < 0.7f;
	}


	@Override
	protected boolean handleRelease() {
		if (editText.isMissing(tt9)) {
			return false;
		}

		editText.run(tt9);

		return true;
	}


	@Override
	protected String getAccessibilityText() {
		return editText.isActive(tt9) ? back.getName(tt9) : editText.getName(tt9);
	}


	@Override
	protected int getCentralIcon() {
		return editText.isActive(tt9) ? back.getIcon() : new CmdTxtCut().getIcon();
	}


	@Override
	protected float getCentralIconScale() {
		float scale = 1;
		if (!editText.isActive(tt9)) {
			scale = isKeySmall() ? 0.7f : 0.8f;
		}

		return super.getCentralIconScale() * scale;
	}


	@Override
	public void render() {
		resetIconCache();
		setEnabled(editText.isAvailable(tt9));
		super.render();
	}
}
