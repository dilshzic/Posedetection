package com.algorithmx.posedetection.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.algorithmx.posedetection.MainViewModel
import com.algorithmx.posedetection.data.PoseEntity
import com.algorithmx.posedetection.ui.components.FaceMeshOverlay
import com.algorithmx.posedetection.ui.components.PoseOverlay
import com.algorithmx.posedetection.ui.components.PoseThumbnail

@Composable
fun PoseDetailView(
    detail: MainViewModel.PoseDetail,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val similarPoses by viewModel.similarPoses.collectAsState()
    val filterLabel by viewModel.currentFilterLabel.collectAsState()
    val context = LocalContext.current
    var showClothingOnly by remember { mutableStateOf(false) }

    BackHandler {
        if (showClothingOnly) {
            showClothingOnly = false
        } else if (filterLabel != null) {
            viewModel.clearLabelFilter()
        } else {
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showClothingOnly && detail.clothingMask != null) {
            Image(
                bitmap = detail.clothingMask.asImageBitmap(),
                contentDescription = "Isolated Clothing",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
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
        }
        
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    if (showClothingOnly) showClothingOnly = false
                    else if (filterLabel != null) viewModel.clearLabelFilter() 
                    else onBack() 
                },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            if (detail.clothingMask != null) {
                FilterChip(
                    selected = showClothingOnly,
                    onClick = { showClothingOnly = !showClothingOnly },
                    label = { Text("Clothing Mask") },
                    leadingIcon = { Icon(Icons.Default.Checkroom, null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        labelColor = Color.White,
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null
                )
            }
        }
        
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (filterLabel != null) {
                    SimilarPosesSection(
                        label = filterLabel!!,
                        poses = similarPoses,
                        onPoseClick = { viewModel.selectPose(context, it) }
                    )
                } else {
                    DefaultDetailContent(detail, onLabelClick = { viewModel.filterByLabel(it) })
                }
            }
        }
    }
}

@Composable
private fun DefaultDetailContent(
    detail: MainViewModel.PoseDetail,
    onLabelClick: (String) -> Unit
) {
    Text("AI Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    
    if (!detail.entity.caption.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Description", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(detail.entity.caption, style = MaterialTheme.typography.bodyMedium)
    }

    if (detail.entity.autoLabels.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("Detected Attributes (Click to see similar)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            detail.entity.autoLabels.forEach { label ->
                SuggestionChip(
                    onClick = { onLabelClick(label) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text("Physical Data", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text(
        "Mapped ${detail.poseResult.pose.allPoseLandmarks.size} skeletal points. Face Mesh overlay enabled.",
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun SimilarPosesSection(
    label: String,
    poses: List<PoseEntity>,
    onPoseClick: (PoseEntity) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Images matching: ", style = MaterialTheme.typography.bodyMedium)
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(12.dp))
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poses) { pose ->
            Box(modifier = Modifier.size(120.dp)) {
                PoseThumbnail(
                    pose = pose,
                    isSelected = false,
                    onClick = { onPoseClick(pose) },
                    onLongClick = {}
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
