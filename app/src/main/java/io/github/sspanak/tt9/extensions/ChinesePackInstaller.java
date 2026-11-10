package io.github.sspanak.tt9.extensions;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.Logger;

/** Android's package installer verifies the split against the installed app's version and signer. */
public final class ChinesePackInstaller {
	public static final String SPLIT_NAME = "chinese";
	public static final String LANGUAGE_SPLIT_NAME = "config.zh_rCN";
	private static final String CHANNEL = "chinese_pack";
	private static final int NOTIFICATION_ID = 462106;
	private static final String REMOVE = "remove";
	private static final String OFFER = "offer";

	private ChinesePackInstaller() {}

	private static SharedPreferences state(Context context) {
		return context.getSharedPreferences(CHANNEL, Context.MODE_PRIVATE);
	}

	private static File installedApk(Context context, String splitName) {
		ApplicationInfo info = context.getApplicationInfo();
		try {
			String[] names = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).splitNames;
			if (names != null && info.splitSourceDirs != null) {
				for (int i = 0; i < names.length && i < info.splitSourceDirs.length; i++) {
					if (splitName.equals(names[i])) return new File(info.splitSourceDirs[i]);
				}
			}
		} catch (PackageManager.NameNotFoundException ignored) {}
		return null;
	}

	public static boolean isInstalled(Context context) { return installedApk(context, SPLIT_NAME) != null; }
	public static boolean isLanguageInstalled(Context context) { return installedApk(context, LANGUAGE_SPLIT_NAME) != null; }
	public static boolean isRemovalPending(Context context) {
		if (!state(context).getBoolean(REMOVE, false)) return false;
		if (isInstalled(context) || isLanguageInstalled(context)) return true;
		completed(context); // Removing splits can kill the callback activity before it records success.
		return false;
	}

	public static boolean needsInstall(Context context) {
		File downloaded = ChinesePack.getApk(context);
		if (!downloaded.isFile()) return false;
		try {
			return differs(downloaded, installedApk(context, SPLIT_NAME))
				|| (ChinesePack.getLanguageApk(context).isFile() && differs(ChinesePack.getLanguageApk(context), installedApk(context, LANGUAGE_SPLIT_NAME)));
		} catch (IOException exception) {
			Logger.w(CHANNEL, "Cannot inspect Chinese split: " + exception.getMessage());
			return true;
		}
	}

	private static boolean differs(File downloaded, File installed) throws IOException {
		return installed == null || !MessageDigest.isEqual(hash(downloaded), hash(installed));
	}

	private static byte[] hash(File file) throws IOException {
		try {
			MessageDigest hash = MessageDigest.getInstance("SHA-256");
			try (FileInputStream input = new FileInputStream(file)) {
				byte[] buffer = new byte[16384];
				for (int size; (size = input.read(buffer)) != -1;) hash.update(buffer, 0, size);
			}
			return hash.digest();
		} catch (NoSuchAlgorithmException exception) {
			throw new IOException(exception);
		}
	}

	public static void dictionaryInstalled(Context context) {
		state(context).edit().putBoolean(REMOVE, false).apply();
		if (needsInstall(context)) schedule(context);
		else completed(context);
	}

	public static void dictionaryRemoved(Context context) {
		state(context).edit().clear().apply();
		NotificationManager manager = context.getSystemService(NotificationManager.class);
		if (manager != null) manager.cancel(NOTIFICATION_ID);
		if (!isInstalled(context) && !isLanguageInstalled(context)) return;
		state(context).edit().putBoolean(REMOVE, true).apply();
		schedule(context);
	}

	/** Legacy dictionary URLs may lack extension files; offer local import once per app version. */
	public static void offerMissingPack(Context context) {
		if (state(context).getInt("offered_version", -1) == BuildConfig.VERSION_CODE) return;
		state(context).edit().putInt("offered_version", BuildConfig.VERSION_CODE).apply();
		schedule(context);
	}

	private static void schedule(Context context) {
		if (context instanceof ChinesePackInstallActivity) {
			state(context).edit().putBoolean(OFFER, false).apply();
			return; // Its retained callback continues to consent; cancellation must stay cancelled.
		}
		state(context).edit().putBoolean(OFFER, true).apply();
		if (context instanceof Activity activity) {
			activity.runOnUiThread(() -> offer(activity));
			return;
		}
		NotificationManager manager = context.getSystemService(NotificationManager.class);
		if (manager == null || !manager.areNotificationsEnabled()) return;
		if (Build.VERSION.SDK_INT >= 26) {
			manager.createNotificationChannel(new NotificationChannel(CHANNEL, context.getString(R.string.chinese_pack_title), NotificationManager.IMPORTANCE_DEFAULT));
		}
		Intent intent = new Intent(context, ChinesePackInstallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("download_pack", !isRemovalPending(context) && !needsInstall(context));
		PendingIntent action = PendingIntent.getActivity(context, NOTIFICATION_ID,
			intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		manager.notify(NOTIFICATION_ID, new NotificationCompat.Builder(context, CHANNEL)
			.setSmallIcon(R.drawable.ic_dictionary_update).setContentTitle(context.getString(R.string.chinese_pack_title))
			.setContentText(context.getString(R.string.chinese_pack_system_confirmation)).setContentIntent(action).setAutoCancel(true).build());
	}

	/** Called only from a resumed settings Activity; cancellation must not create a prompt loop. */
	public static void offer(Activity activity) {
		boolean removing = isRemovalPending(activity);
		if (activity.isFinishing() || activity.isDestroyed() || !state(activity).getBoolean(OFFER, false)) return;
		state(activity).edit().putBoolean(OFFER, false).apply();
		if (removing || needsInstall(activity)) launch(activity);
		else if (!isInstalled(activity) || !ChinesePack.isDataAvailable(activity)) downloadPack(activity);
	}

	public static void launch(Activity activity) {
		activity.startActivity(new Intent(activity, ChinesePackInstallActivity.class));
	}

	public static void importPack(Activity activity) {
		activity.startActivity(new Intent(activity, ChinesePackInstallActivity.class).putExtra("pick_pack", true));
	}

	public static void downloadPack(Activity activity) {
		activity.startActivity(new Intent(activity, ChinesePackInstallActivity.class).putExtra("download_pack", true));
	}

	static void completed(Context context) {
		state(context).edit().remove(REMOVE).remove(OFFER).apply();
		NotificationManager manager = context.getSystemService(NotificationManager.class);
		if (manager != null) manager.cancel(NOTIFICATION_ID);
	}
}
