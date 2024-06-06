package io.github.sspanak.tt9.ui.dialogs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Permissions;

public class RequestPermissionDialog extends AppCompatActivity {
	private final Permissions permissions;

	public RequestPermissionDialog() {
		super();
		permissions = new Permissions(this);
	}

	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);

		// currently there is only one permission to request, so we don't ovecomplicate it
		permissions.requestRecordAudio();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		showPermissionRequiredMessage(permissions, grantResults);
		finish();
		reviveMain();
	}

	private void reviveMain() {
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(PopupDialog.INTENT_CLOSE, "");
		startService(intent);
	}

	private void showPermissionRequiredMessage(@NonNull String[] permissions, @NonNull int[] grantResults) {
		if (permissions.length == 0) {
			return;
		}

		if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_DENIED) {
			UI.toastLong(this, R.string.voice_input_mic_permission_is_needed);
		}
	}

	public static void show(InputMethodService ims) {
		Intent intent = new Intent(ims, RequestPermissionDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		ims.startActivity(intent);
	}
}
