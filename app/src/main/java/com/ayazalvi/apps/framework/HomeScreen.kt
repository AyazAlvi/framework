package com.ayazalvi.apps.framework

import android.os.Bundle
import com.ayazalvi.apps.framework.databinding.LayoutHomeBinding
import com.ayazalvi.framework.core.navigator.push
import com.ayazalvi.framework.core.screen.Screen
import com.ayazalvi.framework.core.screen.ScreenContext
import com.ayazalvi.framework.savedpref.persist

// The constructor precisely matches the framework's requirement, allowing the '::HomeScreen' shortcut
class HomeScreen(context: ScreenContext) : Screen<LayoutHomeBinding>(context) {

    val title by persist("SAVED_STATE", "First Value")

    override fun onUI() {
        title.bind(ui.text) { value, view -> view.text = value }
        ui.btn.setOnClickListener { title.value  = ui.edit.text.toString() }
        ui.btn2.setOnClickListener { navigator.push<DetailScreen>(Bundle().apply { putString("FINAL_COUNT", title.value) }, ui.btn2) }
    }

}