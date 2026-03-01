package com.algorithmx.posedetection.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImportFolderDialog(
    onDismiss: () -> Unit, 
    onImportImages: (String, List<Uri>) -> Unit,
    onImportFolder: (String, Uri) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedFolderUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        selectedUris = it
        selectedFolderUri = null
    }
    
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        selectedFolderUri = it
        selectedUris = emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Collection Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { imagesLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (selectedUris.isNotEmpty()) "${selectedUris.size} Imgs" else "Select Files")
                    }
                    
                    FilledTonalButton(
                        onClick = { folderLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (selectedFolderUri != null) "Folder Selected" else "Select Folder")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && (selectedUris.isNotEmpty() || selectedFolderUri != null),
                onClick = { 
                    if (selectedFolderUri != null) {
                        onImportFolder(name, selectedFolderUri!!)
                    } else {
                        onImportImages(name, selectedUris)
                    }
                }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BulkLabelDialog(onDismiss: () -> Unit, onLabel: (String) -> Unit) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Category Label") },
        text = {
            OutlinedTextField(
                value = label, 
                onValueChange = { label = it }, 
                label = { Text("Label (e.g. Correct)") },
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = { if (label.isNotBlank()) onLabel(label) }) {
                Text("Apply to Selected")
            }
        }
    )
}
