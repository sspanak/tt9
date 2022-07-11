# Traditional T9
T9 IME input for Android devices with hardware keypad.
This is a clone of the [original project](https://github.com/Clam-/TraditionalT9) by Clam-.

## About the project
traditional-t9 is an Input Method Editor for Android that implements predictive text using the hardware keypad on the device. **Useless without a hardware numerical keypad.**

Original Wiki: [Traditional T9 keypad IME for Android](https://github.com/Clam-/TraditionalT9/wiki/Traditional-T9-keypad-IME-for-Android)

## Building
The recommended way of building is using Android Studio. As the of time of writing this, the current version is Android Studio Chipmunk | 2021.2.1 Patch 1.

### Building a Debug .apk
Clone and Build instructions ...

### Building a Release .apk
The project is configured to build an unsigned release variant by default. You just need to select the "release" variant from Android Studio options, then `Build -> Rebuild Project`. After that, just ignore all warnings until you get to the end of the process. You will find the `.apk` in the generated 'build/' folder.

### Building a Signed .apk
- Make sure you have a signing key. If you don't have one, follow the [official manual](https://developer.android.com/studio/publish/app-signing#sign-apk).
- In `build.gradle` find the `signingConfigs` and `buildTypes` sections and uncomment them.
- Set properly the environment variables listed in `signingConfigs.release` _(You may need to restart Android Studio after that)_. Alternatively, you may simply type the actual key path, alias and passwords there. **Just make sure not to commit them!**
- Build the project normally. Android Studio should show you where it has generated the signed `.apk` file. If not, look for it in the `build/` folder.

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
