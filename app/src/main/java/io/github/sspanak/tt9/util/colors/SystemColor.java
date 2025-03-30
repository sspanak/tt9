package io.github.sspanak.tt9.util.colors;

public class SystemColor {
	protected int color;

	protected SystemColor(int color) {
		this.color = color;
	}

	public SystemColor() {
		color = 0;
	}

	final public int get() {
		return color;
	}

	final public String toCssColor() {
		return "color:" + toHex() + ";";
	}

	final public String toHex() {
		String hexColor = String.format("%06x", color);
		hexColor = hexColor.length() == 8 ? hexColor.substring(2) : hexColor;
		return "#" + hexColor;
	}
}
