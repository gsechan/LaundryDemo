package com.gabesechansoftware.laundrydemoserver

class APIErrorException(val errors: List<String>): RuntimeException()