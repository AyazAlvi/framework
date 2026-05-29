package com.ayazalvi.framework

open class ActionableException (val msg: String, val actionName: String, val action: (Screen<*>) -> Unit) : Exception()