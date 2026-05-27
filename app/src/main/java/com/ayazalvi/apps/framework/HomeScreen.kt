package com.ayazalvi.apps.framework

import android.os.Bundle
import com.ayazalvi.apps.framework.databinding.LayoutHomeBinding

// The constructor precisely matches the framework's requirement, allowing the '::HomeScreen' shortcut
class HomeScreen(context: ScreenContext) : Screen<LayoutHomeBinding>(context) {

    val textInput = state("text", "")

    override fun onUI() {
        textInput.bind(ui.text) { value, view -> view.text = value }
        ui.btn.setOnClickListener { textInput.value = ui.edit.text.toString() }
        ui.btn2.setOnClickListener { navigator.push<DetailScreen>(Bundle().apply { putString("FINAL_COUNT", textInput.value) }) }
    }
}