package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneOffset

data class AvailableTimesResponse(
    val pickup: List<AvailableDateTime>,
    val delivery: List<AvailableDateTime>,
    val minTimeBetweenPickupAndDelivery: Long //ms minimum between pickup and delivery
)

data class AvailableDateTime(
    val date: Long, //UTC millis of midnight on that day
    val times: List<TimeRange>
)

data class TimeRange(val startTime: Long, val endTime:Long) //Start and end of a range, in ms from midnight

@RestController
class AvailableTimesController {
    @GetMapping("/availableTimes")
    fun availableTimes(): NetworkResponse<AvailableTimesResponse> {
        val tomorrow = LocalDate.now(ZoneOffset.UTC).atStartOfDay().plusDays(1)
        val tomorrowMs = tomorrow.toInstant(ZoneOffset.UTC).toEpochMilli()
        return NetworkResponse(
            AvailableTimesResponse(
                pickup = listOf(
                    AvailableDateTime(
                        date = tomorrowMs,
                        times = listOf(
                            TimeRange(9*60*60*1000, 10*60*60*1000),
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                            TimeRange(15*60*60*1000, 16*60*60*1000),

                            )
                    ),
                    AvailableDateTime(
                        date = tomorrow.plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli(),
                        times = listOf(
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                            )
                    ),
                    AvailableDateTime(
                        date = tomorrow.plusDays(3).toInstant(ZoneOffset.UTC).toEpochMilli(),
                        times = listOf(
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                        )
                    ),

                ),
                delivery = listOf(
                    AvailableDateTime(
                        date = tomorrowMs,
                        times = listOf(
                            TimeRange(9*60*60*1000, 10*60*60*1000),
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                            TimeRange(15*60*60*1000, 16*60*60*1000),

                            )
                    ),
                    AvailableDateTime(
                        date = tomorrow.plusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli(),
                        times = listOf(
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                        )
                    ),
                    AvailableDateTime(
                        date = tomorrow.plusDays(6).toInstant(ZoneOffset.UTC).toEpochMilli(),
                        times = listOf(
                            TimeRange(10*60*60*1000, 11*60*60*1000),
                        )
                    ),

                ),
                minTimeBetweenPickupAndDelivery = 24*2*60*60*1000
            )
        )
    }
}