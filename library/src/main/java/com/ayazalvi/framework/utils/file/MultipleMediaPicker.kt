package com.ayazalvi.framework.utils.file

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.ayazalvi.framework.core.screen.Screen

class MultipleMediaPicker (screen: Screen<*>) : FilePicker<List<Uri>, PickVisualMediaRequest> (screen, ActivityResultContracts.PickMultipleVisualMedia()) {

    fun pickImage (max: Int = -1, flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly, maxItems = max.coerceIn(2, 100)))
    }
    fun pickVideo (max: Int = -1, flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly, maxItems = max.coerceIn(2, 100)))
    }
    fun pickImageAndVideo (max: Int = -1, flag: Int = 0) {
        pick(flag, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo, maxItems = max.coerceIn(2, 100)))
    }
}