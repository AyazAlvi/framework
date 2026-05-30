package com.ayazalvi.apps.framework

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.core.net.toUri
import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding
import com.ayazalvi.framework.core.navigator.presentBottomSheet
import com.ayazalvi.framework.core.navigator.presentDialog
import com.ayazalvi.framework.core.screen.Screen
import com.ayazalvi.framework.core.screen.ScreenContext
import com.ayazalvi.framework.core.screen.state
import com.ayazalvi.framework.utils.exception.ActionableException
import com.ayazalvi.framework.utils.exception.cast
import com.ayazalvi.framework.utils.file.MediaPicker
import com.google.android.material.snackbar.Snackbar

class DetailScreen(context: ScreenContext) : Screen<LayoutDetailBinding>(context) {

    val picker = MediaPicker(this)
//    val displayText = state("display_text", arguments?.getString("FINAL_COUNT") ?: "NOTHING")
    val displayText = state<Pair<String?, String?>>("display_text2", Pair(null, null))

    @SuppressLint("SetTextI18n")
    override fun onUI() {
//        displayText.bind(ui.text) { text, view -> view.text = text }
        picker.init { data, err -> if (data == null) this@DetailScreen cast ActionableException(err, "Okay") { Toast.makeText(activity, "Clicked!", Toast.LENGTH_SHORT).show() } else displayText.value = data.toString() to err }
        displayText.bind(ui.text) { data, view -> view.text = "Uri: " + data.first + "\nError: " + data.second; ui.image.setImageURI(data.first?.toUri()) }
        ui.btn1.setOnClickListener { picker.pickImage() }
        ui.btn2.setOnClickListener { navigator.presentDialog<DetailScreen>() }
        ui.btn.setOnClickListener { onBackPressed() }
        ui.btn3.setOnClickListener { navigator.presentBottomSheet<DetailScreen>() }
//        backPressOverrideEnabled = true
    }

    override fun onActionException(action: ActionableException) {
        val s = Snackbar.make(activity.window.decorView, action.msg, Snackbar.LENGTH_SHORT)
        s.setAction(action.actionName) { action.action(this) }
        s.show()
    }

//    override fun onBackPressed() {
//        Toast.makeText(activity, "Back Pressed!", Toast.LENGTH_SHORT).show()
//        popWithResult("RESUL", Bundle().apply { putString("uri", displayText.value.first); putString("data", displayText.value.second) })
//        popWithResult("RESUL", bundleOf("uri" to displayText.value.first, "data" to displayText.value.second))
//    }
}