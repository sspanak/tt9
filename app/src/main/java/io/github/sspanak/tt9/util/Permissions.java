package io.github.sspanak.tt9.util;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class Permissions {
	private final Activity activity;

	public Permissions(Activity activity) {
		this.activity = activity;
	}

	public boolean noPostNotifications() {
		return
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
			&& activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
	}

	public void requestPostNotifications() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			requestPermission(Manifest.permission.POST_NOTIFICATIONS);
		}
	}

	public boolean noWriteStorage() {
		return
			Build.VERSION.SDK_INT < Build.VERSION_CODES.R
			&& isRefused(Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	public void requestWriteStorage() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
	}

	private void requestPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			activity.requestPermissions(new String[] { permission }, 0);
		}
	}

	private boolean isRefused(String permission) {
		return
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
			&& activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
	}
}
