package io.github.sspanak.tt9.ui.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ui.UI;

public class EditWordDialog extends AddWordDialog {
	public EditWordDialog(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
		super(tt9, language, word);
	}


	@Override
	protected void setStrings() {
		title = "Editing: " + word;
		cancelLabel = tt9.getString(android.R.string.cancel);
		neutralLabel = null;
		OKLabel = tt9.getString(R.string.add_word_add);
		message = null;
	}


	private void showAddDialog() {
		super.setStrings();
		super.show();
	}


	public void show() {
		if (isWordTooShort) {
			UI.toastLong(context, R.string.add_word_no_selection);
			close();
			return;
		}

		render(this::showAddDialog, this::close, null, null);
	}
}
