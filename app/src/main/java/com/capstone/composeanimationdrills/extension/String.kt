package com.capstone.composeanimationdrills.extension

fun String.Companion.randomText(length: Int = 100): String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
    return (0..length)
        .map { charset.random() }
        .joinToString("")
}

