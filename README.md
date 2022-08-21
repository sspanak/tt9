# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

This is a clone of the [original project](https://github.com/Clam-/TraditionalT9) by Clam-.

## Building
The recommended way of building is using Android Studio. As the of time of writing this, the current version is Android Studio Chipmunk | 2021.2.1 Patch 1.

### Building a Debug .apk
If you have not configure Android Studio yet, follow [the official manual](https://developer.android.com/training/basics/firstapp), then follow the simple steps below to get the project running.

- _Import the project in Android Studio._
- _Prevent the "Default Activity not found" issue._ The app does not have a default view or a launcher icon. For this reason, you must configure Android Studio not to launch anything after installing, otherwise it will fail with "Default Activity not found" or a similar message. To do so:
    - Open "Edit Configurations..." (Press Shift 3 times and select it from the command list)
    - Go to "General" tab.
    - Change "Launch Options" to "Nothing"
    - Hit "OK"

That's it! Now you should be able to deploy and debug the app on your device.

You can find more info in this [Github issue](https://github.com/android/input-samples/issues/18).

### Building a Release .apk
The project is configured to build an unsigned release variant by default. You just need to select the "release" variant from Android Studio options, then `Build -> Rebuild Project`. After that, just ignore all warnings until you get to the end of the process. You will find the `.apk` in the generated 'build/' folder.

### Building a Signed .apk
- Make sure you have a signing key. If you don't have one, follow the [official manual](https://developer.android.com/studio/publish/app-signing#sign-apk).
- In `build.gradle` find the `signingConfigs` and `buildTypes` sections and uncomment them.
- Set properly the environment variables listed in `signingConfigs.release` _(You may need to restart Android Studio after that)_. Alternatively, you may simply type the actual key path, alias and passwords there. **Just make sure not to commit them!**
- Build the project normally. Android Studio should show you where it has generated the signed `.apk` file. If not, look for it in the `build/` folder.

## Adding a new language
To support a new language one needs to:

- Add status icons
    - Create a proper icon for each screen size. The icon needs to contain the abbreviation of the language. (e.g. "En" for "English").
    - The font must be Roboto Lt at an adequate size to fit the icon square with minimum padding.
    - The text must be white and the background must be transparent as per the [official Android guide](https://android-doc.github.io/guide/practices/ui_guidelines/icon_design_status_bar.html).
    - To simplify the process, you could use Android Studio. It has a built-in icon generator accessible by right-cicking on "drawable" folder -> New -> Image Asset. Then choose "Icon Type": "Notification Icons", "Asset Type": Text, "Trim": No, "Padding": 0%.
- Find a suitable dictionary and add it to `assets` folder.
- Create a new language class in `languages` folder. Make sure to set all properties. The ID should be any ID unused by another language. Currently, the range is limited between 1 and 31, so there can be 31 languages in total.
- Add the new language to the list in `LanguageCollection.java`. You only need to add it in one place, in the constructor. The order is irrelevant.
- Add a new entry in `res/values/const.xml`. Make sure the new ID matches the one in the language class.
- Add new entries in `res/values/arrays.xml`. Make sure to do so in the the translated `values-xx` folders.
- Add translations in `res/values-your-lang` folder.

## Using the app
See the [user manual](docs/user-manual.md).

## Word Lists
Here is detailed information and licenses about the word lists used:
- [Bulgarian word list](docs/bgWordlistReadme.txt)
- [English word list](docs/enWordlistReadme.txt)
- [French word list](docs/frWordlistReadme.txt)
- [German word list](docs/deWordlistReadme.txt)
- [Russian word list](docs/ruWordlistReadme.txt)
- [Ukrainian word list](docs/ukWordlistReadme.txt)

## License
- The source code, the logo image and the icons are licensed under the conditions described in [LICENSE.txt](LICENSE.txt).
- The word lists / dictionaries are licensed under the licenses provided in the [respective readme files](#word-lists), where applicable.
- [Silver foil photo created by rawpixel.com - www.freepik.com](https://www.freepik.com/photos/silver-foil)
- "Roboto" font is under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- "Negotiate" and "Vibrocentric" fonts are under [The Fontspring Desktop/Ebook Font End User License](docs/desktop-ebook-EULA-1.8.txt).