package com.algorithmx.posedetection.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object BitmapUtils {
    fun getMutableBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri).copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
