package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.net.Uri;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.Logger;

public class CustomWordsImporter extends AbstractFileProcessor {
	private Uri file;

	@Override
	protected void runSync(Activity activity) {
		Logger.d(getClass().getSimpleName(), "=========> Starting to import: + "+ file);
	}

	public boolean run(@NonNull Activity activity, Uri file) {
		if (file == null) {
			Logger.e(getClass().getSimpleName(), "Can not import words from a NULL file URI");
			sendFailure();
			return false;
		}

		this.file = file;
		return super.run(activity);
	}

	@Override
	protected void sendSuccess() {

	}
}
