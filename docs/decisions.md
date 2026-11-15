# Decisions

Every decision taken for TickTracker, in the order it was made, with the reason for it and its effect on the app. This is the source of truth for what the app should do. For how the pieces fit together at run time, see [flow_state.md](flow_state.md).

**How to use this file**

- New decisions are appended with the next number. Entries are never deleted.
- When a decision is reversed, the old entry is marked **Superseded** and points to the one that replaced it.
- Dates are the day the decision was made. All entries so far come from the planning session on 2026-11-14 and are listed in the order they were taken.
- "Reference app" means the app in the screenshots that this design is modelled on.

## Index

| # | Date | Decision | Status |
| --- | --- | --- | --- |
| [D01](#d01-three-sections-habits-plan-tasks) | 2026-11-14 | Three sections: Habits, Plan, Tasks | Active |
| [D02](#d02-all-data-stays-on-the-device) | 2026-11-14 | All data stays on the device | Active |
| [D03](#d03-build-as-an-installable-web-app) | 2026-11-14 | Build as an installable web app | Superseded by D20 |
| [D04](#d04-locked-features-of-the-reference-app-are-out-of-scope) | 2026-11-14 | Locked features of the reference app are out of scope | Active |
| [D05](#d05-habit-model-kind-direction-repeat-goal) | 2026-11-14 | Habit model: kind, direction, repeat goal | Active |
| [D06](#d06-four-step-add-habit-wizard) | 2026-11-14 | Four-step add habit wizard | Active |
| [D07](#d07-measurable-habits-unit-daily-target-goal) | 2026-11-14 | Measurable habits: unit, daily target, goal | Active |
| [D08](#d08-counts-are-set-with-a-stepper-only) | 2026-11-14 | Counts are set with a stepper only | Active |
| [D09](#d09-a-habit-can-have-several-categories) | 2026-11-14 | A habit can have several categories | Active |
| [D10](#d10-reminders-in-all-three-sections) | 2026-11-14 | Reminders in all three sections | Active |
| [D11](#d11-personal-app-nothing-locked) | 2026-11-14 | Personal app, nothing locked | Active |
| [D12](#d12-tick-habits-are-tapped-measurable-habits-are-typed) | 2026-11-14 | Tick habits are tapped, measurable habits are typed | Active |
| [D13](#d13-reminders-repeat-daily-weekly-habits-streak-by-week) | 2026-11-14 | Reminders repeat daily; weekly habits streak by week | Active |
| [D14](#d14-reminder-times-are-chosen-freely) | 2026-11-14 | Reminder times are chosen freely | Active |
| [D15](#d15-reset-undoes-todays-entry) | 2026-11-14 | Reset undoes today's entry | Active |
| [D16](#d16-entries-are-editable-on-their-own-day-only) | 2026-11-14 | Entries are editable on their own day only | Active |
| [D17](#d17-a-new-measurable-entry-replaces-the-old-one) | 2026-11-14 | A new measurable entry replaces the old one | Active |
| [D18](#d18-day-resets-at-2-am) | 2026-11-14 | Day resets at 2 am | Superseded by D19 |
| [D19](#d19-day-resets-at-midnight) | 2026-11-14 | Day resets at midnight | Active |
| [D20](#d20-native-android-app-with-on-device-alarms) | 2026-11-14 | Native Android app with on-device alarms | Active |
| [D21](#d21-kotlin-and-jetpack-compose) | 2026-11-14 | Kotlin and Jetpack Compose | Active |
| [D22](#d22-reminders-stay-until-dismissed) | 2026-11-14 | Reminders stay until dismissed | Active |
| [D23](#d23-room-database-gradle-builds-direct-apk-install) | 2026-11-14 | Room database, Gradle builds, direct APK install | Active |
| [D24](#d24-git-is-managed-by-the-owner-only) | 2026-11-14 | Git is managed by the owner only | Active |
| [D25](#d25-project-setup) | 2026-11-14 | Project setup: package, SDK levels, structure | Proposed |
| [D26](#d26-tracking-behaviours-confirmed) | 2026-11-15 | Tracking behaviours confirmed | Active |
| [D27](#d27-a-habit-can-be-deleted-from-its-card) | 2026-11-15 | A habit can be deleted from its card | Proposed |

---

## D01: Three sections: Habits, Plan, Tasks

**Date:** 2026-11-14 · **Status:** Active

**Decision.** The app has three sections, switched from a floating bottom navigation bar.

- **Habits.** Three top tabs: Measure, Tick and Weekly. Category chips under the tabs filter the list.
- **Plan.** A week strip to pick a day, and a vertical timeline of that day: scheduled blocks with start, end and duration; a "now" marker; gaps between blocks shown with their length and a "Plan this slot" shortcut; fixed anchors such as "Wind down"; a circle to tick a block off.
- **Tasks.** Two tabs. *Timeline* groups tasks by date with per-day progress (`3/3`), an "All done" state, a collapsible completed list and inline "Add task". *All Tasks* is a flat list grouped by priority with due dates, a collapsible completed section and search.
- **Shell.** Header with today's date and a settings button. A floating action button that changes with the section (add habit, add block, add task). Dark theme with a green accent.

**Reason.** Follows the reference app's layout, which is the experience the owner wants.

**Effect.** Sets the navigation structure and the three areas of the data model. Habits is specified in full by later decisions; Plan and Tasks are specified only at screen level so far.

## D02: All data stays on the device

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Habits, entries, plans and tasks are stored on the phone. There is no account, no sync and no backend.

**Reason.** The owner asked for local storage "for now".

**Effect.** The app works fully offline. Data is lost if the app is uninstalled or the phone is lost, so export and backup is an open item. Storage sits behind a repository layer so sync could be added later without rewriting the screens.

## D03: Build as an installable web app

**Date:** 2026-11-14 · **Status:** Superseded by [D20](#d20-native-android-app-with-on-device-alarms)

**Decision.** Build a web app installed to the home screen (PWA), so one codebase runs on Android, iOS and desktop.

**Reason.** The owner wanted a mobile app that was not limited to Android.

**Effect.** None remaining. It forced reminders through a server (Web Push), which led to D20.

## D04: Locked features of the reference app are out of scope

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Not planned: cloud sync and accounts, calendar import, AI or insights features, and multiple task sheets. iOS and web versions joined this list with D20.

**Reason.** Each is a separate piece of work, and none is needed for the core tracker.

**Effect.** The header icons for these features in the reference app are left out. They can be revisited as new decisions.

## D05: Habit model: kind, direction, repeat goal

**Date:** 2026-11-14 · **Status:** Active

**Decision.** A habit is defined by independent choices, not by a fixed list of types.

| Choice | Options |
| --- | --- |
| Kind | **Tick** (yes or no per day) or **Measurable** (numbers, distance or time) |
| Direction (Tick only) | **Build** a positive routine, or **Quit** one |
| Repeat goal (Tick) | **Once a day**, **Multiple per day**, or **Weekly goal** |

Every habit also has a name, an icon, a colour and categories. Examples: "10k" (Tick, Build, once a day), "Reclaim" (Tick, Quit), "Gym" (Tick, Build, weekly goal of 4).

**Reason.** This is how the reference app's add habit flow presents the choices.

**Effect.** Defines the habit record. Kind decides which tab a habit appears under (D01) and how it is logged (D12). Direction decides the buttons on a Tick card. Repeat goal decides how progress and streaks are counted (D13).

## D06: Four-step add habit wizard

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Habits are created in a full-screen wizard with a segmented progress bar, back navigation and one primary button at the bottom.

1. **What kind of habit?** Tick or Measurable. Tick expands to Build a Habit / Quit a Habit with a one-line explanation of the selected option.
2. **Name your habit.** Name field under a large preview of the icon in the chosen colour. Icon picker with a row of glyph icons, a row of emoji and "See All". Colour picker with a row of swatches and "See All".
3. **How do you track it?** Repeat goal for Tick habits, or unit, target and goal for Measurable habits (D07), then categories (D09).
4. **Set a daily reminder.** Optional (D10). Ends with "Save Habit".

Built-in categories: Art, Finances, Fitness, Health, Nutrition, Social, Study, Work, Morning, Day, Evening, Other, plus "+ Custom".

**Reason.** Follows the reference app.

**Effect.** Defines the creation screens. The step-by-step flow is drawn in [flow_state.md](flow_state.md#2-creating-a-habit).

## D07: Measurable habits: unit, daily target, goal

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Step 3 of the wizard for a Measurable habit asks for:

- **Unit:** hr, min, sec, km, mi, pages, glasses, reps, kcal, words, $, or a custom unit.
- **Daily target:** typed into a number field.
- **Goal:** Everyday or Weekly.

**Reason.** Specified by the owner. A target is typed because it can be any number, such as 7 km or 2,000 kcal, which a stepper would make slow to reach.

**Effect.** A measurable habit stores a unit, a target and a goal period. The card shows today's entry against the target (e.g. `5/7 km`).

## D08: Counts are set with a stepper only

**Date:** 2026-11-14 · **Status:** Active

**Decision.** For Multiple per Day (e.g. 2/day) and Weekly Goal (e.g. 3/week), the count is changed with − and + buttons. There is no text field. This applies only to these two counts; the measurable target is typed (D07).

**Reason.** Specified by the owner. These counts are small whole numbers.

**Effect.** The count is always a valid whole number, so it needs no input validation.

## D09: A habit can have several categories

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Category selection is multi-select.

**Reason.** Specified by the owner. A habit such as a morning run is both Fitness and Morning.

**Effect.** Categories are stored as a many-to-many link, not a single field. A habit appears under every category chip it belongs to.

## D10: Reminders in all three sections

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Reminders can be set on habits, Plan blocks and tasks. Every habit can have them, whatever its kind or repeat goal.

For habits they are set in step 4 of the wizard and later from the habit's settings:

- A toggle turns reminders on or off for the habit.
- A wheel time picker (hour, minute in 5-minute steps, AM/PM) with "Set This Time" adds a reminder to the list.
- A habit can have one reminder or several (e.g. 09:00 and 18:00). Each is listed with its time and can be removed individually.

**Reason.** The owner asked for single or multiple alarms per item, and confirmed reminders belong in all three sections.

**Effect.** Reminders are their own records, each linked to a habit, a block or a task. How reminders are set for Plan blocks and tasks is not yet designed.

## D11: Personal app, nothing locked

**Date:** 2026-11-14 · **Status:** Active

**Decision.** The app is for a single user, its owner. No feature is paywalled. In particular, "See All" in the colour picker opens the full palette.

**Reason.** The owner: "this app is only for me."

**Effect.** No accounts, purchases or locked states anywhere in the UI. Later choices can be made for one person and one phone, which is what made D20 acceptable.

## D12: Tick habits are tapped, measurable habits are typed

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Each habit is a card with its icon, name, streak, action buttons, an overflow menu and a history grid in the habit's colour.

**Tick card: tap to complete.**

- Build habit: a tick button marks today done. It fills the button and today's cell in the heatmap.
- Weekly-goal habit: a tick button plus a skip (−) button.
- Quit habit: a shield button (resisted) and an × button (slipped).
- History is a heatmap of the last few months: one column per week, rows Monday to Sunday, month labels above, today outlined.

**Measure card: enter the amount manually.**

- A + button opens entry of how much was done today, in the habit's unit.
- A reset button sits beside it (D15).
- History is a grid of recent weeks: one column per week, rows Monday to Sunday, each cell showing that day's value and unit, with a Total row summing each week. Today's cell and the current week's total are highlighted.

**Weekly tab.** Shows every habit with the current week as seven cells, Monday to Sunday, and its progress (`0/7 days`, `0 resisted this week`, `0/4 this week`).

**Reason.** Specified by the owner, with screenshots of both cards.

**Effect.** Defines the two card layouts and what an entry is: a state for a Tick habit, a number for a Measurable one.

## D13: Reminders repeat daily; weekly habits streak by week

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Reminders repeat every day, including for weekly-goal habits. Streaks are counted in days for daily habits and in weeks for weekly-goal habits; a week counts when its goal is met.

**Reason.** Specified by the owner. A weekly habit can be done on any day, so it needs a prompt every day, but success is judged over the week.

**Effect.** Reminders have no repeat setting; "Daily" is the only option. The streak calculation has two modes.

## D14: Reminder times are chosen freely

**Date:** 2026-11-14 · **Status:** Active

**Decision.** A reminder can be set for any time of day. It has no relation to when the item was created or scheduled.

**Reason.** Specified by the owner.

**Effect.** A reminder stores only a time of day. It is not derived from a block's start time or a task's due date.

## D15: Reset undoes today's entry

**Date:** 2026-11-14 · **Status:** Active

**Decision.** The reset button on a Measure card removes today's entry, returning the day to empty.

**Reason.** Specified by the owner.

**Effect.** Reset acts on today only, in line with D16.

## D16: Entries are editable on their own day only

**Date:** 2026-11-14 · **Status:** Active

**Decision.** An entry can be added, changed or undone only on the day it belongs to. Once the day changes, it is locked. Past days cannot be added or edited, and future days cannot be filled in ahead.

**Reason.** The owner chose to keep entries locked once the day is over.

**Effect.** The history grids are read-only; tapping a past cell does nothing. A habit forgotten until after the day ends cannot be logged for that day. The lock applies to Tick and Measurable habits alike.

## D17: A new measurable entry replaces the old one

**Date:** 2026-11-14 · **Status:** Active

**Decision.** If today already has an entry, + edits it. The number entered replaces the existing one; it is not added to it. Walking 2 km more after logging 5 km means entering 7.

**Reason.** Specified by the owner.

**Effect.** There is at most one entry per habit per day. The entry dialog opens pre-filled with the current value.

## D18: Day resets at 2 am

**Date:** 2026-11-14 · **Status:** Superseded by [D19](#d19-day-resets-at-midnight)

**Decision.** A day runs from 2:00 am to 2:00 am, so a late-night entry counts for the day before.

**Reason.** To let entries made just after midnight still count.

**Effect.** None remaining. Reversed minutes later by D19.

## D19: Day resets at midnight

**Date:** 2026-11-14 · **Status:** Active · **Supersedes:** D18

**Decision.** A day is the calendar day in local time. At 12:00 am the previous day locks and a new one starts. Measure and Tick habits use the same clock, as do Plan and Tasks.

**Reason.** The owner: one clock for everything "will be easier that way."

**Effect.** "Today" means the same thing everywhere in the app, with no offset arithmetic. An entry forgotten until 12:05 am cannot be logged for the previous day (D16).

## D20: Native Android app with on-device alarms

**Date:** 2026-11-14 · **Status:** Active · **Supersedes:** D03

**Decision.** TickTracker is a native Android app. Reminders are scheduled on the phone by Android's own alarm scheduler.

**Reason.** Reminders must reach the user when the app is closed. A web app cannot schedule a notification on the device, so it would need a server to send Web Push. Web Push was briefly accepted, then rejected by the owner because a push notification can go unnoticed. The owner uses an Android phone, so giving up iOS and web costs nothing.

**Effect.**

- No server, no hosting, nothing leaves the phone. D02 now holds with no exceptions.
- Reminders fire at the set minute, with the app closed and with the phone offline.
- iOS and web versions are out of scope (D04).
- The app is installed as an APK instead of opened from a link.
- How it works:
  1. When a reminder is added, changed or removed, the app registers it with `AlarmManager` as an exact alarm for its next occurrence.
  2. At the set time Android wakes the app, which posts the notification and schedules the next day's occurrence.
  3. Android clears all alarms when the phone restarts, so the app re-registers every reminder on boot, and again when the time zone or clock changes.
- The app must handle three things:
  - **Notification permission**, requested the first time a reminder is turned on.
  - **Exact alarm permission**, required for on-the-minute delivery on recent Android versions.
  - **Battery optimisation.** Samsung and some other phones put unused apps to sleep, which drops alarms. The app must be excluded, and should detect this and point the user to the setting.

## D21: Kotlin and Jetpack Compose

**Date:** 2026-11-14 · **Status:** Active

**Decision.** The app is written in Kotlin with a Jetpack Compose UI. The alternative considered was a web-technology UI inside a native shell (Capacitor).

**Reason.** The reason for going native was reliable, noticeable reminders. Kotlin gives direct control over exact alarms, restart handling and notification behaviour, with no plugin layer in between. Capacitor's main benefit, keeping a web or iOS version possible, is not needed.

**Effect.** One Android codebase in one language. All screens are built from scratch in Compose to match the reference screenshots.

## D22: Reminders stay until dismissed

**Date:** 2026-11-14 · **Status:** Active

**Decision.** When a reminder fires, its notification stays on the phone until the user dismisses it or opens it. It does not time out and the app does not clear it. Tapping it opens the app at the item it belongs to.

The options considered were a normal notification, one that stays until dismissed, and a full-screen ringing alarm.

**Reason.** The owner's concern was reminders going unnoticed.

**Effect.** The notification is posted with no timeout. It sounds once when it fires and does not keep ringing; continuous ringing would be the full-screen alarm option, which was not chosen.

## D23: Room database, Gradle builds, direct APK install

**Date:** 2026-11-14 · **Status:** Active

**Decision.** Data is stored in a Room database (SQLite) behind a repository layer. The app is built with Gradle from the command line; Android Studio is optional. It is installed directly on the phone over USB, with no Play Store listing.

**Reason.** Proposed during planning as the standard choices for a local Android app. Room fits the relational data here (habits, entries, categories, reminders). The build machine already has JDK 17 and the Android SDK. Became Active when the owner asked for the project to be scaffolded on this basis.

**Effect.** Sets the storage and build tooling. Build prerequisites are JDK 17, the Android SDK with `ANDROID_HOME` set, and a phone with USB debugging enabled.

## D24: Git is managed by the owner only

**Date:** 2026-11-14 · **Status:** Active

**Decision.** The coding assistant only changes files in the working tree. It never commits, and never stages, branches, pushes or otherwise changes the repository's git state. The owner handles all git operations by hand.

**Reason.** The owner wants to manage git themselves.

**Effect.** Changes from the assistant arrive as uncommitted edits for the owner to review, stage and commit. Commit history reflects the owner's own grouping of changes.

## D25: Project setup

**Date:** 2026-11-14 · **Status:** Proposed

**Decision.** Choices made while creating the project skeleton:

| Choice | Value |
| --- | --- |
| Package name and app ID | `com.ticktracker` |
| Minimum Android version | Android 8.0 (API 26) |
| Target SDK / compile SDK | 36 / 37 |
| Modules | One, `app` |
| Sharing objects between classes | A hand-written container (`AppContainer`), no dependency injection library |
| Navigation | Navigation Compose with one activity |
| Room version | Room 2.8, not the newer Room 3 |
| Library versions | Latest stable releases on 2026-11-14, listed in `gradle/libs.versions.toml` |
| Reminder storage | Reminders link to habits only, until Plan and Tasks are designed |
| Database migrations | Not set up yet. Must be added before real data is stored, or an update could wipe it |

**Reason.** Made by the assistant during scaffolding, to get a building project. API 26 lets the app use the standard date and time classes with no extra library, and covers any phone from 2017 onward. One module and a hand-written container keep a single-person app simple. Room 2.8 was chosen over Room 3 as the long-established version. Compile SDK 37 is required by the current libraries. Not yet confirmed by the owner.

**Effect.** Fixes the project layout described in [file_guide.md](file_guide.md). The app ID is hard to change once the app is installed with data, because Android treats a new ID as a different app. Linking reminders only to habits means the reminders table will change when Plan and Tasks arrive.

## D26: Tracking behaviours confirmed

**Date:** 2026-11-15 · **Status:** Active

**Decision.** The behaviours that had been assumed while writing the flow are confirmed:

1. **Undo a tick.** Tapping a completed tick again undoes it, on the same day. The same applies to skip, resisted and slipped.
2. **Multiple per day.** Each tap of the tick button adds one, up to the target. Tapping again when full clears the day.
3. **Measurable streaks.** A day counts toward the streak only when the entry reaches the target.
4. **Weekly measurable target.** With a Weekly goal, the typed target is for the whole week, and the week counts when its Total reaches it.
5. **Skip.** Available only on weekly-goal habits. A skipped day adds nothing to the week's count and does not break anything.
6. **Streak includes today.** The streak counts today (or this week) as soon as it is met. Until then it still shows the run up to yesterday (or last week); an unfinished day has not broken the streak.
7. **Reminders always fire.** A reminder is not skipped when the habit is already done for the day.
8. **Quit habits have no repeat goal.** A Quit habit is always judged once a day, so the wizard does not ask for one.

**Reason.** The owner reviewed the assumptions listed in flow_state.md and confirmed them as correct.

**Effect.** These rules are implemented in `domain/HabitProgress.kt` and `ui/habits/HabitsViewModel.kt` and covered by unit tests. Because reminders always fire, the reminder code never needs to look at the day's entries.

## D27: A habit can be deleted from its card

**Date:** 2026-11-15 · **Status:** Proposed

**Decision.** The overflow menu on every habit card has "Delete habit". After a confirmation, the habit is removed together with its history and its reminders. There is no undo and no way to edit a habit yet.

**Reason.** Added by the assistant while building the cards, so that habits created while testing can be removed. Not yet confirmed by the owner. What can be edited after creation is still open.

**Effect.** Deleting is permanent: the entries are removed from the database, and the reminders are cancelled with Android.

---

## Open items

Not yet decided. Each becomes a numbered decision when settled.

- Add flows for Plan blocks and Tasks, including how their reminders are set.
- Data export and backup, since data is device-only (D02).
- Light theme, or dark only.
- Editing a habit after it is created: what can be changed, and what happens to its history.
