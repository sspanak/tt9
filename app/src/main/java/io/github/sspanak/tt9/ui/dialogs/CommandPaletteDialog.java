package io.github.sspanak.tt9.ui.dialogs;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.ActivityWithNavigation;

public class CommandPaletteDialog extends ActivityWithNavigation {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.command_palette);
		prependCommandIndexes();
	}

	private void prependCommandIndexes() {

	}

	public static void show(InputMethodService ims) {
		Intent intent = new Intent(ims, CommandPaletteDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		ims.startActivity(intent);
	}
}
