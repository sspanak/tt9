package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.Arrays;

import io.github.sspanak.tt9.preferences.settings.SettingsColors;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class BaseSoftKeyWithIcons extends BaseSoftKeyCustomizable {
	private Drawable icon = null; // central icon
	protected int centralIconColor = SettingsColors.DEFAULT_TEXT_COLOR;

	public static final int ICON_POSITION_TOP_RIGHT = 0;
	public static final int ICON_POSITION_TOP_LEFT = 1;
	public static final int ICON_POSITION_BOTTOM_RIGHT = 2;
	public static final int ICON_POSITION_BOTTOM_LEFT = 3;
	private final Drawable[] cornerIcon = { null, null, null, null };

	public BaseSoftKeyWithIcons(Context context) { super(context); }
	public BaseSoftKeyWithIcons(Context context, AttributeSet attrs) { super(context, attrs); }
	public BaseSoftKeyWithIcons(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void initColors(@NonNull SettingsStore settings) {
		super.initColors(settings);
		centralIconColor = textColor;
	}

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
		float settingsScale = tt9 != null ? tt9.getSettings().getNumpadKeyFontSizePercent() / 100f : 1;
		return keyboardSizeScale * Math.min(getScreenScaleX(), getScreenScaleY()) * settingsScale;
	}


	/**
	 * Returns the resource ID of the respective icon. If the key does not have an icon at that position,
	 * return -1. The scale is controlled by super.getCornerElementScale().
	 */
	protected int getCornerIcon(int position) {
		return -1;
	}


	/**
	 * A fail-safe method to get the icon drawable for the respective positions.
	 */
	private Drawable getCornerIconCompat(int position) {
		if (position < 0 || position >= cornerIcon.length) {
			return null;
		} else if (cornerIcon[position] == null && getCornerIcon(position) > 0) {
			cornerIcon[position] = AppCompatResources.getDrawable(getContext(), getCornerIcon(position));
		} else if (getCornerIcon(position) <= 0) {
			cornerIcon[position] = null;
		}

		return cornerIcon[position];
	}


	@Override
	protected float getCornerElementScale(int position) {
		float keyboardSizeScale = Math.min(1, Math.max(getTT9Width(), getTT9Height()));
		float settingsScale = tt9 != null ? tt9.getSettings().getNumpadKeyFontSizePercent() / 100f : 1;
		return keyboardSizeScale * Math.min(getScreenScaleX(), getScreenScaleY()) * settingsScale;
	}


	protected void resetIconCache() {
		icon = null;
		Arrays.fill(cornerIcon, null);
	}


	/**
	 * Renders one of the key icons. It could be either the central icon, in the place of the main title,
	 * or a hold icon, displayed in the upper right corner.
	 */
	private void renderOverlayDrawable(String elementTag, @Nullable Drawable drawable, int color, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = overlay.findViewWithTag(elementTag);
		if (!(element instanceof ImageView el)) {
			return;
		}

		el.setImageDrawable(drawable);
		el.setColorFilter(color);
		el.setAlpha(isEnabled ? 1 : 0.4f);

		if (drawable != null) {
			el.setScaleX(scale);
			el.setScaleY(scale);
		}
	}


	public void render() {
		boolean isKeyEnabled = isEnabled();

		getOverlayWrapper();
		renderOverlayDrawable("overlay_icon", getCentralIconCompat(), centralIconColor, getCentralIconScale(), isKeyEnabled);
		renderOverlayDrawable("overlay_top_right_icon", getCornerIconCompat(ICON_POSITION_TOP_RIGHT), cornerElementColor, getCornerElementScale(ICON_POSITION_TOP_RIGHT), isKeyEnabled && isHoldEnabled());
		renderOverlayDrawable("overlay_top_left_icon", getCornerIconCompat(ICON_POSITION_TOP_LEFT), cornerElementColor, getCornerElementScale(ICON_POSITION_TOP_LEFT), isKeyEnabled);
		renderOverlayDrawable("overlay_bottom_right_icon", getCornerIconCompat(ICON_POSITION_BOTTOM_RIGHT), cornerElementColor, getCornerElementScale(ICON_POSITION_BOTTOM_RIGHT), isKeyEnabled);
		renderOverlayDrawable("overlay_bottom_left_icon", getCornerIconCompat(ICON_POSITION_BOTTOM_LEFT), cornerElementColor, getCornerElementScale(ICON_POSITION_BOTTOM_LEFT), isKeyEnabled);

		super.render();
	}
}
