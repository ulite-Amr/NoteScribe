package com.example.notescribe.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uliteamr.notescribe.presentation.icons.Favorite
import com.uliteamr.notescribe.presentation.icons.PushPin
import com.uliteamr.notescribe.presentation.theme.NoteScribeTheme

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

/**
 * Immutable data holder for a single note card.
 *
 * @property id            Unique identifier used as a stable key in lazy lists.
 * @property title         Primary headline shown in the card.
 * @property content       Short preview of the note body (truncated to one line in the UI).
 * @property category      Label displayed above the title (e.g. "Development").
 * @property categoryColor Tint applied to the category indicator bar and label text.
 *                         Defaults to [Color.Unspecified], which lets the composable fall back
 *                         to [MaterialTheme.colorScheme.primary].
 * @property date          Pre-formatted date/time string shown at the bottom-end of the card
 *                         (e.g. "JAN 26 • 04:30 AM").
 * @property isFavorite    Whether the favourite icon is visible and active.
 * @property isPinned      Whether the pin icon is visible and active.
 * @property todoCompleted Number of completed to-do items in this note.
 * @property todoTotal     Total number of to-do items in this note.
 *                         When both values are zero the progress section is hidden entirely.
 */
data class NoteCardData(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val categoryColor: Color = Color.Unspecified,
    val date: String,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val todoCompleted: Int = 0,
    val todoTotal: Int = 0,
)

// ---------------------------------------------------------------------------
// Main card composable
// ---------------------------------------------------------------------------

/**
 * A Material 3 card that summarises a single note.
 *
 * The card is composed of three distinct sections stacked vertically:
 * 1. **Header row** – category indicator + label on the start, action icons on the end.
 * 2. **Body** – title, optional to-do progress row, and a one-line content preview.
 * 3. **Footer** – formatted date anchored to the end edge.
 *
 * All data is driven through [NoteCardData], making this composable stateless and
 * easy to use inside a `LazyColumn`.
 *
 * @param data     The note data to render.
 * @param onClick  Invoked when the user taps anywhere on the card.
 * @param modifier Optional [Modifier] forwarded to the outer [Card].
 */
@Composable
fun NoteCard(
    data: NoteCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedCategoryColor = if (data.categoryColor != Color.Unspecified) {
        data.categoryColor
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Row 1: category label + action icons ────────────────────────
            NoteCardHeader(
                category = data.category,
                categoryColor = resolvedCategoryColor,
                isFavorite = data.isFavorite,
                isPinned = data.isPinned,
            )

            // ── Row 2: title ────────────────────────────────────────────────
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Row 3: to-do progress (hidden when no tasks exist) ──────────
            if (data.todoTotal > 0) {
                NoteCardProgress(
                    completed = data.todoCompleted,
                    total = data.todoTotal,
                )
            }

            // ── Row 4: content preview ──────────────────────────────────────
            Text(
                text = data.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Row 5: date stamp (end-aligned) ─────────────────────────────
            Text(
                text = data.date,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables (extracted to avoid code duplication and aid reuse)
// ---------------------------------------------------------------------------

/**
 * Top header row of the card containing:
 * - A coloured category indicator bar followed by the category name.
 * - Optional favourite and pin icons on the trailing edge.
 *
 * Extracted as its own composable so it can be reused in other card variants
 * (e.g. a compact list tile) without duplicating layout logic.
 *
 * @param category      Display name of the note's category.
 * @param categoryColor Resolved colour applied to the indicator bar and label.
 * @param isFavorite    Shows the favourite icon when `true`.
 * @param isPinned      Shows the pin icon when `true`.
 */
@Composable
private fun NoteCardHeader(
    category: String,
    categoryColor: Color,
    isFavorite: Boolean,
    isPinned: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category indicator + label
        CategoryLabel(name = category, color = categoryColor)

        // Action icons (only rendered when active to keep the layout clean)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isFavorite) {
                NoteActionIcon(
                    icon = Favorite,
                    contentDescription = "Favourite",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (isPinned) {
                NoteActionIcon(
                    icon = PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A small coloured vertical bar paired with an all-caps category name label.
 *
 * Separated from [NoteCardHeader] so it can be reused in search results,
 * filter chips, or any other surface that needs to display a category.
 *
 * @param name  Category display name (rendered in UPPERCASE automatically).
 * @param color Colour applied to both the bar and the text.
 */
@Composable
private fun CategoryLabel(
    name: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Thin coloured indicator bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp)),
        )

        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/**
 * A single 18 × 18 dp icon used for note actions (favourite, pin, etc.).
 *
 * Extracted so that every action icon shares identical size and padding
 * without repeating [Modifier] boilerplate at each call site.
 *
 * @param iconResId          Drawable resource identifier for the icon.
 * @param contentDescription Accessibility label forwarded to [Icon].
 * @param tint               Colour applied to the icon.
 */
@Composable
private fun NoteActionIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
) {
    Icon(
       imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(18.dp),
    )
}

/**
 * A horizontal progress row showing how many to-do items have been completed.
 *
 * Composed of a slim [LinearProgressIndicator] followed by a "completed/total"
 * counter label. Only rendered when [total] > 0 (enforced by the caller).
 *
 * @param completed Number of finished tasks.
 * @param total     Total number of tasks. Must be greater than zero.
 */
@Composable
private fun NoteCardProgress(
    completed: Int,
    total: Int,
) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(60.dp)
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )

        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun NoteCardPreview() {
    NoteScribeTheme {
        Column {
            NoteCard(
                data = NoteCardData(
                    id = 1L,
                    title = "Note Title Sample",
                    content = "This is where the brief content goes...",
                    category = "Development",
                    date = "JAN 26 • 04:30 AM",
                    isFavorite = true,
                    isPinned = true,
                    todoCompleted = 3,
                    todoTotal = 5,
                ),
                onClick = {},
            )
            NoteCard(
                data = NoteCardData(
                    id = 2L,
                    title = "Meeting Notes",
                    content = "Discussed Q1 roadmap and assigned tasks to the team.",
                    category = "Work",
                    date = "FEB 01 • 09:00 AM",
                    isFavorite = false,
                    isPinned = false,
                ),
                onClick = {},
            )
        }
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
private fun NoteCardPreviewd() {
    NoteScribeTheme {
        Column {
            NoteCard(
                data = NoteCardData(
                    id = 1L,
                    title = "Note Title Sample",
                    content = "This is where the brief content goes...",
                    category = "Development",
                    date = "JAN 26 • 04:30 AM",
                    isFavorite = true,
                    isPinned = true,
                    todoCompleted = 3,
                    todoTotal = 5,
                ),
                onClick = {},
            )
            NoteCard(
                data = NoteCardData(
                    id = 2L,
                    title = "Meeting Notes",
                    content = "Discussed Q1 roadmap and assigned tasks to the team.",
                    category = "Work",
                    date = "FEB 01 • 09:00 AM",
                    isFavorite = false,
                    isPinned = false,
                ),
                onClick = {},
            )
        }
    }
}