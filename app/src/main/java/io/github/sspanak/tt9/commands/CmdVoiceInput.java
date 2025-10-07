package io.github.sspanak.tt9.commands;

public class CmdVoiceInput implements Command {
	public static final String ID = "key_voice_input";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_fn_voice; }
	public int getIconOff() { return io.github.sspanak.tt9.R.drawable.ic_fn_voice_off; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_voice_input; }

	public static void run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.toggleVoiceInput();
		}
	}
}
