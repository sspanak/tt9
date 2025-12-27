package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.dialogs.TutorialDialog;

public class CmdShowTutorial implements Command {
	@Override public String getId() { return "cmd_show_tutorial"; }
	@Override public int getIcon() { return R.drawable.ic_fn_tutorial; }
	@Override public int getName() { return 0; }
	@Override public String getName(@NonNull Context context) { return null; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.getSettings().getTutorialSeen();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.startActivity(TutorialDialog.generateShowIntent(tt9.getApplicationContext()));
		return true;
	}
}
