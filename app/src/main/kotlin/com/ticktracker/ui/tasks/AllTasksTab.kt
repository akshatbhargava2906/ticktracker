package com.ticktracker.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.TextMuted
import java.time.LocalDate

/** Groups are shown highest priority first, as in the reference app. */
private val PRIORITY_ORDER = listOf(
    TaskPriority.HIGH,
    TaskPriority.MEDIUM,
    TaskPriority.LOW,
    TaskPriority.NONE,
)

/**
 * The All Tasks tab (D01, D29): one list at a time, its open tasks grouped by priority,
 * then "New Task" and the completed tasks. The row at the top switches between lists.
 */
@Composable
fun AllTasksTab(
    items: List<TaskItem>,
    lists: List<TaskListEntity>,
    selectedListId: Long?,
    today: LocalDate,
    onSelectList: (Long) -> Unit,
    onAddList: () -> Unit,
    onRenameList: (TaskListEntity) -> Unit,
    onDeleteList: (TaskListEntity) -> Unit,
    onToggle: (TaskItem) -> Unit,
    onOpen: (TaskItem) -> Unit,
    onAdd: () -> Unit,
) {
    val inList = items.filter { it.task.listId == selectedListId }
    val open = inList.filter { !it.task.completed }
    val done = inList.filter { it.task.completed }.sortedByDescending { it.task.completedAt }

    // Priority groups are open by default; the completed list is closed.
    val groupClosed = remember { mutableStateMapOf<TaskPriority, Boolean>() }
    var completedOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ListSwitcher(
            lists = lists,
            selectedListId = selectedListId,
            onSelect = onSelectList,
            onAdd = onAddList,
            onRename = onRenameList,
            onDelete = onDeleteList,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            PRIORITY_ORDER.forEach { priority ->
                // Earliest date first; tasks with no date go last.
                val group = open
                    .filter { it.task.priority == priority }
                    .sortedBy { it.task.date ?: LocalDate.MAX }
                if (group.isEmpty()) return@forEach
                val expanded = groupClosed[priority] != true

                item(key = "header-$priority") {
                    CollapsibleHeader(
                        label = "⚑  ${priority.label}",
                        count = group.size,
                        expanded = expanded,
                        onToggle = { groupClosed[priority] = expanded },
                        labelColor = priorityColor(priority),
                    )
                }
                if (expanded) {
                    items(group, key = { it.task.id }) { item ->
                        TaskRow(item, today, showDate = true, onToggle = { onToggle(item) }, onOpen = { onOpen(item) })
                    }
                }
            }

            item(key = "new-task") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAdd)
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "New Task",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }

            if (done.isNotEmpty()) {
                item(key = "completed-header") {
                    CollapsibleHeader("Completed", done.size, completedOpen, { completedOpen = !completedOpen })
                }
                if (completedOpen) {
                    items(done, key = { "done-${it.task.id}" }) { item ->
                        TaskRow(item, today, showDate = true, onToggle = { onToggle(item) }, onOpen = { onOpen(item) })
                    }
                }
            }
        }
    }
}

/** The row of list names. The selected one is green and underlined; + adds a list. */
@Composable
private fun ListSwitcher(
    lists: List<TaskListEntity>,
    selectedListId: Long?,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    onRename: (TaskListEntity) -> Unit,
    onDelete: (TaskListEntity) -> Unit,
) {
    val green = MaterialTheme.colorScheme.primary
    val selected = lists.firstOrNull { it.id == selectedListId }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            lists.forEach { list ->
                val isSelected = list.id == selectedListId
                Column(
                    modifier = Modifier
                        .clickable { onSelect(list.id) }
                        .padding(end = 20.dp)
                        .width(androidx.compose.foundation.layout.IntrinsicSize.Max),
                ) {
                    Text(
                        text = list.name,
                        color = if (isSelected) green else TextMuted,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) green else Color.Transparent),
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, CardOutline),
                onClick = onAdd,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "New list",
                    tint = TextMuted,
                    modifier = Modifier
                        .padding(5.dp)
                        .size(18.dp),
                )
            }
        }

        // Rename or delete the selected list.
        if (selected != null) {
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "List options", tint = TextMuted)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename list") },
                        onClick = {
                            menuOpen = false
                            onRename(selected)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete list") },
                        // There must always be one list to put tasks in.
                        enabled = lists.size > 1,
                        onClick = {
                            menuOpen = false
                            onDelete(selected)
                        },
                    )
                }
            }
        }
    }
}
