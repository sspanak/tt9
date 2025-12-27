package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class TutorialDialog extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_tutorial_classic);
		initCloseButton();
	}

	public static Intent generateShowIntent(Context context) {
		Intent intent = new Intent(context, TutorialDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		return intent;
	}

	@Override
	public void finish() {
		super.finish();
		new SettingsStore(getApplicationContext()).setTutorialSeen();
	}

	private void initCloseButton() {
		View button = findViewById(R.id.tutorial_close_button);
		if (!(button instanceof Button)) {
			return;
		}

		button.setOnClickListener(v -> finish());
		button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tutorial_button_background_color)));
		((Button) button).setTextColor(ContextCompat.getColor(this, R.color.tutorial_button_text_color));
	}
}
