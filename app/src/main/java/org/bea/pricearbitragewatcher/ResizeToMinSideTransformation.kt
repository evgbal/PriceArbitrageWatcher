package org.bea.pricearbitragewatcher

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class ResizeAndCropTransformation(private val targetSize: Int) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // Определяем масштаб для увеличения изображения до нужного размера
        val scale = targetSize.toFloat() / minOf(toTransform.width, toTransform.height)

        val resizedWidth = (toTransform.width * scale).toInt()
        val resizedHeight = (toTransform.height * scale).toInt()

        // Масштабируем изображение
        val resizedBitmap = Bitmap.createScaledBitmap(toTransform, resizedWidth, resizedHeight, true)

        // Обрезаем до квадрата
        val minSide = minOf(resizedWidth, resizedHeight)
        val left = (resizedWidth - minSide) / 2
        val top = (resizedHeight - minSide) / 2

        return Bitmap.createBitmap(resizedBitmap, left, top, minSide, minSide)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("ResizeAndCropTransformation:$targetSize").toByteArray())
    }
}