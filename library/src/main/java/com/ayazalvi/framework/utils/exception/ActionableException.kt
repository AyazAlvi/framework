package com.ayazalvi.framework.utils.exception

import com.ayazalvi.framework.core.screen.Screen

open class ActionableException (val msg: String, val actionName: String, val action: (Screen<*>) -> Unit)

infix fun Screen<*>.cast (actionableException: ActionableException) = onActionException(actionableException)
