package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

public class BaseSoftKeyWithIcons extends SoftKey {
	private Drawable icon = null;
	private Drawable holdIcon = null;


	public BaseSoftKeyWithIcons(Context context) { super(context); }
	public BaseSoftKeyWithIcons(Context context, AttributeSet attrs) { super(context, attrs); }
	public BaseSoftKeyWithIcons(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	/**
	 * Returns the central icon resource ID. If the key does not have a central icon, return -1. The scale
	 */
	protected int getCentralIcon() { return -1; }


	/**
	 * A fail-safe method to get the central icon drawable.
	 */
	private Drawable getCentralIconCompat() {
		if (icon == null && getCentralIcon() > 0) {
			icon = AppCompatResources.getDrawable(getContext(), getCentralIcon());
		} else if (getCentralIcon() <= 0) {
			icon = null;
		}

		return icon;
	}


	/**
	 * Same as getTitleScale(), but for keys that have icons instead of text.
	 */
	protected float getCentralIconScale() {
		float keyboardSizeScale = Math.max(0.7f, Math.min(getTT9Width(), getTT9Height()));
		keyboardSizeScale = Math.min(1.15f, keyboardSizeScale);
		return keyboardSizeScale * Math.min(getScreenScaleX(), getScreenScaleY());
	}


	/**
	 * Returns the hold icon resource ID. If the key does not have a hold icon, return -1. The scale
	 * is controlled by super.getHoldElementScale().
	 */
	protected int getHoldIcon() { return -1; }


	/**
	 * A fail-safe method to get the hold icon drawable.
	 */
	private Drawable getHoldIconCompat() {
		if (holdIcon == null && getHoldIcon() > 0) {
			holdIcon = AppCompatResources.getDrawable(getContext(), getHoldIcon());
		} else if (getHoldIcon() <= 0) {
			holdIcon = null;
		}

		return holdIcon;
	}


	protected void resetIconCache() {
		icon = null;
		holdIcon = null;
	}


	/**
	 * Renders one of the key icons. It could be either the central icon, in the place of the main title,
	 * or a hold icon, displayed in the upper right corner.
	 */
	private void renderOverlayDrawable(String elementTag, @Nullable Drawable drawable, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = ((RelativeLayout) getParent()).findViewWithTag(elementTag);
		if (!(element instanceof ImageView el)) {
			return;
		}

		el.setImageDrawable(drawable);
		if (!isEnabled) {
			el.setColorFilter(Color.GRAY);
		} else {
			el.clearColorFilter();
		}

		if (drawable != null) {
			el.setScaleX(scale);
			el.setScaleY(scale);
		}
	}


	public void render() {
		boolean isKeyEnabled = isEnabled();

		getOverlayWrapper();
		renderOverlayDrawable("overlay_icon", getCentralIconCompat(), getCentralIconScale(), isKeyEnabled);
		renderOverlayDrawable("overlay_hold_icon", getHoldIconCompat(), getHoldElementScale(), isKeyEnabled && isHoldEnabled());

		super.render();
	}
}
