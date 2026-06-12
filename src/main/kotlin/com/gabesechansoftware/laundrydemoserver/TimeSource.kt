package com.gabesechansoftware.laundrydemoserver

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TimeSource {
    fun now(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}