# Traditional T9
T9 IME input for Android devices with hardware keypad.
This is a clone of the [original project](https://github.com/Clam-/TraditionalT9) by Clam-.

## About the project
traditional-t9 is an Input Method Editor for Android that implements predictive text using the hardware keypad on the device. **Useless without a hardware numerical keypad.**

Original Wiki: [Traditional T9 keypad IME for Android](https://github.com/Clam-/TraditionalT9/wiki/Traditional-T9-keypad-IME-for-Android)

## Adding a new language
To support a new language one needs to:

- Modify CharMap.java
- New Map needs to be created with the characters to be encountered in addWord or in the user dictionary when loaded.
- New character array needs to be added for characters that are to cycle on each number press.
- New array needs to be made to tell where the capital letters start in that array.
- modify LangHelper.java
- Add status icons
    - Create proper icons for each mode (e.g. "Ab", "En", "12") and each screen size. The font must be Roboto Lt at an adequate size to fit the icon square with minimum padding. Text must be white and the background must be transparent as per the [official Android guide](https://android-doc.github.io/guide/practices/ui_guidelines/icon_design_status_bar.html). To simplify the process, you could use Android Studio. It has a built-in icon generator accessible by right-cicking on "drawable" folder -> New -> Image Asset. Then choose "Icon Type": "Notification Icons", "Asset Type": Text, "Trim": No, "Padding": 0%.
    - Add new entry in ICONMAP
- Add new LANGUAGE enum entry e.g. FR(3,5) (index, id) Where index is index in arrays like LOCALES, and id is the identifier used in the database and such. The latter should never change unless database update is done.
- Make sure new id matches const.xml
- Add the LOCALE of the language in the LOCALES Locale array
- Add translations for arrays.xml and strings.xml in to new files in the appropriate locale folder (e.g. res/values-de/arrays.xml.) AndroidStudio has a cute/nice Translation Editor which might be handy.
    - Edit the base arrays.xml file to add the new language. (pref_lang_titles, pref_lang_values)
    - Exclude translatable="false" items from the new locale arrays.xml file.
    - Also make sure the new language is added to pref_loaduserdictdesc (base non-locale strings.xml)
- Find a suitable dictionary and add it to assets

That should be it? I hope.
