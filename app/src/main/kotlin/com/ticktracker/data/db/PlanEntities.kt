package com.ticktracker.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** What a planned block is (D30). */
enum class BlockKind {
    /**
     * No longer created: a task planned on the Plan is stored as a real task (D36).
     * Kept so older data and backups can be read, and converted on start-up.
     */
    TASK,

    /** Just a record of what was done, such as "Scrolled Insta". Nothing to tick. */
    NOTE,
}

/** One block on the Plan timeline, made with "Plan this slot" (D30). Added in database version 3. */
@Entity(tableName = "plan_blocks", indices = [Index("date")])
data class PlanBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: LocalDate,
    /** Minutes after midnight. */
    val startMinute: Int,
    val durationMinutes: Int,
    val kind: BlockKind,
    val completed: Boolean = false,
    /** Minutes before the start that the reminder fires; 0 is at the start. Null is no reminder. */
    val reminderOffsetMinutes: Int? = null,
    /** Epoch milliseconds. */
    val createdAt: Long,
)

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_blocks ORDER BY date, startMinute")
    fun observeAll(): Flow<List<PlanBlockEntity>>

    @Query("SELECT * FROM plan_blocks WHERE id = :id")
    suspend fun getById(id: Long): PlanBlockEntity?

    /** Open task blocks with a reminder, for re-registering alarms. */
    @Query("SELECT * FROM plan_blocks WHERE completed = 0 AND kind = 'TASK' AND reminderOffsetMinutes IS NOT NULL")
    suspend fun getWithReminders(): List<PlanBlockEntity>

    /** Blocks stored as tasks before D36, waiting to be converted into real tasks. */
    @Query("SELECT * FROM plan_blocks WHERE kind = 'TASK'")
    suspend fun getTaskBlocks(): List<PlanBlockEntity>

    @Insert
    suspend fun insert(block: PlanBlockEntity): Long

    @Update
    suspend fun update(block: PlanBlockEntity)

    @Query("DELETE FROM plan_blocks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
