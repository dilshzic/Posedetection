package com.algorithmx.posedetection.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.ui.components.EmptyStateView
import com.algorithmx.posedetection.ui.components.PoseThumbnail

@Composable
fun ClustersView(viewModel: MainViewModel) {
    val clusters by viewModel.clusters.collectAsState()
    val poses by viewModel.posesInFolder.collectAsState()
    val context = LocalContext.current
    
    var kValue by remember { mutableFloatStateOf(3f) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Clustering", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Button(
                onClick = { viewModel.clusterPoses(kValue.toInt()) },
                shape = RoundedCornerShape(12.dp),
                enabled = poses.isNotEmpty()
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text("Group")
            }
        }

        Spacer(Modifier.height(16.dp))
        
        Text("Number of Groups: ${kValue.toInt()}", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = kValue,
            onValueChange = { kValue = it },
            valueRange = 2f..10f,
            steps = 7
        )

        Spacer(Modifier.height(16.dp))

        if (clusters.isEmpty()) {
            EmptyStateView("Tap 'Group' to automatically categorize poses in this folder using metadata.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(clusters.toList()) { (clusterId, clusterPoses) ->
                    Column {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Group ${clusterId + 1} (${clusterPoses.size} items)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clusterPoses.forEach { pose ->
                                Box(modifier = Modifier.size(120.dp)) {
                                    PoseThumbnail(
                                        pose = pose,
                                        isSelected = false,
                                        onClick = { viewModel.selectPose(context, pose) },
                                        onLongClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
