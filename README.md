# TickTracker

A personal habit tracker, day planner and task list for Android.

TickTracker runs entirely on the phone. There is no account and no server: everything you log stays on the device, and reminders are scheduled by the phone itself, so they fire whether or not the app is open.

> **Status:** feature complete. Habits, Plan, Tasks, reminders, settings and backup are all built. Some parts are still being checked on a phone; see [docs/file_guide.md](docs/file_guide.md).

## What it does

- **Habits.** Track habits you tick off (build a routine, or quit one) and habits you measure (kilometres, pages, minutes). See streaks, a heatmap of recent months and the current week at a glance.
- **Plan.** Lay out today or any day ahead as a timeline, with the gaps between items shown so they can be filled. Tasks planned here also appear in Tasks; notes stay on the Plan.
- **Tasks.** Keep tasks by date or by priority, in several lists, with sub-tasks and repeats.
- **Reminders.** Daily reminders on habits, and one-off reminders on tasks and planned blocks. A reminder stays on the phone until you dismiss it.
- **Backup.** Export everything to a file and import it back, from Settings. The app keeps data only on the phone, so export now and then.

## How it is built

A native Android app written in Kotlin with Jetpack Compose, storing its data in an on-device database. Light and dark themes.

## Documentation

| Document | What it covers |
| --- | --- |
| [docs/decisions.md](docs/decisions.md) | Every design decision, with its date, reason and effect on the app. The source of truth for what the app should do. |
| [docs/flow_state.md](docs/flow_state.md) | How the app flows from creating a habit to tracking it, and the states a habit and a reminder move through. |
| [docs/file_guide.md](docs/file_guide.md) | What every file is for, and where it sits in the flow. |

## Getting started

Prerequisites:

- JDK 17
- Android SDK. Put its location in a `local.properties` file in the project root:
  ```
  sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
  ```
- An Android phone (Android 8 or later) with Developer options and USB debugging enabled, connected by USB

Commands, run from the project root on Windows:

| To | Run |
| --- | --- |
| Build the app | `.\gradlew.bat assembleDebug` |
| Build and install on the connected phone | `.\gradlew.bat installDebug` |
| Run the unit tests | `.\gradlew.bat testDebugUnitTest` |

The built APK is at `app/build/outputs/apk/debug/app-debug.apk`. The first build downloads Gradle and the libraries, so it needs an internet connection and takes a few minutes.
