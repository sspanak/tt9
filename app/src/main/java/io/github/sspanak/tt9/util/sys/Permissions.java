package io.github.sspanak.tt9.util.sys;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class Permissions {
	private static final HashMap<String, Boolean> firstTimeAsking = new HashMap<>();
	@NonNull private final Activity activity;

	public Permissions(@NonNull Activity activity) {
		this.activity = activity;
	}

	public boolean noPostNotifications() {
		return
			DeviceInfo.AT_LEAST_ANDROID_13
			&& isRefused(Manifest.permission.POST_NOTIFICATIONS)
			&& (
				Boolean.TRUE.equals(firstTimeAsking.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, true))
				|| activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
			);
	}

	public void requestPostNotifications() {
		if (DeviceInfo.AT_LEAST_ANDROID_13) {
			firstTimeAsking.put(Manifest.permission.POST_NOTIFICATIONS, false);
			requestPermission(Manifest.permission.POST_NOTIFICATIONS);
		}
	}

	public boolean noRecordAudio() {
		return isRefused(Manifest.permission.RECORD_AUDIO);
	}

	public void requestRecordAudio() {
		requestPermission(Manifest.permission.RECORD_AUDIO);
	}

	public boolean noWriteStorage() {
		return
			!DeviceInfo.AT_LEAST_ANDROID_11
			&& isRefused(Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	public void requestWriteStorage() {
		if (!DeviceInfo.AT_LEAST_ANDROID_11) {
			requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
	}

	protected void requestPermission(String permission) {
		activity.requestPermissions(new String[] { permission }, 0);
	}

	protected boolean isRefused(String permission) {
		return activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
	}
}
