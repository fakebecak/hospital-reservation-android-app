package com.tamboto.hospitalreservation.helper

import java.util.*

fun getDay(day: Int): String {
  return when (day) {
    1 -> "Ahad"
    2 -> "Senin"
    3 -> "Selasa"
    4 -> "Rabo"
    5 -> "Kamis"
    6 -> "Jumat"
    7 -> "Sabtu"
    else -> ""
  }
}

fun isToday(date: Date): Boolean {
  val currentDate = Date()
  return "${date.date}${date.month}${date.year}" == "${currentDate.date}${currentDate.month}${currentDate.year}"
}