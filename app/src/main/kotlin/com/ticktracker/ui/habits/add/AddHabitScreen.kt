package com.ticktracker.ui.habits.add

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The four-step add habit wizard (D06, flow_state.md section 2). The same screen edits an
 * existing habit, starting at the name step (D31).
 * This file is the frame: back / close, progress bar, the current step and the bottom button.
 * The steps themselves are in AddHabitSteps.kt.
 */
@Composable
fun AddHabitScreen(
    onClose: () -> Unit,
    viewModel: AddHabitViewModel = viewModel(factory = AddHabitViewModel.Factory),
) {
    val state = viewModel.state
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // System back goes to the previous step; on the first step it leaves the wizard.
    BackHandler(enabled = !state.isFirstStep) { viewModel.back() }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        IconButton(
            onClick = { if (state.isFirstStep) onClose() else viewModel.back() },
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
        ) {
            if (state.isFirstStep) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        StepProgress(
            step = state.step,
            stepCount = AddHabitState.STEP_COUNT,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (!viewModel.loaded) return@Column
            when (state.step) {
                0 -> KindStep(state, viewModel)
                1 -> NameStep(state, viewModel)
                2 -> TrackingStep(state, categories, viewModel)
                else -> ReminderStep(state, viewModel)
            }
        }

        Button(
            onClick = { if (state.isLastStep) viewModel.save(onClose) else viewModel.next() },
            enabled = viewModel.loaded && state.canContinue && !state.saving,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(58.dp),
        ) {
            Text(
                text = when {
                    !state.isLastStep -> "Continue"
                    state.isEditing -> "Save Changes"
                    else -> "Save Habit"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
