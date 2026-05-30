package com.ayazalvi.apps.framework

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding
import com.ayazalvi.apps.framework.databinding.LayoutHomeBinding
import com.ayazalvi.framework.core.activity.FrameworkActivity
import com.ayazalvi.framework.core.navigator.push
import com.ayazalvi.framework.core.screen.ScreenRegistry

class MainActivity : FrameworkActivity() {

    override val fragmentContainerId: Int = R.id.main_container

    override fun onRegisterScreens(registry: ScreenRegistry) {
        registry.register(LayoutHomeBinding::inflate, ::HomeScreen)
        registry.register(LayoutDetailBinding::inflate, ::DetailScreen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (savedInstanceState == null) { push<HomeScreen>() }
    }
}