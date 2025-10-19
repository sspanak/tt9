package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SoftKey extends BaseClickableKey {
	protected RelativeLayout overlay = null;

	private static float screenScaleX = 0;
	private static float screenScaleY = 0;


	public SoftKey(Context context) { super(context); }
	public SoftKey(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	/**
	 * Bopomofo letters are very large, so we need to scale them down a bit in some keys. Hence,
	 * the convenience method.
	 */
	protected boolean isBopomofo() {
		return tt9 != null && !tt9.isInputModeNumeric() && LanguageKind.isChineseBopomofo(tt9.getLanguage());
	}


	protected boolean hasLettersOnAllKeys() {
		return tt9 != null && tt9.getLanguage() != null && tt9.getLanguage().hasLettersOnAllKeys();
	}


	/**
	 * Returns a scale factor for the screen width, used to adjust the key size and text size. Mostly,
	 * useful for tablets or larger devices, where the keys are too big but the text remains small.
	 */
	protected float getScreenScaleX() {
		if (screenScaleX == 0) {
			boolean isLandscape = DeviceInfo.isLandscapeOrientation(getContext());
			float width = isLandscape ? DeviceInfo.getScreenWidthDp(getContext()) : DeviceInfo.getScreenHeightDp(getContext());

			screenScaleX = Math.min(
				width / SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_NORMAL_WIDTH,
				SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_MAX
			);
		}
		return screenScaleX;
	}


	/**
	 * Same as getScreenScaleX(), but used for the key height.
	 */
	protected float getScreenScaleY() {
		if (screenScaleY == 0) {
			boolean isLandscape = DeviceInfo.isLandscapeOrientation(getContext());
			float height = isLandscape ? DeviceInfo.getScreenHeightDp(getContext()) : DeviceInfo.getScreenWidthDp(getContext());

			screenScaleY = Math.min(
				height / SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_NORMAL_HEIGHT,
				SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_MAX
			);
		}
		return screenScaleY;
	}


	protected float getTT9Width() {
		return tt9 != null ? tt9.getNormalizedWidth() : 1;
	}


	protected float getTT9Height() {
		return tt9 != null ? tt9.getNormalizedHeight() : 1;
	}


	/**
	 * isDynamic
	 * Returns true if the key should be rendered more often to reflect dynamic state changes. This is
	 * used to determine whether BaseMainLayout.renderDynamicKeys() should include this key or not.
	 * For example, Shift key changes its appearance, while typing, based on the current text case.
	 */
	public boolean isDynamic() {
		return false;
	}


	public void setHeight(int height) {
		if (height <= 0) {
			return;
		}

		// adjust the key height
		ViewGroup.LayoutParams layout = getLayoutParams();
		if (layout != null) {
			layout.height = height;
			setLayoutParams(layout);
		}

		// adjust the overlay height (if it exists)
		getOverlayWrapper();
		layout = overlay != null ? overlay.getLayoutParams() : null;
		if (layout != null) {
			layout.height = height;
			overlay.setLayoutParams(layout);
		}
	}



	/**
	 * getTitle
	 * Generates the name of the key, for example: "OK", "Backspace", "1", etc...
	 */
	protected String getTitle() { return null; }


	/**
	 * getNoEmojiTitle
	 * Generates a text representation of the key title, when emojis are not supported and getTitle()
	 * is meant to return an emoji.
	 */
	protected int getNoEmojiTitle() { return 0; }


	/**
	 * Returns a meaningful key title depending on the current emoji support.
	 */
	private String getTitleCompat() {
		if (
			getNoEmojiTitle() > 0
			&& (
				Characters.NO_EMOJI_SUPPORT
				|| (new Text(getText().toString()).startsWithGraphic() && !new Paint().hasGlyph(getText().toString()))
			)
		) {
			return getContext().getString(getNoEmojiTitle());
		} else {
			return getTitle();
		}
	}


	/**
	 * Returns a multiplier for scaling the central text size. Used to automatically fit the text
	 * when changing the keyboard dimensions, and to make it look good on different screen sizes.
	 */
	protected float getTitleScale() {
		float keyboardSizeScale = Math.max(0.7f, Math.min(getTT9Width(), getTT9Height()));
		float screenSizeScale = Math.min(getScreenScaleX(), getScreenScaleY());
		float settingsScale = tt9 != null ? tt9.getSettings().getNumpadKeyFontSizePercent() / 100f : 1;
		return keyboardSizeScale * screenSizeScale * settingsScale;
	}


	/**
	 * Generates a String describing the "hold" function of the key. The String will be displayed
	 * in the upper right corner.
	 */
	protected String getHoldText() {
		return null;
	}

	/**
	 * Similar to getTitleScale(), adjusts the font size of the corner text or icon
	 */
	protected float getCornerElementScale(int position) {
		return 1;
	}


	protected void getOverlayWrapper() {
		if (overlay == null) {
			ViewParent parent = getParent();
			if (parent instanceof RelativeLayout) {
				overlay = (RelativeLayout) parent;
			}
		}
	}


	/**
	 * Renders the central text of the key and styles it based on "isEnabled".
	 */
	private void renderTitle(boolean isEnabled) {
		String title = getTitleCompat();
		if (title == null) {
			return;
		}

		setTextColor(getTextColors().withAlpha(isEnabled ? 255 : 110));

		float scale = getTitleScale();
		if (scale == 1) {
			setText(title);
			return;
		}

		SpannableString text = new SpannableString(title);
		text.setSpan(new RelativeSizeSpan(scale), 0, title.length(), 0);
		setText(text);
	}


	/**
	 * Renders text in the given overlay element, with optional scaling and alpha. The overlay
	 * text elements are either the "hold" text or the "swipe" text.
	 */
	protected void renderOverlayText(String elementTag, @Nullable String text, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = ((RelativeLayout) getParent()).findViewWithTag(elementTag);
		if (!(element instanceof TextView el)) {
			return;
		}

		el.setTextColor(el.getTextColors().withAlpha(isEnabled ? 255 : 110));

		if (text == null || scale == 1) {
			el.setText(text);
			return;
		}

		SpannableString scaledText = new SpannableString(text);
		scaledText.setSpan(new RelativeSizeSpan(scale), 0, scaledText.length(), 0);
		el.setText(scaledText);
	}


	/**
	 * render
	 * Sets the key labels and icons using. Potentially, it can also adjust padding and margins and
	 * other visual properties of the key.
	 */
	public void render() {
		boolean isKeyEnabled = isEnabled();
		renderTitle(isKeyEnabled);
		getOverlayWrapper();
		renderOverlayText("overlay_hold_text", getHoldText(), getCornerElementScale(BaseSoftKeyWithIcons.ICON_POSITION_TOP_RIGHT), isKeyEnabled && isHoldEnabled());
	}
}
