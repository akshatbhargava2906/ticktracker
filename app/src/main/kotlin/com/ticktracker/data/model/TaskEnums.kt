package com.ticktracker.data.model

/** How important a task is. All Tasks groups by this, highest first (D29). */
enum class TaskPriority(val label: String) {
    NONE("No Priority"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
}

/** How often a task comes back after it is ticked off (D29). */
enum class TaskRepeat(val label: String) {
    NEVER("Never"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
}
