package com.example.axiom.ui.components.shared.bottomSheet

// SearchBar.kt


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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
    containerWidth: Dp? = null,               // if provided, use fixed container width
    centerWhenUnfocused: Boolean = true,
    tint: Color = Color(0xFF007AFF),
    onSearch: (String) -> Unit = {},
    onClear: (() -> Unit)? = null,
    onSearchDone: () -> Unit = {},
    renderTrailingIcons: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    // measured width (when containerWidth not provided)
    var measuredWidthDp by remember { mutableStateOf(0.dp) }

    // Query state

    var isFocused by remember { mutableStateOf(false) }

    // Animations - single progress value 0..1
    val focusAnim = remember { Animatable(0f) }
    val clearScale = remember { Animatable(0f) }
    val clearOpacity = remember { Animatable(0f) }

    val currentWidthDp = remember(containerWidth, measuredWidthDp) {
        derivedStateOf { containerWidth ?: measuredWidthDp }
    }


    val density = LocalDensity.current


    // When focus changes, animate

    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusAnim.animateTo(
                1f,
                spring(dampingRatio = 0.8f, stiffness = 500f)
            )
        } else {
            focusAnim.animateTo(
                0f,
                tween(250)
            )
        }
    }
    // show/hide clear button based on query
    LaunchedEffect(value) {
        if (value.isNotEmpty()) {
            clearScale.animateTo(1f, animationSpec = spring())
            clearOpacity.animateTo(1f, animationSpec = tween(durationMillis = 180))
        } else {
            clearScale.animateTo(0f, animationSpec = spring(dampingRatio = 1f))
            clearOpacity.animateTo(0f, animationSpec = tween(durationMillis = 160))
        }
    }

    // top-level container
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
        // Animated container (search bar) width
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .height(IntrinsicSize.Min)
//        )
        var searchWidthPx by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .onGloballyPositioned {
                    searchWidthPx = it.size.width.toFloat()
                }
        )
        {
            // background with blur-like effect:
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                            val blurRadius = when {
                                focusAnim.value <= 0.3f ->
                                    lerp(0f, 20f, focusAnim.value / 0.3f)

                                focusAnim.value <= 0.5f ->
                                    lerp(20f, 30f, (focusAnim.value - 0.3f) / 0.2f)

                                else ->
                                    lerp(30f, 0f, (focusAnim.value - 0.5f) / 0.5f)
                            }.coerceIn(0f, 30f)

                            if (blurRadius > 0.5f) {
                                renderEffect = RenderEffect
                                    .createBlurEffect(
                                        blurRadius,
                                        blurRadius,
                                        Shader.TileMode.CLAMP
                                    )
                                    .asComposeRenderEffect()
                            } else {
                                renderEffect = null
                            }
                        }
                    }
                    .background(color = Color(0x1E767680), shape = RoundedCornerShape(12.dp))
                    .shadow(elevation = 0.dp, shape = RoundedCornerShape(12.dp))
            )

            // Content row
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Calculate translation for centered state
//                val centerTranslateX by remember {
//                    derivedStateOf {
//                        if (!centerWhenUnfocused) 0f
//                        else {
//                            with(density) {
//                                val totalPx = currentWidthDp.value.toPx()
//                                val iconAndPaddingPx = 36.dp.toPx() * textCenterOffsetDp
//                                val centerOffsetPx =
//                                    (totalPx - iconAndPaddingPx) / 2f - 10.dp.toPx()
//
//                                val t = focusAnim.value
//                                centerOffsetPx * (1f - t)
//                            }
//                        }
//                    }
//                }

                val centerTranslateX by remember {
                    derivedStateOf {
                        if (!centerWhenUnfocused || searchWidthPx == 0f) 0f
                        else {
                            val horizontalPadding = with(density) { 12.dp.toPx() }
                            val iconWidth = with(density) { 17.sp.toDp().toPx() }
                            val iconGap = with(density) { 8.dp.toPx() }

                            val contentWidth = iconWidth + iconGap

                            val centerOffset =
                                (searchWidthPx - contentWidth) / 2f - horizontalPadding

                            centerOffset * (1f - focusAnim.value)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .offset { IntOffset(x = centerTranslateX.toInt(), y = 0) }
                        .fillMaxWidth()
                ) {
                    // Leading icon
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(17.sp.value.dp)
                        )

                    }

                    // Input field container (flex:1)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Using BasicTextField to customize visuals
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
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                }
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

                                Box {
                                    if (value.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 17.sp,
                                            textAlign = TextAlign.Center,

                                            )
                                    }
                                    innerTextField()
                                }
                            },
                            onTextLayout = {},

                            )
                    }

                    // Clear button (animated scale + opacity)
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
                } // Row end
            } // Box content end
        } // Box container end

        // Cancel button animated style - slide + fade similar to RN
        AnimatedVisibility(
            visible = isFocused,
            enter = slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { it }   // slide from right
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { it }    // slide to right
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