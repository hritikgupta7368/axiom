package com.example.axiom.ui.components.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

@Composable
fun AnimatedHeaderScrollView(
    largeTitle: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 1. HAZE STATE: The camera that captures the background
    val hazeState = remember { HazeState() }

    // 2. OVERSCROLL TRACKING FOR iOS RUBBER-BANDING
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollOffset.value > 0f && available.y < 0f) {
                    val consumed = available.y.coerceAtLeast(-overscrollOffset.value)
                    coroutineScope.launch {
                        overscrollOffset.snapTo(overscrollOffset.value + consumed)
                    }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    val resistance = 0.45f // iOS drag resistance
                    coroutineScope.launch {
                        overscrollOffset.snapTo(overscrollOffset.value + (available.y * resistance))
                    }
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

    // 3. SCROLL ANIMATION CALCULATIONS
    val headerHeightPx = with(density) { 90.dp.toPx() }
    val scrollY = scrollState.value
    val normalizedScroll = (scrollY / headerHeightPx).coerceIn(0f, 1f)

    val largeTitleOpacity = 1f - (normalizedScroll * 1.5f).coerceIn(0f, 1f)
    val smallHeaderOpacity =
        if (normalizedScroll > 0.6f) ((normalizedScroll - 0.6f) * 2.5f).coerceIn(0f, 1f) else 0f
    val zoomScale = 1f + (overscrollOffset.value / 800f).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Apply Haze to the ROOT box so it can "see" the scrolling list
            .haze(hazeState)
            .nestedScroll(nestedScrollConnection)
    ) {

        // --- 1. MAIN SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .graphicsLayer { translationY = overscrollOffset.value }
        ) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                content()
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- 2. FIXED TOP HEADER (WITH TRUE BACKDROP BLUR) ---
        if (smallHeaderOpacity > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .zIndex(100f)
//                    .graphicsLayer { alpha = smallHeaderOpacity }
                    .alpha(smallHeaderOpacity)

            ) {

                // LAYER A: The Frosted Glass (Blurs the background)
                Box(
                    modifier = Modifier
                        .matchParentSize()
//                        .graphicsLayer { clip = false }
                        .hazeChild(state = hazeState)
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // LAYER B: The Crisp Text (Sits safely on top)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(200f),
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
    }
}