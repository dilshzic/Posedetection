package com.algorithmx.posedetection.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.ui.components.BulkLabelDialog
import com.algorithmx.posedetection.ui.components.PoseThumbnail

@Composable
fun PoseListScreen(folderName: String, viewModel: MainViewModel, onBack: () -> Unit) {
    // Intercept system back button to go back to folder list
    BackHandler(onBack = onBack)

    val poses by viewModel.posesInFolder.collectAsState()
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showBulkLabelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text(folderName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.weight(1f))
                if (selectedIds.isNotEmpty()) {
                    TextButton(onClick = { showBulkLabelDialog = true }) {
                        Text("Label (${selectedIds.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(poses) { pose ->
                val isSelected = selectedIds.contains(pose.id)
                PoseThumbnail(
                    pose = pose,
                    isSelected = isSelected, 
                    onClick = { viewModel.selectPose(context, pose) },
                    onLongClick = {
                        selectedIds = if (isSelected) selectedIds - pose.id else selectedIds + pose.id
                    }
                )
            }
        }
    }

    if (showBulkLabelDialog) {
        BulkLabelDialog(
            onDismiss = { showBulkLabelDialog = false },
            onLabel = { label ->
                viewModel.bulkLabel(selectedIds.toList(), label)
                selectedIds = emptySet()
                showBulkLabelDialog = false
            }
        )
    }
}
