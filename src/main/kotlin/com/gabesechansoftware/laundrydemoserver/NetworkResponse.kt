package com.gabesechansoftware.laundrydemoserver

enum class NetworkErrorType{
    NONE,
    BAD_AUTH,
    API_SPECIFIC_ERROR,
}
data class NetworkResponse<T>(val success: Boolean, val errorType: String, val error: String?, val data: T?) {
    constructor(data:T) : this(true, NetworkErrorType.NONE.toString(), null, data)
    constructor(errorType: NetworkErrorType, error: String): this(false, errorType.toString(), error, null)
}