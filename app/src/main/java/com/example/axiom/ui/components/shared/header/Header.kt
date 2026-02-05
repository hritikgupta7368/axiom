package com.example.axiom.ui.components.shared.header


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.button.AppIcon
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons

@Immutable
data class HeaderActionSpec(
    val icon: AppIcon,
    val contentDescription: String?,
    val onClick: () -> Unit,
    val visible: Boolean = true,
    val enabled: Boolean = true
)

sealed interface SearchSpec {
    data object Hidden : SearchSpec

    data class IconOnly(
        val action: HeaderActionSpec
    ) : SearchSpec

    data class Inline(
        val expanded: Boolean,
        val onExpandedChange: (Boolean) -> Unit,
        val query: String,
        val onQueryChange: (String) -> Unit,
        val placeholder: String = "Search...",
        val enabled: Boolean = true,
        val onSearchImeAction: (() -> Unit)? = null,
        val showClearButton: Boolean = true,
        val clearButtonIcon: AppIcon = AppIcons.Close,
        val onClear: (() -> Unit)? = null
    ) : SearchSpec
}

@Immutable
data class ListHeaderStyle(
    val backgroundColor: Color,
    val contentPadding: PaddingValues,
    val titleStyle: TextStyle,
    val iconTint: Color,
    val iconSize: Dp,
    val spacing: Dp,
    val searchFieldShape: CornerBasedShape,
    val animationMillis: Int
)

object ListHeaderDefaults {
    @Composable
    fun style(
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
        iconTint: Color = MaterialTheme.colorScheme.onSurface,
        iconSize: Dp = 22.dp,
        spacing: Dp = 8.dp,
        searchFieldShape: CornerBasedShape = RoundedCornerShape(14.dp),
        animationMillis: Int = 220
    ): ListHeaderStyle = ListHeaderStyle(
        backgroundColor = backgroundColor,
        contentPadding = contentPadding,
        titleStyle = titleStyle,
        iconTint = iconTint,
        iconSize = iconSize,
        spacing = spacing,
        searchFieldShape = searchFieldShape,
        animationMillis = animationMillis
    )
}


@Composable
fun ListHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: ListHeaderStyle = ListHeaderDefaults.style(),

    // Normal mode actions
    back: HeaderActionSpec? = null,
    add: HeaderActionSpec? = null,           // ← new: the + button

    // Selection mode
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onCancelSelection: () -> Unit = {},
    edit: HeaderActionSpec? = null,          // only shown when selectedCount == 1
    delete: HeaderActionSpec? = null,        // shown when selectedCount >= 1

    // Search
    search: SearchSpec = SearchSpec.Hidden,

    leadingSlot: (@Composable RowScope.() -> Unit)? = null,
    trailingSlot: (@Composable RowScope.() -> Unit)? = null,
) {
    val animMs = style.animationMillis

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
            .padding(style.contentPadding)
    ) {
        // ── Top row ───────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left side: Back OR Cancel
            if (isSelectionMode) {
                AppIconButton(
                    icon = AppIcons.Close,           // or ArrowBack / Cancel icon
                    contentDescription = "Cancel selection",
                    onClick = onCancelSelection,
                    tint = style.iconTint,
                    iconSize = style.iconSize
                )
            } else if (leadingSlot != null) {
                leadingSlot()
            } else {
                AnimatedVisibility(
                    visible = back?.visible != false,
                    enter = fadeIn(tween(animMs)) + scaleIn(tween(animMs), 0.92f),
                    exit = fadeOut(tween(animMs)) + scaleOut(tween(animMs), 0.92f)
                ) {
                    back?.let {
                        AppIconButton(
                            icon = it.icon,
                            contentDescription = it.contentDescription,
                            onClick = it.onClick,
                            tint = style.iconTint,
                            iconSize = style.iconSize
                        )
                    }
                }
            }

            Spacer(Modifier.width(style.spacing))

            // Center: Title OR Selected count
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = { titleTransform(animMs) },
                label = "title-or-selection"
            ) { selecting ->
                if (selecting) {
                    Text(
                        text = "$selectedCount selected",
                        style = style.titleStyle,
                        color = MaterialTheme.colorScheme.primary, // optional: highlight
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = style.spacing)
                    )
                } else {
                    Text(
                        text = title,
                        style = style.titleStyle,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = style.spacing)
                    )
                }
            }

            // Right side: actions
            if (trailingSlot != null) {
                trailingSlot()
            } else {
                // ── Selection mode actions ──
                if (isSelectionMode) {
                    if (selectedCount == 1) {
                        AnimatedHeaderAction(edit, style)
                    }
                    AnimatedHeaderAction(delete, style)
                }
                // ── Normal mode actions ──
                else {
                    // Search toggle (icon always visible unless inline expanded)
                    when (search) {
                        is SearchSpec.Hidden -> Unit
                        is SearchSpec.IconOnly -> {
                            AnimatedHeaderAction(search.action, style)
                        }

                        is SearchSpec.Inline -> {
                            AnimatedHeaderAction(
                                action = HeaderActionSpec(
                                    icon = if (search.expanded) AppIcons.Close else AppIcons.Search,
                                    contentDescription = if (search.expanded) "Close search" else "Search",
                                    onClick = { search.onExpandedChange(!search.expanded) }
                                ),
                                style = style
                            )
                        }
                    }

                    // Add button (only in normal mode)
                    AnimatedHeaderAction(add, style)
                }
            }
        }

        // ── Search bar (only for Inline mode) ──────────────────────────────
// ── Search bar (only for Inline mode) ──────────────────────────────
        if (search is SearchSpec.Inline) {
            Spacer(Modifier.height(8.dp))   // or 10.dp / whatever you had

            AnimatedVisibility(
                visible = search.expanded,
                enter = fadeIn(tween(animMs)) + slideInVertically(tween(animMs)) { it / 3 },
                exit = fadeOut(tween(animMs)) + slideOutVertically(tween(animMs)) { it / 3 }
            ) {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current  // optional

                OutlinedTextField(
                    value = search.query,
                    onValueChange = search.onQueryChange,
                    enabled = search.enabled,
                    singleLine = true,
                    placeholder = { Text(search.placeholder) },
                    leadingIcon = {
                        AppIconButton(
                            icon = AppIcons.Search,
                            tint = style.iconTint,
                            contentDescription = "Search",
                            onClick = {}
                        )
                    },
                    trailingIcon = if (search.showClearButton) {
                        {
                            AnimatedVisibility(
                                visible = search.query.isNotBlank(),
                                enter = fadeIn(tween(animMs)) + scaleIn(
                                    tween(animMs),
                                    initialScale = 0.9f
                                ),
                                exit = fadeOut(tween(animMs)) + scaleOut(
                                    tween(animMs),
                                    targetScale = 0.9f
                                )
                            ) {
                                AppIconButton(
                                    icon = search.clearButtonIcon,
                                    contentDescription = "Clear search",
                                    onClick = {
                                        search.onClear?.invoke() ?: search.onQueryChange("")
                                        focusRequester.requestFocus()  // keep focus after clear (nice UX)
                                    },
                                    tint = style.iconTint,
                                    iconSize = 20.dp
                                )
                            }
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { search.onSearchImeAction?.invoke() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)           // ← key line
                        .clip(style.searchFieldShape)
                )

                // Auto-focus + show keyboard when the bar becomes visible
                LaunchedEffect(search.expanded) {
                    if (search.expanded) {
                        focusRequester.requestFocus()
                        // Optional: force keyboard visibility (usually not needed, but helps in edge cases)
                        keyboardController?.show()
                    }
                }
            }
        }
    }
}


@Composable
private fun AnimatedHeaderAction(
    action: HeaderActionSpec?,
    style: ListHeaderStyle
) {
    val animMs = style.animationMillis
    AnimatedVisibility(
        visible = action?.visible == true,
        enter = fadeIn(tween(animMs)) + scaleIn(tween(animMs), initialScale = 0.9f),
        exit = fadeOut(tween(animMs)) + scaleOut(tween(animMs), targetScale = 0.9f)
    ) {
        if (action != null) {
            AppIconButton(
                icon = action.icon,
                contentDescription = action.contentDescription,
                onClick = action.onClick,
                tint = style.iconTint,
                iconSize = style.iconSize,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun titleTransform(animMs: Int): ContentTransform {
    return fadeIn(tween(animMs)) togetherWith fadeOut(tween(animMs))
}
