package com.algorithmx.posedetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.posedetection.data.PoseEntity
import com.algorithmx.posedetection.data.PoseRepository
import com.algorithmx.posedetection.data.toLandmarkDataList
import com.algorithmx.posedetection.logic.FaceMeshProcessor
import com.algorithmx.posedetection.logic.ImageDescriptionProcessor
import com.algorithmx.posedetection.logic.ImageLabelingProcessor
import com.algorithmx.posedetection.logic.PoseDetectorProcessor
import com.algorithmx.posedetection.logic.PoseUtils
import com.algorithmx.posedetection.logic.UnsupervisedClassifier
import com.algorithmx.posedetection.logic.ClothingSegmenter
import com.algorithmx.posedetection.logic.BitmapUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

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

    private val _clusters = MutableStateFlow<Map<Int, List<PoseEntity>>>(emptyMap())
    val clusters: StateFlow<Map<Int, List<PoseEntity>>> = _clusters.asStateFlow()

    private val _similarPoses = MutableStateFlow<List<PoseEntity>>(emptyList())
    val similarPoses: StateFlow<List<PoseEntity>> = _similarPoses.asStateFlow()

    private val _currentFilterLabel = MutableStateFlow<String?>(null)
    val currentFilterLabel: StateFlow<String?> = _currentFilterLabel.asStateFlow()

    private val poseProcessor = PoseDetectorProcessor()
    private val faceMeshProcessor = FaceMeshProcessor()
    private val labelingProcessor = ImageLabelingProcessor()
    private var descriptionProcessor: ImageDescriptionProcessor? = null
    private var clothingSegmenter: ClothingSegmenter? = null
    private val unsupervisedClassifier = UnsupervisedClassifier()

    private var isImporting = false
    
    // Track the current folder collection job to prevent leaks/overlap
    private var folderCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            repository.uniqueFolders.collectLatest {
                _folders.value = it
            }
        }
    }

    fun selectFolder(folderName: String?) {
        _selectedFolderName.value = folderName
        _clusters.value = emptyMap()
        
        // CRITICAL: Cancel the previous folder collector before starting a new one
        folderCollectionJob?.cancel()
        
        if (folderName != null) {
            loadFolder(folderName)
        } else {
            _posesInFolder.value = emptyList()
        }
    }

    private fun loadFolder(folderName: String) {
        folderCollectionJob = viewModelScope.launch {
            repository.getPosesByFolder(folderName).collect {
                _posesInFolder.value = it
            }
        }
    }

    fun selectPose(context: Context, pose: PoseEntity) {
        if (clothingSegmenter == null) {
            clothingSegmenter = ClothingSegmenter(context.applicationContext)
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val uri = Uri.parse(pose.imagePath)
            
            val poseResult = poseProcessor.detectPose(context, uri)
            val faceMeshResult = faceMeshProcessor.detectFaceMesh(context, uri)
            
            val clothingMask = withContext(Dispatchers.Default) {
                val bitmap = BitmapUtils.getMutableBitmapFromUri(context, uri)
                bitmap?.let { clothingSegmenter?.segmentClothing(it) }
            }

            if (poseResult != null) {
                _selectedPoseDetail.value = PoseDetail(pose, poseResult, faceMeshResult, clothingMask)
            }
            _uiState.value = UiState.Idle
        }
    }
    
    fun clearSelection() {
        _selectedPoseDetail.value = null
    }

    fun filterByLabel(label: String) {
        _currentFilterLabel.value = label
        _similarPoses.value = _posesInFolder.value.filter { 
            it.autoLabels.contains(label) || it.label == label
        }
    }

    fun clearLabelFilter() {
        _currentFilterLabel.value = null
        _similarPoses.value = emptyList()
    }

    fun onImagesSelected(context: Context, uris: List<Uri>, folderName: String) {
        if (isImporting) return
        isImporting = true

        if (descriptionProcessor == null) {
            descriptionProcessor = ImageDescriptionProcessor(context.applicationContext)
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val existingPaths = repository.getPosePathsInFolder(folderName).toSet()
                val uniqueUris = uris.distinctBy { it.toString() }
                    .filter { !existingPaths.contains(it.toString()) }

                uniqueUris.forEach { uri ->
                    val poseResult = poseProcessor.detectPose(context, uri)
                    val labels = labelingProcessor.labelImage(context, uri)
                    val description = descriptionProcessor?.describeImage(uri)
                    
                    if (poseResult != null) {
                        val poseEntity = PoseEntity(
                            imagePath = uri.toString(),
                            folderName = folderName,
                            autoLabels = labels,
                            caption = description,
                            timestamp = System.currentTimeMillis(),
                            landmarks = poseResult.pose.toLandmarkDataList(),
                            imageWidth = poseResult.imageWidth,
                            imageHeight = poseResult.imageHeight
                        )
                        repository.insertPose(poseEntity)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Import failed")
            } finally {
                _uiState.value = UiState.Idle
                isImporting = false
            }
        }
    }

    fun clusterPoses(k: Int = 20, usePose: Boolean = true) {
        viewModelScope.launch {
            val currentPoses = _posesInFolder.value
            if (currentPoses.isNotEmpty()) {
                _clusters.value = if (usePose) {
                    unsupervisedClassifier.clusterByPoseAdvanced(currentPoses, k, 10)
                } else {
                    unsupervisedClassifier.clusterByMetadata(currentPoses, k)
                }
            }
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
            if (pose.landmarks.size > 24) {
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
        clothingSegmenter?.close()
    }

    data class PoseDetail(
        val entity: PoseEntity,
        val poseResult: PoseDetectorProcessor.PoseResult,
        val faceMeshResult: FaceMeshProcessor.FaceMeshResult?,
        val clothingMask: Bitmap? = null
    )

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
    }
}
