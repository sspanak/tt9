package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;

abstract public class ItemSearch extends ItemTextInput {
	public ItemSearch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public ItemSearch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public ItemSearch(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	public ItemSearch(@NonNull Context context) {
		super(context);
	}


	@Override protected int getLargeLayout() {
		return DeviceInfo.AT_LEAST_ANDROID_12 ? R.layout.pref_input_text : R.layout.pref_input_text_large;
	}


	protected void setTextField(@NonNull PreferenceViewHolder holder) {
		super.setTextField(holder);
		if (textField != null) {
			setIcon();
		}
	}


	private void setIcon() {
		Context context = getContext();
		Drawable searchIcon = AppCompatResources.getDrawable(context, R.drawable.ic_fn_search);
		if (searchIcon != null) {
			searchIcon.setTint(context.getResources().getColor(R.color.keyboard_text));
		}

		if (LanguageKind.isRTL(LanguageCollection.getDefault())) {
			textField.setCompoundDrawablesWithIntrinsicBounds(null, null, searchIcon, null);
		} else {
			textField.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
		}
	}
}
