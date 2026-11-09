package com.mariofronza.face_collection_app.ui

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun utcParser() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

/** Calendar days come as midnight UTC; read the day itself so it never shifts to the previous one. */
fun formatDay(iso: String): String = iso.take(10).split("-").reversed().joinToString("/")

fun formatTime(iso: String): String =
    utcParser().parse(iso)?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: ""

fun formatTimestamp(iso: String): String =
    utcParser().parse(iso)?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: ""

val PHOTO_TYPES = mapOf(
    "normal" to "Normal",
    "smilling" to "Sorrindo",
    "closedEyes" to "Olhos fechados",
    "rightSide" to "Lado direito",
    "leftSide" to "Lado esquerdo",
)
