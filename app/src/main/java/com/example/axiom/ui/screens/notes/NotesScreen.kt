package com.example.axiom.ui.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.notes.NoteEntity
import com.example.axiom.data.notes.NotesViewModel
import com.example.axiom.data.notes.NotesViewModelFactory
import kotlinx.coroutines.launch

// Color Definitions
private val NeonPink = Color(0xFFFF00CC)
private val NeonPurple = Color(0xFFAA00FF)
private val NeonCyan = Color(0xFF00FFFF)
private val NeonLime = Color(0xFFCCFF00)
private val NeonOrange = Color(0xFFFF6600)
private val BackgroundDark = Color(0xFF000000)
private val CardDark = Color(0xFF111111)
private val CardGray = Color(0xFF1A1A1A)


private val NoteAccentColors = listOf(
    NeonPink,
    NeonPurple,
    NeonCyan,
    NeonLime,
    NeonOrange
)


@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit
) {
    val accentColor = remember(note.id) {
        NoteAccentColors[(note.id % NoteAccentColors.size).toInt()]
    }

    val maxLines = remember(note.id) {
        listOf(4, 6, 8, 12).random()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
//            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardGray.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                // TITLE (optional height impact)
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(8.dp))
                }

                // CONTENT (this drives height)
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBDBDBD),
                    lineHeight = 16.sp,
                    maxLines = maxLines,              // KEY: variable but bounded
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: NotesViewModel = viewModel(
        factory = NotesViewModelFactory(context)
    )

    val notes by viewModel.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }





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




            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = notes,
                    key = { it.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onOpenNote(note.id) }
                    )
                }
            }


        }
        FloatingActionButton(
            onClick = {
                scope.launch {
                    val noteId = viewModel.createNewNote()
                    onOpenNote(noteId)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
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

