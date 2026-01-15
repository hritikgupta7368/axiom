package com.example.axiom.ui.screens.finances.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.button.AppButton
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Purchase Screen") },
                navigationIcon = {


                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "AppIconButton variants", modifier = Modifier.padding(bottom = 16.dp))

            // Simple icon button


        }
    }
}