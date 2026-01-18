package com.example.axiom.ui.screens.notes

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// Color Definitions
private val NeonPink = Color(0xFFFF00CC)
private val NeonPurple = Color(0xFFAA00FF)
private val NeonCyan = Color(0xFF00FFFF)
private val NeonLime = Color(0xFFCCFF00)
private val NeonOrange = Color(0xFFFF6600)
private val BackgroundDark = Color(0xFF000000)
private val CardDark = Color(0xFF111111)
private val CardGray = Color(0xFF1A1A1A)

// Data Models
sealed class NoteType {
    data class Text(val content: String, val tags: List<String> = emptyList(), val category: String? = null) : NoteType()
    data class Checklist(val title: String, val items: List<ChecklistItem>) : NoteType()
    data class Design(val title: String, val description: String, val colors: List<Color>) : NoteType()
    data class Secure(val title: String, val keys: List<String>) : NoteType()
    data class Event(val title: String, val time: String, val description: String) : NoteType()
}

data class ChecklistItem(val text: String, val isChecked: Boolean)

data class Note(
    val id: String,
    val type: NoteType,
    val timestamp: Long,
    val borderColor: Color
)

// Dummy Data
private fun getDummyNotes(): List<Note> = listOf(
    Note(
        id = "1",
        type = NoteType.Text(
            content = "Q4 Marketing Strategy\n\nFocus on user retention loops. Need to draft the email sequence for the new onboarding flow.\n\nKey metrics:\n- Activation rate\n- Day 7 retention",
            tags = listOf("#work", "#priority"),
            category = "Project Alpha"
        ),
        timestamp = System.currentTimeMillis() - 3600000,
        borderColor = Color(0xFF3B82F6)
    ),
    Note(
        id = "2",
        type = NoteType.Checklist(
            title = "Groceries",
            items = listOf(
                ChecklistItem("Almond Milk", true),
                ChecklistItem("Avocados (3)", false),
                ChecklistItem("Sourdough Bread", false),
                ChecklistItem("Espresso Beans", false)
            )
        ),
        timestamp = System.currentTimeMillis() - 7200000,
        borderColor = NeonLime
    ),
    Note(
        id = "3",
        type = NoteType.Design(
            title = "Neon UI Vibe",
            description = "Color palette inspiration for the dashboard redesign.",
            colors = listOf(NeonPink, NeonPurple, NeonCyan)
        ),
        timestamp = System.currentTimeMillis() - 10800000,
        borderColor = NeonPurple
    ),
    Note(
        id = "4",
        type = NoteType.Secure(
            title = "API Keys",
            keys = listOf("sk_live_51M...xYz", "pk_test_89K...aBc")
        ),
        timestamp = System.currentTimeMillis() - 14400000,
        borderColor = NeonCyan
    ),
    Note(
        id = "5",
        type = NoteType.Event(
            title = "Design Sync",
            time = "Today, 2:00 PM",
            description = "Review wireframes with the product team. Bring updated user flow diagrams."
        ),
        timestamp = System.currentTimeMillis() - 18000000,
        borderColor = NeonPink
    )
)

@Composable
fun NotesScreen(
    onBack: () -> Unit
) {
    var notes by remember { mutableStateOf(getDummyNotes()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNoteType by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Gradient Blobs
        Box(
            modifier = Modifier
                .offset(y = 80.dp)
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 160.dp)
                .blur(100.dp)
                .background(NeonCyan.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-160).dp)
                .blur(120.dp)
                .background(NeonPink.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Search
            NotesHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBack = onBack
            )

            // Notes Grid
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                items(notes.filter {
                    if (searchQuery.isEmpty()) true
                    else when (val type = it.type) {
                        is NoteType.Text -> type.content.contains(searchQuery, ignoreCase = true) ||
                                type.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
                        is NoteType.Checklist -> type.title.contains(searchQuery, ignoreCase = true) ||
                                type.items.any { item -> item.text.contains(searchQuery, ignoreCase = true) }
                        is NoteType.Design -> type.title.contains(searchQuery, ignoreCase = true)
                        is NoteType.Secure -> type.title.contains(searchQuery, ignoreCase = true)
                        is NoteType.Event -> type.title.contains(searchQuery, ignoreCase = true) ||
                                type.description.contains(searchQuery, ignoreCase = true)
                    }
                }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { /* Handle note click */ },
                        onToggleChecklistItem = { itemIndex ->
                            notes = notes.map {
                                if (it.id == note.id && it.type is NoteType.Checklist) {
                                    val checklist = it.type
                                    val updatedItems = checklist.items.mapIndexed { index, item ->
                                        if (index == itemIndex) item.copy(isChecked = !item.isChecked)
                                        else item
                                    }
                                    it.copy(type = checklist.copy(items = updatedItems))
                                } else it
                            }
                        }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(56.dp),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
        ) {

            val rotation = 0f



            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonPink, NeonPurple)
                        ),
                        shape = CircleShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(rotation)
                )
            }
        }

        // Add Note Dialog
//        if (showAddDialog) {
//            AddNoteDialog(
//                onDismiss = { showAddDialog = false },
//                onAddNote = { noteType ->
//                    val newNote = Note(
//                        id = UUID.randomUUID().toString(),
//                        type = noteType,
//                        timestamp = System.currentTimeMillis(),
//                        borderColor = when (noteType) {
//                            is NoteType.Text -> Color(0xFF3B82F6)
//                            is NoteType.Checklist -> NeonLime
//                            is NoteType.Design -> NeonPurple
//                            is NoteType.Secure -> NeonCyan
//                            is NoteType.Event -> NeonPink
//                        }
//                    )
//                    notes = listOf(newNote) + notes
//                    showAddDialog = false
//                }
//            )
//        }
        if (showAddDialog) {
            AddNoteDialog(
                onDismiss = { showAddDialog = false },
                onAddNote = { newNote -> // The lambda now directly receives the fully constructed 'newNote'
                    notes = listOf(newNote) + notes
                    showAddDialog = false
                }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.8f))
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Notes",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = { /* Settings */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(CardGray, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isEmpty()) Color.Gray else NeonPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search ideas, lists, tags...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onToggleChecklistItem: (Int) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardGray.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, note.borderColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            when (val type = note.type) {
                is NoteType.Text -> TextNoteContent(type)
                is NoteType.Checklist -> ChecklistNoteContent(type, onToggleChecklistItem)
                is NoteType.Design -> DesignNoteContent(type)
                is NoteType.Secure -> SecureNoteContent(type)
                is NoteType.Event -> EventNoteContent(type)
            }
        }
    }
}

@Composable
private fun TextNoteContent(note: NoteType.Text) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        if (note.category != null) {
            Text(
                text = note.category.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = note.content.lines().first(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = note.content.lines().drop(1).joinToString("\n"),
            fontSize = 11.sp,
            color = Color.Gray,
            lineHeight = 16.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        if (note.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                note.tags.forEach { tag ->
                    Text(
                        text = tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF60A5FA),
                        modifier = Modifier
                            .background(
                                Color(0xFF3B82F6).copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                Color(0xFF3B82F6).copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistNoteContent(
    note: NoteType.Checklist,
    onToggleItem: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NeonLime, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        note.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onToggleItem(index) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (item.isChecked) NeonLime else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (item.isChecked) NeonLime else Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.text,
                    fontSize = 13.sp,
                    color = if (item.isChecked) Color.Gray else Color.White,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                )
            }
        }
    }
}

@Composable
private fun DesignNoteContent(note: NoteType.Design) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonPink, Color(0xFF333399))
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DESIGN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = note.description,
                fontSize = 11.sp,
                color = NeonPurple.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                note.colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureNoteContent(note: NoteType.Secure) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        note.keys.forEach { key ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = key,
                    fontSize = 9.sp,
                    color = NeonCyan.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Light
                )
            }
        }

        Text(
            text = "Updated 2h ago",
            fontSize = 9.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 12.dp)
        )
    }
}

@Composable
private fun EventNoteContent(note: NoteType.Event) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NeonPink.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = NeonPink
                )
            }

            Column {
                Text(
                    text = note.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = note.time,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = note.description,
            fontSize = 11.sp,
            color = Color(0xFFD1D5DB),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAddNote: (Note) -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var textContent by remember { mutableStateOf("") }
    var checklistTitle by remember { mutableStateOf("") }
    var checklistItems by remember { mutableStateOf(listOf("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text(
                text = "Create New Note",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Note Type Selection
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteTypeButton("Text", Icons.Default.Create, selectedType == "Text") {
                        selectedType = "Text"
                    }
                    NoteTypeButton("Checklist", Icons.Default.CheckCircle, selectedType == "Checklist") {
                        selectedType = "Checklist"
                    }
                }

                // Input based on type
                when (selectedType) {
                    "Text" -> {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            label = { Text("Note content", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonPink,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                    "Checklist" -> {
                        OutlinedTextField(
                            value = checklistTitle,
                            onValueChange = { checklistTitle = it },
                            label = { Text("List title", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        checklistItems.forEachIndexed { index, item ->
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    checklistItems = checklistItems.toMutableList().apply {
                                        this[index] = newValue
                                    }
                                },
                                label = { Text("Item ${index + 1}", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        TextButton(
                            onClick = { checklistItems = checklistItems + "" },
                            colors = ButtonDefaults.textButtonColors(contentColor = NeonLime)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }
                }
            }
        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    when (selectedType) {
//                        "Text" -> if (textContent.isNotEmpty()) {
//                            onAddNote(NoteType.Text(textContent))
//                        }
//                        "Checklist" -> if (checklistTitle.isNotEmpty() && checklistItems.any { it.isNotEmpty() }) {
//                            onAddNote(
//                                NoteType.Checklist(
//                                    title = checklistTitle,
//                                    items = checklistItems.filter { it.isNotEmpty() }
//                                        .map { ChecklistItem(it, false) }
//                                )
//                            )
//                        }
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = NeonPink,
//                    contentColor = Color.White
//                ),
//                enabled = selectedType != null
//            ) {
//                Text("Create")
//            }
//        },
        confirmButton = {
            Button(
                onClick = {
                    val noteType: NoteType? = when (selectedType) {
                        "Text" -> if (textContent.isNotEmpty()) {
                            NoteType.Text(content = textContent)
                        } else null
                        "Checklist" -> if (checklistTitle.isNotEmpty() && checklistItems.any { it.isNotEmpty() }) {
                            NoteType.Checklist(
                                title = checklistTitle,
                                items = checklistItems
                                    .filter { it.isNotEmpty() }
                                    .map { ChecklistItem(it, false) }
                            )
                        } else null
                        else -> null
                    }

                    noteType?.let {
                        // Create a full Note object before passing it
                        val newNote = Note(
                            id = System.currentTimeMillis().toString(), // A simple way to generate a unique ID
                            type = it,
                            borderColor = Color.Gray, // Provide a default color,
                            timestamp = System.currentTimeMillis()
                        )
                        onAddNote(newNote)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPink,
                    contentColor = Color.White
                ),
                enabled = selectedType != null &&
                        (selectedType == "Text" && textContent.isNotEmpty() ||
                                selectedType == "Checklist" && checklistTitle.isNotEmpty())
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NoteTypeButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) NeonPink.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (isSelected) NeonPink else Color.Gray
        ),
        border = BorderStroke(1.dp, if (isSelected) NeonPink else Color.Gray),
        modifier = Modifier.height(40.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}