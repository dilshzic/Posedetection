package com.algorithmx.posedetection.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.ui.components.EmptyStateView
import com.algorithmx.posedetection.ui.components.FolderCard
import com.algorithmx.posedetection.ui.components.ImportFolderDialog

@Composable
fun FolderView(viewModel: MainViewModel) {
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolderName.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (selectedFolder == null) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Collections", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                FilledTonalButton(
                    onClick = { showImportDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Import")
                }
            }
            
            Spacer(Modifier.height(16.dp))

            if (folders.isEmpty()) {
                EmptyStateView("No folders yet. Import images to start.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(folders) { folder ->
                        FolderCard(folder) {
                            viewModel.selectFolder(folder)
                        }
                    }
                }
            }
        }
    } else {
        PoseListScreen(selectedFolder!!, viewModel) { 
            viewModel.selectFolder(null) 
        }
    }

    if (showImportDialog) {
        ImportFolderDialog(
            onDismiss = { showImportDialog = false },
            onImportImages = { name, uris ->
                viewModel.onImagesSelected(context, uris, name)
                showImportDialog = false
            },
            onImportFolder = { name, treeUri ->
                val uris = mutableListOf<Uri>()
                val directory = DocumentFile.fromTreeUri(context, treeUri)
                directory?.listFiles()?.forEach { file ->
                    if (file.type?.startsWith("image/") == true) {
                        uris.add(file.uri)
                    }
                }
                viewModel.onImagesSelected(context, uris, name)
                showImportDialog = false
            }
        )
    }
}
