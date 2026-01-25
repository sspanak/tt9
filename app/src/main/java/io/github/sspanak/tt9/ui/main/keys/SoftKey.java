package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsColors;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SoftKey extends BaseClickableKey {
	private static float screenSizeScale = 0;

	protected RelativeLayout overlay = null;

	protected int textColor = SettingsColors.DEFAULT_KEY_TEXT_COLOR;
	protected int cornerElementColor = textColor;
	@NonNull protected ColorStateList backgroundColor = ColorStateList.valueOf(SettingsColors.DEFAULT_KEY_BACKGROUND_COLOR);
	@NonNull protected ColorStateList borderColor = ColorStateList.valueOf(SettingsColors.DEFAULT_KEY_BORDER_COLOR);
	@NonNull protected ColorStateList rippleColor = ColorStateList.valueOf(SettingsColors.DEFAULT_KEY_RIPPLE_COLOR);


	public SoftKey(Context context) { super(context); }
	public SoftKey(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	public void setTT9(TraditionalT9 tt9) {
		super.setTT9(tt9);
		if (tt9 != null) {
			initColors(tt9.getSettings());
		}
	}


	/**
	 * Loads the current color scheme from the settings, so that we can use them in render().
	 */
	protected void initColors(@NonNull SettingsStore settings) {
		backgroundColor = settings.getKeyFnBackgroundColor();
		borderColor = settings.getKeyFnBorderColor();
		rippleColor = settings.getKeyFnRippleColor();
		textColor = settings.getKeyFnTextColor();
		cornerElementColor = settings.getKeyFnCornerElementColor();
	}


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
	 * Returns a scale factor for the screen size, used to adjust the key size and text size. Mostly,
	 * useful for tablets or larger devices, where the keys are too big but the text remains small.
	 */
	protected float getScreenSizeScale() {
		if (screenSizeScale > 0) {
			return screenSizeScale;
		}

		final boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9);
		final float width = isLandscape ? DeviceInfo.getScreenWidthDp(getContext()) : DeviceInfo.getScreenHeightDp(getContext());
		final float height = isLandscape ? DeviceInfo.getScreenHeightDp(getContext()) : DeviceInfo.getScreenWidthDp(getContext());

		final float screenScaleX = Math.min(
			width / SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_NORMAL_SIZE,
			SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_MAX
		);

		final float screenScaleY = Math.min(
			height / SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_NORMAL_SIZE,
			SettingsStore.SOFT_KEY_SCALE_SCREEN_COMPENSATION_MAX
		);

		screenSizeScale = Math.min(screenScaleX, screenScaleY);
		return screenSizeScale;
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


	public void setWeight(float weight) {
		if (weight < 0) {
			return;
		}

		getOverlayWrapper();
		View targetView = overlay != null ? overlay : this;

		LinearLayout.LayoutParams newParams;
		ViewGroup.LayoutParams currentParams = targetView.getLayoutParams();
		if (currentParams instanceof LinearLayout.LayoutParams params) {
			newParams = params;
		} else {
			newParams = new LinearLayout.LayoutParams(currentParams);
		}

		newParams.weight = weight;
		targetView.setLayoutParams(newParams);
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
			&& new Text(getText().toString()).startsWithGraphic()
			&& !new Paint().hasGlyph(getText().toString())
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
		float settingsScale = tt9 != null ? tt9.getSettings().getNumpadKeyFontSizePercent() / 100f : 1;
		return keyboardSizeScale * getScreenSizeScale() * settingsScale;
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
	 * Renders the click effect of the key without performing the actual action.
	 */
	public void renderClick() {
		if (isEnabled()) {
			setPressed(true);
			setPressed(false);
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
	protected void renderOverlayText(String elementTag, @Nullable String text, int color, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = overlay.findViewWithTag(elementTag);
		if (!(element instanceof TextView el)) {
			return;
		}

		el.setTextColor(color);
		el.setAlpha(isEnabled ? 1 : 0.4f);

		if (text == null || scale == 1) {
			el.setText(text);
			return;
		}

		SpannableString scaledText = new SpannableString(text);
		scaledText.setSpan(new RelativeSizeSpan(scale), 0, scaledText.length(), 0);
		el.setText(scaledText);
	}


	/**
	 * Renders or removes the under-shadow, that acts like a border and gives a 3D effect,
	 * increasing visual separation of the keys. Also, allows for toggling between Material2 and
	 * Material3 style regardless of the Android version.
	 */
	private void renderShadow() {
		final boolean shadows = tt9 != null && tt9.getSettings().getKeyShadows();
		post(() -> {
			setElevation(shadows ? SettingsStore.KEY_SHADOW_ELEVATION : 0);
			setTranslationZ(shadows ? SettingsStore.KEY_SHADOW_TRANSLATION : 0);
		});
	}


	/**
	 * render
	 * Sets the key labels, colors and icons using. Potentially, it can also adjust padding and margins and
	 * other visual properties of the key.
	 */
	public void render() {
		setBackgroundTintList(backgroundColor);
		setRippleColor(rippleColor);
		setStrokeColor(borderColor);
		setStrokeWidth(borderColor.getDefaultColor() == Color.TRANSPARENT ? 0 : 2);
		setTextColor(textColor);

		boolean isKeyEnabled = isEnabled();
		renderTitle(isKeyEnabled);
		getOverlayWrapper();
		renderOverlayText("overlay_hold_text", getHoldText(), cornerElementColor, getCornerElementScale(BaseSoftKeyWithIcons.ICON_POSITION_TOP_RIGHT), isKeyEnabled && isHoldEnabled());
		renderShadow();
	}
}
