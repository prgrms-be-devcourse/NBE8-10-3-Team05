package com.back.global.exception

//JvmOverloads는 컴파일시 java생성자 자동생성.
class ServiceException @JvmOverloads constructor(
    val resultCode: String,
    val msg: String,
    cause: Throwable? = null
) : RuntimeException("$resultCode : $msg", cause) {

    val location: String
        get() = stackTrace.firstOrNull()?.let {
            "${it.className}.${it.methodName}:${it.lineNumber}"
        } ?: "Unknown Location"
}