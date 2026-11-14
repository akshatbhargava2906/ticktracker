# TickTracker

A personal habit tracker, day planner and task list for Android.

TickTracker runs entirely on the phone. There is no account and no server: everything you log stays on the device, and reminders are scheduled by the phone itself, so they fire whether or not the app is open.

> **Status:** design agreed, implementation not started.

## What it does

- **Habits.** Track habits you tick off (build a routine, or quit one) and habits you measure (kilometres, pages, minutes). See streaks, a heatmap of recent months and the current week at a glance.
- **Plan.** Lay out a day as a timeline of blocks, with the gaps between them shown so they can be filled.
- **Tasks.** Keep tasks by date or by priority.
- **Reminders.** Set one or several daily reminders on anything. A reminder stays on the phone until you dismiss it.

## How it is built

A native Android app written in Kotlin with Jetpack Compose, storing its data in an on-device database.

## Documentation

| Document | What it covers |
| --- | --- |
| [docs/decisions.md](docs/decisions.md) | Every design decision, with its date, reason and effect on the app. The source of truth for what the app should do. |
| [docs/flow_state.md](docs/flow_state.md) | How the app flows from creating a habit to tracking it, and the states a habit and a reminder move through. |

## Getting started

Build and run commands will be added when the project is scaffolded.

Prerequisites:

- JDK 17
- Android SDK, with `ANDROID_HOME` pointing at it
- An Android phone with Developer options and USB debugging enabled
