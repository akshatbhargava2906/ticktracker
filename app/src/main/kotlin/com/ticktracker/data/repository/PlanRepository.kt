package com.ticktracker.data.repository

import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.PlanDao
import com.ticktracker.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow

/** The only way screens read or change Plan blocks (D30). */
class PlanRepository(
    private val dao: PlanDao,
    private val scheduler: ReminderScheduler,
) {
    fun observeBlocks(): Flow<List<PlanBlockEntity>> = dao.observeAll()

    suspend fun getBlock(id: Long): PlanBlockEntity? = dao.getById(id)

    /** Creates a block when its id is 0, otherwise updates it. Sets or cancels its reminder. */
    suspend fun saveBlock(block: PlanBlockEntity) {
        val saved = if (block.id == 0L) {
            block.copy(id = dao.insert(block))
        } else {
            dao.update(block)
            block
        }
        scheduler.scheduleBlock(saved)
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val block = dao.getById(id) ?: return
        val updated = block.copy(completed = completed)
        dao.update(updated)
        scheduler.scheduleBlock(updated)
    }

    suspend fun delete(id: Long) {
        scheduler.cancelBlock(id)
        dao.deleteById(id)
    }
}
