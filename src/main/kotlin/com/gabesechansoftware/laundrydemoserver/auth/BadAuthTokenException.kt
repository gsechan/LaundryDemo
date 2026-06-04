package com.gabesechansoftware.laundrydemoserver.auth

class BadAuthTokenException(token: String): RuntimeException("Bad auth token: $token") {
}