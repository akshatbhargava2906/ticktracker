package com.ticktracker.ui.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.repository.TaskDraft
import java.time.LocalDate

enum class TasksTab(val label: String) {
    TIMELINE("Timeline"),
    ALL("All Tasks"),
}

/**
 * The Tasks section (D01, D29): the Timeline and All Tasks tabs, search, and the task sheet.
 *
 * [addRequests] goes up by one each time the + button in the bottom bar is tapped.
 * [openTaskId] is a task to open, set when the app was opened from its reminder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    addRequests: Int = 0,
    openTaskId: Long? = null,
    onTaskOpened: () -> Unit = {},
    viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(TasksTab.TIMELINE) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var chosenListId by rememberSaveable { mutableStateOf<Long?>(null) }

    // The task open in the sheet, and the list dialogs.
    var editing by remember { mutableStateOf<TaskDraft?>(null) }
    var addingList by remember { mutableStateOf(false) }
    var renamingList by remember { mutableStateOf<TaskListEntity?>(null) }
    var deletingList by remember { mutableStateOf<TaskListEntity?>(null) }

    // The list shown under All Tasks: the chosen one if it still exists, otherwise the first.
    val selectedListId = chosenListId?.takeIf { id -> ui.lists.any { it.id == id } } ?: ui.lists.firstOrNull()?.id

    fun newTask(date: LocalDate?) {
        val listId = selectedListId ?: return
        editing = TaskDraft(listId = listId, date = date)
    }

    // The + button in the bottom bar: a new task for today.
    var handledAdds by rememberSaveable { mutableIntStateOf(addRequests) }
    LaunchedEffect(addRequests, ui.loaded) {
        if (ui.loaded && addRequests > handledAdds) {
            handledAdds = addRequests
            newTask(ui.today)
        }
    }

    // Opened from a reminder: show that task once the tasks have loaded.
    LaunchedEffect(openTaskId, ui.loaded) {
        val id = openTaskId ?: return@LaunchedEffect
        if (!ui.loaded) return@LaunchedEffect
        ui.items.firstOrNull { it.task.id == id }?.let { editing = it.toDraft() }
        onTaskOpened()
    }

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryTabRow(
                selectedTabIndex = tab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.weight(1f),
            ) {
                TasksTab.entries.forEach { item ->
                    Tab(
                        selected = item == tab,
                        onClick = { tab = item },
                        text = { Text(item.label) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(
                onClick = {
                    searching = !searching
                    if (!searching) query = ""
                },
            ) {
                Icon(
                    imageVector = if (searching) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = if (searching) "Close search" else "Search tasks",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search tasks") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // Search narrows both tabs to tasks whose title contains the text.
        val items = if (query.isBlank()) {
            ui.items
        } else {
            ui.items.filter { it.task.title.contains(query.trim(), ignoreCase = true) }
        }

        when (tab) {
            TasksTab.TIMELINE -> TimelineTab(
                items = items,
                today = ui.today,
                onToggle = { viewModel.toggle(it.task) },
                onOpen = { editing = it.toDraft() },
                onAdd = { date -> newTask(date) },
            )

            TasksTab.ALL -> AllTasksTab(
                items = items,
                lists = ui.lists,
                selectedListId = selectedListId,
                today = ui.today,
                onSelectList = { chosenListId = it },
                onAddList = { addingList = true },
                onRenameList = { renamingList = it },
                onDeleteList = { deletingList = it },
                onToggle = { viewModel.toggle(it.task) },
                onOpen = { editing = it.toDraft() },
                onAdd = { newTask(ui.today) },
            )
        }
    }

    editing?.let { draft ->
        TaskSheet(
            initial = draft,
            lists = ui.lists,
            today = ui.today,
            onClose = { edited ->
                viewModel.save(edited)
                editing = null
            },
            onDelete = {
                draft.id?.let(viewModel::delete)
                editing = null
            },
        )
    }

    if (addingList) {
        TextInputDialog(
            title = "New list",
            initial = "",
            confirmLabel = "Add",
            onDismiss = { addingList = false },
            onConfirm = { name ->
                viewModel.addList(name) { id ->
                    chosenListId = id
                    tab = TasksTab.ALL
                }
                addingList = false
            },
        )
    }

    renamingList?.let { list ->
        TextInputDialog(
            title = "Rename list",
            initial = list.name,
            confirmLabel = "Rename",
            onDismiss = { renamingList = null },
            onConfirm = { name ->
                viewModel.renameList(list.id, name)
                renamingList = null
            },
        )
    }

    deletingList?.let { list ->
        val count = ui.items.count { it.task.listId == list.id }
        AlertDialog(
            onDismissRequest = { deletingList = null },
            title = { Text("Delete ${list.name}?") },
            text = {
                Text(
                    if (count == 0) {
                        "The list is empty."
                    } else {
                        "Its $count ${if (count == 1) "task" else "tasks"} will be deleted too. This cannot be undone."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteList(list.id)
                        deletingList = null
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingList = null }) { Text("Cancel") } },
        )
    }
}
