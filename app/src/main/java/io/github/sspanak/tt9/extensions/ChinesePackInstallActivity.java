package io.github.sspanak.tt9.extensions;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

/** Installation/removal always goes through Android's own consent and signature verification. */
public class ChinesePackInstallActivity extends Activity {
	private static final int UNKNOWN_SOURCES = 1;
	private static final int CONFIRM_INSTALL = 2;
	private static final int PICK_PACK = 3;
	private ImportResult importResult;

	@Override protected void onCreate(Bundle state) {
		super.onCreate(state);
		if (getLastNonConfigurationInstance() instanceof ImportResult previous) {
			importResult = previous;
			showImportProgress();
			importResult.attach(this);
		} else if (state != null && state.getBoolean("importing")) {
			fail("Chinese import was interrupted");
		} else if (state == null) {
			if (getIntent().hasExtra(PackageInstaller.EXTRA_STATUS)) handleStatus(getIntent());
			else begin();
		}
	}

	@Override public Object onRetainNonConfigurationInstance() { return importResult; }

	@Override protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean("importing", importResult != null);
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleStatus(intent);
	}

	private void begin() {
		if (getIntent().getBooleanExtra("pick_pack", false)) {
			startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
				.setType("application/zip").putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/zip", "application/x-zip-compressed", "application/octet-stream"}), PICK_PACK);
			return;
		}
		if (getIntent().getBooleanExtra("download_pack", false)) {
			LanguageCollection.init(this);
			Language language = LanguageCollection.getByLocale("zh_CN");
			showImportProgress();
			importResult = new ImportResult();
			importResult.attach(this);
			if (language == null || !DictionaryLoader.downloadChinesePack(this, new SettingsStore(this), language, importResult)) {
				fail("Chinese download is unavailable or already running");
			}
			return;
		}
		if (!ChinesePackInstaller.isRemovalPending(this) && !ChinesePackInstaller.needsInstall(this)) {
			ChinesePackInstaller.completed(this);
			finish();
			return;
		}
		beginInstall();
	}

	private void beginInstall() {
		if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
			startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())), UNKNOWN_SOURCES);
		} else {
			commit();
		}
	}

	@Override protected void onActivityResult(int request, int result, Intent data) {
		super.onActivityResult(request, result, data);
		if (request == UNKNOWN_SOURCES) {
			if (Build.VERSION.SDK_INT < 26 || getPackageManager().canRequestPackageInstalls()) commit();
			else finish();
		} else if (request == CONFIRM_INSTALL && result == RESULT_CANCELED) {
			finish();
		} else if (request == PICK_PACK) {
			if (result != RESULT_OK || data == null || data.getData() == null) { finish(); return; }
			LanguageCollection.init(this);
			Language language = LanguageCollection.getByLocale("zh_CN");
			showImportProgress();
			importResult = new ImportResult();
			importResult.attach(this);
			if (language == null || !DictionaryLoader.loadChinesePack(this, new SettingsStore(this), language, data.getData(), importResult)) {
				fail("Chinese dictionary import is unavailable or already running");
			}
		}
	}

	private void showImportProgress() {
		TextView progress = new TextView(this);
		progress.setText(R.string.chinese_pack_importing);
		setContentView(progress);
	}

	private static final class ImportResult implements Consumer<Boolean> {
		private WeakReference<ChinesePackInstallActivity> activity = new WeakReference<>(null);
		private Boolean success;

		void attach(ChinesePackInstallActivity current) {
			activity = new WeakReference<>(current);
			deliver();
		}

		@Override public void accept(Boolean result) { success = result; deliver(); }

		private void deliver() {
			ChinesePackInstallActivity current = activity.get();
			if (success == null || current == null || current.isDestroyed()) return;
			current.importResult = null;
			if (!success) {
				Toast.makeText(current, R.string.chinese_pack_download_failed, Toast.LENGTH_LONG).show();
				current.finish();
			} else if (ChinesePackInstaller.needsInstall(current)) current.beginInstall();
			else current.finish();
		}
	}

	private void commit() {
		boolean remove = ChinesePackInstaller.isRemovalPending(this);
		if ((remove && !ChinesePackInstaller.isInstalled(this) && !ChinesePackInstaller.isLanguageInstalled(this)) || (!remove && !ChinesePackInstaller.needsInstall(this))) {
			ChinesePackInstaller.completed(this);
			finish();
			return;
		}
		PackageInstaller installer = getPackageManager().getPackageInstaller();
		int sessionId = -1;
		try {
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_INHERIT_EXISTING);
			params.setAppPackageName(getPackageName());
			if (Build.VERSION.SDK_INT >= 31) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED);
			sessionId = installer.createSession(params);
			try (PackageInstaller.Session session = installer.openSession(sessionId)) {
				if (remove) {
					if (ChinesePackInstaller.isInstalled(this)) session.removeSplit(ChinesePackInstaller.SPLIT_NAME);
					if (ChinesePackInstaller.isLanguageInstalled(this)) session.removeSplit(ChinesePackInstaller.LANGUAGE_SPLIT_NAME);
				}
				else {
					writeApk(session, ChinesePack.getApk(this));
					if (ChinesePack.getLanguageApk(this).isFile()) writeApk(session, ChinesePack.getLanguageApk(this));
				}
				Intent result = new Intent(this, ChinesePackInstallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				int mutable = Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0;
				Bundle options = null;
				if (Build.VERSION.SDK_INT >= 34) {
					int mode = Build.VERSION.SDK_INT >= 36 ? ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE : ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED;
					options = ActivityOptions.makeBasic().setPendingIntentCreatorBackgroundActivityStartMode(mode).toBundle();
				}
				session.commit(PendingIntent.getActivity(this, sessionId, result, PendingIntent.FLAG_UPDATE_CURRENT | mutable, options).getIntentSender());
			}
		} catch (IOException | RuntimeException exception) {
			if (sessionId >= 0) {
				try { installer.abandonSession(sessionId); } catch (RuntimeException ignored) {}
			}
			fail(exception.getMessage());
		}
	}

	private void writeApk(PackageInstaller.Session session, java.io.File apk) throws IOException {
		try (FileInputStream input = new FileInputStream(apk); OutputStream output = session.openWrite(apk.getName(), 0, -1)) {
			byte[] buffer = new byte[16384];
			for (int size; (size = input.read(buffer)) != -1;) output.write(buffer, 0, size);
			session.fsync(output);
		}
	}

	private void handleStatus(Intent intent) {
		int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
		if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
			Intent confirmation = intent.getParcelableExtra(Intent.EXTRA_INTENT);
			if (confirmation != null) startActivityForResult(confirmation, CONFIRM_INSTALL);
			else fail("Missing Android confirmation");
		} else if (status == PackageInstaller.STATUS_SUCCESS) {
			ChinesePackInstaller.completed(this);
			finish();
		} else {
			fail(intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
		}
	}

	private void fail(String reason) {
		Logger.e(ChinesePackInstallActivity.class.getSimpleName(), "Chinese split installation: " + reason);
		Toast.makeText(this, R.string.chinese_pack_install_failed, Toast.LENGTH_LONG).show();
		finish();
	}
}
