package com.example.axiom.ui.components.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.axiom.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

val headerHeight = 44.dp + 54.dp // You can adjust this to 56.dp or whatever looks best

@Composable
fun AnimatedHeaderScrollView(
    largeTitle: String,
    subtitle: String? = null,
    query: String = "",
    updateQuery: (String) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val hazeState = remember { HazeState() }
    val overscrollOffset = remember { Animatable(0f) }

    // 1. NEW: State to track if the search bar is revealed
    var isSearchRevealed by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Hide search bar when user starts scrolling the list back up
                if (available.y < -5f && scrollState.value > 10) {
                    isSearchRevealed = false
                }

                if (overscrollOffset.value > 0f && available.y < 0f) {
                    val consumed = available.y.coerceAtLeast(-overscrollOffset.value)
                    coroutineScope.launch { overscrollOffset.snapTo(overscrollOffset.value + consumed) }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Reveal search bar when pulling down at the very top of the list
                if (available.y > 10f) {
                    isSearchRevealed = true
                }

                if (available.y > 0f) {
                    val resistance = 0.45f
                    coroutineScope.launch { overscrollOffset.snapTo(overscrollOffset.value + (available.y * resistance)) }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (overscrollOffset.value > 0f) {
                    overscrollOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
                return Velocity.Zero
            }
        }
    }

    val headerHeightPx = with(density) { headerHeight.toPx() }
    val scrollY = scrollState.value
    val normalizedScroll = (scrollY / headerHeightPx).coerceIn(0f, 1f)

    val largeTitleOpacity = 1f - (normalizedScroll * 1.5f).coerceIn(0f, 1f)
    val smallHeaderOpacity =
        if (normalizedScroll > 0.6f) ((normalizedScroll - 0.6f) * 2.5f).coerceIn(0f, 1f) else 0f
    val zoomScale = 1f + (overscrollOffset.value / 800f).coerceAtLeast(0f)

    // Smoothly fade out small title when search is opened
    val animatedTitleAlpha by animateFloatAsState(
        targetValue = if (isSearchRevealed) 0f else smallHeaderOpacity,
        label = "titleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(nestedScrollConnection)
    ) {
        // --- 1. MAIN SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
                .verticalScroll(scrollState)
                .graphicsLayer { translationY = overscrollOffset.value }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    // 2. FIX: Reduced top padding to move title up
                    .padding(top = 80.dp, bottom = 16.dp)
                    .graphicsLayer {
                        alpha = largeTitleOpacity
                        scaleX = zoomScale
                        scaleY = zoomScale
                        transformOrigin = TransformOrigin(0f, 1f)
                    }
            ) {
                Text(
                    text = largeTitle,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 3. FIX: PULL-TO-REVEAL SEARCH BAR (Embedded in scrolling content)
            AnimatedVisibility(
                visible = isSearchRevealed,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(20.dp)
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = { updateQuery(it) },
                        textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                        cursorBrush = SolidColor(Color(0xFFFFCC00)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text("Search", color = Color(0xFF8E8E93), fontSize = 17.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E8E93))
                                .clickable { updateQuery("") }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) { content() }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- 2. FIXED TOP HEADER (Glass & Title) ---
        if (smallHeaderOpacity > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .zIndex(100f)
                    .graphicsLayer { alpha = smallHeaderOpacity }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .hazeChild(state = hazeState)
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(200f)
                        .graphicsLayer { alpha = animatedTitleAlpha },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = largeTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        // --- 3 & 4. COMBINED ACTION CONTAINER (Left & Right Buttons) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .zIndex(300f),
            contentAlignment = Alignment.Center
        ) {
            // A single AnimatedVisibility controls BOTH sides fading out if you wanted to hide them
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // --- LEFT SIDE: Back Button ---
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { /* Handle Back */ }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_ios),
                            contentDescription = "Back",
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Folders",
                            color = Color(0xFFFFCC00),
                            fontSize = 17.sp,
                            modifier = Modifier.offset(x = (-4).dp)
                        )
                    }

                    // --- RIGHT SIDE: Action Buttons ---
                    AnimatedContent(
                        targetState = false, // Replace with your selection active state
                        transitionSpec = {
                            if (targetState) {
                                (slideInVertically(tween(300)) { it } + fadeIn(tween(300))) togetherWith (slideOutVertically(
                                    tween(300)
                                ) { -it } + fadeOut(tween(300)))
                            } else {
                                (slideInVertically(tween(300)) { -it } + fadeIn(tween(300))) togetherWith (slideOutVertically(
                                    tween(300)
                                ) { it } + fadeOut(tween(300)))
                            }
                        },
                        label = "Actions"
                    ) { selectionActive ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectionActive) {
                                IconButton(onClick = { /* Edit */ }) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { /* Delete */ }) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFF453A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                // 4. FIX: Removed Search Button, only Add remains
                                IconButton(onClick = { /* Add */ }) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "Add",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimatedHeaderScrollViewPreview() {

    MaterialTheme {
        Surface(color = Color.Black) {

            var query by remember { mutableStateOf("") }

            AnimatedHeaderScrollView(
                largeTitle = "Notes",
                subtitle = "iCloud",
                query = query,
                updateQuery = { query = it }
            ) {

                // Dummy scrolling content
                repeat(30) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(60.dp)
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Note Item ${index + 1}",
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}