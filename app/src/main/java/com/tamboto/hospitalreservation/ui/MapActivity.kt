package com.tamboto.hospitalreservation.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.R
import com.tamboto.hospitalreservation.databinding.ActivityMapBinding
import com.tamboto.hospitalreservation.model.Polyclinic

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
  private lateinit var binding: ActivityMapBinding
  private lateinit var polyclinicList: ArrayList<Polyclinic>
  private var polyclinicId: String? = ""

  private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMapBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbarMap)
    supportActionBar?.title = "Lokasi Poliklinik"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    db = Firebase.firestore

    polyclinicId = intent.getStringExtra(POLYCLINIC)

    val mapFragment = supportFragmentManager.findFragmentById(
      R.id.fragmentMap
    ) as? SupportMapFragment
    mapFragment?.getMapAsync(this)
  }

  private fun addMarkers(googleMap: GoogleMap) {
    polyclinicList.forEach { polyclinic ->
      Log.i("Polyclinic", polyclinic.toString())
      polyclinic.location?.let {
        googleMap.addMarker(
          MarkerOptions()
            .title(polyclinic.name)
            .position(
              LatLng(it.latitude, it.longitude)
            )
        )
        googleMap.moveCamera(
          CameraUpdateFactory.newLatLngZoom(
            LatLng(
              it.latitude,
              it.longitude
            ), 32f
          )
        )
      }
    }
    binding.progressBarMap.visibility = View.GONE
  }

  override fun onMapReady(googleMap: GoogleMap) {
    polyclinicList = ArrayList()
    if (!polyclinicId.isNullOrEmpty()) {
      db.collection("polyclinics").document(polyclinicId!!).get()
        .addOnSuccessListener { doc ->
          val mPoly = doc.toObject(Polyclinic::class.java)
          mPoly?.let {
            mPoly.id = doc.id
            polyclinicList.add(mPoly)
            addMarkers(googleMap)
          }
        }
        .addOnFailureListener { e ->
          binding.progressBarMap.visibility = View.GONE
          Toast.makeText(this, "Error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
        }
    } else {
      db.collection("polyclinics").get()
        .addOnSuccessListener { documents ->
          for (doc in documents) {
            val mPoly = doc.toObject(Polyclinic::class.java)
            mPoly.id = doc.id
            polyclinicList.add(mPoly)
          }
          addMarkers(googleMap)
        }
        .addOnFailureListener { e ->
          binding.progressBarMap.visibility = View.GONE
          Toast.makeText(this, "Error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
        }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val POLYCLINIC = "polyclinic"
  }
}