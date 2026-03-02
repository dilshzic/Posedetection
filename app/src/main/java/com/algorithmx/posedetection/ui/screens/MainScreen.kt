package com.algorithmx.posedetection.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.posedetection.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("Folders", "Clusters", "Analytics")
    val selectedDetail by viewModel.selectedPoseDetail.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("POSE & FACE AI", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        },
        bottomBar = {
            if (selectedDetail == null) {
                NavigationBar(
                    tonalElevation = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            label = { Text(title, fontWeight = FontWeight.Bold) },
                            icon = { 
                                val icon = when(index) {
                                    0 -> Icons.AutoMirrored.Filled.List
                                    1 -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.Info
                                }
                                Icon(icon, null) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)))
        )) {
            if (selectedDetail != null) {
                PoseDetailView(selectedDetail!!, viewModel, onBack = { viewModel.clearSelection() })
            } else {
                AnimatedContent(targetState = currentTab, label = "tab_transition") { target ->
                    when (target) {
                        0 -> FolderView(viewModel)
                        1 -> ClustersView(viewModel)
                        2 -> AnalyticsView(viewModel)
                    }
                }
            }
        }
    }
}
