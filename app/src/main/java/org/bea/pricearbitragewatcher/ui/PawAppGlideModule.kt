package org.bea.pricearbitragewatcher.ui

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

fun loadSvgFromAssets(imageView: ImageView, assetPath: String, w: Int, h: Int) {
    imageView.post {
        val width = (if (w == 0) imageView.measuredWidth else w)
        val height = (if (h == 0) imageView.measuredHeight else h)

        if (width > 0 && height > 0) {
            val targetSize = minOf(width, height) // Масштабируем по минимальной стороне
            renderSvg(imageView, assetPath, targetSize)
        }
    }
}

private fun renderSvg(imageView: ImageView, assetPath: String, targetSize: Int) {
    try {
        val context = imageView.context
        val inputStream = context.assets.open(assetPath)
        val svg = SVG.getFromInputStream(inputStream)
        inputStream.close()

        // Создаём Drawable из SVG
        val pictureDrawable = PictureDrawable(svg.renderToPicture())

        // Устанавливаем в ImageView
        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // Отключаем хардварный рендеринг
        imageView.setImageDrawable(pictureDrawable)

        // Устанавливаем scaleType и обновляем размеры
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        val params = imageView.layoutParams
        params.width = targetSize
        params.height = targetSize
        imageView.layoutParams = params

    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: SVGParseException) {
        e.printStackTrace()
    }
}

//fun loadSvgFromAssets(imageView: ImageView, assetPath: String) {
//
//
//    imageView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
//        override fun onPreDraw(): Boolean {
//            imageView.viewTreeObserver.removeOnPreDrawListener(this)
//
//            val width = imageView.width
//            val height = imageView.height
//
//            if (width > 0 && height > 0) {
//                val targetSize = minOf(width, height) // Масштабируем по минимальной стороне
//                renderSvg(imageView, assetPath, targetSize)
//            }
//
//            return true
//        }
//    })
//}
//
//private fun renderSvg(imageView: ImageView, assetPath: String, targetSize: Int) {
//    try {
//        val context = imageView.context
//        val inputStream = context.assets.open(assetPath)
//        val svg = SVG.getFromInputStream(inputStream)
//        inputStream.close()
//
//        // Создаём Drawable из SVG
//        val pictureDrawable = PictureDrawable(svg.renderToPicture())
//
//        // Устанавливаем в ImageView
//        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // Отключаем хардварный рендеринг
//        imageView.setImageDrawable(pictureDrawable)
//
//        // Масштабируем через LayoutParams
//        val params = imageView.layoutParams
//        params.width = targetSize
//        params.height = targetSize
//        imageView.layoutParams = params
//        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } catch (e: SVGParseException) {
//        e.printStackTrace()
//    }
//}


//fun loadSvgFromAssets(imageView: ImageView, assetPath: String, targetSize: Int) {
//    try {
//        val context = imageView.context
//        val inputStream = context.assets.open(assetPath)
//        val svg = SVG.getFromInputStream(inputStream)
//        inputStream.close()
//
//        // Создаём Drawable из SVG
//        val pictureDrawable = PictureDrawable(svg.renderToPicture())
//
//        // Настраиваем ImageView
//        imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // Отключаем хардварный рендеринг
//        imageView.setImageDrawable(pictureDrawable)
//
//        // Масштабируем через LayoutParams
//        val params = imageView.layoutParams
//        params.width = targetSize
//        params.height = targetSize
//        imageView.layoutParams = params
//        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } catch (e: SVGParseException) {
//        e.printStackTrace()
//    }
//}

//@GlideModule
//class SvgModule : AppGlideModule() {
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        registry.replace(
//            InputStream::class.java,
//            SVG::class.java,
//            SvgDecoder()
//        )
//    }
//}
//
//class SvgDecoder : ResourceDecoder<InputStream, SVG> {
//    override fun handles(source: InputStream, options: Options): Boolean = true
//
//    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG>? {
//        return try {
//            val svg = SVG.getFromInputStream(source)
//            SimpleResource(svg)
//        } catch (ex: SVGParseException) {
//            null
//        }
//    }
//}
//
//class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {
//    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<PictureDrawable>? {
//        val svg = toTranscode.get()
//        val picture = svg.renderToPicture()
//        return SimpleResource(PictureDrawable(picture))
//    }
//}




//@GlideModule
//class PawAppGlideModule : AppGlideModule() {
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        // Регистрируем декодер для преобразования InputStream в SVG
//        registry.append(InputStream::class.java, SVG::class.java, SvgDecoder(context))
//        // Регистрируем трансформер для преобразования SVG в PictureDrawable
//        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
//    }
//}
//
//// Декодер для преобразования InputStream в SVG
//class SvgDecoder(private val context: Context) : ResourceDecoder<InputStream, SVG> {
//    override fun handles(source: InputStream, options: Options): Boolean {
//        return true
//    }
//
//    override fun decode(
//        source: InputStream,
//        width: Int,
//        height: Int,
//        options: Options
//    ): Resource<SVG>? {
//        return try {
//            val svg = SVG.getFromInputStream(source)
//            SimpleResource(svg)
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка декодирования SVG", e)
//        }
//    }
//}
//
//// Трансформер для преобразования SVG в PictureDrawable
//class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {
//    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<PictureDrawable>? {
//        val svg = toTranscode.get()
//        val picture = svg.renderToPicture()
//        val drawable = PictureDrawable(picture)
//        return SimpleResource(drawable)
//    }
//}



