package com.algorithmx.posedetection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.algorithmx.posedetection.data.PoseEntity

@Composable
fun PoseThumbnail(pose: PoseEntity, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = pose.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
            contentScale = ContentScale.Crop
        )
        
        IconButton(
            onClick = onLongClick,
            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
        ) {
            Icon(
                if (isSelected) Icons.Default.CheckCircle else Icons.Default.AddCircle, 
                null, 
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
            )
        }

        if (pose.label != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 8.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    pose.label, 
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
