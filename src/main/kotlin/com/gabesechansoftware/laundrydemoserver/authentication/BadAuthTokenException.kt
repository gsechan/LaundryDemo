package com.gabesechansoftware.laundrydemoserver.authentication

class BadAuthTokenException(token: String?): RuntimeException("Bad auth token: $token") {
}