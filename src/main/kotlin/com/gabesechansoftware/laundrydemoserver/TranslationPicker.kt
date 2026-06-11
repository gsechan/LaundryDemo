package com.gabesechansoftware.laundrydemoserver

import kotlin.collections.forEach

data class Transaltion(val name: String, val locale: String)

fun findNameMatchingBestLocale(names: List<Transaltion>, locales: List<String>): String? {
    locales.forEach { locale ->
        allSublocales(locale).forEach { sublocale ->
            names.forEach { name->
                if(name.locale == sublocale) {
                    return@findNameMatchingBestLocale name.name
                }
            }
        }
    }
    return null
}

private fun allSublocales(locale: String): List<String> {
    return listOfNotNull(locale, locale.substringBefore("-").takeIf { it != locale })
}