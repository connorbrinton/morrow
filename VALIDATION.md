# Validation

Creation-environment checks for the offline version:

- Python content validation: PASS — four weeks, 28 lessons, October 19–November 15, 2026.
- Python tests: PASS — five tests (complete content, missing scripture, duplicate dates, invalid dates, duplicate YAML keys).
- JVM parser tests: PASS — three tests (complete pack/date selection, unsupported schema, invalid dates).
- Android debug APK assembly: PASS — package `family.morrow`, version 0.1.0, min API 26, target API 35.
- Android lint: PASS with zero errors; nonblocking warnings remain for dependency/tooling recommendations, vector compatibility, explicit orientation, preference extensions, and backup configuration.
- Inspection of the built APK: PASS — 28 bundled lessons with all referenced KJV passages; no INTERNET permission.
- App source contains no content downloader, remote-source setting, or downloaded-content cache. Narration selects an installed voice marked as not requiring a network connection.

Physical TV/emulator navigation and offline narration have not been exercised. See DEVICE-CHECKS.md. ICB text is not included; licensing/source remains unresolved. GitHub Actions is configured; its remote run is separate from these local results.
