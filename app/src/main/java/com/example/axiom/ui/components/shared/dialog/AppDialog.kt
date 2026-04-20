package com.example.axiom.ui.components.shared.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // Allows us to use custom width/padding
                dismissOnBackPress = true,
                dismissOnClickOutside = false // Handled manually below for animation sync
            )
        ) {
            // Full screen backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.40f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    ),
                contentAlignment = Alignment.Center
            ) {
                // The Animated visibility wrapper matches your React Native timings & scales
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(550)) + scaleIn(
                        initialScale = 0.6f,
                        animationSpec = tween(550)
                    ),
                    exit = fadeOut(tween(650)) + scaleOut(
                        targetScale = 0.85f,
                        animationSpec = tween(650)
                    )
                ) {
                    // Hook into the transition to extract custom 3D rotation
                    val rotateX by transition.animateFloat(
                        transitionSpec = {
                            if (targetState == EnterExitState.Visible) tween(550) else tween(650)
                        },
                        label = "3D Rotation"
                    ) { state ->
                        when (state) {
                            EnterExitState.PreEnter -> -55f
                            EnterExitState.Visible -> 0f
                            EnterExitState.PostExit -> -25f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp) // apply here
                            .graphicsLayer {
                                rotationX = rotateX
                                cameraDistance = 1000f // Sets perspective for the 3D pop
                            }
                            // Stop clicks on the dialog from bubbling to the backdrop
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DialogPreviewScreen() {
    var isDialogOpen by remember { mutableStateOf(false) }

    // Fake App Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        // --- TRIGGER BUTTON ---
        IconButton(
            onClick = { isDialogOpen = true },
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Trigger Dialog",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // --- THE DIALOG ---
        AppDialog(
            visible = isDialogOpen,
            onDismissRequest = { isDialogOpen = false }
        ) {
            // Dialog Content Layout
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 24.dp) // Limits width on large screens
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Red Icon Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0x26FF6B6B), CircleShape), // rgba(255,107,107,0.15)

                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Typography
                Text(
                    text = "Delete item?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "This action cannot be undone.",
                    color = Color(0xFF8E8E93),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        text = "Cancel",
                        onClick = { isDialogOpen = false },
                        variant = ButtonVariant.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        text = "Delete",
                        onClick = {
                            // Perform delete logic here
                            isDialogOpen = false
                        },
                        variant = ButtonVariant.Red,
                        icon = Icons.Default.Delete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
