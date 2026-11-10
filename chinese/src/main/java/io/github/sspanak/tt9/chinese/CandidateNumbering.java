package io.github.sspanak.tt9.chinese;

/** Stable shortcuts for each packed row, selected relative to the highlighted candidate. */
final class CandidateNumbering {
	private final PackedCandidateNavigation navigation;

	CandidateNumbering(PackedCandidateNavigation navigation) {
		this.navigation = navigation;
	}

	int getIndex(int number, int selectedIndex) {
		int row = navigation.getRow(selectedIndex);
		if (row < 0 || number < 1 || number > 9) return -1;
		int index = navigation.getRowStart(row) + number - 1;
		return index < navigation.getRowEnd(row) ? index : -1;
	}

	int getNumber(int index) {
		int row = navigation.getRow(index);
		if (row < 0) return 0;
		int number = index - navigation.getRowStart(row) + 1;
		return number <= 9 ? number : 0;
	}
}
