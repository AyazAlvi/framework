package com.ayazalvi.framework.utils.file

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.ayazalvi.framework.core.screen.Screen

abstract class FilePicker <T, I> (private val screen: Screen<*>, private val contract: ActivityResultContract<I, T>) {
    var data: T? = null; private set
    var flag : Int = 0

    private var picker: ActivityResultLauncher<I>? = null
    private var bind : ((data: T?, err: String) -> Unit)? = null

    fun init (binder: (data: T?, err: String) -> Unit) {
        picker = screen.hostFragment!!.registerForActivityResult(contract) { output ->
            this.data = output
            if (output == null) bind?.invoke(null, "No media selected")
            else if ((output as? List<*>)?.isEmpty() == true) bind?.invoke(null, "No media selected")
            else bind?.invoke(output, "")
        }
        bind(binder)
    }
    fun bind (binder: (data: T?, err: String) -> Unit) { this.bind = binder }

    protected fun pick (flag: Int, input: I) {
        this.flag = flag
        picker?.launch(input)
    }

}