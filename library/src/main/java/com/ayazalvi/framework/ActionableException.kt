package com.ayazalvi.framework

open class ActionableException (val msg: String, val actionName: String, val action: (Screen<*>) -> Unit)

infix fun Screen<*>.cast (actionableException: ActionableException) = onActionException(actionableException)
