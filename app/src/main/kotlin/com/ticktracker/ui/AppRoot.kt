package com.ticktracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ticktracker.ui.habits.add.AddHabitScreen
import com.ticktracker.ui.habits.add.AddHabitViewModel
import com.ticktracker.ui.settings.SettingsScreen
import com.ticktracker.ui.habits.HabitsScreen
import com.ticktracker.ui.plan.PlanScreen
import com.ticktracker.ui.tasks.TasksScreen
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface
import com.ticktracker.util.headerDate
import com.ticktracker.util.today
import com.ticktracker.util.todayFlow

/** The three sections reachable from the bottom navigation (D01). */
enum class Section(val route: String, val label: String, val icon: ImageVector) {
    HABITS("habits", "Habits", Icons.Filled.CheckCircle),
    PLAN("plan", "Plan", Icons.Filled.DateRange),
    TASKS("tasks", "Tasks", Icons.AutoMirrored.Filled.List),
}

/** Something to show straight away, set when the app was opened by tapping a reminder. */
sealed interface OpenTarget {
    data class Habit(val id: Long) : OpenTarget
    data class Task(val id: Long) : OpenTarget
    data class Block(val id: Long) : OpenTarget
}

/** Routes that are not sections. They are shown full screen, without the shell. */
object Routes {
    const val ADD_HABIT = "add-habit"
    const val EDIT_HABIT = "edit-habit/{${AddHabitViewModel.ARG_HABIT_ID}}"
    const val SETTINGS = "settings"

    fun editHabit(id: Long) = "edit-habit/$id"
}

/** The app shell: header, the current screen, and the floating bottom bar. */
@Composable
fun AppRoot(
    openTarget: OpenTarget? = null,
    onTargetOpened: () -> Unit = {},
) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val section = Section.entries.firstOrNull { it.route == currentRoute }

    // How many times + was tapped on the Tasks section. The Tasks screen opens a new task for each.
    var addTaskRequests by rememberSaveable { mutableIntStateOf(0) }
    var addPlanRequests by rememberSaveable { mutableIntStateOf(0) }

    // Opened from a reminder: leave whatever screen is showing and go to the section the
    // habit or task lives in. That screen then brings it into view.
    LaunchedEffect(openTarget) {
        val destination = when (openTarget) {
            is OpenTarget.Habit -> Section.HABITS
            is OpenTarget.Task -> Section.TASKS
            is OpenTarget.Block -> Section.PLAN
            null -> return@LaunchedEffect
        }
        nav.navigate(destination.route) {
            popUpTo(nav.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = { if (section != null) AppHeader(onSettings = { nav.navigate(Routes.SETTINGS) }) },
        bottomBar = {
            if (section != null) {
                BottomBar(
                    current = section,
                    onSelect = { nav.switchSection(it) },
                    onAdd = {
                        when (section) {
                            Section.HABITS -> nav.navigate(Routes.ADD_HABIT)
                            Section.PLAN -> addPlanRequests++
                            Section.TASKS -> addTaskRequests++
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Section.HABITS.route,
            // Marking the padding as consumed stops screens from adding the system bar space twice.
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable(Section.HABITS.route) {
                HabitsScreen(
                    focusHabitId = (openTarget as? OpenTarget.Habit)?.id,
                    onFocusHandled = onTargetOpened,
                    onEditHabit = { nav.navigate(Routes.editHabit(it)) },
                )
            }
            composable(Section.PLAN.route) {
                PlanScreen(
                    addRequests = addPlanRequests,
                    openBlockId = (openTarget as? OpenTarget.Block)?.id,
                    onBlockOpened = onTargetOpened,
                )
            }
            composable(Section.TASKS.route) {
                TasksScreen(
                    addRequests = addTaskRequests,
                    openTaskId = (openTarget as? OpenTarget.Task)?.id,
                    onTaskOpened = onTargetOpened,
                )
            }
            composable(Routes.ADD_HABIT) { AddHabitScreen(onClose = { nav.popBackStack() }) }
            composable(
                route = Routes.EDIT_HABIT,
                arguments = listOf(navArgument(AddHabitViewModel.ARG_HABIT_ID) { type = NavType.LongType }),
            ) {
                AddHabitScreen(onClose = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

private fun NavHostController.switchSection(section: Section) {
    navigate(section.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppHeader(onSettings: () -> Unit) {
    // Follows the clock, so the date changes at midnight while the app is open.
    val date by remember { todayFlow() }.collectAsStateWithLifecycle(initialValue = today())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = headerDate(date),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun BottomBar(
    current: Section,
    onSelect: (Section) -> Unit,
    onAdd: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape,
            color = CardSurface,
            border = BorderStroke(1.dp, CardOutline),
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Section.entries.forEach { section ->
                    SectionButton(
                        section = section,
                        selected = section == current,
                        onClick = { onSelect(section) },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.CenterEnd),
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

/** One item in the bottom bar. The selected section shows its label in a green pill. */
@Composable
private fun SectionButton(section: Section, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primary else CardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = section.icon,
            contentDescription = section.label,
            modifier = Modifier.size(20.dp),
            tint = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        )
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = section.label,
                color = colors.onPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
