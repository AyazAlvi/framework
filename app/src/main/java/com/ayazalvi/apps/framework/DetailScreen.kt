package com.ayazalvi.apps.framework

import android.widget.Toast
import androidx.core.net.toUri
import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding
import com.ayazalvi.framework.ActionableException
import com.ayazalvi.framework.Screen
import com.ayazalvi.framework.ScreenContext
import com.ayazalvi.framework.cast
import com.ayazalvi.framework.presentBottomSheet
import com.ayazalvi.framework.presentDialog
import com.ayazalvi.framework.state
import com.google.android.material.snackbar.Snackbar

class DetailScreen(context: ScreenContext) : Screen<LayoutDetailBinding>(context) {

    lateinit var picker: ImagePicker
//    val displayText = state("display_text", arguments?.getString("FINAL_COUNT") ?: "NOTHING")
    val displayText = state<Pair<String?, String?>>("display_text2", Pair(null, null))

    override fun onUI() {
//        displayText.bind(ui.text) { text, view -> view.text = text }
        displayText.bind(ui.text) { data, view ->view.text = "Uri: " + data.first + "\nError: " + data.second; ui.image.setImageURI(data.first?.toUri()) }
        picker = ImagePicker(fragment) { uri, err -> if (uri == null) this@DetailScreen cast ActionableException(err, "Okay") { Toast.makeText(activity, "Clicked!", Toast.LENGTH_SHORT).show() } else displayText.value = uri.toString() to err }
        ui.btn1.setOnClickListener { picker.pick() }
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