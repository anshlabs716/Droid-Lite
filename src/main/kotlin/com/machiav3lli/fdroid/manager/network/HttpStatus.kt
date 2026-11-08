package com.machiav3lli.fdroid.manager.network

// Minimal HTTP status holder replacing ktor's HttpStatusCode.
// Keeps call sites small: code + description + success checks.
@JvmInline
value class HttpStatus(val code: Int) {
    val value: Int get() = code

    val isSuccess: Boolean get() = code in 200..299
    val description: String
        get() = when (code) {
            200 -> "OK"
            206 -> "Partial Content"
            304 -> "Not Modified"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            408 -> "Request Timeout"
            416 -> "Requested Range Not Satisfiable"
            429 -> "Too Many Requests"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            504 -> "Gateway Timeout"
            507 -> "Insufficient Storage"
            else -> "HTTP $code"
        }

    operator fun compareTo(other: Int): Int = code.compareTo(other)

    companion object {
        val OK = HttpStatus(200)
        val NotModified = HttpStatus(304)
        val BadRequest = HttpStatus(400)
        val NotFound = HttpStatus(404)
        val RequestTimeout = HttpStatus(408)
        val RequestedRangeNotSatisfiable = HttpStatus(416)
        val Conflict = HttpStatus(409)
        val InternalServerError = HttpStatus(500)
        val NotImplemented = HttpStatus(501)
        val GatewayTimeout = HttpStatus(504)
        val InsufficientStorage = HttpStatus(507)

        fun fromValue(code: Int) = HttpStatus(code)
    }
}
