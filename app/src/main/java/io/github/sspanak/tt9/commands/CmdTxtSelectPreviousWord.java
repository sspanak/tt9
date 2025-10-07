package io.github.sspanak.tt9.commands;

public class CmdTxtSelectPreviousWord implements Command {
	public static final String ID = "key_txt_select_left_word";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_txt_word_back; }
	public int getName() { return 0; }
}
