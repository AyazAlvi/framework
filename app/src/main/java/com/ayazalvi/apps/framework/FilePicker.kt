package com.ayazalvi.apps.framework

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ImagePicker (activity: Fragment, private val listener: ImagePicker.(uri: Uri?, err: String) -> Unit) {
    var flag = 0; private set
    private val v = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        this.uri = uri
        if (uri != null) listener(uri, "") else listener(null, "No media selected")
    }
    var uri: Uri? = null
    fun pick (flag: Int = 0) {
        this.flag = flag
        v.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}