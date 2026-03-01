package com.algorithmx.posedetection

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.posedetection.data.LandmarkData
import com.algorithmx.posedetection.data.PoseEntity
import com.algorithmx.posedetection.data.PoseRepository
import com.algorithmx.posedetection.logic.FaceMeshProcessor
import com.algorithmx.posedetection.logic.ImageDescriptionProcessor
import com.algorithmx.posedetection.logic.ImageLabelingProcessor
import com.algorithmx.posedetection.logic.PoseDetectorProcessor
import com.algorithmx.posedetection.logic.PoseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PoseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _posesInFolder = MutableStateFlow<List<PoseEntity>>(emptyList())
    val posesInFolder: StateFlow<List<PoseEntity>> = _posesInFolder.asStateFlow()

    private val _selectedPoseDetail = MutableStateFlow<PoseDetail?>(null)
    val selectedPoseDetail: StateFlow<PoseDetail?> = _selectedPoseDetail.asStateFlow()

    private val _selectedFolderName = MutableStateFlow<String?>(null)
    val selectedFolderName: StateFlow<String?> = _selectedFolderName.asStateFlow()

    private val poseProcessor = PoseDetectorProcessor()
    private val faceMeshProcessor = FaceMeshProcessor()
    private val labelingProcessor = ImageLabelingProcessor()
    private var descriptionProcessor: ImageDescriptionProcessor? = null

    init {
        viewModelScope.launch {
            repository.uniqueFolders.collectLatest {
                _folders.value = it
            }
        }
    }

    fun selectFolder(folderName: String?) {
        _selectedFolderName.value = folderName
        if (folderName != null) {
            loadFolder(folderName)
        }
    }

    private fun loadFolder(folderName: String) {
        viewModelScope.launch {
            repository.getPosesByFolder(folderName).collectLatest {
                _posesInFolder.value = it
            }
        }
    }

    fun selectPose(context: Context, pose: PoseEntity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val uri = Uri.parse(pose.imagePath)
            val poseResult = poseProcessor.detectPose(context, uri)
            val faceMeshResult = faceMeshProcessor.detectFaceMesh(context, uri)
            
            if (poseResult != null) {
                _selectedPoseDetail.value = PoseDetail(pose, poseResult, faceMeshResult)
            }
            _uiState.value = UiState.Idle
        }
    }
    
    fun clearSelection() {
        _selectedPoseDetail.value = null
    }

    fun onImagesSelected(context: Context, uris: List<Uri>, folderName: String) {
        if (descriptionProcessor == null) {
            descriptionProcessor = ImageDescriptionProcessor(context.applicationContext)
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            uris.forEach { uri ->
                val poseResult = poseProcessor.detectPose(context, uri)
                val labels = labelingProcessor.labelImage(context, uri)
                
                // Using Image Describe
                val description = descriptionProcessor?.describeImage(uri)
                
                if (poseResult != null) {
                    val poseEntity = PoseEntity(
                        imagePath = uri.toString(),
                        folderName = folderName,
                        autoLabels = labels,
                        caption = description,
                        timestamp = System.currentTimeMillis(),
                        landmarks = poseResult.pose.allPoseLandmarks.map {
                            LandmarkData(it.position3D.x, it.position3D.y, it.position3D.z, it.inFrameLikelihood)
                        },
                        imageWidth = poseResult.imageWidth,
                        imageHeight = poseResult.imageHeight
                    )
                    repository.insertPose(poseEntity)
                }
            }
            _uiState.value = UiState.Idle
        }
    }

    fun bulkLabel(ids: List<Long>, label: String) {
        viewModelScope.launch {
            repository.updateLabelsInBulk(ids, label)
        }
    }

    fun getStats(poses: List<PoseEntity>): Map<String, Double> {
        if (poses.isEmpty()) return emptyMap()
        
        val avgLikelihood = poses.flatMap { it.landmarks }.map { it.likelihood }.average()
        
        val avgKneeAngle = poses.mapNotNull { pose ->
            if (pose.landmarks.size > 26) {
                PoseUtils.getAngle(pose.landmarks[24], pose.landmarks[26], pose.landmarks[28])
            } else null
        }.average()

        return mapOf(
            "Total Poses" to poses.size.toDouble(),
            "Avg Confidence" to avgLikelihood,
            "Avg Knee Angle" to if (avgKneeAngle.isNaN()) 0.0 else avgKneeAngle,
            "Labeled Count" to poses.count { it.label != null }.toDouble()
        )
    }

    fun getLabelDistribution(poses: List<PoseEntity>): Map<String, Int> {
        val distribution = mutableMapOf<String, Int>()
        poses.forEach { pose ->
            val label = pose.label ?: "Unlabeled"
            distribution[label] = distribution.getOrDefault(label, 0) + 1
        }
        return distribution.toList().sortedByDescending { it.second }.toMap()
    }

    override fun onCleared() {
        super.onCleared()
        descriptionProcessor?.close()
    }

    data class PoseDetail(
        val entity: PoseEntity,
        val poseResult: PoseDetectorProcessor.PoseResult,
        val faceMeshResult: FaceMeshProcessor.FaceMeshResult?
    )

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
    }
}
