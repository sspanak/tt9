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
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.sys.Permissions;

public class RequestPermissionDialog extends AppCompatActivity {
	public static final String PARAMETER_PERMISSION = "tt9.popup_dialog.parameter.permission";

	private final Permissions permissions;


	public RequestPermissionDialog() {
		super();
		permissions = new Permissions(this);
	}


	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		render(getPermission());
	}

	@Override
	public void finish() {
		super.finish();
		reviveMain();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		showPermissionRequiredMessage(permissions, grantResults);
		finish();
	}


	private void reviveMain() {
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(UI.COMMAND_WAKEUP_MAIN, UI.COMMAND_WAKEUP_MAIN);
		startService(intent);
	}

	private void showPermissionRequiredMessage(@NonNull String[] permissions, @NonNull int[] grantResults) {
		if (permissions.length == 0) {
			return;
		}

		if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_DENIED) {
			UI.toastLong(this, R.string.voice_input_mic_permission_is_needed);
		}

		if (permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS)) {
			new SettingsStore(this).setNotificationsApproved(grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}


	@NonNull
	private String getPermission() {
		Intent i = getIntent();
		String permission = i != null ? i.getStringExtra(PARAMETER_PERMISSION) : "";
		return permission != null ? permission : "";
	}


	private void render(@NonNull String permission) {
		if (permission.equals(Manifest.permission.RECORD_AUDIO) && permissions.noRecordAudio()) {
			permissions.requestRecordAudio();
		} else if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
			if (permissions.noPostNotifications()) {
				permissions.requestPostNotifications();
			} else {
				new SettingsStore(this).setNotificationsApproved(true);
				finish();
			}
		} else {
			finish();
		}
	}


	public static void show(InputMethodService ims, String permission) {
		Intent intent = new Intent(ims, RequestPermissionDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_PERMISSION, permission);
		ims.startActivity(intent);
	}
}
