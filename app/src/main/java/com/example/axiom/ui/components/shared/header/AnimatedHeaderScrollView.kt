package com.example.axiom.ui.components.shared.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.axiom.R
import com.example.axiom.ui.theme.AxiomTheme
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
fun TopRightPillMenu(
    isSelectionMode: Boolean,
    selectionCount: Int,
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onThirdOptionClick: () -> Unit = {} // Reserved space!
) {


    val iconTint = AxiomTheme.components.card.title


    Surface(
        shape = CircleShape,
//        color = Color(0xFF131313),
        color = AxiomTheme.components.card.background,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF222222)
        ),
        modifier = Modifier
            .padding(end = 16.dp)
            // This is the magic for the smooth iOS pill expansion/contraction
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                alignment = Alignment.CenterEnd
            )
    ) {
        AnimatedContent(
            targetState = isSelectionMode,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.8f, animationSpec = tween(220)))
                    .togetherWith(
                        fadeOut(tween(150)) + scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(150)
                        )
                    )
                    .using(SizeTransform(clip = false))
            },
            label = "PillActions"
        ) { selectionActive ->
            Row(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionActive) {
                    // --- STATE 2: Grouped Icons (Edit, Delete, + Space for 3rd) ---

                    // Option 1: Edit
                    if (selectionCount == 1) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }


                    // Option 2: Delete
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onThirdOptionClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = "pin",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }


                } else {
                    // --- STATE 1: Single Icon (Add) ---
                    IconButton(onClick = onAddClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "Add",
                            tint = iconTint,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedHeaderScrollView(
    largeTitle: String,
    subtitle: String? = null,
    query: String = "",
    updateQuery: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    showBack: Boolean = false,
    selectionCount: Int = 0,
    onToggleSelectionMode: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onThirdOptionClick: () -> Unit = {},
    onBack: () -> Unit = {},
    backText: String = "Back",
    isParentRoute: Boolean = true,
    onHeaderClick: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit

) {
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val hazeState = remember { HazeState() }
    val overscrollOffset = remember { Animatable(0f) }

    var isSearchLocked by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Check firstVisibleItemIndex to know if we are at the top
                if (isSearchLocked && available.y < 0f && lazyListState.firstVisibleItemIndex > 0) {
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
                if (available.y > 0f && lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
                    val resistance = 0.45f
                    coroutineScope.launch { overscrollOffset.snapTo(overscrollOffset.value + (available.y * resistance)) }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
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
    val scrollY = if (lazyListState.firstVisibleItemIndex == 0) {
        lazyListState.firstVisibleItemScrollOffset.toFloat()
    } else {
        headerHeightPx // Cap the value if we've scrolled past the first item
    }
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

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalHeaderHeight = headerHeight + statusBarHeight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AxiomTheme.colors.background)
//            .background(Color(0xFFF3F4F6)) //from drible -> Color(0xFFF6F6F6)
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // Drops focus, hides keyboard
                })
            }
    ) {
        // --- 1. MAIN SCROLLABLE CONTENT ---
        LazyColumn(
            state = lazyListState,

            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
                .graphicsLayer { translationY = overscrollOffset.value }
        ) {
            item(key = "large_title_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 80.dp, bottom = 16.dp)
                        .graphicsLayer {
                            alpha = largeTitleOpacity
                            scaleX = zoomScale
                            scaleY = zoomScale
                            transformOrigin = TransformOrigin(0f, 1f)
                        }
                ) {
                    Column(
                        modifier = if (onHeaderClick != null) {
                            Modifier.clickable { onHeaderClick() }
                        } else {
                            Modifier
                        }
                    ) {
                        Text(
                            text = largeTitle,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp,
                            color = AxiomTheme.colors.textPrimary
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
                }
            }
            if (isParentRoute) {
                item(key = "search_bar_header") {
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
                }
            }



            content()
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(100.dp)) }


        }

// --- 2. FIXED TOP HEADER (Glass & Title) ---
        if (smallHeaderOpacity > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeaderHeight)
                    .zIndex(100f)
                    .graphicsLayer { alpha = smallHeaderOpacity }
            ) {
                // THE BLURRED BACKGROUND WITH EASED FEATHERING
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .hazeChild(
                            state = hazeState,
                            // To look like iOS, you need a high blur radius.
                            // Ensure your Haze configuration is using at least 20.dp - 30.dp
                        )
                        // This tint replicates the iOS dark mode translucent material
                        .background(Color(0x801C1C1E))
                        .drawWithContent {
                            drawContent()

                            // This array mimics an iOS-style cubic bezier easing curve
                            // It eliminates the ugly, harsh linear banding.
                            val smoothGradientStops = arrayOf(
                                0.0f to Color.Black,
                                0.55f to Color.Black,                     // Solid behind the status bar
                                0.70f to Color.Black.copy(alpha = 0.85f), // Gentle falloff begins
                                0.85f to Color.Black.copy(alpha = 0.45f), // Midpoint of the curve
                                0.95f to Color.Black.copy(alpha = 0.10f), // Very soft tail
                                1.0f to Color.Transparent                 // Completely fades out
                            )

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colorStops = smoothGradientStops,
                                    startY = 0f,
                                    endY = size.height
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                )

                // THE CRISP TEXT
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = statusBarHeight)
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
        if (isParentRoute) {
            val yellow = Color(0xFFFFCC00) // Assuming your original yellow color

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

                    // --- LEFT SIDE: Back vs Cancel ---
                    AnimatedContent(
                        targetState = isSelectionMode,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "LeftHeaderAction"
                    )
//                    { selectionActive ->
//                        if (selectionActive) {
//                            // Cancel Button
//                            Text(
//                                text = "Cancel",
//                                color = yellow,
//                                fontSize = 17.sp,
//                                modifier = Modifier
//                                    .padding(start = 16.dp)
//                                    .clip(RoundedCornerShape(8.dp))
//                                    .clickable { onToggleSelectionMode() } // Usually toggles isSelectionMode to false
//                                    .padding(vertical = 8.dp)
//                            )
//                        } else {
//                            // Back Button with Icon
//                            Row(
//                                modifier = Modifier
//                                    .padding(start = 8.dp)
//                                    .clip(RoundedCornerShape(8.dp))
//                                    .clickable { onBack() }
//                                    .padding(horizontal = 4.dp, vertical = 8.dp),
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(2.dp)
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.back_ios),
//                                    contentDescription = "Back",
//                                    tint = yellow,
//                                    modifier = Modifier.size(24.dp)
//                                )
//                                Text(
//                                    text = backText,
//                                    color = yellow,
//                                    fontSize = 17.sp,
//                                    letterSpacing = (-0.2).sp,
//                                    modifier = Modifier.offset(x = (-5).dp)
//                                )
//                            }
//                        }
//                    }

                    { selectionActive ->

                        when {
                            showBack -> {
                                // Back (forced)
                                Row(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onBack() }
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
                                        text = backText,
                                        color = yellow,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp,
                                        modifier = Modifier.offset(x = (-5).dp)
                                    )
                                }
                            }

                            selectionActive -> {
                                // Cancel
                                Text(
                                    text = "Cancel",
                                    color = yellow,
                                    fontSize = 17.sp,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onToggleSelectionMode() }
                                        .padding(vertical = 8.dp)
                                )
                            }

                            else -> {
                                // Default Back
                                Row(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onBack() }
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
                                        text = backText,
                                        color = yellow,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp,
                                        modifier = Modifier.offset(x = (-5).dp)
                                    )
                                }
                            }
                        }
                    }

                    // --- RIGHT SIDE: The Pill Menu ---
                    TopRightPillMenu(
                        isSelectionMode = isSelectionMode,
                        selectionCount = selectionCount,
                        onAddClick = onAddClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onThirdOptionClick = { onThirdOptionClick() }
                    )
                }
            }
        }
    }
}

