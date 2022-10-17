# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

All Source code and documentation are available on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9).

## Initial Setup
In order to use Traditional T9, you need to enable it as an Android keyboard. To do so:

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

#### 0-key
- **In 123 mode:** type "0" or hold it to type "+".
- **In ABC mode:** type secondary punctuation or hold to type "0".
- **In Predictive mode:** type space or hold to type "0".

#### 1- to 9-key
- **In 123 mode:** type the respective number.
- **In ABC and Predictive mode:** type a letter/punctuation character or hold to type the respective number.

#### Text Mode Key (Hash/Pound/#):
- **Short press:** Cycle input modes (abc → ABC → Predictive → 123)
- **Short press while typing:** Change between UPPERCASE/lowercase.
- **Long press:** Select the next language.
- **Number-only fields:** Type a "#". Changing the mode is not possible in such fields.

#### Other Actions Key (Star/✱):
- **Short press:** Add a new word to the dictionary.
- **Long press:** Open the Configration screen.

#### Backspace Key (Back/↩):
- Just deletes text.

**Note:** "Back" key plays a somewhat special role in Android. This role needs to be preserved for your phone to remain usable. Have in mind the notes below:
- **Short Press when there is no text**: Go back to the previous screen (the system default action).
- **Short Press when there is text:** Some applications, most notably Firefox and Spotify, take full control of the "Back" key. This means, it may function as the application authors intended, instead of as backspace. In such cases, you could use the on-screen backspace instead. Unfortunately, nothing else could be done, because this is a restriction posed by Android.
- **Long Press**: Whatever the system default action is (i.e. show running applications list).

## On-screen soft keys
All functionality is available using the keypad, but for convenience, on touchscreen phones or the ones with customizable function keys, you could also use the on-screen soft keys.

#### Left Soft Key:
Open the [Settings screen](#settings-screen).

#### Right Soft Key:
Backspace.

## Settings Screen
On the Configuration screen, you can choose your preferred languages, load a dictionary for Predictive mode or view this manual.

To access it:
- Start typing in a text field to wake up TT9.
- Use the on-screen gear button or hold Other Actions Key.

## License
- The source code, the logo image and the icons are licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- The word lists / dictionaries are licensed under the licenses provided in the respective readme files found in the source code, where applicable.
- [Silver foil photo created by rawpixel.com - www.freepik.com](https://www.freepik.com/photos/silver-foil)
- "Roboto" font is under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- "Negotiate" and "Vibrocentric" fonts are under [The Fontspring Desktop/Ebook Font End User License](desktop-ebook-EULA-1.8.txt)

## Privacy Policy
Traditional T9 does not collect any information about you or about the way you are using using the application.
