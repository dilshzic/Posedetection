package com.algorithmx.posedetection.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter.ImageSegmenterOptions
import java.nio.ByteBuffer

class ClothingSegmenter(private val context: Context) {

    private var imageSegmenter: ImageSegmenter? = null

    init {
        setupImageSegmenter()
    }

    private fun setupImageSegmenter() {
        // MediaPipe internal resource utility often requires a path with a slash.
        // Using './' prefix to satisfy the 'last_slash_idx != std::string::npos' check.
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath("./selfie_multiclass_256x256.tflite")

        val optionsBuilder = ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setOutputCategoryMask(true)
            .setOutputConfidenceMasks(false)

        imageSegmenter = ImageSegmenter.createFromOptions(context, optionsBuilder.build())
    }

    fun segmentClothing(bitmap: Bitmap): Bitmap? {
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = imageSegmenter?.segment(mpImage) ?: return null
            
            val categoryMask = result.categoryMask().orElse(null) ?: return null
            
            val buffer = ByteBufferExtractor.extract(categoryMask)
            val width = categoryMask.width
            val height = categoryMask.height
            
            val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(width * height)
            
            buffer.rewind()
            for (i in 0 until width * height) {
                val category = buffer.get().toInt() and 0xFF
                if (category == 4) { // 4 is Clothes
                    val x = i % width
                    val y = i / width
                    if (x < bitmap.width && y < bitmap.height) {
                        pixels[i] = bitmap.getPixel(x, y)
                    } else {
                        pixels[i] = Color.TRANSPARENT
                    }
                } else {
                    pixels[i] = Color.TRANSPARENT
                }
            }
            
            outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            outputBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        imageSegmenter?.close()
    }
}
