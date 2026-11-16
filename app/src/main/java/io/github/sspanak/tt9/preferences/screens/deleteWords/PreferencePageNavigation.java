package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;

/**
 * A single row with "‹ Previous page" on the left and "Next page ›" on the right. Each link is
 * focusable on its own, so it can be reached with a keypad, too.
 */
class PreferencePageNavigation extends ScreenPreference {
	@Nullable private Runnable onPrevious;
	@Nullable private Runnable onNext;
	private boolean hasPrevious = false;
	private boolean hasNext = false;

	PreferencePageNavigation(@NonNull Context context) {
		super(context);
		setPersistent(false);
		setSelectable(false);
	}

	void setHandlers(@Nullable Runnable onPrevious, @Nullable Runnable onNext) {
		this.onPrevious = onPrevious;
		this.onNext = onNext;
	}

	void setAvailable(boolean hasPrevious, boolean hasNext) {
		this.hasPrevious = hasPrevious;
		this.hasNext = hasNext;
		setVisible(hasPrevious || hasNext);
		notifyChanged();
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		bindLink(holder.findViewById(R.id.page_navigation_previous), hasPrevious, onPrevious);
		bindLink(holder.findViewById(R.id.page_navigation_next), hasNext, onNext);
	}

	private void bindLink(@Nullable View link, boolean available, @Nullable Runnable handler) {
		if (link == null) {
			return;
		}

		// INVISIBLE keeps the other link in place
		link.setVisibility(available ? View.VISIBLE : View.INVISIBLE);
		link.setOnClickListener(available && handler != null ? v -> handler.run() : null);
	}

	@Override
	public int getDefaultLayout() {
		return R.layout.pref_page_navigation;
	}

	@Override
	public int getLargeLayout() {
		return R.layout.pref_page_navigation_large;
	}
}
