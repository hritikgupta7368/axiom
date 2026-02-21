package com.example.axiom.ui.components.shared.bottomSheet

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.sp

@SuppressLint("UnrememberedMutableState", "RestrictedApi")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    containerWidth: Dp? = null,
    centerWhenUnfocused: Boolean = true,
    tint: Color = Color(0xFF007AFF),
    onSearch: (String) -> Unit = {},
    onClear: (() -> Unit)? = null,
    onSearchDone: () -> Unit = {},
    renderTrailingIcons: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var measuredWidthDp by remember { mutableStateOf(0.dp) }
    var isFocused by remember { mutableStateOf(false) }

    // Use an active state so it doesn't re-center if you unfocus but text is still there
    val isActive = isFocused || value.isNotEmpty()

    val focusAnim = remember { Animatable(0f) }
    val clearScale = remember { Animatable(0f) }
    val clearOpacity = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusAnim.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 500f))
        } else {
            focusAnim.animateTo(0f, tween(250))
        }
    }

    LaunchedEffect(value) {
        if (value.isNotEmpty()) {
            clearScale.animateTo(1f, animationSpec = spring())
            clearOpacity.animateTo(1f, animationSpec = tween(durationMillis = 180))
        } else {
            clearScale.animateTo(0f, animationSpec = spring(dampingRatio = 1f))
            clearOpacity.animateTo(0f, animationSpec = tween(durationMillis = 160))
        }
    }

    var searchWidthPx by remember { mutableFloatStateOf(0f) }

    // Pre-calculate the exact static width of the placeholder text and icon
    // to prevent jittery infinite measurement loops during animation.
    val staticContentWidthPx = remember(placeholder, density) {
        val placeholderWidth = textMeasurer.measure(
            text = placeholder,
            style = TextStyle(fontSize = 17.sp, fontFamily = FontFamily.Default)
        ).size.width.toFloat()
        val iconWidth = with(density) { 18.dp.toPx() }
        val padding = with(density) { 8.dp.toPx() }
        placeholderWidth + iconWidth + padding
    }

    val centerTranslateX by remember {
        derivedStateOf {
            if (!centerWhenUnfocused || searchWidthPx == 0f) 0f
            else {
                val horizontalPadding = with(density) { 12.dp.toPx() * 2 }
                val availableWidth = searchWidthPx - horizontalPadding
                val centerOffset = (availableWidth - staticContentWidthPx) / 2f
                // Apply coercion to prevent negative offset if the screen is too small
                (centerOffset.coerceAtLeast(0f)) * (1f - focusAnim.value)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .onGloballyPositioned { layoutCoordinates ->
                if (containerWidth == null) {
                    measuredWidthDp = (layoutCoordinates.size.width.toFloat() / density.density).dp
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .onGloballyPositioned {
                    searchWidthPx = it.size.width.toFloat()
                }
        ) {
            // Background Layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val blurRadius = when {
                                focusAnim.value <= 0.3f -> lerp(0f, 20f, focusAnim.value / 0.3f)
                                focusAnim.value <= 0.5f -> lerp(
                                    20f,
                                    30f,
                                    (focusAnim.value - 0.3f) / 0.2f
                                )

                                else -> lerp(30f, 0f, (focusAnim.value - 0.5f) / 0.5f)
                            }.coerceIn(0f, 30f)

                            renderEffect = if (blurRadius > 0.5f) {
                                RenderEffect.createBlurEffect(
                                    blurRadius,
                                    blurRadius,
                                    Shader.TileMode.CLAMP
                                ).asComposeRenderEffect()
                            } else null
                        }
                    }
                    .background(color = Color(0x1E767680), shape = RoundedCornerShape(12.dp))
                    .shadow(elevation = 0.dp, shape = RoundedCornerShape(12.dp))
            )

            // Content Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Leading Icon
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .graphicsLayer { translationX = centerTranslateX },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Input Field (Maintains weight(1f) to prevent layout jumps)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .graphicsLayer { translationX = centerTranslateX },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = {
                                onValueChange(it)
                                onSearch(it)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default
                            ),
                            cursorBrush = SolidColor(tint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isFocused = it.isFocused }
                                .padding(vertical = 10.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search,
                                autoCorrectEnabled = false
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    onSearchDone()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (value.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 17.sp
                                            // Ensure this isn't globally centered, just left aligned inside the box
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    // Clear Button
                    if (value.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .scale(clearScale.value)
                                .graphicsLayer { alpha = clearOpacity.value }
                                .clickable {
                                    onValueChange("")
                                    onClear?.invoke()
                                },
                        ) {
                            if (renderTrailingIcons != null) {
                                renderTrailingIcons()
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "clear",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cancel Button
        AnimatedVisibility(
            visible = isFocused,
            enter = slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { it }
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { it }
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .height(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Cancel",
                    color = tint,
                    fontSize = 17.sp,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                        onValueChange("")
                        onClear?.invoke()
                        onSearchDone()
                    }
                )
            }
        }
    }
}