package com.uliteamr.notescribe.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uliteamr.notescribe.presentation.icons.MoreVert

/**
 * A highly customizable and reusable top app bar component that features a vertical gradient background.
 * It uses a slot-based design pattern to accept dynamic content for navigation, actions, and sub-bar elements.
 *
 * @param modifier The [Modifier] to be applied to the root container.
 * @param scrollBehavior The [TopAppBarScrollBehavior] to control the bar's expansion or collapse states.
 * @param themes The list of [Color] objects utilized to draw the vertical background gradient brush.
 * @param navigationSlot The optional composable block positioned at the start of the top app bar.
 * @param headerSlot The optional composable block representing the title or central layout of the bar.
 * @param actionsSlot Additional functional elements placed horizontally before the options dropdown menu.
 * @param optionsMenuSlot The dynamic content drawn inside the pre-styled modern dropdown menu sheet.
 * @param footerSlot A flexible lower container suitable for hosting structural elements like page tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    themes: List<Color> = listOf(MaterialTheme.colorScheme.primaryContainer, Color.Transparent),
    navigationSlot: @Composable () -> Unit = {},
    headerSlot: @Composable () -> Unit = {},
    actionsSlot: @Composable RowScope.() -> Unit = {},
    optionsMenuSlot: @Composable ColumnScope.(onDismiss: () -> Unit) -> Unit = {},
    footerSlot: @Composable ColumnScope.() -> Unit = {}
) {
    val gradientBrush = remember(themes) {
        Brush.verticalGradient(colors = themes)
    }

    Column(
        modifier = modifier.background(brush = gradientBrush)
    ) {
        TopAppBar(
            title = headerSlot,
            navigationIcon = navigationSlot,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            actions = {
                Row(
                    modifier = Modifier.padding(end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionsSlot()
                    AdaptiveActionSheet(content = optionsMenuSlot)
                }
            }
        )
        footerSlot()
    }
}

/**
 * A private custom wrapper that manages the visibility state and visual aesthetics of the contextual action menu.
 * It enforces a high-radius rounded corner shape to provide an expressive interface layout.
 *
 * @param content The structural layout components to render inside the opened popup window.
 */
@Composable
private fun AdaptiveActionSheet(
    content: @Composable ColumnScope.(onDismiss: () -> Unit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        FilledIconButton(
            onClick = { isExpanded = true },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = MoreVert,
                contentDescription = "Options Menu"
            )
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(20.dp)
            )
        ) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.widthIn(min = 180.dp)
            ) {
                content { isExpanded = false }
            }
        }
    }
}

/**
 * An expressive extension item layout confined directly to the [ColumnScope] of a dropdown interface.
 * It completely eliminates external dependencies or signatures by leveraging standard layout primitives.
 *
 * @param label The semantic text layout presented within the custom row block.
 * @param onClick The functional action sequence executed upon selecting this interactive row.
 * @param modifier Custom [Modifier] adjustments applied directly to the item container wrapper.
 * @param isCritical Marks the option as destructive, altering typography colors to indicate severe impact.
 * @param customFont Optional custom [FontFamily] applied to the internal descriptive typography layer.
 * @param iconSlot Dynamic composable block responsible for rendering image graphics seamlessly.
 */
@Composable
fun ColumnScope.ContextMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    customFont: FontFamily? = null,
    iconSlot: (@Composable () -> Unit)? = null
) {
    val operationalColor = if (isCritical) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (iconSlot != null) {
            CompositionLocalProvider(LocalContentColor provides operationalColor) {
                Box(
                    modifier = Modifier.sizeIn(minWidth = 22.dp, minHeight = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    iconSlot()
                }
            }
        }

        Text(
            text = label,
            color = operationalColor,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = customFont,
            fontWeight = FontWeight.SemiBold
        )
    }
}