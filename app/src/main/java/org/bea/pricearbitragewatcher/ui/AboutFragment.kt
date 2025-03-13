package org.bea.pricearbitragewatcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.bea.pricearbitragewatcher.BuildConfig
import org.bea.pricearbitragewatcher.R
import org.bea.pricearbitragewatcher.databinding.FragmentAboutBinding

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
    }
}