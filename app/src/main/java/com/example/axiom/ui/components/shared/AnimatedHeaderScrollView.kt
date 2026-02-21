package com.example.axiom.ui.components.shared

import android.os.Build
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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

    // 1. OVERSCROLL TRACKING FOR iOS RUBBER-BANDING
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If we are currently stretched and dragging up, consume the drag to un-stretch first
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
                // When we drag down past the top of the list, accumulate the stretch
                if (available.y > 0f) {
                    val resistance = 0.45f // iOS-like drag resistance
                    coroutineScope.launch {
                        overscrollOffset.snapTo(overscrollOffset.value + (available.y * resistance))
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Spring bounce back to 0 when the user releases their finger
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

    // 2. SCROLL ANIMATION CALCULATIONS
    val headerHeightPx = with(density) { 90.dp.toPx() }
    val scrollY = scrollState.value
    val normalizedScroll = (scrollY / headerHeightPx).coerceIn(0f, 1f)

    // Large title fades out as you scroll down
    val largeTitleOpacity = 1f - (normalizedScroll * 1.5f).coerceIn(0f, 1f)

    // Small sticky header fades in
    val smallHeaderOpacity =
        if (normalizedScroll > 0.6f) ((normalizedScroll - 0.6f) * 2.5f).coerceIn(0f, 1f) else 0f

    // Scale calculation based on the overscroll drag
    val zoomScale = 1f + (overscrollOffset.value / 800f).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .nestedScroll(nestedScrollConnection) // Attach the rubber-banding gesture
    ) {

        // --- 1. MAIN SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                // THIS FIXES THE DRAG: Translates the ENTIRE list (title + cards) down when pulling
                .graphicsLayer { translationY = overscrollOffset.value }
        ) {

            // Large Title Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp, bottom = 16.dp) // Tightened top space
                    .graphicsLayer {
                        alpha = largeTitleOpacity
                        scaleX = zoomScale
                        scaleY = zoomScale
                        // Anchor the stretch to the bottom-left so it grows upwards and outwards
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

            // Cards Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                content() // Your Schedule/Tasks cards go here
            }

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding so last item is scannable
        }

        // --- 2. FIXED TOP BLUR HEADER ---
        // Placing this AT THE END of the Box + adding zIndex ensures it ALWAYS covers the cards
        if (smallHeaderOpacity > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .zIndex(10f)
                    .graphicsLayer { alpha = smallHeaderOpacity }
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(20.dp, BlurredEdgeTreatment.Unbounded)
                        } else {
                            Modifier.background(Color.Black.copy(alpha = 0.85f))
                        }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = largeTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}