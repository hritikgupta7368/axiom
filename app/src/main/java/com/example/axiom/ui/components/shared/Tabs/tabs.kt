package com.example.axiom.ui.components.shared.Tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp


// --- Data Class for Tabs ---
data class TabItem(
    val title: String,
    val icon: ImageVector? = null,
    val showTitle: Boolean = true,
    val content: @Composable () -> Unit
)

@Composable
fun AnimatedTopTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0xFF8B8B8B),
    backgroundColor: Color = Color.Transparent
) {
    Column(modifier = modifier.fillMaxSize()) {

        // --- Tab Bar ---
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = backgroundColor,
            edgePadding = 20.dp, // Matches RN paddingHorizontal: 20
            divider = {}, // Removes the default bottom divider line
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = activeColor,
                        height = 2.dp
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val isActive = index == selectedIndex

                // Animated values for Scale, Opacity, and Y-Offset
                val progress by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0f,
                    animationSpec = tween(durationMillis = 250),
                    label = "TabProgress"
                )

                val scale = lerp(0.92f, 1f, progress)
                val yOffset = lerp(2f, 0f, progress).dp
                val opacity = lerp(0.4f, 1f, progress)

                // Custom Tab Button
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Removes ripple for custom animation
                            onClick = { onTabSelected(index) }
                        )
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .scale(scale)
                            .offset(y = yOffset)
                            .alpha(opacity)
                    ) {
                        if (tab.icon != null) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isActive) activeColor else inactiveColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (tab.showTitle) {
                            Text(
                                text = tab.title,
                                color = if (isActive) activeColor else inactiveColor,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        // --- Content Area (Lag-Free & Slide Animated) ---
        // AnimatedContent removes inactive tabs from memory completely.
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                // Determine slide direction based on index change
                if (targetState > initialState) {
                    // Slide left
                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                } else {
                    // Slide right
                    slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                }
            },
            modifier = Modifier.weight(1f),
            label = "ContentAnimation"
        ) { targetIndex ->
            // Render only the active tab's content
            Box(modifier = Modifier.fillMaxSize()) {
                tabs[targetIndex].content()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    // 1. Hoisted State: You can control this from a ViewModel or Route
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 2. Define the tabs
    val tabs = listOf(
        TabItem(
            title = "For You",
            icon = Icons.Default.Info,
            content = { ForYouContent() }
        ),
        TabItem(
            title = "Trending",
            icon = Icons.Default.PlayArrow,
            content = { TrendingContent() }
        ),
        TabItem(
            title = "New",
            icon = Icons.Default.Add,
            showTitle = false, // Icon only, just like your RN code
            content = { NewContent() }
        )
    )

    // 3. Layout Wrapper
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Dark background
    ) {
        // Header
        Text(
            text = "Explore",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, top = 60.dp, bottom = 16.dp)
        )

        // The Tabs Component
        AnimatedTopTabs(
            tabs = tabs,
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            backgroundColor = Color.Transparent
        )
    }
}

// ==========================================
// MOCK CONTENT LAYOUTS (Matching your preview)
// ==========================================

@Composable
fun ForYouContent() {
    // LazyColumn is the equivalent of FlatList. It recycles views for zero lag.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Form Header
        item {
            Text(
                text = "System Configuration",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Generate 50 dummy form sections
        items(50) { index ->
            // rememberSaveable ensures the state isn't lost when the item scrolls off-screen
            var textValue by rememberSaveable { mutableStateOf("") }
            var isToggled by rememberSaveable { mutableStateOf(false) }
            var sliderValue by rememberSaveable { mutableStateOf(0.5f) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(14.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Title
                Text(
                    text = "Module block ${index + 1}",
                    color = Color(0xFFA78BFA), // The purple accent from your RN code
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // 1. Text Input
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("API Endpoint or Key", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA78BFA),
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // 2. Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Enable Webhooks", color = Color.White, fontSize = 16.sp)
                        Text("Sends payload on events", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isToggled,
                        onCheckedChange = { isToggled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFA78BFA)
                        )
                    )
                }

                // 3. Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Timeout limit", color = Color.White, fontSize = 14.sp)
                        Text(
                            "${(sliderValue * 100).toInt()}s",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFA78BFA),
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )
                }
            }
        }

        // Form Footer
        item {
            Button(
                onClick = { /* Save action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA))
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TrendingContent() {
    val items = listOf("Design", "Code", "Music")
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { i, text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${i + 1}",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun NewContent() {
    val items = listOf("Today", "This Week", "This Month")
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { i, text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            Color(0xFFFBBF24).copy(alpha = 1f - (i * 0.3f)),
                            RoundedCornerShape(4.dp)
                        )
                )
                Text(text, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}