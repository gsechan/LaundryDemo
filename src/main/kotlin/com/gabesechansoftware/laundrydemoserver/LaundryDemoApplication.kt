package com.gabesechansoftware.laundrydemoserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LaundryDemoApplication

fun main(args: Array<String>) {
    runApplication<LaundryDemoApplication>(*args)
}
