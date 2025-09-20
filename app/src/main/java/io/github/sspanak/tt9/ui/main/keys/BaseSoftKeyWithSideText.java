package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class BaseSoftKeyWithSideText extends BaseSoftKeyWithIcons {
	public BaseSoftKeyWithSideText(Context context) { super(context); }
	public BaseSoftKeyWithSideText(Context context, AttributeSet attrs) { super(context, attrs); }
	public BaseSoftKeyWithSideText(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	protected String getTopText() { return null; }
	protected String getRightText() { return null; }
	protected String getBottomText() { return null; }
	protected String getLeftText() { return null; }

	protected float getTopElementScale() { return getHoldElementScale(); }
	protected float getRightElementScale() { return getHoldElementScale(); }
	protected float getBottomElementScale() { return getHoldElementScale(); }
	protected float getLeftElementScale() { return getHoldElementScale(); }

	@Override
	public void render() {
		boolean isKeyEnabled = isEnabled();

		getOverlayWrapper();
		renderOverlayText("overlay_top_text", getTopText(), getTopElementScale(), isKeyEnabled);
		renderOverlayText("overlay_right_text", getRightText(), getRightElementScale(), isKeyEnabled);
		renderOverlayText("overlay_bottom_text", getBottomText(), getBottomElementScale(), isKeyEnabled);
		renderOverlayText("overlay_left_text", getLeftText(), getLeftElementScale(), isKeyEnabled);
		super.render();
	}
}
