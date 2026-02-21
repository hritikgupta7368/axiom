package com.example.axiom.ui.screens.home

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.axiom.BuildConfig
import com.example.axiom.ui.components.shared.Aurora // Ensure this import points to your new Aurora component
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    Scaffold(
        containerColor = Color.Transparent, // Make Scaffold transparent so edge-to-edge works
    ) { padding ->

        // Parent Box fills max size WITHOUT Scaffold padding
        // This allows the background to draw behind the status bar and navigation bar.
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Background Layer: Aurora for API 33+, Circles for older devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Aurora(
                    modifier = Modifier.fillMaxSize(), // Fills entire screen including status bar
                    speed = 0.5f,
                    intensity = 1f
                )
            } else {
                // Fallback gradient base + animated circles
                val fallbackGradient = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                        Color(0xFF020308) // Match Aurora dark sky color
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fallbackGradient)
                ) {
                    AnimatedGradientCircles()
                }
            }

            // Foreground Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Apply Scaffold padding HERE so content respects system bars
                    .padding(24.dp),  // Your original screen padding
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                HeaderBlock(visible)

                GlassSystemStateCard(visible)

                Spacer(Modifier.height(96.dp))
            }
        }
    }
}

/* ───────────────────────── HEADER ───────────────────────── */

@Composable
private fun HeaderBlock(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "AXIOM" + " v" + BuildConfig.VERSION_CODE,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White // Forced white to pop against dark Aurora background
            )
            Text(
                text = "Control · Records · Memory",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/* ───────────────────── GLASS SYSTEM CARD ───────────────────── */

@Composable
private fun GlassSystemStateCard(visible: Boolean) {

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(700, delayMillis = 200)) +
                scaleIn(tween(700, delayMillis = 200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .blur(20.dp)
            )

            Card(
                modifier = Modifier.matchParentSize(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "System State",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "All systems stable\nNo pending actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        text = "Last sync: moments ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/* ─────────────────── ANIMATED BACKGROUND FALLBACK ─────────────────── */

@Composable
private fun AnimatedGradientCircles() {

    val transition = rememberInfiniteTransition(label = "bg")

    val scale1 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "scale1"
    )

    val scale2 by transition.animateFloat(
        initialValue = 1.1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            tween(16000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "scale2"
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .scale(scale1)
            .offset(x = 140.dp, y = (-120).dp)
            .blur(90.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(260.dp)
            .scale(scale2)
            .offset(x = (-140).dp, y = 220.dp)
            .blur(100.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}