package com.axel_stein.pizzatestapp.data.model

class ApiError(
    val code: Int,
    msg: String
): Throwable(msg)