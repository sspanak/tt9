package io.github.sspanak.tt9.commands;

public class CmdEditText implements Command {
	public static final String ID = "key_edit_text";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_txt_cut; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_edit_text; }

	public static boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		} else if (tt9.isTextEditingActive()) {
			return tt9.hideTextEditingPalette();
		} else {
			tt9.showTextEditingPalette();
			return true;
		}
	}
}
