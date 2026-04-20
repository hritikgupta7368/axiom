package com.example.axiom.ui.components.shared.CheckBox

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ==========================================
// TOP LEVEL COLORS & CONSTANTS
// ==========================================
val DefaultCheckmarkColor = Color(1f, 1f, 1f, 0.12f) // Classic vibrant blue

// Sizes mimicking your sm/md requirement
enum class CheckboxSize(
    val checkbox: Dp,
    val box: Dp
) {
    SM(20.dp, 32.dp),
    MD(24.dp, 30.dp), //36
    LG(60.dp, 44.dp)
}

private const val PADDING = 10f
private const val VIEWPORT_SIZE = 64f + PADDING
private const val TOTAL_VIEWBOX_SIZE = VIEWPORT_SIZE + PADDING // 74f total span (-10 to 64)

private const val BOX_FACTOR = 0.73f

@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    tickColor: Color = MaterialTheme.colorScheme.onSurface,
    boxColor: Color = Color(1f, 1f, 1f, 0.12f),
    strokeWidth: Float = 4.5f, // Adjusted slightly higher for Android visual parity when scaled
    size: CheckboxSize = CheckboxSize.MD,
    showBorder: Boolean = false
) {
    // === Animation States ===
    val tickProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (checked) 300 else 250,
            easing = if (checked) CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
            else CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
        ),
        label = "TickProgress"
    )

    val borderProgress by animateFloatAsState(
        targetValue = if (showBorder) 1f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = if (showBorder) CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
            else CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
        ),
        label = "BorderProgress"
    )

    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.8f,
        animationSpec = if (checked) {
            spring(
                dampingRatio = 0.6f,
                stiffness = 150f
            ) // Matches your RN spring mass/damping/stiffness
        } else {
            tween(durationMillis = 100)
        },
        label = "ScaleProgress"
    )

    // === Paths & Measurements ===
    val tickPath = remember {
        Path().apply {
            moveTo(20f, 32f)
            lineTo(28f, 40f)
            lineTo(44f, 24f)
        }
    }

    val boxPath = remember {
        Path().apply {
            moveTo(24f, 0.5f)
            lineTo(40f, 0.5f)
            cubicTo(48.5809f, 0.5f, 54.4147f, 2.18067f, 58.117f, 5.88299f)
            cubicTo(61.8193f, 9.58532f, 63.5f, 15.4191f, 63.5f, 24f)
            lineTo(63.5f, 40f)
            cubicTo(63.5f, 48.5809f, 61.8193f, 54.4147f, 58.117f, 58.117f)
            cubicTo(54.4147f, 61.8193f, 48.5809f, 63.5f, 40f, 63.5f)
            lineTo(24f, 63.5f)
            cubicTo(15.4191f, 63.5f, 9.58532f, 61.8193f, 5.88299f, 58.117f)
            cubicTo(2.18067f, 54.4147f, 0.5f, 48.5809f, 0.5f, 40f)
            lineTo(0.5f, 24f)
            cubicTo(0.5f, 15.4191f, 2.18067f, 9.58532f, 5.88299f, 5.88299f)
            cubicTo(9.58532f, 2.18067f, 15.4191f, 0.5f, 24f, 0.5f)
            close()
        }
    }

    val pathMeasure = remember { PathMeasure() }
    val pathSegment = remember { Path() }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(size.box)
            .background(
                color = Color.White.copy(alpha = 0.12f), // backgroundColor: "rgba(255,255,255,0.12)"
                shape = RoundedCornerShape(12.dp)        // borderRadius: 12
            ),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = modifier
                .size(size.checkbox)
                .clip(CircleShape)
                .let {
                    if (onCheckedChange != null) {
                        it.clickable(
                            interactionSource = interactionSource,
                            indication = null, // Set to null for silent tap, or use LocalIndication.current for standard ripple
                            role = Role.Checkbox,
                            onClick = { onCheckedChange(!checked) }
                        )
                    } else it
                }
        ) {
            // Calculate scaling to map the 74x74 viewport to our dp size
            val canvasSize = this.size.width
            val scaleFactor = canvasSize / TOTAL_VIEWBOX_SIZE

            scale(scaleX = scaleFactor, scaleY = scaleFactor, pivot = Offset.Zero) {
                // Translate accounts for the viewBox="-10 -10 ..." offset
                translate(left = PADDING, top = PADDING) {

                    // 1. Draw Animated Border Box
                    if (borderProgress > 0f) {
                        pathSegment.reset()
                        pathMeasure.setPath(boxPath, false)
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = pathMeasure.length * borderProgress,
                            destination = pathSegment,
                            startWithMoveTo = true
                        )
                        drawPath(
                            path = pathSegment,
                            color = boxColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 2. Draw Animated Tick
                    if (tickProgress > 0f) {
                        // Tick scales from center (32, 32 in the original viewport)
                        scale(scale = scale, pivot = Offset(32f, 32f)) {
                            pathSegment.reset()
                            pathMeasure.setPath(tickPath, false)
                            pathMeasure.getSegment(
                                startDistance = 0f,
                                stopDistance = pathMeasure.length * tickProgress,
                                destination = pathSegment,
                                startWithMoveTo = true
                            )
                            drawPath(
                                path = pathSegment,
                                color = tickColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedCheckPreview() {
    var checked by remember { mutableStateOf(true) }

    // === Container (matches styles.container) ===
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // backgroundColor: "#000"
            .padding(horizontal = 24.dp)   // paddingHorizontal: 24
    ) {

        // Offset mapping to your <View style={{ marginTop: 100 }}>
        Column(modifier = Modifier.padding(top = 100.dp)) {

            // === Card (matches styles.card & Pressable) ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(alpha = 0.08f), // backgroundColor: "rgba(255,255,255,0.08)"
                        shape = RoundedCornerShape(18.dp)        // borderRadius: 18
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { checked = !checked }
                    .padding(20.dp),                             // padding: 20
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // === Left Text (matches styles.left) ===
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Love Reacticx?",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap to toggle",
                        color = Color.White.copy(alpha = 0.6f), // color: "rgba(255,255,255,0.6)"
                        fontSize = 13.sp
                    )
                }

                // === Checkbox Wrapper (matches styles.checkbox) ===
                Box(
                    modifier = Modifier
                        .size(44.dp) // width: 44, height: 44
                        .background(
                            color = Color.White.copy(alpha = 0.12f), // backgroundColor: "rgba(255,255,255,0.12)"
                            shape = RoundedCornerShape(12.dp)        // borderRadius: 12
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    // === The Checkbox itself ===
                    AnimatedCheckbox(
                        checked = checked,
                        onCheckedChange = null, // Handled by the parent Card's clickable
//                        checkmarkColor = Color.White,
                        strokeWidth = 5.5f,
                        size = CheckboxSize.LG, // Set to 60.dp to match your size={60}
                        showBorder = false // Border is hidden since the wrapper Box acts as the background
                    )
                }
            }

            AnimatedCheckbox(
                checked = checked,
                onCheckedChange = null, // Handled by the parent Card's clickable
//                        checkmarkColor = Color.White,
                strokeWidth = 5.5f,
                size = CheckboxSize.LG, // Set to 60.dp to match your size={60}
                showBorder = false // Border is hidden since the wrapper Box acts as the background
            )
        }
    }
}