package com.ayazalvi.apps.framework

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding
import com.ayazalvi.framework.Screen
import com.ayazalvi.framework.ScreenContext
import com.ayazalvi.framework.presentBottomSheet
import com.ayazalvi.framework.presentDialogForResult
import com.ayazalvi.framework.state

class DetailScreen(context: ScreenContext) : Screen<LayoutDetailBinding>(context) {

    lateinit var picker: ImagePicker
//    val displayText = state("display_text", arguments?.getString("FINAL_COUNT") ?: "NOTHING")
    val displayText = state<Pair<Uri?, String?>>("display_text2", Pair(null, null))

    override fun onUI() {
        picker = ImagePicker(fragment) { uri, err -> displayText.value = uri to err }
//        displayText.bind(ui.text) { text, view -> view.text = text }
        displayText.bind(ui.text) { data, view -> view.text = "Uri: " + data.first + "\nError: " + data.second; ui.image.setImageURI(data.first) }
        ui.btn1.setOnClickListener { picker.pick() }
        ui.btn2.setOnClickListener { presentDialogForResult<DetailScreen>("RESUL") { displayText.value = it.getString("uri")?.toUri() to it.getString("data") } }
        ui.btn.setOnClickListener { onBackPressed() }
        ui.btn3.setOnClickListener { navigator.presentBottomSheet<DetailScreen>() }
        backPressOverrideEnabled = true
    }

    override fun onBackPressed() {
//        Toast.makeText(activity, "Back Pressed!", Toast.LENGTH_SHORT).show()
        popWithResult("RESUL", Bundle().apply { putString("uri", displayText.value.first?.toString()); putString("data", displayText.value.second) })
//        popWithResult("RESUL", bundleOf("uri" to displayText.value.first?.toString(), "data" to displayText.value.second))
    }
}