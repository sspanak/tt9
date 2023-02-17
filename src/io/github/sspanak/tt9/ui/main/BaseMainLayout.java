package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

abstract class BaseMainLayout {
	protected TraditionalT9 tt9;
	private final int xml;

	protected View view = null;
	protected ArrayList<SoftKey> keys = new ArrayList<>();

	public BaseMainLayout(TraditionalT9 tt9, int xml) {
		this.tt9 = tt9;
		this.xml = xml;
	}


	/** setDarkTheme
	 * Changes the main view colors according to the theme.
	 *
	 * We need to do this manually, instead of relying on the Context to resolve the appropriate colors,
	 * because this View is part of the main service View. And service Views are always locked to the
	 * system context and theme.
	 *
	 * More info:
	 * <a href="https://stackoverflow.com/questions/72382886/system-applies-night-mode-to-views-added-in-service-type-application-overlay">...</a>
	 */
	abstract public void setDarkTheme(boolean yes);


	/**
	 * render
	 * Do all the necessary stuff to display the View.
	 */
	abstract public void render();


	/**
	 * getKeys
	 * Returns a list of all the usable Soft Keys.
	 */
	abstract protected ArrayList<SoftKey> getKeys();


	public View getView() {
		if (view == null) {
			view = View.inflate(tt9.getApplicationContext(), xml, null);
		}

		return view;
	}

	public void show() {
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
	}

	public void hide() {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
	}

	public void enableClickHandlers() {
		for (SoftKey key : getKeys()) {
			key.setTT9(tt9);
		}
	}



	protected ArrayList<SoftKey> getKeysFromContainer(ViewGroup container) {
		ArrayList<SoftKey> keyList = new ArrayList<>();
		final int childrenCount = container != null ? container.getChildCount() : 0;

		for (int i = 0; i < childrenCount; i++) {
			View child = container.getChildAt(i);
			if (child instanceof SoftKey) {
				keyList.add((SoftKey) child);
			}
		}

		return keyList;
	}
}
