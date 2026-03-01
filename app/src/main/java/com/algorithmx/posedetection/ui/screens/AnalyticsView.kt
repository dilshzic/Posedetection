package com.algorithmx.posedetection.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.ui.components.EmptyStateView
import com.algorithmx.posedetection.ui.components.StatCard

@Composable
fun AnalyticsView(viewModel: MainViewModel) {
    val poses by viewModel.posesInFolder.collectAsState()
    val stats = viewModel.getStats(poses)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Detailed Analytics", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(20.dp))
        
        if (poses.isEmpty()) {
            EmptyStateView("Open a folder to see detailed analytics.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(stats.toList()) { (key, value) ->
                    StatCard(key, value)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Labels Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LabelDistributionTable(viewModel.getLabelDistribution(poses))
                }
            }
        }
    }
}

@Composable
fun LabelDistributionTable(distribution: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Label", fontWeight = FontWeight.ExtraBold)
                Text("Count", fontWeight = FontWeight.ExtraBold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            distribution.forEach { (label, count) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label)
                    Text(count.toString(), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
