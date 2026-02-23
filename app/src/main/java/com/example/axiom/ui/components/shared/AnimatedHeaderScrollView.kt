package com.example.axiom.ui.components.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.axiom.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

val headerHeight = 44.dp + 54.dp

//colors
val yellow = Color(0xFFFED702)
val searchbar = Color(0xFF373739)
val placeholder = Color(0xFFFFFFFF)

@Composable
fun AnimatedHeaderScrollView(
    largeTitle: String,
    subtitle: String? = null,
    query: String = "",
    updateQuery: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    onAddClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
//    content: @Composable ColumnScope.() -> Unit
    content: LazyListScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val hazeState = remember { HazeState() }
    val overscrollOffset = remember { Animatable(0f) }

    var isSearchLocked by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Unlock and hide search bar natively when user scrolls UP normally
                if (isSearchLocked && available.y < 0f && scrollState.value > 5) {
                    isSearchLocked = false
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
                // FLUID FIX 2: Only stretch to reveal search if we are at the absolute top of the list
                if (available.y > 0f && scrollState.value == 0) {
                    val resistance = 0.45f
                    coroutineScope.launch { overscrollOffset.snapTo(overscrollOffset.value + (available.y * resistance)) }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                // FLUID FIX 3: If pulled down more than 100 pixels, lock it open!
                if (overscrollOffset.value > 100f) {
                    isSearchLocked = true
                }

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

    val searchBarMaxHeightDp = 58.dp // 36dp for bar + 16dp for padding
    val searchBarMaxHeightPx = with(density) { searchBarMaxHeightDp.toPx() }
    val rawHeightFromDrag = (overscrollOffset.value * 0.5f).coerceIn(0f, searchBarMaxHeightPx)

    val targetSearchHeight =
        if (isSearchLocked) searchBarMaxHeightDp else with(density) { rawHeightFromDrag.toDp() }

    val searchHeight by animateDpAsState(
        targetValue = targetSearchHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), // Buttery smooth spring
        label = "searchHeight"
    )

    val searchAlpha by animateFloatAsState(
        targetValue = if (searchHeight > 10.dp) (searchHeight / searchBarMaxHeightDp).coerceIn(
            0f,
            1f
        ) else 0f,
        label = "searchAlpha"
    )

    val animatedTitleAlpha by animateFloatAsState(
        targetValue = if (isSearchLocked) 0f else smallHeaderOpacity,
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
                    // Restored your 80.dp top padding choice
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
                    letterSpacing = (-0.4).sp,
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

            // FLUID FIX 5: Layout Masking. We don't remove it from the composition.
            // We just animate the height of the parent box and clip it. Zero stutter.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(searchHeight)
                    .graphicsLayer { alpha = searchAlpha }
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.BottomCenter // Keeps it anchored to the bottom as it opens
            ) {
                // The actual internal row is always full height so it doesn't squish, it just gets masked
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(searchbar)
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
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.4).sp,
                        ),
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
                        modifier = Modifier
                            .padding(bottom = 12.dp)

                    )
                }
            }
        }

        // --- 3 & 4. COMBINED ACTION CONTAINER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .zIndex(300f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE
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
                        tint = yellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Folders",
                        color = yellow,
                        fontSize = 17.sp,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.offset(x = (-5).dp)
                    )
                }

                // RIGHT SIDE
                AnimatedContent(
                    targetState = isSelectionMode, // Replace with your selection active state
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
                            IconButton(onClick = onEditClick) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            IconButton(onClick = onAddClick) {
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

