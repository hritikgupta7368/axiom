package com.example.axiom

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.axiom.ui.navigation.RootScaffold
import com.example.axiom.ui.theme.AxiomTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.axiom.ui.screens.calendar.CalendarScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AxiomTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
//                    CalendarScreen(
//                        modifier = Modifier.padding(padding)
//                    )
//                }


                    val navController = rememberNavController()
                    RootScaffold(navController)


            }
        }
    }
}

@Composable
fun TestScreen(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Firestore setup
    val db = FirebaseFirestore.getInstance()
    
    // Room setup
    val database = AppDatabase.getDatabase(context)
    val localUsers by database.userDao().getAll().collectAsState(initial = emptyList())
    
    // DataStore setup
    val userPreferences = UserPreferences(context)
    val savedName by userPreferences.userName.collectAsState(initial = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Firestore Test", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            if (name.isNotEmpty() && email.isNotEmpty()) {
                val user = User(name, email)
                db.collection("users")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        statusMessage = "Firestore: Added with ID: ${documentReference.id}"
                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        statusMessage = "Firestore Error: $e"
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Enter name and email", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Add to Firestore")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Room DB Test", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            if (name.isNotEmpty() && email.isNotEmpty()) {
                scope.launch {
                    database.userDao().insert(LocalUser(name = name, email = email))
                    statusMessage = "Room: Added $name"
                }
            }
        }) {
            Text("Add to Room DB")
        }
        Text("Local Users Count: ${localUsers.size}")
        if (localUsers.isNotEmpty()) {
            Text("Last User: ${localUsers.last().name}")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("DataStore Test", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            if (name.isNotEmpty()) {
                scope.launch {
                    userPreferences.saveUserName(name)
                    statusMessage = "DataStore: Saved $name"
                }
            }
        }) {
            Text("Save Name to DataStore")
        }
        Text("Saved Name in DataStore: $savedName")
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = statusMessage)
    }
}