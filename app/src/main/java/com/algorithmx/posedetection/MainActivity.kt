package com.algorithmx.posedetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algorithmx.posedetection.data.PoseDatabase
import com.algorithmx.posedetection.data.PoseRepository
import com.algorithmx.posedetection.ui.screens.MainScreen
import com.algorithmx.posedetection.ui.theme.PosedetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = PoseDatabase.getDatabase(this)
        val repository = PoseRepository(database.poseDao())
        val factory = MainViewModelFactory(repository)
        
        enableEdgeToEdge()
        setContent {
            PosedetectionTheme {
                val viewModel: MainViewModel = viewModel(factory = factory)
                MainScreen(viewModel)
            }
        }
    }
}
