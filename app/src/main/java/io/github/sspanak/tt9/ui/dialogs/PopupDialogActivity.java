package io.github.sspanak.tt9.ui.dialogs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.util.Logger;

public class PopupDialogActivity extends AppCompatActivity {
	private static final String LOG_TAG = PopupDialogActivity.class.getSimpleName();


	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		PopupDialog dialog = getDialog();
		if (dialog != null) {
			dialog.render();
		} else {
			onDialogClose("");
		}
	}


	private PopupDialog getDialog() {
		Intent i = getIntent();

		String popupType = i != null ? i.getStringExtra(PopupDialog.PARAMETER_DIALOG_TYPE) : "";
		popupType = popupType != null ? popupType : "";

		return switch (popupType) {
			case AddWordDialog.TYPE -> new AddWordDialog(this, i, this::onDialogClose);
			case AutoUpdateMonolog.TYPE -> new AutoUpdateMonolog(this, i, this::onDialogClose);
			case ChangeLanguageDialog.TYPE -> new ChangeLanguageDialog(this, i, this::onDialogClose);
			default -> {
				Logger.w(LOG_TAG, "Unknown popup type: '" + popupType + "'. Not displaying anything.");
				yield null;
			}
		};
	}

	private void onDialogClose(String message) {
		finish();
		Intent intent = new Intent(this, TraditionalT9.class);
		if (message != null) {
			intent.putExtra(PopupDialog.INTENT_CLOSE, message);
		}
		startService(intent);
	}

	private void onDialogClose(HashMap<String, String> messages) {
		finish();
		Intent intent = new Intent(this, TraditionalT9.class);
		if (messages != null) {
			intent.putExtra(PopupDialog.INTENT_CLOSE, "");
			for (String key : messages.keySet()) {
				intent.putExtra(key, messages.get(key));
			}
		}

		startService(intent);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		onDialogClose((String) null);
	}
}
