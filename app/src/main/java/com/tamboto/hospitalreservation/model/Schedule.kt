package com.tamboto.hospitalreservation.model

data class Schedule(
  var id: String = "",
  var polyclinicId: String = "",
  var doctorId: String = "",
  var start: String = "",
  var end: String = "",
  var day: Int = -1,
  var status: String = "",
)
