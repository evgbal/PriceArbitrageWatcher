package org.bea.pricearbitragewatcher.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.caverock.androidsvg.SVG
//import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import org.bea.pricearbitragewatcher.BuildConfig
import org.bea.pricearbitragewatcher.R
import org.bea.pricearbitragewatcher.databinding.FragmentAboutBinding
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.bea.pricearbitragewatcher.ResizeAndCropTransformation

//import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou

data class AboutInfo(
    val appName: String,
    val version: String,
    val author: String
)

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding  // Используем ViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создаём объект с инфой о приложении
        val aboutInfo = AboutInfo(
            appName = getString(R.string.price_arbitrage_watcher),
            version = BuildConfig.VERSION_NAME, // Динамическое получение версии
            author = getString(R.string.developed_by)
        )

        // Заполняем данные в UI
        binding.appNameTextView.text = aboutInfo.appName
        binding.versionTextView.text = getString(R.string.version_format, aboutInfo.version)
        binding.authorTextView.text = aboutInfo.author


//        Glide.with(this)
//            .`as`(SVG::class.java)
//            .load("file:///android_asset/gate_io_logo_h_text.svg") // Либо путь к файлу
//            //.transition(DrawableTransitionOptions.withCrossFade())
//            .into(binding.imageGateIo)


//        loadSvgFromAssets(binding.imageGateIo, "file:///android_asset/gate_io_logo_h_text.svg", 200, 100)
//        loadSvgFromAssets(binding.imageHuobi, "file:///android_asset/huobi_logo_h_text.svg", 200, 100)
//        loadSvgFromAssets(binding.imageCoinEx, "file:///android_asset/coinex_logo_h_text.svg", 200, 100)




        loadSvgFromAssets(binding.imageGateIo, "gate_io_logo_h_text.svg"
            , dpToPx(160, requireContext()), dpToPx(80, requireContext()))
        loadSvgFromAssets(binding.imageHuobi, "huobi_logo_h_text.svg"
            , dpToPx(160, requireContext()), dpToPx(80, requireContext()))
        loadSvgFromAssets(binding.imageCoinEx, "coinex_logo_h_text.svg"
            , dpToPx(160, requireContext()), dpToPx(80, requireContext()))

//        val targetSize = 100
//        Glide.with(this)
//            .asDrawable()
//            .load("file:///android_asset/gate_io_logo_h_text.svg")
//            .transform(
//                ResizeAndCropTransformation(targetSize),
//            )
//            .into(object : CustomTarget<Drawable>() {
//                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//                    binding.imageGateIo.setImageDrawable(resource)
//                    // Масштабируем изображение с помощью ScaleType
//                    //binding.imageGateIo.scaleType = ImageView.ScaleType.FIT_CENTER // Можно выбрать другие варианты
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // Clean up when the view is destroyed or the resource is unloaded
//                }
//            })
//
//        Glide.with(this)
//            .asDrawable()
//            .load("file:///android_asset/huobi_logo_h_text.svg")
//            .transform(
//                ResizeAndCropTransformation(targetSize),
//            )
//            .into(object : CustomTarget<Drawable>() {
//                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//                    binding.imageHuobi.setImageDrawable(resource)
//                    // Масштабируем изображение с помощью ScaleType
//                    //binding.imageHuobi.scaleType = ImageView.ScaleType.FIT_CENTER // Можно выбрать другие варианты
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // Clean up when the view is destroyed or the resource is unloaded
//                }
//            })
//
//
//        Glide.with(this)
//            .asDrawable()
//            .load("file:///android_asset/coinex_logo_h_text.svg")
//            .transform(
//                ResizeAndCropTransformation(targetSize),
//            )
//            .into(object : CustomTarget<Drawable>() {
//                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//                    binding.imageCoinEx.setImageDrawable(resource)
//                    // Масштабируем изображение с помощью ScaleType
//                    //binding.imageCoinEx.scaleType = ImageView.ScaleType.FIT_CENTER // Можно выбрать другие варианты
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // Clean up when the view is destroyed or the resource is unloaded
//                }
//            })




//        Glide.with(this)
//            .`as`(SVG::class.java)
//            .load("file:///android_asset/huobi_logo_h_text.svg") // Либо путь к файлу
//            //.transition(DrawableTransitionOptions.withCrossFade())
//            .into(binding.imageHuobi)
//
//        Glide.with(this)
//            .`as`(SVG::class.java)
//            .load("file:///android_asset/coinex_logo_h_text.svg") // Либо путь к файлу
//            //.transition(DrawableTransitionOptions.withCrossFade())
//            .into(binding.imageCoinEx)



//        GlideToVectorYou.init().with(context).load(
//            Uri.parse("file:///android_asset/gate_io_logo_h_text.svg"),
//            binding.imageGateIo
//        )

//        GlideToVectorYou.init().with(context).load(
//            Uri.parse("file:///android_asset/huobi_logo_h_text.svg"),
//            binding.imageHuobi
//        )
//
//        GlideToVectorYou.init().with(context).load(
//            Uri.parse("file:///android_asset/coinex_logo_h_text.svg"),
//            binding.imageCoinEx
//        )
    }

    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}