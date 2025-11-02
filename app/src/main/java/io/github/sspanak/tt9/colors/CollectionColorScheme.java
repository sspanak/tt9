package io.github.sspanak.tt9.colors;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.Logger;

public class CollectionColorScheme {
	private static final String LOG_TAG = AbstractColorScheme.class.getSimpleName();


	public static AbstractColorScheme[] getAll(@NonNull Context context) {
		ColorSchemes schemes = new ColorSchemes();

		AbstractColorScheme[] all = new AbstractColorScheme[schemes.list.size()];
		int i = 0;
		for (FactoryColorScheme factory : schemes.list.values()) {
			try {
				AbstractColorScheme scheme = factory.create(context);
				if (scheme != null) {
					all[i++] = scheme;
				}
			} catch (Exception e) {
				Logger.e(LOG_TAG, "Failed to get color scheme. " + e);
			}
		}

		return sort(all);
	}


	private static AbstractColorScheme[] sort(@NonNull AbstractColorScheme[] schemes) {
		if (schemes.length <= 1) {
			return schemes;
		}

		AbstractColorScheme[] sorted = new AbstractColorScheme[schemes.length];
		System.arraycopy(schemes, 0, sorted, 0, schemes.length);

		for (int i = 0; i < schemes.length - 1; i++) {
			if (!AbstractColorScheme.isGreaterThan(sorted[i], sorted[i + 1])) {
				AbstractColorScheme tmp1 = sorted[i];
				AbstractColorScheme tmp2 = sorted[i + 1];
				sorted[i] = tmp2;
				sorted[i + 1] = tmp1;
				i = 0;
			}
		}

		return sorted;
	}


	@NonNull
	public static AbstractColorScheme get(@NonNull Context context, int schemeId) {
		FactoryColorScheme factory = new ColorSchemes().list.get(schemeId);
		if (factory == null) {
			Logger.e(LOG_TAG, "Unknown color scheme type: " + schemeId + ". Falling back to system default.");
			factory = ColorSchemeSystem::new;
		}

		try {
			return factory.create(context);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed to get color scheme type: " + schemeId + ". " + e);
			return new ColorSchemeSystem(context);
		}
	}


	@NonNull
	public static AbstractColorScheme get(@NonNull Context context, @NonNull String schemeId) {
		int intId;
		try {
			intId = Integer.parseInt(schemeId);
		} catch (NumberFormatException e) {
			Logger.e(LOG_TAG, "Failed to parse color scheme id: " + schemeId + ". " + e);
			intId = ColorSchemeSystem.ID;
		}
		return get(context, intId);
	}
}
