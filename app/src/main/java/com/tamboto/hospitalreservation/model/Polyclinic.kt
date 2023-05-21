package com.tamboto.hospitalreservation.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

data class Polyclinic(
  var id: String = "",
  var name: String = "",
  var icon: String = "",
  var description: String = "",
  var location: GeoPoint? = null
)