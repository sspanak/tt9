package io.github.sspanak.tt9.ggml;

public enum DecodingMode {
	GREEDY(0),
	BEAM_SEARCH_2(2),
	BEAM_SEARCH_5(5);

	private final int value;

	DecodingMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
