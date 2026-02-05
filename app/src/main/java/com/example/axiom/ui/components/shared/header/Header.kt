package com.example.axiom.ui.components.shared.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

private enum class HeaderMode {
    NORMAL, SELECTION, SEARCH
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

    val mode = when {
        search is SearchSpec.Inline && search.expanded -> HeaderMode.SEARCH
        isSelectionMode -> HeaderMode.SELECTION
        else -> HeaderMode.NORMAL
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
            .padding(style.contentPadding)
    ) {

        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                when (targetState) {
                    HeaderMode.SEARCH ->
                        (fadeIn(tween(animMs)) + slideInVertically(tween(animMs)) { it / 3 })
                            .togetherWith(
                                fadeOut(tween(animMs)) + slideOutVertically(tween(animMs)) { -it / 3 }
                            )

                    HeaderMode.SELECTION ->
                        (fadeIn(tween(animMs)) + slideInVertically(tween(animMs)) { -it / 3 })
                            .togetherWith(
                                fadeOut(tween(animMs)) + slideOutVertically(tween(animMs)) { it / 3 }
                            )

                    else ->
                        fadeIn(tween(animMs)) togetherWith fadeOut(tween(animMs))
                }
            },
            label = "list-header-mode"
        ) { currentMode ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(style.backgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {

                when (currentMode) {

                    HeaderMode.NORMAL -> {
                        // LEFT
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            back?.let {
                                AppIconButton(
                                    icon = it.icon,
                                    contentDescription = it.contentDescription,
                                    onClick = it.onClick,
                                    tint = style.iconTint,
                                    iconSize = style.iconSize
                                )
                                Spacer(Modifier.width(style.spacing))
                            }

                            Text(
                                text = title,
                                style = style.titleStyle
                            )


                        }

                        Spacer(Modifier.weight(1f))

                        // RIGHT
                        Row {
                            if (search is SearchSpec.Inline) {
                                AppIconButton(
                                    icon = AppIcons.Search,
                                    contentDescription = "Search",
                                    onClick = { search.onExpandedChange(true) },
                                    tint = style.iconTint,
                                    iconSize = style.iconSize
                                )
                            }
                            add?.let {
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

                    HeaderMode.SELECTION -> {
                        // LEFT
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconButton(
                                icon = AppIcons.Close,
                                contentDescription = "Cancel selection",
                                onClick = onCancelSelection,
                                tint = style.iconTint,
                                iconSize = style.iconSize
                            )
                            Spacer(Modifier.width(style.spacing))
                            Text(
                                text = "$selectedCount selected",
                                style = style.titleStyle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        // RIGHT
                        Row {
                            if (selectedCount == 1) {
                                edit?.let {
                                    AppIconButton(
                                        icon = it.icon,
                                        contentDescription = it.contentDescription,
                                        onClick = it.onClick,
                                        tint = style.iconTint,
                                        iconSize = style.iconSize
                                    )
                                }
                            }
                            delete?.let {
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

                    HeaderMode.SEARCH -> {
                        val focusRequester = remember { FocusRequester() }
                        val keyboardController = LocalSoftwareKeyboardController.current
                        val inline = search as SearchSpec.Inline

                        OutlinedTextField(
                            value = inline.query,
                            onValueChange = inline.onQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = { Text(inline.placeholder) },
                            singleLine = true,
                            leadingIcon = {
                                AppIconButton(
                                    AppIcons.Search,
                                    tint = style.iconTint,
                                    iconSize = 20.dp,
                                    onClick = {},
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (inline.query.isNotBlank()) {
                                    AppIconButton(
                                        icon = inline.clearButtonIcon,
                                        contentDescription = "Clear",
                                        onClick = {
                                            inline.onClear?.invoke()
                                                ?: inline.onQueryChange("")
                                        },
                                        tint = style.iconTint,
                                        iconSize = 20.dp
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { inline.onSearchImeAction?.invoke() }
                            ),
                            shape = style.searchFieldShape
                        )

                        Spacer(Modifier.width(style.spacing))

                        AppIconButton(
                            icon = AppIcons.Close,
                            contentDescription = "Cancel search",
                            onClick = { inline.onExpandedChange(false) },
                            tint = style.iconTint,
                            iconSize = style.iconSize
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
                }
            }
        }


    }
}



