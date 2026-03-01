package com.algorithmx.posedetection.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.ui.components.FaceMeshOverlay
import com.algorithmx.posedetection.ui.components.PoseOverlay

@Composable
fun PoseDetailView(detail: MainViewModel.PoseDetail, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(detail.entity.imagePath),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        PoseOverlay(
            pose = detail.poseResult.pose,
            imageWidth = detail.poseResult.imageWidth,
            imageHeight = detail.poseResult.imageHeight
        )
        detail.faceMeshResult?.let {
            FaceMeshOverlay(
                meshes = it.meshes,
                imageWidth = it.imageWidth,
                imageHeight = it.imageHeight
            )
        }
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
        
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 300.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("AI Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (!detail.entity.caption.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Caption", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(detail.entity.caption, style = MaterialTheme.typography.bodyMedium)
                }

                if (detail.entity.autoLabels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Auto Labels", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        detail.entity.autoLabels.forEach { label ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Skeletal Data", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Detected ${detail.poseResult.pose.allPoseLandmarks.size} landmarks. Face Mesh Active.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
