package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;
import io.github.sspanak.tt9.ui.dialogs.ConfirmDictionaryUpdateDialog;
import io.github.sspanak.tt9.ui.dialogs.PopupDialog;

public class PopupDialogActivity extends AppCompatActivity {
	private static final String LOG_TAG = PopupDialogActivity.class.getSimpleName();
	public static final String DIALOG_ADD_WORD_INTENT = "tt9.popup_dialog.add_word";
	public static final String DIALOG_CONFIRM_WORDS_UPDATE_INTENT = "tt9.popup_dialog.confirm_words_update";
	public static final String DIALOG_CLOSED_INTENT = "tt9.popup_dialog.closed";


	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		PopupDialog dialog = getDialog();
		if (dialog != null) {
			dialog.render();
		}
	}


	private PopupDialog getDialog() {
		Intent i = getIntent();

		String popupType = i != null ? i.getStringExtra("popup_type") : "";
		popupType = popupType != null ? popupType : "";

		switch (popupType) {
			case DIALOG_ADD_WORD_INTENT:
				return new AddWordDialog(this, i, this::onDialogClose);
			case DIALOG_CONFIRM_WORDS_UPDATE_INTENT:
				return new ConfirmDictionaryUpdateDialog(this, i, this::onDialogClose);
			default:
				Logger.w(LOG_TAG, "Unknown popup type: '" + popupType + "'. Not displaying anything.");
				return null;
		}
	}

	private void onDialogClose(String message) {
		finish();
		if (message != null && !message.isEmpty()) {
			sendMessageToMain(message);
		}
	}

	private void sendMessageToMain(String message) {
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(DIALOG_CLOSED_INTENT, message);
		startService(intent);
	}
}
