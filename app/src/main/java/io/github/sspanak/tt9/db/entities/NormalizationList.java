package io.github.sspanak.tt9.db.entities;

import java.util.regex.Pattern;

public class NormalizationList {
	private final static Pattern validPositions = Pattern.compile("^[0-9,]+\\d|\\d+$");

	public int langId = -1;
	public String positions = null;

	public NormalizationList(String rawNormalizationResponse) {
		if (rawNormalizationResponse == null) {
			return;
		}

		String[] parts = rawNormalizationResponse.split(",", 2);
		if (arePartsValid(parts)) {
			langId = Integer.parseInt(parts[0]);
			positions = parts[1];
		}
	}

	private boolean arePartsValid(String[] parts) {
		if (parts.length != 2 || !validPositions.matcher(parts[1]).matches()) {
			return false;
		}

		try {
			Integer.parseInt(parts[0]);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
