package com.ticktracker.ui.habits.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface

/** The segmented bar at the top of the wizard: one segment per step, filled up to the current one. */
@Composable
fun StepProgress(step: Int, stepCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(stepCount) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (index <= step) MaterialTheme.colorScheme.primary else CardOutline),
            )
        }
    }
}

@Composable
fun StepTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp, bottom = 20.dp),
    )
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp),
    )
}

/** A rounded pill that is either selected (green outline and text) or not. */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) colors.primary else CardOutline),
        onClick = onClick,
    ) {
        Text(
            text = label,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

/** A count changed only with − and +. There is no text field (D08). */
@Composable
fun CountStepper(
    count: Int,
    suffix: String,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        StepperButton("−", enabled = count > range.first) { onChange(count - 1) }
        Text(
            text = "$count / $suffix",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        StepperButton("+", enabled = count < range.last) { onChange(count + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, if (enabled) colors.primary else CardOutline),
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = 26.sp,
                color = if (enabled) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * A vertical scrolling wheel that snaps to one value, as used in the reminder time picker.
 * Shows three rows; the middle one is the selected value.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 52.dp,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val rowHeightPx = with(LocalDensity.current) { rowHeight.toPx() }

    // The list is padded by one row above and below, so the first "visible" item is the middle row.
    val selected by remember {
        derivedStateOf {
            val pastHalf = listState.firstVisibleItemScrollOffset > rowHeightPx / 2
            (listState.firstVisibleItemIndex + if (pastHalf) 1 else 0).coerceIn(items.indices)
        }
    }
    LaunchedEffect(selected) { onSelected(selected) }

    Box(modifier = modifier.height(rowHeight * 3), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
        )
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(vertical = rowHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items.size) { index ->
                val isSelected = index == selected
                Box(Modifier.height(rowHeight).width(72.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = items[index],
                        fontSize = if (isSelected) 28.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}
