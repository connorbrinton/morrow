# Morrow

A fully offline, parent-led Android TV app for daily scripture study with young children. Each day moves through **Discover → Read → Understand → Talk & do → Remember**, with separate prompts for ages 3 and 6 and a shared activity. Supports Android TV/Google TV 8.0 (API 26) and later.

Includes 28 daily lessons for **October 19–November 15, 2026**, following the Come, Follow Me weekly assignments for Jeremiah, Lamentations, Ezekiel, and Daniel. Lessons select short passages rather than attempt to cover every assigned chapter. All explanations and activities are original family suggestions; this is not an official Church application.

## One repository, one app

All content lives in this repository and is packaged in the APK. The app reads only its bundled asset: no server, CMS, separate content repository, runtime downloads, accounts, or network permission. Settings and completion markers stay on the device. Updating lessons means editing the YAML and installing a new build.

### Customize a day

Edit the appropriate file under `content/`. There is one file per week. The fields are:

| Field | Purpose |
| --- | --- |
| `date`, `title`, `reference` | Day, display title, scripture passage key |
| `scene` | Built-in illustration: `heart`, `tree`, or `book` |
| `discover` | Short opening context |
| `explain` | Parent-led explanation, labeled separately from scripture |
| `younger`, `older` | Prompts for each age |
| `activity` | Something to do together |
| `remember` | Closing takeaway |

Keep dates quoted, e.g. `'2026-10-19'`. Each week has seven consecutive days starting on Monday. Scripture quotations are separate from the explanation, in `translations.KJV` and (when permitted text is available) `translations.ICB`. Keys match each day's `reference`. Preserve exact Bible wording; put explanations and paraphrases in `explain`.

To add a week, copy a weekly YAML file, update the dates, passages, and prompts, and rebuild. The build validates all files, combines them, and packages the generated `build/content-assets/content.yaml`. No generated content needs to be committed.

## Build and install

Requirements: **JDK 17**, Android SDK **platform 35 / build-tools 35.0.0**, Python 3, and dependencies from `requirements.txt`. Set `ANDROID_HOME` or use a local `local.properties` containing `sdk.dir=/path/to/android-sdk`. Android Studio can configure the SDK for you.

```sh
python3 -m pip install -r requirements.txt
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Content preparation runs automatically as part of the build. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

Enable developer options/ADB on the TV and install it:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The installed app needs no internet connection. Building the project needs network access the first time to obtain development dependencies.

### GitHub builds

Push changes to this repository or select **Actions → Build TV app → Run workflow**. The workflow validates lessons, runs parser tests and lint, and uploads the **morrow-debug** APK artifact.

CI debug signing keys are temporary, so APKs from different workflow runs may require uninstalling the old version, which clears settings/progress. For ongoing installs, build locally using the same debug key or configure a stable private signing key. Never commit private signing keys.

## Translations and narration

KJV is included. KJV/ICB is a saved choice; **ICB text is not included pending a permitted source**. Missing ICB passages show a clear message and a **Use KJV** button. The app never silently labels KJV text as ICB. To add authorized ICB text, put it under the matching passage keys, add required attribution to the app and `NOTICE.md`, and rebuild. There is no Bible API integration.

Read aloud uses an **already installed offline English Android text-to-speech voice**. Network-only voices are excluded; if no compatible offline voice exists, Read aloud is disabled. Voice installation is managed by the TV/device outside this app. Reading, pictures, discussion prompts, and activities always work offline.

## TV controls

Use arrows to navigate, OK to activate, and Back to return through the lesson or to Home. The primary study/Next button receives focus on entry, with a gold outline on focused buttons. Focus the reading area to scroll long text. Steps can be skipped or revisited, and Finish records completion without streaks. Outside the included date range, the nearest available lesson is offered with its actual date. Scheduling uses the TV's local date.

## Checks

```sh
python3 scripts/prepare_content.py
python3 -m unittest discover -s scripts -p 'test_*.py'
./gradlew testDebugUnitTest lintDebug assembleDebug
```

See `VALIDATION.md` for creation-time results and `DEVICE-CHECKS.md` for remaining TV acceptance checks. See `NOTICE.md` for scripture sources and translation notices. Application code is licensed under the repository's AGPL-3.0 license; third-party scripture and dependency rights are separate.
