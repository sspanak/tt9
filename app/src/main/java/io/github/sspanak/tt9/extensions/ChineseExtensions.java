package io.github.sspanak.tt9.extensions;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.util.Logger;

public final class ChineseExtensions {
	private static boolean resolved;
	@Nullable private static ChineseExtension extension;

	private ChineseExtensions() {}

	/** Split changes restart the process; a missing split is a valid state. */
	@Nullable public static synchronized ChineseExtension get() {
		if (!resolved) {
			resolved = true;
			try {
				extension = (ChineseExtension) Class.forName("io.github.sspanak.tt9.chinese.ChineseFeature")
					.getDeclaredConstructor().newInstance();
			} catch (ClassNotFoundException ignored) {
			} catch (ReflectiveOperationException | LinkageError exception) {
				Logger.e(ChineseExtensions.class.getSimpleName(), "Cannot load Chinese extension: " + exception.getMessage());
			}
		}
		return extension;
	}
}
