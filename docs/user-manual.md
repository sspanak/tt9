# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

All Source code and documentation are available on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9).

## Initial Setup
After installing, in order to use Traditional T9, you need to enable it as an Android keyboard. To do so:

- Go to Android Settings → System → Languages → Keyboards.
- Add Traditional T9 IME.

_The actual menu names may vary depending on your Android version and phone._

### Enabling Predictive Mode
With the default settings, it is only possible to type in 123 and ABC modes. In order to enable the Predictive mode, there are additional steps:

- Open the [Settings screen](#settings-screen).
- Select the desired languages.
- Load the dictionaries.

_If you don't do the above, there will be no suggestions when typing in Predictive mode._

#### Dictionary Tips
Once a dictionary is loaded, it will stay there until you use the Clear option. This means you can enable and disable languages without reloading their dictionaries every time. Just do it once, only the first time.

It also means that if you need to start using language X, you can safely disable all other languages, load only dictionary X (and save time!), then re-enable all languages you used before.

## Hotkeys

#### D-pad Up (↑):
Select previous word/letter suggestion.

#### D-pad Down (↓):
Select next word/letter suggestion.

#### D-pad Right (→):
_Predictive mode only._

- **Single press**: Filter the suggestion list, leaving out only the ones that start with the current word. It doesn't matter if it is a complete word or not. For example, type "rewin" and press Right. It will leave out all words starting with "rewin": "rewin" itself, "rewind", "rewinds", "rewinded", "rewinding", and so on.
- **Double press**: Expand the filter to the full suggestion. For example, type "rewin" and press Right twice. It will first filter by "rewin", then expand the filter to "rewind". You can keep expanding the filter with Right, until you get to the longest suggestion in the list.

Filtering can also be used to type unknown words. Let's say you want to type "Anakin", which is not in the dictionary. Start with "A", then press Right to hide "B" and "C". Now press 6-key. Since the filter is on, in addition to the real dictionary words, it will provide all possible combinations for 6: "Am", "An", "Ao". Select "An" and press Right to confirm your selection. Now pressing 2-key, will provide "Ana", "Anb", "Anc". You can keep going, until you complete "Anakin".

#### D-pad Left (←):
_Predictive mode only._

- Clear the suggestion filter, if applied.
- When no filter is applied, accept the current word as-is, even if it does not fully match a suggestion, then jump before it.

#### 0-key:
- **In 123 mode:**
  - **Press:**: type "0".
  - **Hold:** type "+".
- **In ABC mode:**
  - **Press:** type space, newline or special/math characters.
  - **Hold:** type "0".
- **In Predictive mode:**
  - **Press:** type space, newline or special/math characters.
  - **Multiple Press:** type multiple spaces.
  - **Hold:** type "0".

#### 1- to 9-key:
- **In 123 mode:** type the respective number.
- **In ABC and Predictive mode:** type a letter/punctuation character or hold to type the respective number.

#### Add Word Key (Default: Press ✱):
Add a new word to the dictionary for the current language.

#### Backspace Key (Default: Press ↩ / Back):
Just deletes text.

**Note:** The default "Back" key plays a somewhat special role in Android. This role needs to be preserved for your phone to remain usable. Have in mind the notes below:
- **Short Press when there is no text**: Go back to the previous screen (the system default action).
- **Short Press when there is text:** Some applications, most notably Firefox and Spotify, take full control of the "Back" key. This means, it may function as the application authors intended, instead of as backspace. In such cases, you could use the on-screen backspace instead. Unfortunately, nothing else could be done, because this is a restriction posed by Android.
- **Long Press**: Whatever the system default action is (i.e. show running applications list).

All this does not apply, when using other keys. They will just delete text

#### Next Input Mode Key (Default: Press #):
- **Press when there are no suggestions:** Cycle the input modes (abc → ABC → Predictive → 123). Note that only 123 mode is available in numeric fields and Predictive mode is not available in password fields.
- **Press while suggestions are on:** Toggle the suggestions between UPPERCASE and lowercase.
- **Number-only fields:** No special action. Type a "#" with the default key. Changing the mode is not possible in such fields.

#### Next Language Key (Default: Hold #):
Select the next language, when mulitple languages have been enabled from the Settings.

#### Settings Key (Default: Hold ✱):
Open the Configration screen.

## On-screen Soft keys
All functionality is available using the keypad, but for convenience, on touchscreen phones, you could also use the on-screen keys. If you instead prefer to have more screen space, disable them from the Settings.

#### Left Soft Key:
Open the [Settings screen](#settings-screen).

#### Right Soft Key:
Backspace.

## Settings Screen
On the Settings screen, you can choose languages for typing, configure the keypad hotkeys or change the application appearance.

To access it:
- Start typing in a text field to wake up TT9.
- Use the on-screen gear button or hold the Settings Key.

## License
- The source code, the logo image and the icons are licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- The dictionaries are licensed under the licenses provided in the [respective readme files](dictionaries/), where applicable. Detailed information about the dictionaries is also available there.
- [Silver foil photo created by rawpixel.com - www.freepik.com](https://www.freepik.com/photos/silver-foil)
- "Roboto" font is under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- "Negotiate" and "Vibrocentric" fonts are under [The Fontspring Desktop/Ebook Font End User License](desktop-ebook-EULA-1.8.txt).

## Privacy Policy
Traditional T9 does not collect any information about you or about the way you are using using the application.
