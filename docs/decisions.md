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
| [D04](#d04-locked-features-of-the-reference-app-are-out-of-scope) | 2026-11-14 | Locked features of the reference app are out of scope | Active, amended by D29 |
| [D05](#d05-habit-model-kind-direction-repeat-goal) | 2026-11-14 | Habit model: kind, direction, repeat goal | Active |
| [D06](#d06-four-step-add-habit-wizard) | 2026-11-14 | Four-step add habit wizard | Active |
| [D07](#d07-measurable-habits-unit-daily-target-goal) | 2026-11-14 | Measurable habits: unit, daily target, goal | Active |
| [D08](#d08-counts-are-set-with-a-stepper-only) | 2026-11-14 | Counts are set with a stepper only | Active |
| [D09](#d09-a-habit-can-have-several-categories) | 2026-11-14 | A habit can have several categories | Active |
| [D10](#d10-reminders-in-all-three-sections) | 2026-11-14 | Reminders in all three sections | Active |
| [D11](#d11-personal-app-nothing-locked) | 2026-11-14 | Personal app, nothing locked | Active |
| [D12](#d12-tick-habits-are-tapped-measurable-habits-are-typed) | 2026-11-14 | Tick habits are tapped, measurable habits are typed | Active |
| [D13](#d13-reminders-repeat-daily-weekly-habits-streak-by-week) | 2026-11-14 | Reminders repeat daily; weekly habits streak by week | Active |
| [D14](#d14-reminder-times-are-chosen-freely) | 2026-11-14 | Reminder times are chosen freely | Active for habits; tasks differ, see D29 |
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
| [D25](#d25-project-setup) | 2026-11-14 | Project setup: package, SDK levels, structure | Active |
| [D26](#d26-tracking-behaviours-confirmed) | 2026-11-15 | Tracking behaviours confirmed | Active |
| [D27](#d27-a-habit-can-be-deleted-from-its-card) | 2026-11-15 | A habit can be deleted from its card | Active |
| [D28](#d28-database-changes-never-delete-data) | 2026-11-15 | Database changes never delete data | Active |
| [D29](#d29-tasks-the-full-task-sheet) | 2026-11-15 | Tasks: the full task sheet | Active |
| [D30](#d30-what-the-plan-timeline-shows) | 2026-11-15 | What the Plan timeline shows | Active, amended by D35 |
| [D31](#d31-editing-a-habit) | 2026-11-17 | Editing a habit | Active |
| [D32](#d32-plan-details) | 2026-11-17 | Plan details: habit blocks, block reminders, overlaps | Active, point 1 superseded by D35 |
| [D33](#d33-backup-to-a-file) | 2026-11-17 | Backup to a file | Active |
| [D34](#d34-dark-theme-only) | 2026-11-17 | Dark theme only | Active |
| [D35](#d35-the-plan-is-for-day-planning-only) | 2026-11-17 | The Plan is for day planning only | Active |
| [D36](#d36-tasks-planned-on-the-plan-are-real-tasks) | 2026-11-17 | Tasks planned on the Plan are real tasks | Active |

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

**Date:** 2026-11-14 · **Status:** Active (confirmed 2026-11-17)

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
| Database migrations | Not set up at this point. Set up on 2026-11-15, see D28 |

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

**Date:** 2026-11-15 · **Status:** Active (confirmed 2026-11-17)

**Decision.** The overflow menu on every habit card has "Delete habit". After a confirmation, the habit is removed together with its history and its reminders. There is no undo. Editing is D31.

**Reason.** Added by the assistant while building the cards, so that habits created while testing can be removed. Confirmed by the owner on 2026-11-17.

**Effect.** Deleting is permanent: the entries are removed from the database, and the reminders are cancelled with Android.

## D28: Database changes never delete data

**Date:** 2026-11-15 · **Status:** Active

**Decision.** When the structure of the database changes in a new version of the app, the data already on the phone is carried over by a migration. The app never uses Room's "destructive" fallback, which would empty the database instead.

- The structure of every database version is saved as a file in `app/schemas/` and committed.
- Every structure change raises the database version and comes with a migration: an automatic one for simple changes (a new table, a new column with a default), a hand-written one for anything else.
- The steps are written at the top of `data/db/Migrations.kt`.

The database is at version 1, the structure the app has had since the skeleton.

**Reason.** The phone holds the only copy of the data (D02). The owner agreed to set this up before logging habits for real. Plan and Tasks will add tables, so structure changes are certain.

**Effect.** Installing a newer build over the app keeps all habits, history and reminders. If a structure change is ever made without a migration, the app fails to open the database and stops, loudly, instead of quietly losing data; the fix is to add the missing migration. Uninstalling the app still deletes everything, which is why export and backup remains an open item.

## D29: Tasks: the full task sheet

**Date:** 2026-11-15 · **Status:** Active · **Amends:** D04, D10, D14

**Decision.** A task is created and edited in a bottom sheet with everything the reference app's task sheet has, including the parts it locks or that were earlier out of scope.

| Field | Behaviour |
| --- | --- |
| Title | Typed at the top, beside a circle that ticks the task off. |
| Date | Any day, or none. A task with no date appears only under All Tasks. Clearing the date also clears the time, reminder and repeat. |
| Time | Optional time of day. Setting a time on a task with no date gives it today's date. |
| Reminder | Needs a time first. Choose how early it fires: at the time of the task, or 5, 10, 15, 30 minutes or 1 hour before. It fires once. |
| Repeat | Never, Daily, Weekly or Monthly. |
| List | Which list the task belongs to. |
| Priority | None, Low, Medium or High. |
| Sub-tasks | A checklist inside the task. Each item is ticked off separately. |

Rules:

- **No save button.** Closing the sheet keeps the changes. A task with no title is not saved. Delete removes the task.
- **Repeating tasks.** Ticking off a repeating task creates its next occurrence as a new task, with the sub-tasks unticked. The next date is counted from the task's own date, so a weekly task stays on its weekday. If the task was overdue, dates already in the past are skipped. The completed one stays on its day as history. Unticking it and ticking it again does not create a second copy.
- **Lists.** There is always at least one list; the first is "My Tasks". Lists can be added, renamed and deleted. Deleting a list deletes its tasks. The Timeline shows tasks from every list; All Tasks shows one list at a time.
- **Task reminders** are not sent once the task is ticked off, and are not sent for a time that has already passed. Tapping one opens the task.
- **Tasks are not locked by day.** Unlike habit entries (D16), a task can be added to, edited on or ticked off for any day, past or future.
- **Search** filters both tabs by title.

**Reason.** The owner sent the reference app's task sheet and chose to build all of it, with reminders that fire a chosen time before the task. Repeat is locked as a paid feature in the reference app; here nothing is locked (D11).

**Effect.**

- **Amends D04:** multiple task lists are now in scope.
- **Amends D14:** a task reminder is tied to the task's time. Habit reminders are unchanged: any time of day, repeating daily.
- **Amends D10:** a task has at most one reminder, where a habit can have several.
- The database moves to version 2 with three new tables: task lists, tasks and sub-tasks. Existing habit data is carried over (D28).
- Task reminders use the same alarm scheduling, restart handling and stay-until-dismissed notifications as habits (D20, D22).
- A task stores a duration, unused in the Tasks section, so that it can be drawn as a block on the Plan timeline (D30).

## D30: What the Plan timeline shows

**Date:** 2026-11-15 · **Status:** Active, amended by [D35](#d35-the-plan-is-for-day-planning-only) (habits are no longer shown) and [D36](#d36-tasks-planned-on-the-plan-are-real-tasks) (a planned Task is a real task; "Task in my list" is gone)

**Decision.** The Plan timeline for a day shows three kinds of item:

1. **Blocks** added with "Plan this slot".
2. **Tasks that have a time** on that day. Ticking one on the timeline ticks it in Tasks too.
3. **Habits at their reminder times.** A habit with an 18:00 reminder appears at 6:00 PM, and can be ticked from there.

The day's start time and the "Wind down" time are settings, with defaults of 7:00 AM and 10:00 PM.

The "Plan this slot" sheet asks for a title, a date, a start time and a duration, and what the item is: a **Task** (tracked and ticked off on the Plan only), a **Note** (just a record of what was done, with nothing to tick), or a **Task in my list** (also added to the Tasks section).

**Reason.** Chosen by the owner from the reference app's Plan screen, where the habit "10k" appears as a block at its reminder time.

**Effect.** Plan is a view across all three sections, not a separate silo: it reads tasks and habits as well as its own blocks. A settings screen is needed for the day's start and wind-down times. Plan is not built yet.

## D31: Editing a habit

**Date:** 2026-11-17 · **Status:** Active

**Decision.** "Edit habit" in a card's overflow menu opens the add habit wizard filled in with the habit, starting at the name step, and ending with "Save Changes".

| Can be changed | Cannot be changed |
| --- | --- |
| Name, icon, colour | Tick or Measurable |
| Categories | Build or Quit |
| Reminders | Repeat goal (once a day, multiple per day, weekly) |
| Counts: times per day, times per week (stepper) | Unit |
| Target amount (typed) | Goal period (everyday or weekly) |

**Reason.** Proposed by the assistant and accepted by the owner. The fixed fields decide what an entry means: a count, a done day or an amount in a unit. Changing them would leave the existing history meaning something else. To change one, delete the habit and create a new one.

**Effect.**

- A new target or count applies to the whole history, not only from the day it changed. Past days are re-judged against it, so a streak can grow or shrink after an edit.
- Saving replaces the habit's categories and reminders with the ones in the wizard, and re-registers the reminders with Android.
- The wizard keeps the fixed fields as they were whatever is tapped, and says so on the tracking step.

## D32: Plan details

**Date:** 2026-11-17 · **Status:** Active, point 1 superseded by [D35](#d35-the-plan-is-for-day-planning-only) · **Builds on:** D30

**Decision.**

1. **Habit blocks.** A habit shows on the timeline as a 30-minute block at each of its reminder times, from the day it was created onward. It can be logged from there on today only (D16): Tick habits behave like their tick button, Quit habits are marked resisted, and Measurable habits open the amount dialog.
2. **Block reminders.** A planned block of kind Task has the same one-off reminder as a task: at the start, or 5, 10, 15, 30 minutes or 1 hour before. Notes have no reminder. Tapping the reminder opens the Plan on the block's day.
3. **Overlaps.** Items that overlap are listed one after another in time order. No gap is shown between them.
4. **Gaps.** Free time from the day's start up to wind down is shown as a gap, with "Plan this slot", when it is at least 15 minutes long. Items before the day's start or after wind down still appear, in time order.
5. **Now.** On today a "now" marker sits after anything already started and before the next thing still ahead.
6. **The sheet.** "Plan this slot" opens with the gap's start time and up to an hour's length; the + button opens it at the next five minutes on today, or at the day's start on other days. It is saved with the tick in its corner; closing it any other way discards it. A "Task in my list" is saved as a task in the first list, with its time and length, so it shows in Tasks and on the timeline.
7. **Tapping an item** opens it: a block in the Plan sheet, a task in the task sheet. Habits are only logged, not opened.

**Reason.** Proposed by the assistant and accepted by the owner. Points 1 to 3 were the open questions left by D30; the rest follow the reference screenshot.

**Effect.** The database moves to version 3 with a Plan blocks table, carried over automatically from version 2 (D28). The Plan reads tasks and habits as well as its own blocks, so ticking something there ticks it everywhere.

## D33: Backup to a file

**Date:** 2026-11-17 · **Status:** Active

**Decision.** Settings has "Export data" and "Import data".

- **Export** writes everything to one JSON file the user saves anywhere, such as Google Drive: habits, history, categories, reminders, tasks, lists, sub-tasks, Plan blocks and settings.
- **Import** replaces everything in the app with the contents of a file, after a warning that the current data will be lost.
- The file is read and checked in full before anything changes. A file that is not a TickTracker backup, is damaged, or comes from a newer version of the app is refused, and the app's data is left as it was.
- After an import, every reminder is cancelled and set again from the restored data.

**Reason.** The data lives only on the phone (D02), so uninstalling the app or losing the phone loses it. Proposed by the assistant and accepted by the owner.

**Effect.** Backups are manual; nothing is copied anywhere unless the user exports. The file layout has its own version number, so later versions of the app can keep reading old backups.

## D34: Dark theme only

**Date:** 2026-11-17 · **Status:** Active

**Decision.** The app has a dark theme only, with the green accent (D01). There is no light theme and no theme setting.

**Reason.** Chosen by the owner.

**Effect.** The colours stay defined once, in `ui/theme/Theme.kt`.

## D35: The Plan is for day planning only

**Date:** 2026-11-17 · **Status:** Active · **Amends:** D30 · **Supersedes:** D32 point 1

**Decision.** Habits no longer appear on the Plan timeline. The Plan shows only blocks made with "Plan this slot" and tasks that have a time on that day. Habits are tracked and logged in the Habits section alone.

**Reason.** The owner wants the Plan used solely for planning the day.

**Effect.** Habit reminders still fire as before; they just have no block on the timeline. The Plan no longer reads habit data at all. Tasks with a time still appear, since a timed task is part of planning the day.

## D36: Tasks planned on the Plan are real tasks

**Date:** 2026-11-17 · **Status:** Active · **Amends:** D30, D32

**Decision.** Anything planned as a **Task** on the Plan is saved as a task, so it appears in the Tasks section (Timeline and All Tasks) as well as on the Plan timeline, at its time. It is one task, not a copy: editing, ticking or deleting it in either place changes it in both. "What is this?" now offers two choices:

- **Task:** saved as a task on that day, with its time, length and reminder, in the chosen list (the list choice appears when there is more than one list).
- **Note:** just a record, such as "Scrolled Insta". Saved as a Plan block and shown on the Plan only, never in Tasks.

"Task in my list" is gone, since every planned task is now in a list.

**Reason.** The owner found that a task planned on the Plan did not appear in Tasks, while a task added in Tasks did appear on the Plan. With the Plan used for planning today and the days ahead, the two sections must show the same tasks: the Plan shows them in time order, Tasks shows them as lists.

**Effect.**

- Tasks planned before this change, which were stored as Plan blocks, are converted into real tasks in the first list when the app starts, keeping their day, time, length, reminder and done state. The same conversion runs after restoring an older backup.
- A note being edited can be switched to Task; it then becomes a task and the note is removed.
- Plan blocks are now only ever notes, so they never have a reminder. The database structure is unchanged.

---

## Open items

Not yet decided. Each becomes a numbered decision when settled.

None. Every question raised so far has been decided.
