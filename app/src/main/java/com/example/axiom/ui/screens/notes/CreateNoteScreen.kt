package com.example.axiom.ui.screens.notes


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.notes.NoteEditorViewModel
import com.example.axiom.data.notes.NoteEditorViewModelFactory
import com.example.axiom.data.notes.SaveState
import com.example.axiom.ui.components.shared.colorPicker.ColorPicker


@Composable
private fun SaveStateText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Composable
fun TransparentTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(),
    fontSize: TextUnit,
    textColor: Color,
    selectionColor: Color,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions()
) {
    val selectionColors = TextSelectionColors(
        handleColor = selectionColor,
        backgroundColor = selectionColor.copy(alpha = 0.4f)
    )


    CompositionLocalProvider(
        LocalTextSelectionColors provides selectionColors
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(textColor),
            textStyle = textStyle.copy(
                color = textColor,
                fontSize = fontSize
            )
        ) { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isBlank()) {
                    Text(
                        text = hint,
                        modifier = Modifier.alpha(0.5f),
                        style = textStyle.copy(
                            color = textColor,
                            fontSize = fontSize
                        )
                    )
                }
                innerTextField()
            }
        }
    }
}


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
    val color by viewModel.color.collectAsState()
    var showColorPicker by remember { mutableStateOf(true) }
    val snackBarHostState = remember { SnackbarHostState() }



    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Note",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = saveState,
                                transitionSpec = {
                                    slideInVertically { it / 2 } + fadeIn() togetherWith
                                            slideOutVertically { -it / 2 } + fadeOut()
                                },
                                label = "SaveState"
                            ) { state ->
                                when (state) {
                                    SaveState.LOADING -> SaveStateText("Loading…")
                                    SaveState.SAVING -> SaveStateText("Saving…")
                                    SaveState.SAVED -> SaveStateText("Saved")
                                    SaveState.READY -> Spacer(Modifier.width(1.dp))
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            IconButton(
                                onClick = { showColorPicker = !showColorPicker }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )


            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            AnimatedVisibility(
                visible = showColorPicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ColorPicker(
                    selectedBackground = color,
                    onColorPicked = { bg, _ ->
                        bg?.let(viewModel::onColorChange)
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            TransparentTextField(
                value = title,
                hint = "Title",
                onValueChange = viewModel::onTitleChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 25.sp,
                selectionColor = Color(0xFF6366F1),
                textColor = Color.White
            )



            Spacer(modifier = Modifier.height(16.dp))

            TransparentTextField(
                value = content,
                hint = "Text",
                onValueChange = viewModel::onContentChange,
                singleLine = false,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 16.sp,
                selectionColor = Color(0xFF6366F1),
                textColor = Color.White
            )

        }
    }
}
