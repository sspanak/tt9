package io.github.sspanak.tt9.colors;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.Logger;

public class CollectionColorScheme {
	private static final String LOG_TAG = AbstractColorScheme.class.getSimpleName();


	public static AbstractColorScheme[] getAll(@NonNull Context context) {
		return new AbstractColorScheme[] {
			new ColorSchemeSystem(context),
			new ColorSchemeSystemDark(context),
			new ColorSchemeSystemLight(context)
		};
	}


	@NonNull
	public static AbstractColorScheme get(@NonNull Context context, int schemeId) {
		AbstractColorScheme[] schemes = getAll(context);
		for (AbstractColorScheme scheme : schemes) {
			if (scheme.getId() == schemeId) {
				return scheme;
			}
		}

		Logger.w(LOG_TAG, "Color scheme with id: " + schemeId + " not found. Returning default scheme.");
		return new ColorSchemeSystem(context);
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
