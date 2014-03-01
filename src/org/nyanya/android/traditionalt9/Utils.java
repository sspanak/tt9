package org.nyanya.android.traditionalt9;

import android.text.InputType;
import android.util.Log;

@SuppressWarnings("unused")
class Utils {

	public static void printFlags(int inputType) {
		if ((inputType & InputType.TYPE_CLASS_DATETIME) == InputType.TYPE_CLASS_DATETIME)
			Log.i("Utils.printFlags", "TYPE_CLASS_DATETIME");
		if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER)
			Log.i("Utils.printFlags", "TYPE_CLASS_NUMBER");
		if ((inputType & InputType.TYPE_CLASS_PHONE) == InputType.TYPE_CLASS_PHONE)
			Log.i("Utils.printFlags", "TYPE_CLASS_PHONE");
		if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT)
			Log.i("Utils.printFlags", "TYPE_CLASS_TEXT");
		if ((inputType & InputType.TYPE_DATETIME_VARIATION_DATE) == InputType.TYPE_DATETIME_VARIATION_DATE)
			Log.i("Utils.printFlags", "TYPE_DATETIME_VARIATION_DATE");
		if ((inputType & InputType.TYPE_DATETIME_VARIATION_NORMAL) == InputType.TYPE_DATETIME_VARIATION_NORMAL)
			Log.i("Utils.printFlags", "TYPE_DATETIME_VARIATION_NORMAL");
		if ((inputType & InputType.TYPE_DATETIME_VARIATION_TIME) == InputType.TYPE_DATETIME_VARIATION_TIME)
			Log.i("Utils.printFlags", "YPE_DATETIME_VARIATION_TIME");
		if ((inputType & InputType.TYPE_NULL) == InputType.TYPE_NULL)
			Log.i("Utils.printFlags", "TYPE_NULL");
		if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL)
			Log.i("Utils.printFlags", "TYPE_NUMBER_FLAG_DECIMAL");
		if ((inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == InputType.TYPE_NUMBER_FLAG_SIGNED)
			Log.i("Utils.printFlags", "TYPE_NUMBER_FLAG_SIGNED");
		if ((inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) == InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_AUTO_COMPLETE");
		if ((inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) == InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_AUTO_CORRECT");
		if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) == InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_CAP_CHARACTERS");
		if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) == InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_CAP_SENTENCES");
		if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_WORDS) == InputType.TYPE_TEXT_FLAG_CAP_WORDS)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_CAP_WORDS");
		if ((inputType & InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_IME_MULTI_LINE");
		if ((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_MULTI_LINE");
		if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
			Log.i("Utils.printFlags", "TYPE_TEXT_FLAG_NO_SUGGESTIONS");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_EMAIL_ADDRESS");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT) == InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_EMAIL_SUBJECT");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_FILTER) == InputType.TYPE_TEXT_VARIATION_FILTER)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_FILTER");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE) == InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_LONG_MESSAGE");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_NORMAL) == InputType.TYPE_TEXT_VARIATION_NORMAL)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_NORMAL");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_PASSWORD");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_PERSON_NAME) == InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_PERSON_NAME");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_PHONETIC) == InputType.TYPE_TEXT_VARIATION_PHONETIC)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_PHONETIC");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS) == InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_POSTAL_ADDRESS");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE) == InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_SHORT_MESSAGE");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_URI) == InputType.TYPE_TEXT_VARIATION_URI)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_URI");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_VISIBLE_PASSWORD");
		if ((inputType & InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT) == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT)
			Log.i("Utils.printFlags", "TYPE_TEXT_VARIATION_WEB_EDIT_TEXT");

	}
}
