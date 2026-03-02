package com.algorithmx.posedetection.logic

import com.algorithmx.posedetection.data.PoseEntity
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.distance.EuclideanDistance
import java.util.Locale

class UnsupervisedClassifier {

    class PoseWrapper(val pose: PoseEntity, private val features: DoubleArray) : Clusterable {
        override fun getPoint(): DoubleArray = features
    }

    /**
     * Clusters poses based on physical landmark positions with a minimum cluster size.
     * Tries to create up to [k] groups, each with at least [minSize] items.
     */
    fun clusterByPoseAdvanced(poses: List<PoseEntity>, k: Int = 20, minSize: Int = 10): Map<Int, List<PoseEntity>> {
        if (poses.isEmpty()) return emptyMap()

        // 1. Prepare data
        val wrappers = poses.mapNotNull { pose ->
            if (pose.landmarks.isEmpty()) null
            else {
                // Use a fixed number of landmarks (e.g., first 33) to ensure vector consistency
                val features = pose.landmarks.take(33).flatMap { 
                    listOf(it.x.toDouble(), it.y.toDouble()) 
                }.toDoubleArray()
                
                // Ensure the vector size is consistent (66 for 33 landmarks)
                if (features.size == 66) PoseWrapper(pose, features) else null
            }
        }

        if (wrappers.isEmpty()) return mapOf(0 to poses)

        // 2. Determine safe K based on total population and minSize
        // We can only support up to (Total / minSize) clusters to guarantee size
        val maxPossibleK = (wrappers.size / minSize).coerceAtLeast(1)
        val targetK = k.coerceAtMost(maxPossibleK)

        // 3. Initial clustering
        val clusterer = KMeansPlusPlusClusterer<PoseWrapper>(targetK)
        val clusters = try {
            clusterer.cluster(wrappers)
        } catch (e: Exception) {
            return mapOf(0 to poses)
        }

        // 4. Identify valid and invalid clusters
        val centers = clusters.map { it.center.point }
        val clusterPoints = clusters.map { it.points.toMutableList() }
        
        val validIndices = clusterPoints.indices.filter { clusterPoints[it].size >= minSize }.toMutableList()
        val invalidIndices = clusterPoints.indices.filter { clusterPoints[it].size < minSize }

        // 5. Redistribution logic
        val finalMap = mutableMapOf<Int, MutableList<PoseEntity>>()
        
        if (validIndices.isEmpty()) {
            // If no cluster reached the minimum, merge everything into the largest one found
            val largestIdx = clusterPoints.indices.maxByOrNull { clusterPoints[it].size } ?: 0
            return mapOf(0 to wrappers.map { it.pose })
        }

        validIndices.forEach { idx ->
            finalMap[idx] = clusterPoints[idx].map { it.pose }.toMutableList()
        }

        val distanceMetric = EuclideanDistance()
        invalidIndices.forEach { idx ->
            clusterPoints[idx].forEach { wrapper ->
                // Reassign to the nearest valid cluster center
                val nearestValidIdx = validIndices.minBy { validIdx ->
                    distanceMetric.compute(wrapper.point, centers[validIdx])
                }
                finalMap[nearestValidIdx]?.add(wrapper.pose)
            }
        }

        // 6. Return re-indexed map
        return finalMap.values.withIndex().associate { it.index to it.value }
    }

    /**
     * Clusters poses based on their metadata (auto-labels and descriptions).
     */
    fun clusterByMetadata(poses: List<PoseEntity>, k: Int): Map<Int, List<PoseEntity>> {
        if (poses.isEmpty() || k <= 0) return emptyMap()

        val vocabulary = mutableSetOf<String>()
        poses.forEach { pose ->
            pose.autoLabels.forEach { vocabulary.add(it.lowercase(Locale.ROOT)) }
            pose.caption?.let { caption ->
                val words = caption.lowercase(Locale.ROOT)
                    .split(Regex("\\W+"))
                    .filter { it.length > 3 }
                vocabulary.addAll(words)
            }
        }

        val vocabList = vocabulary.toList()
        if (vocabList.isEmpty()) return mapOf(0 to poses)

        val wrappers = poses.map { pose ->
            val featureVector = DoubleArray(vocabList.size)
            pose.autoLabels.forEach { label ->
                val idx = vocabList.indexOf(label.lowercase(Locale.ROOT))
                if (idx != -1) featureVector[idx] = 1.0
            }
            pose.caption?.let { caption ->
                val words = caption.lowercase(Locale.ROOT).split(Regex("\\W+"))
                words.forEach { word ->
                    val idx = vocabList.indexOf(word)
                    if (idx != -1) featureVector[idx] = 1.0
                }
            }
            PoseWrapper(pose, featureVector)
        }

        return try {
            val clusterer = KMeansPlusPlusClusterer<PoseWrapper>(k.coerceAtMost(wrappers.size))
            val clusters = clusterer.cluster(wrappers)
            val result = mutableMapOf<Int, List<PoseEntity>>()
            clusters.forEachIndexed { index, cluster ->
                result[index] = cluster.points.map { it.pose }
            }
            result
        } catch (e: Exception) {
            mapOf(0 to poses)
        }
    }

    fun clusterByLandmarks(poses: List<PoseEntity>, k: Int): Map<Int, List<PoseEntity>> {
        return clusterByPoseAdvanced(poses, k, 10)
    }
}
