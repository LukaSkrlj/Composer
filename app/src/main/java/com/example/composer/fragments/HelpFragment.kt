package com.example.composer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.composer.R
import com.example.composer.activities.Piano

class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.help_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val help: WebView = view.findViewById<WebView>(R.id.help)

        help.settings.builtInZoomControls = true
        help.settings.javaScriptEnabled = true
        help.settings.builtInZoomControls = false;
        help.settings.displayZoomControls = false;
        help.loadUrl("file:///android_asset/index.html")
        super.onViewCreated(view, savedInstanceState)
    }
}
