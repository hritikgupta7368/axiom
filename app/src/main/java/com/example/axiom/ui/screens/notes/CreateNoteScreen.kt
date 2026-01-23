package com.example.axiom.ui.screens.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.notes.NoteEditorViewModel
import com.example.axiom.data.notes.NoteEditorViewModelFactory
import com.example.axiom.data.notes.SaveState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: NoteEditorViewModel = viewModel(
        factory = NoteEditorViewModelFactory(
            context = context,
            noteId = noteId
        )
    )

    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val saveState by viewModel.saveState.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Note", style = MaterialTheme.typography.titleLarge)

                        when (saveState) {
                            SaveState.LOADING -> Text("Loading…", fontSize = 12.sp)
                            SaveState.SAVING -> Text("Saving…", fontSize = 12.sp)
                            SaveState.SAVED -> Text("Saved", fontSize = 12.sp)
                            SaveState.READY -> {}
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content field (multiline, main editor)
            OutlinedTextField(
                value = content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxSize(),
                placeholder = { Text("Start writing…") },
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}
