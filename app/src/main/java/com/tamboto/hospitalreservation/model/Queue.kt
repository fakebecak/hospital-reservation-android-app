package com.tamboto.hospitalreservation.model

import com.google.firebase.Timestamp

data class Queue(
  var id: String = "",
  var scheduleId: String = "",
  var userId: String = "",
  var orderNumber: Int = -1,
  var status: String = "",
  var queuedAt: Timestamp = Timestamp.now(),
)
