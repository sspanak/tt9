package io.github.sspanak.tt9.preferences;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.AssetFile;

public class PrivacyPolicyFile extends AssetFile {
	public PrivacyPolicyFile(@NonNull Context context) {
		super(context.getAssets(), BuildConfig.DOCS_DIR + "/privacy.html");
	}
}
