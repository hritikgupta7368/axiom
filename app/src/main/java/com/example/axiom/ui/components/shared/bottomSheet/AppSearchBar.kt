//package com.example.axiom.ui.components.shared.bottomSheet
//
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.scaleIn
//import androidx.compose.animation.scaleOut
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//// ─────────────────────────────────────────────────────────────────────────────
////  Colours — tweak freely
//// ─────────────────────────────────────────────────────────────────────────────
//private val PillBackground = Color(0xFF1C1C1E)
//private val PlaceholderColor = Color(0xFF8E8E93)
//private val SearchTextColor = Color.White
//private val IconTint = Color(0xFF8E8E93)
//
//// ─────────────────────────────────────────────────────────────────────────────
////  SearchBar
//// ─────────────────────────────────────────────────────────────────────────────
///**
// * iOS-style animated SearchBar.
// *
// * State is fully hoisted — the component only manages the text; you do your
// * own filtering externally using [query].
// *
// * Example usage:
// *
// *   var query by remember { mutableStateOf("") }
// *   SearchBar(query = query, onQueryChange = { query = it })
// *   val filtered = allItems.filter { it.name.contains(query, ignoreCase = true) }
// */
//@Composable
//fun SearchBar(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    placeholder: String = "Search",
//    tint: Color = Color(0xFF007AFF),
//    onSearchDone: () -> Unit = {},
//    onClear: () -> Unit = {},
//    onCancel: () -> Unit = {},
//) {
//    val focusRequester = remember { FocusRequester() }
//    val focusManager = LocalFocusManager.current
//    var isFocused by remember { mutableStateOf(false) }
//
//    val active = isFocused || query.isNotEmpty()
//
//    // 0 → 1 spring that drives the centering-to-left-align transition
//    val focusProgress by animateFloatAsState(
//        targetValue = if (active) 1f else 0f,
//        animationSpec = spring(dampingRatio = 0.72f, stiffness = 220f),
//        label = "focusProgress"
//    )
//
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//
//        // ── Pill container ────────────────────────────────────────────────────
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .height(44.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(PillBackground)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 10.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                // Leading spacer shrinks to 0 as the bar becomes focused
//                // giving the illusion of the content sliding from centre → left
//                val leadingWeight = lerp(0.20f, 0.001f, focusProgress)
//                Spacer(modifier = Modifier.weight(leadingWeight))
//
//                // Search icon
//                Icon(
//                    imageVector = Icons.Default.Search,
//                    contentDescription = null,
//                    tint = IconTint,
//                    modifier = Modifier.size(18.dp)
//                )
//
//                Spacer(modifier = Modifier.width(6.dp))
//
//                // Placeholder + input layered in a Box
//                Box(
//                    modifier = Modifier.weight(1f),
//                    contentAlignment = Alignment.CenterStart
//                ) {
//                    if (query.isEmpty()) {
//                        Text(
//                            text = placeholder,
//                            style = TextStyle(
//                                color = PlaceholderColor,
//                                fontSize = 17.sp,
//                                fontWeight = FontWeight.Normal
//                            )
//                        )
//                    }
//                    BasicTextField(
//                        value = query,
//                        onValueChange = onQueryChange,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .focusRequester(focusRequester)
//                            .onFocusChanged { state -> isFocused = state.isFocused },
//                        singleLine = true,
//                        textStyle = TextStyle(
//                            color = SearchTextColor,
//                            fontSize = 17.sp,
//                            fontWeight = FontWeight.Normal
//                        ),
//                        cursorBrush = SolidColor(tint),
//                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//                        keyboardActions = KeyboardActions(onSearch = {
//                            focusManager.clearFocus()
//                            onSearchDone()
//                        })
//                    )
//                }
//
//                // Clear (×) button — springs in when there is text
//                AnimatedVisibility(
//                    visible = query.isNotEmpty(),
//                    enter = scaleIn(spring(dampingRatio = 0.55f, stiffness = 400f)) +
//                            fadeIn(tween(150)),
//                    exit = scaleOut(tween(120)) + fadeOut(tween(120))
//                ) {
//                    IconButton(
//                        onClick = {
//                            onQueryChange("")
//                            onClear()
//                            focusRequester.requestFocus()
//                        },
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Clear,
//                            contentDescription = "Clear",
//                            tint = IconTint,
//                            modifier = Modifier.size(17.dp)
//                        )
//                    }
//                }
//
//                // Mirror spacer on the right (also shrinks on focus)
//                val trailingWeight = lerp(0.20f, 0.001f, focusProgress)
//                Spacer(modifier = Modifier.weight(trailingWeight))
//            }
//        }
//
//        // ── Cancel button ─────────────────────────────────────────────────────
//        AnimatedVisibility(
//            visible = active,
//            enter = slideInHorizontally(
//                initialOffsetX = { it },
//                animationSpec = spring(dampingRatio = 0.72f, stiffness = 220f)
//            ) + fadeIn(tween(140, delayMillis = 60)),
//            exit = slideOutHorizontally(
//                targetOffsetX = { it },
//                animationSpec = tween(180)
//            ) + fadeOut(tween(120))
//        ) {
//            TextButton(
//                onClick = {
//                    focusManager.clearFocus()
//                    onQueryChange("")
//                    onCancel()
//                },
//                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
//            ) {
//                Text(
//                    text = "Cancel",
//                    color = tint,
//                    fontSize = 17.sp,
//                    fontWeight = FontWeight.Normal
//                )
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
//private fun lerp(start: Float, stop: Float, fraction: Float): Float =
//    start + fraction * (stop - start)