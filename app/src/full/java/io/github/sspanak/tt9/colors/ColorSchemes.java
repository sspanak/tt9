package io.github.sspanak.tt9.colors;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class ColorSchemes {
	@NonNull
	public final HashMap<Integer, FactoryColorScheme> list = new HashMap<>();

	public ColorSchemes() {
		list.put(ColorSchemeSystem.ID, ColorSchemeSystem::new);
		list.put(ColorSchemeSystemDark.ID, ColorSchemeSystemDark::new);
		list.put(ColorSchemeSystemLight.ID, ColorSchemeSystemLight::new);
	}
}
