package com.ticktracker.data.model

/** D05: a habit is either ticked off or measured. Decides its tab and how it is logged. */
enum class HabitKind { TICK, MEASURABLE }

/** D05: Tick habits only. Build a routine, or quit one. */
enum class HabitDirection { BUILD, QUIT }

/** D05: Tick habits only. How often the habit is meant to be done. */
enum class RepeatGoal { ONCE_A_DAY, MULTIPLE_PER_DAY, WEEKLY_GOAL }

/** D07: Measurable habits only. Whether the target is judged per day or per week. */
enum class GoalPeriod { EVERYDAY, WEEKLY }

/**
 * What was recorded for a Tick habit on one day (flow_state.md section 4).
 * DONE and SKIPPED belong to Build habits, RESISTED and SLIPPED to Quit habits.
 */
enum class EntryState { DONE, SKIPPED, RESISTED, SLIPPED }
