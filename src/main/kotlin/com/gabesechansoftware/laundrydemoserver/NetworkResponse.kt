package com.gabesechansoftware.laundrydemoserver

enum class NetworkErrorType{
    NONE,
    BAD_AUTH,
    API_SPECIFIC_ERROR,
}
data class NetworkResponse<T>(val success: Boolean, val errorType: String, val errors: List<String>, val data: T?) {
    constructor(data:T) : this(true, NetworkErrorType.NONE.toString(), emptyList(), data)
    constructor(errorType: NetworkErrorType, error: String): this(false, errorType.toString(), listOf(error), null)
    constructor(errorType: NetworkErrorType, errors: List<String>): this(false, errorType.toString(), errors, null)
}