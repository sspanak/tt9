package io.github.sspanak.tt9.commands;

public class CmdTxtSelectNextWord implements Command {
	public static final String ID = "key_txt_select_next_word";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_txt_word_forward; }
	public int getName() { return 0; }
}
