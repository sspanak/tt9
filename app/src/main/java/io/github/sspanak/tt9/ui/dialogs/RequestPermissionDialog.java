package io.github.sspanak.tt9.ui.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Permissions;

public class RequestPermissionDialog extends PopupDialog {
	public static final String TYPE = "tt9.popup_dialog.request_permission";
	private Permissions permissions;

	RequestPermissionDialog(@NonNull Activity context, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher);
		permissions = new Permissions(context);
	}

	@Override
	void render() {
		// currently there is only one permission to request, so we don't ovecomplicate it
		permissions.requestRecordAudio();
	}

	public static void show(InputMethodService ims) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_DIALOG_TYPE, TYPE);
		ims.startActivity(intent);
	}
}
