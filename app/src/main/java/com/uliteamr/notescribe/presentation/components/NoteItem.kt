package com.uliteamr.notescribe.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.uliteamr.notescribe.presentation.icons.Favorite
import com.uliteamr.notescribe.presentation.icons.PushPin
import com.uliteamr.notescribe.presentation.theme.NoteScribeTheme

data class NoteCardData(
    val id: String,
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
        border = BorderStroke(
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
            NoteCardHeader(
                category = data.category,
                categoryColor = resolvedCategoryColor,
                isFavorite = data.isFavorite,
                isPinned = data.isPinned,
            )

            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            if (data.todoTotal > 0) {
                NoteCardProgress(
                    completed = data.todoCompleted,
                    total = data.todoTotal,
                )
            }

            Text(
                text = data.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = data.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

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
        CategoryLabel(name = category, color = categoryColor)

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

@Composable
private fun CategoryLabel(
    name: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
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

@Composable
private fun NoteCardProgress(
    completed: Int,
    total: Int,
) {
    val progress = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

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
            strokeCap = StrokeCap.Round,
        )

        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun NoteCardPreview() {
    NoteScribeTheme {
        Column {
            NoteCard(
                data = NoteCardData(
                    id = "1",
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
                    id = "2",
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
private fun NoteCardDarkPreview() {
    NoteScribeTheme {
        Column {
            NoteCard(
                data = NoteCardData(
                    id = "1",
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
                    id = "2",
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
