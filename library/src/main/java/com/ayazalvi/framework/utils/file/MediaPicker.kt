package com.ayazalvi.framework.utils.file

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.ayazalvi.framework.core.screen.Screen

class MediaPicker (screen: Screen<*>) : FilePicker<Uri?, PickVisualMediaRequest> (screen, ActivityResultContracts.PickVisualMedia()) {

    fun pickImage (flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    fun pickVideo (flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
    }
    fun pickImageAndVideo (flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }
}