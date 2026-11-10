package io.github.sspanak.tt9.extensions;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/** Resolves one matching release asset; all executable content still uses Android installation consent. */
public final class ChinesePackDownload {
	private static String certificate;
	private static String baseHash;
	private static long checkedGeneration = -1;
	private static boolean current;
	private ChinesePackDownload() {}

	@SuppressWarnings("deprecation")
	private static synchronized String certificate(Context context) throws IOException {
		if (certificate != null) return certificate;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), Build.VERSION.SDK_INT >= 28
				? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES);
			Signature[] signers = Build.VERSION.SDK_INT >= 28 ? info.signingInfo.getApkContentsSigners() : info.signatures;
			if (signers == null || signers.length != 1) throw new IOException("Expected one app signing certificate");
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(signers[0].toByteArray());
			StringBuilder hex = new StringBuilder(64);
			for (byte value : bytes) hex.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
			return certificate = hex.toString();
		} catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException exception) {
			throw new IOException("Cannot identify installed app", exception);
		}
	}

	private static synchronized String baseHash(Context context) throws IOException {
		if (baseHash == null) baseHash = ChinesePackDelivery.sha256(new File(context.getApplicationInfo().sourceDir));
		return baseHash;
	}

	public static void validate(Context context, File directory, boolean required) throws IOException {
		if (!required && !new File(directory, "delivery.properties").isFile()) return;
		try {
			ChinesePackDelivery.validate(directory, context.getPackageName(), BuildConfig.VERSION_CODE,
				BuildConfig.FLAVOR + Character.toUpperCase(BuildConfig.BUILD_TYPE.charAt(0)) + BuildConfig.BUILD_TYPE.substring(1),
				certificate(context), baseHash(context));
		} catch (IOException exception) { throw new InvalidPackException(exception); }
	}

	/** Older manually imported packs are accepted; newly published packs bind to the exact base APK. */
	public static synchronized boolean isCurrent(Context context) {
		if (!ChinesePack.isDataAvailable(context) || !ChinesePackInstaller.isInstalled(context)) return false;
		long generation = ChinesePack.getGeneration();
		if (checkedGeneration != generation) {
			try { validate(context, ChinesePack.getApk(context).getParentFile(), false); current = true; }
			catch (IOException exception) { current = false; }
			checkedGeneration = generation;
		}
		return current;
	}

	public static InputStream open(Context context) throws IOException {
		String name = ChinesePackDelivery.assetName(context.getPackageName(), BuildConfig.VERSION_CODE,
			BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, certificate(context));
		String base = BuildConfig.CHINESE_PACK_BASE_URL;
		URL url = new URL(base + (base.endsWith("/") ? "" : "/") + name);
		if (!"https".equals(url.getProtocol()) && !(BuildConfig.DEBUG && "http".equals(url.getProtocol()) && "127.0.0.1".equals(url.getHost()))) {
			throw new IOException("Chinese downloads require HTTPS");
		}
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(SettingsStore.DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT);
		connection.setReadTimeout(SettingsStore.DICTIONARY_DOWNLOAD_READ_TIMEOUT);
		try {
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) throw new UnavailableException();
			return new FilterInputStream(connection.getInputStream()) {
				private int total;
				private void count(int bytes) throws IOException {
					if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Chinese download cancelled");
					if (bytes > 0 && (total += bytes) > 64 * 1024 * 1024) throw new IOException("Chinese download is too large");
				}
				@Override public int read() throws IOException { int value = in.read(); count(value < 0 ? 0 : 1); return value; }
				@Override public int read(byte[] bytes, int offset, int length) throws IOException { int size = in.read(bytes, offset, length); count(size); return size; }
				@Override public void close() throws IOException { try { super.close(); } finally { connection.disconnect(); } }
			};
		} catch (IOException exception) { connection.disconnect(); throw exception; }
	}

	public static final class UnavailableException extends IOException {}
	public static final class InvalidPackException extends IOException {
		InvalidPackException(IOException cause) { super("Incompatible or damaged Chinese pack", cause); }
	}
}
