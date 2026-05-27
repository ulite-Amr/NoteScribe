package com.uliteamr.notescribe.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uliteamr.notescribe.presentation.icons.FavoriteOutline
import com.uliteamr.notescribe.presentation.icons.PushPin
import com.uliteamr.notescribe.presentation.theme.NoteScribeTheme
import com.uliteamr.notescribe.presentation.utils.formatNoteDate

/** UI representation of a note's status flags. */
data class NoteStatus(
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
)

/**
 * Rounded avatar showing either a custom icon or the first letter of the note title.
 *
 * @param letter First character to display when [icon] is null.
 * @param icon Optional drawable resource ID; shown instead of [letter].
 * @param size Width and height of the avatar box.
 */
@Composable
fun NoteIcon(
    letter: String,
    icon: Int? = null,
    size: Dp = 40.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.6f),
            )
        } else {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** Small animated row of status icons (pin / favorite) for a note. */
@Composable
fun NoteStatusIcons(status: NoteStatus) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        AnimatedVisibility(
            visible = status.isPinned,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            Icon(
                imageVector = PushPin,
                contentDescription = "Pinned",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(14.dp)
                    .semantics { contentDescription = "Pinned note" },
            )
        }

        AnimatedVisibility(
            visible = status.isFavorite,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
        ) {
            Icon(
                imageVector = FavoriteOutline,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(14.dp)
                    .semantics { contentDescription = "Favorite note" },
            )
        }
    }
}

/** Row of non-interactive tag labels. */
@Composable
fun NoteTags(tags: List<String>) {
    if (tags.isEmpty()) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * A single note card displaying icon, title, status, tags, and formatted timestamp.
 *
 * @param letter First character shown in the avatar circle.
 * @param title Note headline (single line, ellipsized).
 * @param timestamp Unix-millis timestamp displayed as formatted date.
 * @param status [NoteStatus] controlling pin/favorite icons.
 * @param tags Optional tag labels shown between title and timestamp.
 * @param modifier Modifier for the root card.
 * @param icon Optional drawable resource ID replacing [letter].
 * @param onClick Called when the card is tapped. **Required** — screens must handle navigation.
 */
@Composable
fun NoteItem(
    letter: String,
    title: String,
    timestamp: Long,
    modifier: Modifier = Modifier,
    status: NoteStatus = NoteStatus(),
    tags: List<String> = emptyList(),
    icon: Int? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoteIcon(letter = letter, icon = icon)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    NoteStatusIcons(status = status)
                }

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    NoteTags(tags = tags)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatNoteDate(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteItemPreview() {
    NoteScribeTheme(true) {
        NoteItem(
            letter = "N",
            title = "Meeting Notes",
            timestamp = System.currentTimeMillis(),
            tags = listOf("Work", "Project"),
            status = NoteStatus(isPinned = true, isFavorite = true),
            onClick = {},
        )
    }
}
