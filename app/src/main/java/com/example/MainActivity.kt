package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      FirebaseApp.initializeApp(this)
      Log.i("FirebaseInit", "Firebase initialized automatically via google-services.")
    } catch (e: Exception) {
      Log.e("FirebaseInit", "Error during Firebase initialization: ${e.message}", e)
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: AppViewModel = viewModel()
        MainAppScreen(viewModel = viewModel)
      }
    }
  }
}
