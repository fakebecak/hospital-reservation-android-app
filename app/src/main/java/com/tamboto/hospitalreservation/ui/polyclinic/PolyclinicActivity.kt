package com.tamboto.hospitalreservation.ui.polyclinic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.tamboto.hospitalreservation.databinding.ActivityPolyclinicBinding
import com.tamboto.hospitalreservation.model.Polyclinic
import com.tamboto.hospitalreservation.model.Schedule
import com.tamboto.hospitalreservation.ui.MapActivity
import com.tamboto.hospitalreservation.ui.schedule.ScheduleActivity

class PolyclinicActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore

  private lateinit var binding: ActivityPolyclinicBinding
  private lateinit var polyclinic: Polyclinic
  private lateinit var scheduleList: ArrayList<Schedule>
  private lateinit var scheduleAdapter: ScheduleAdapter
  private var polyclinicId: String? = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPolyclinicBinding.inflate(layoutInflater)
    setContentView(binding.root)

    db = Firebase.firestore

    setSupportActionBar(binding.toolbarPolyclinic)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = "Detail Poliklinik"

    polyclinicId = intent.getStringExtra(POLYCLINIC_ID)

    binding.cardLocation.setOnClickListener {
      val intent = Intent(this, MapActivity::class.java)
      intent.putExtra(MapActivity.POLYCLINIC, polyclinicId)
      startActivity(intent)
    }

    polyclinicId?.let { id ->
      db.collection("polyclinics").document(id).get()
        .addOnSuccessListener { document ->
          val mPolyclinic = document.toObject(Polyclinic::class.java)
          mPolyclinic?.let {
            polyclinic = mPolyclinic
            polyclinic.id = id
            Picasso.get().load(polyclinic.icon).into(binding.imagePolyclinic)
            binding.textPolyclinicName.text = polyclinic.name
            binding.textPolyclinicDescription.text = polyclinic.description
          }
          fetchSchedules()
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Gagal memuat data: ${e.message.toString()}", Toast.LENGTH_SHORT)
            .show()
          finish()
        }
    }
  }

  private fun fetchSchedules() {
    binding.progressBarPolyclinic.visibility = View.VISIBLE
    scheduleList = ArrayList()
    db.collection("schedules").whereEqualTo("polyclinicId", polyclinicId).get()
      .addOnSuccessListener { documents ->
        binding.progressBarPolyclinic.visibility = View.GONE
        if (!documents.isEmpty) {
          for (doc in documents) {
            val schedule = doc.toObject(Schedule::class.java)
            schedule.id = doc.id
            Log.i("Schedule", schedule.toString())
            scheduleList.add(schedule)
          }
          populateRecyclerSchedule()
        }
      }
      .addOnFailureListener { e ->
        binding.progressBarPolyclinic.visibility = View.GONE
        Toast.makeText(this, "Gagal memuat data: ${e.message.toString()}", Toast.LENGTH_SHORT)
          .show()
      }
  }

  private fun populateRecyclerSchedule() {
    scheduleAdapter = ScheduleAdapter()
    scheduleAdapter.onScheduleItemClicked = { currentSchedule ->
      val intent = Intent(this, ScheduleActivity::class.java)
      intent.putExtra(ScheduleActivity.SCHEDULE_ID, currentSchedule.id)
      startActivity(intent)
    }
    binding.recyclerSchedule.layoutManager = LinearLayoutManager(this)
    binding.recyclerSchedule.adapter = scheduleAdapter
    scheduleAdapter.submitList(scheduleList)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val POLYCLINIC_ID = "polyclinic_id"
  }
}