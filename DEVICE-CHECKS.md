# Device acceptance checks

These checks require a physical TV or TV emulator; the automated build does not establish their results.

1. Install the debug APK. Confirm Morrow appears in the TV launcher and starts with focus on Study today (or the dated nearest lesson).
2. Using only the remote, open a lesson, visit all five steps, scroll long text, go backward, and finish. Focused buttons should have a gold border; bottom controls should remain visible at normal TV display scale.
3. Force-stop/relaunch. Completion and translation choice should persist.
4. Disconnect the TV from the network and relaunch. All 28 bundled lessons, scripture text, and illustrations should be available from the first launch, without configuration.
5. Try Read aloud with an installed offline English voice. Test Stop audio, navigating during speech, and leaving the app: narration should stop. With no offline voice, Read aloud should be disabled.
6. Select ICB. Confirm its missing-text message and Use KJV button; KJV should be labeled accurately.
7. Edit a title in one weekly YAML file, rebuild, and install using the same signing key. The new title should appear, with existing progress preserved.
8. Verify the TV's local date/timezone. Today follows that date. Outside the pack's range, the nearest lesson is explicitly labeled with its date.
