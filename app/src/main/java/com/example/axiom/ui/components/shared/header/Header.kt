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
import androidx.compose.foundation.layout.Arrangement
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
    data class IconOnly(val action: HeaderActionSpec) : SearchSpec
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
        iconSize: Dp = 24.dp,
        spacing: Dp = 12.dp,
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
    // Normal mode
    back: HeaderActionSpec? = null,
    add: HeaderActionSpec? = null,
    // Selection mode
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onCancelSelection: () -> Unit = {},
    edit: HeaderActionSpec? = null,
    delete: HeaderActionSpec? = null,
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
        // ── Top Row ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(style.spacing)
        ) {
            // 1. LEFT: Back / Cancel
            if (isSelectionMode) {
                AppIconButton(
                    icon = AppIcons.Close,
                    contentDescription = "Cancel selection",
                    onClick = onCancelSelection,
                    tint = style.iconTint,
                    iconSize = style.iconSize
                )
            } else if (leadingSlot != null) {
                leadingSlot()
            } else {
                back?.takeIf { it.visible }?.let {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(animMs)) + scaleIn(tween(animMs), 0.92f),
                        exit = fadeOut(tween(animMs)) + scaleOut(tween(animMs), 0.92f)
                    ) {
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

            // 2. CENTER: Title or Selected count
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = { titleTransform(animMs) },
                label = "title-selection"
            ) { inSelection ->
                Text(
                    text = if (inSelection) "$selectedCount selected" else title,
                    style = style.titleStyle,
                    color = if (inSelection) MaterialTheme.colorScheme.primary else style.titleStyle.color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = style.spacing)
                )
            }

            // 3. RIGHT: Actions (properly aligned to end)
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trailingSlot != null) {
                    trailingSlot()
                } else if (isSelectionMode) {
                    // Selection mode actions
                    if (selectedCount == 1) {
                        edit?.let { AnimatedHeaderAction(it, style) }
                    }
                    delete?.let { AnimatedHeaderAction(it, style) }
                } else {
                    // Normal mode actions
                    when (search) {
                        is SearchSpec.Hidden -> Unit
                        is SearchSpec.IconOnly -> AnimatedHeaderAction(search.action, style)
                        is SearchSpec.Inline -> {
                            val searchIcon =
                                if (search.expanded) AppIcons.Close else AppIcons.Search
                            val desc = if (search.expanded) "Close search" else "Search"

                            AnimatedHeaderAction(
                                action = HeaderActionSpec(
                                    icon = searchIcon,
                                    contentDescription = desc,
                                    onClick = { search.onExpandedChange(!search.expanded) }
                                ),
                                style = style
                            )
                        }
                    }
                    add?.let { AnimatedHeaderAction(it, style) }
                }
            }
        }

        // ── Search Bar ──────────────────────────────────────────────────────────
        if (search is SearchSpec.Inline) {
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = search.expanded,
                enter = fadeIn(tween(animMs)) + slideInVertically(tween(animMs)) { it / 3 },
                exit = fadeOut(tween(animMs)) + slideOutVertically(tween(animMs)) { it / 3 }
            ) {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = search.query,
                    onValueChange = search.onQueryChange,
                    singleLine = true,
                    placeholder = { Text(search.placeholder) },
                    leadingIcon = {
                        AppIconButton(
                            icon = AppIcons.Search,
                            tint = style.iconTint,
                            iconSize = 20.dp,
                            onClick = {},
                            contentDescription = "Search",
                        )
                    },
                    trailingIcon = if (search.showClearButton && search.query.isNotBlank()) {
                        {
                            AppIconButton(
                                icon = search.clearButtonIcon,
                                contentDescription = "Clear",
                                onClick = {
                                    search.onClear?.invoke() ?: search.onQueryChange("")
                                    focusRequester.requestFocus() // keep focus after clear
                                },
                                tint = style.iconTint,
                                iconSize = 20.dp
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search.onSearchImeAction?.invoke() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .clip(style.searchFieldShape)
                )

                // Auto-focus + keyboard
                LaunchedEffect(search.expanded) {
                    if (search.expanded) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }

                // Optional: Clear query when collapsing search
                LaunchedEffect(search.expanded) {
                    if (!search.expanded) {
                        search.onQueryChange("")
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
    action?.takeIf { it.visible }?.let {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(style.animationMillis)) + scaleIn(
                tween(style.animationMillis),
                0.9f
            ),
            exit = fadeOut(tween(style.animationMillis)) + scaleOut(
                tween(style.animationMillis),
                0.9f
            )
        ) {
            AppIconButton(
                icon = it.icon,
                contentDescription = it.contentDescription,
                onClick = it.onClick,
                tint = style.iconTint,
                iconSize = style.iconSize,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun titleTransform(animMs: Int): ContentTransform =
    fadeIn(tween(animMs)) togetherWith fadeOut(tween(animMs))