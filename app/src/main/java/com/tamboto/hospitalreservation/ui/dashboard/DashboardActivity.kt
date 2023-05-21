package com.tamboto.hospitalreservation.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.ActivityDashboardBinding
import com.tamboto.hospitalreservation.model.Polyclinic
import com.tamboto.hospitalreservation.model.Schedule
import com.tamboto.hospitalreservation.ui.ProfileActivity
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityDashboardBinding
  private lateinit var adapter: DashboardViewPagerAdapter
  private lateinit var schedules: ArrayList<Schedule>
  private lateinit var polyclinics: ArrayList<Polyclinic>

  private var polyclinicCount = 0
  private var doctorCount = 0
  private var patientCount = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDashboardBinding.inflate(layoutInflater)
    setContentView(binding.root)

    db = Firebase.firestore
    schedules = ArrayList()
    polyclinics = ArrayList()

    binding.cardProfile.setOnClickListener {
      startActivity(Intent(this, ProfileActivity::class.java))
    }

    populateHeaderData()
    populateSchedule()
  }

  private fun populateHeaderData() {
    db.collection("polyclinics").get()
      .addOnSuccessListener { documents ->
        polyclinicCount = documents.size()
        binding.textPolyclinicCount.text = polyclinicCount.toString()
      }

    db.collection("doctors").get()
      .addOnSuccessListener { documents ->
        doctorCount = documents.size()
        binding.textDoctorCount.text = doctorCount.toString()
      }

    db.collection("users").whereEqualTo("role", "user").get()
      .addOnSuccessListener { documents ->
        patientCount = documents.size()
        binding.textPatientCount.text = patientCount.toString()
      }
  }

  private fun populateSchedule() {
    binding.progressBarDashboard.visibility = View.VISIBLE
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_WEEK)
    db.collection("schedules").whereEqualTo("day", day).get()
      .addOnSuccessListener { documents ->
        binding.progressBarDashboard.visibility = View.GONE
        if (!documents.isEmpty) {
          for ((i, document) in documents.withIndex()) {
            if (document.exists()) {
              val schedule = document.toObject(Schedule::class.java)
              schedules.add(schedule)
            }
          }
          fetchPolyclinic()
        }
      }
      .addOnFailureListener { e ->
        binding.progressBarDashboard.visibility = View.GONE
        Toast.makeText(this, "Sesuatu error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
      }
  }

  private fun fetchPolyclinic() {
    binding.progressBarDashboard.visibility = View.VISIBLE
    val allPolyclinic = ArrayList<Polyclinic>()
    db.collection("polyclinics").get()
      .addOnSuccessListener { docs ->
        binding.progressBarDashboard.visibility = View.GONE
        if (!docs.isEmpty) {
          for (doc in docs) {
            val polyclinic = doc.toObject(Polyclinic::class.java)
            polyclinic.id = doc.id
            for (id in schedules.map { it.polyclinicId }) {
              if (id == polyclinic.id) {
                polyclinics.add(polyclinic)
              }
            }
          }
          initViewPager()
        }
      }
      .addOnFailureListener { e ->
        binding.progressBarDashboard.visibility = View.GONE
        Toast.makeText(this, "Error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
      }
  }

  private fun initViewPager() {
    adapter = DashboardViewPagerAdapter(this, polyclinics)
    binding.viewPager2Dashboard.adapter = adapter
//    binding.viewPager2Dashboard.offscreenPageLimit = adapter.itemCount

    TabLayoutMediator(
      binding.tabLayoutDashboard,
      binding.viewPager2Dashboard
    ) { tab, position ->
      tab.text = polyclinics[position].name
    }.attach()

    val tabs = binding.tabLayoutDashboard.getChildAt(0) as ViewGroup
    for (i in 0 until tabs.childCount) {
      val tab = tabs.getChildAt(i)
      val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
      val dp16 = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
      ).toInt()
      val dp4 = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
      ).toInt()
      when (i) {
        0 -> {
          layoutParams.marginStart = dp16
          layoutParams.marginEnd = dp4
        }
        tabs.childCount - 1 -> {
          layoutParams.marginStart = dp4
          layoutParams.marginEnd = dp16
        }
        else -> {
          layoutParams.marginStart = dp4
          layoutParams.marginEnd = dp4
        }
      }
      tab.layoutParams = layoutParams
      tab.requestLayout()
    }
  }

}