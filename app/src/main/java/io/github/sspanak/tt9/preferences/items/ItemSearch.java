package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.search.SearchView;

import io.github.sspanak.tt9.R;

abstract public class ItemSearch extends ItemTextInput {
	private final boolean isModernDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;


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


	@Override protected int getDefaultLayout() {
		return isModernDevice ? R.layout.pref_search_v31 : R.layout.pref_input_text;
	}


	@Override protected int getLargeLayout() {
		return isModernDevice ? R.layout.pref_search_v31 : R.layout.pref_input_text_large;
	}


	protected void setTextField(@NonNull PreferenceViewHolder holder) {
		if (!isModernDevice) {
			super.setTextField(holder);
			return;
		}

		SearchView searchView = holder.itemView.findViewById(R.id.search_view);
		if (searchView != null) {
			this.textField = searchView.getEditText();
		}
	}
}
