# Traditional T9
This manual explains how to configure and use Traditional T9 in different scenarios. For download and installation instructions, please consult the README on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9). The repository includes all source code, a developer's guide, the privacy policy and supplementary documentation.

## Initial Setup
After installing, in order to use Traditional T9, you need to enable it as an Android keyboard. To do so, click on the launcher icon. If you need to take any action, all options besides Initial Setup would be disabled and there would be a label saying TT9 is disabled. Go to Initial Setup and enable it.

_If you don't see the icon right after installing, restart your phone and it should appear. Android is trying to save some battery life by not refreshing the newly installed apps list in some cases._

### Using on a touchscreen-only phone
If your phone does not have a hardware keypad, check out the [On-screen Keypad section](#on-screen-keypad).

### Enabling Predictive Mode
Predictive Mode requires a language dictionary to be loaded in order provide word suggestions. You can toggle the enabled languages and load their dictionaries from: [Settings screen](#settings-screen) → Languages.

In case you have forgotten to load some dictionary, Traditional T9 will do it for you automatically. Just go to some application where you can type and it will start loading. You will be prompted to wait until it completes and after that, you can start typing right away.

_If you [delete one or more dictionaries](#deleting-a-dictionary), they will NOT reload automatically. You will have to do so manually. Only dictionaries for newly enabled languages will load automatically._

### Dictionary Tips

#### Loading a Dictionary
Once a dictionary is loaded, it will stay there until you use one of the "delete" options. This means you can enable and disable languages without reloading their dictionaries every time. Just do it once, only the first time.

It also means that if you need to start using language X, you can safely disable all other languages, load only dictionary X (and save time!), then re-enable all languages you used before.

Have in mind reloading a dictionary will reset the suggestion popularity to the factory defaults. However, there should be nothing to worry about. For the most part, you will see little to no difference in the suggestion order, unless you oftenly use uncommon words.

#### Deleting a Dictionary

If you have stopped using languages X or Y, you could disable them and also use "Delete Unselected", to free some storage space.

To delete everything, regardless of the selection, use "Delete All".

In all cases, your custom added words will be preserved and restored once you reload the respective dictionary.

## Hotkeys

#### Previous Suggestion key (Default: D-pad Left):
Select the previous word/letter suggestion.

#### Next Suggestion key (Default: D-pad Right):
Select the next word/letter suggestion.

#### Filter Suggestions key (Default: D-pad Up):
_Predictive mode only._

- **Single press**: Filter the suggestion list, leaving out only the ones that start with the current word. It doesn't matter if it is a complete word or not. For example, type "remin" and press Filter. It will leave out all words starting with "remin": "remin" itself, "remind", "reminds", "reminded", "reminding", and so on.
- **Double press**: Expand the filter to the full suggestion. For example, type "remin" and press Filter twice. It will first filter by "remin", then expand the filter to "remind". You can keep expanding the filter until you get to the longest dictionary word.

Filtering can also be used to type unknown words. Let's say you want to type "Anakin", which is not in the dictionary. Start with "A", then press Filter to hide "B" and "C". Now press 6-key. Since the filter is on, in addition to the real dictionary words, it will provide all possible combinations for 6: "Am", "An", "Ao". Select "An" and press Filter to confirm your selection. Now pressing 2-key, will provide "Ana", "Anb", "Anc". Keep going, until you get "Anakin".

When filtering is enabled, the base text will become bold and italicized.

#### Clear Filter key (Default: D-pad Down):
_Predictive mode only._

Clear the suggestion filter, if applied.

#### D-pad Center (OK or ENTER):
- When suggestions are displayed, types the currently selected suggestion.
- Otherwise, performs the default action for the current application (e.g. send a message, go to a URL, or just type a new line).

_**Note:** Every application decides on its own what to do when OK is pressed and TT9 has no control over this._

_**Note2:** In messaging applications, you need to enable their "Send with ENTER" or similarly named setting, in order to send messages with OK. If the application has no such setting, it usually means it disallows sending messages this way. Yet, for some of them, TT9 can be enabled to send messages in the [Compatibility Settings](#send-messages-with-ok-in-facebook-messenger)._

#### 0-key:
- **In 123 mode:**
  - **Press:** type "0".
  - **Hold:** type special/math characters.
  - **Hold "0", then Press "Next Mode" (Default: Hold "0", Press "#"):** type currency characters
- **In ABC mode:**
  - **Press:** type space, newline or special/math characters.
  - **Hold:** type "0".
  - **Press "0", then Press "Next Mode" (Default: Press "0", "#"):** type currency characters
- **In Predictive mode:**
  - **Press:** type space, newline or special/math characters.
  - **Double Press:** type the character assigned in Predictive mode settings. (Default: ".")
  - **Hold:** type "0".
  - **Press "0", then Press "Next Mode" (Default: Press "0", "#"):** type currency characters

#### 1- to 9-key:
- **In 123 mode:** type the respective number or hold to type punctuation.
- **In ABC and Predictive mode:** type a letter/punctuation character or hold to type the respective number.

#### Add Word Key (Default: Press ✱):
Add a new word to the dictionary for the current language.

#### Backspace Key:
Just deletes text.

If your phone has a dedicated "Del" or "Clear" key, you do not need to set anything in the Settings, unless you want to have another Backspace. In this case, the blank option: "--" will be automatically preselected.

On phones which have a combined "Delete"/"Back", that key will be selected automatically. However, you can assign "Backspace" function to another key, so "Back" will only navigate back.

_**NB:** Using "Back" as backspace does not work in all applications, most notably Firefox, Spotify and Termux. They are able to take full control of the key and redefine its function, meaning it will do whatever the app authors intended. Unfortunately, nothing can be done, because "Back" plays a special role in Android and its usage is restricted by the system._

_**NB 2:** Holding "Back" key will always trigger the default system action (i.e. show running applications list)._

_In these cases, you could assign another key (all other keys are fully usable), or use the on-screen backspace._

#### Next Input Mode Key (Default: Press #):
- **Press when there are no suggestions:** Cycle the input modes (abc → ABC → Predictive → 123). Note that only 123 mode is available in numeric fields and Predictive mode is not available in password fields.
- **Press while suggestions are on:** Toggle the suggestions between UPPERCASE and lowercase. In case the suggestions are only special characters, switch to the next character group.
- **Number-only fields:** No special action. Type a "#" with the default key. Changing the mode is not possible in such fields.

#### Next Language Key (Default: Hold #):
Select the next language, when multiple languages have been enabled from the Settings.

#### Settings Key (Default: Hold ✱):
Open the Settings configuration screen.

#### Change Keyboard Key (Default: _unassigned_):
Open the Android Change Keyboard dialog where you can select between all installed keyboards.

## On-screen Keypad
On touchscreen-only phones, a fully functional on-screen keypad is available. Enable it from Settings → Appearance → Show On-Screen Numpad.

It is also recommended to disable the special behavior of "Back" key working as "Backspace". It is useful only for a hardware keypad. To do so, go to: Settings → Keyboard → Select Hotkeys → Backspace key, then select the "--" option.

If you do have a hardware keypad and prefer having more screen space, disable the software keys from the Settings → Appearance.

## Settings Screen
On the Settings screen, you can choose languages for typing, configure the keypad hotkeys, change the application appearance or improve the compatibility with your phone.

### How to access the Settings?

#### Method 1
Click on the Traditional T9 launcher icon.

#### Method 2
- Start typing in a text field to wake up TT9.
- Use the on-screen gear button or press the assigned hotkey [Default: Hold ✱].

#### Method 3
- Go to Android Settings → System → Languages → Keyboards (or On-Screen Keyboards/Virtual Keyboards). This is where all installed keyboards
  are configured.
- Select "Traditional T9".

_The actual menu names may vary depending on your phone, Android version and language._

### Compatibility Options & Troubleshooting
For a number of applications or devices, it is possible to enable special options, which will make Traditional T9 work better with them. You can find them in: Settings → Initial Setup, under the Compatibility section.

#### Alternative suggestion scrolling method
On some devices, in Predictive Mode, you may not be able to see all suggestions, or may not be able to scroll the list to the end. The problem occurs sometimes on Android 9 or earlier. Enable the option, if you are experiencing this issue.

#### Key repeat protection
CAT S22 Flip and Qin F21 phones are known for their low quality keypads, which degrade quickly over time, and start registering multiple clicks for a single key press. You may notice this when typing or navigating the phone menus.

For CAT phones the recommended setting is 75-100 ms. For Qin F21, try with 20-30 ms. If you are still experiencing the issue, increase the value a bit, but generally try to keep it as low as possible.

_**Note:** The higher value you set, the slower you will have to type. TT9 will ignore very quick key presses._

_**Note2:** Besides the above, Qin phones may also fail to detect long press. Unfortunately, in this case nothing can be done._

#### Send messages with OK in Facebook Messenger
Facebook Messenger fails to recognize the OK key on some devices, making it impossible to send messages with it. If you prefer to send messages using OK, instead of Messenger's own send button, enable this option. This ensures sending is possible on any phone.

#### Send messages with OK in Google Chat
Similar to the above, but for Google Chat.

_This option is still experimental. It may sometimes fail to detect the "Send" button and click another one. If this starts happening, just close the chat and reopen it._

#### Telegram/Snapchat stickers and emoji panels won't open
This happens if you are using one of the small-sized layouts. Currently, there is no permanent fix, but you can use the following workaround:
- Go to Settings → Appearance and enable On-Screen Numpad.
- Go back to the chat and click the emoji or the stickers button. They will now appear.
- You can now go back to the settings and disable the on-screen numpad. The emoji and stickers panels will remain accessible until you restart the app or the phone.

#### Traditional T9 does not appear immediately in some applications
If you have opened an application where you can type, but TT9 does not wake up, just start typing and it will. Alternatively, you could also use the hotkeys for changing [the input mode](#next-input-mode-key-default-press-) or the [language](#next-language-key-default-hold-). You may also use the OK key, but this is not recommended, because you may accidentally send a message, submit a form or perform another action, depending on the application.

**Long explanation.** The reason for this problem is Android is originally designed for touchscreen devices. Hence, it expects you to touch the text/number field to show the keyboard. It is possible to make TT9 appear without this confirmation, but then, in some cases, Android will forget to hide it when it must. For example, it may remain visible after you have dialed a phone number or after you have submitted text in a search field.

For these reasons, in order to stick with the expected Android standards, the control is in your hands. Just press a key to "touch" the screen and keep typing.