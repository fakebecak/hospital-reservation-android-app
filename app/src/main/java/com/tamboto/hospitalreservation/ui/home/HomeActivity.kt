package com.tamboto.hospitalreservation.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.ActivityHomeBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.helper.isToday
import com.tamboto.hospitalreservation.model.Polyclinic
import com.tamboto.hospitalreservation.model.Queue
import com.tamboto.hospitalreservation.model.Schedule
import com.tamboto.hospitalreservation.ui.MapActivity
import com.tamboto.hospitalreservation.ui.ProfileActivity
import com.tamboto.hospitalreservation.ui.polyclinic.PolyclinicActivity

class HomeActivity : AppCompatActivity() {
  private lateinit var binding: ActivityHomeBinding
  private lateinit var polyclinicAdapter: PolyclinicAdapter
  private lateinit var polyclinicList: ArrayList<Polyclinic>

  private lateinit var db: FirebaseFirestore

  private lateinit var queueList: ArrayList<Queue>
  private lateinit var queue: Queue

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    db = Firebase.firestore

    polyclinicAdapter = PolyclinicAdapter()
    polyclinicAdapter.onPolyclinicItemClicked = { polyclinic ->
      val intent = Intent(this, PolyclinicActivity::class.java)
      intent.putExtra(PolyclinicActivity.POLYCLINIC_ID, polyclinic.id)
      startActivity(intent)
    }
    binding.recyclerPolyclinic.layoutManager = GridLayoutManager(this, 3)
    binding.recyclerPolyclinic.adapter = polyclinicAdapter

    binding.cardProfile.setOnClickListener {
      startActivity(Intent(this, ProfileActivity::class.java))
    }

    fetchPolyclinics()
    checkUserQueue()
  }

  private fun checkUserQueue() {
    val userId = SharedPreferenceHelper
      .getSharedPreferenceString(this, SharedPreferenceHelper.ID, "")
    db.collection("queues").get()
      .addOnSuccessListener { documents ->
        for (document in documents) {
          val mQueue = document.toObject(Queue::class.java)
          mQueue.id = document.id
          if (mQueue.userId == userId && isToday(mQueue.queuedAt.toDate()) && mQueue.status == "waiting") {
            queue = mQueue
            getQueueList()
          }
        }
      }
      .addOnFailureListener { e -> }
  }

  private fun getQueueList() {
    queueList = ArrayList()
    db.collection("queues").whereEqualTo("scheduleId", queue.scheduleId).get()
      .addOnSuccessListener { documents ->
        for (document in documents) {
          val mQueue = document.toObject(Queue::class.java)
          mQueue.id = document.id
          if (isToday(queue.queuedAt.toDate())) {
            queueList.add(mQueue)
            subscribeCardHeaderData()
          }
        }
      }
  }

  private fun subscribeCardHeaderData() {
    var activeQueue = Queue()
    val sortedQueueList = queueList.sortedWith(compareByDescending { it.orderNumber })
    sortedQueueList.map { queue ->
      if (queue.status == "waiting") {
        activeQueue = queue
      }
    }

    binding.textCurrentQueue.text = activeQueue.orderNumber.toString()
    binding.textUserQueue.text =
      "Nomor anda ${queue.orderNumber} (${queue.orderNumber - activeQueue.orderNumber} langkah lagi)"

    var schedule: Schedule
    db.collection("schedules").document(queue.scheduleId).get()
      .addOnSuccessListener { doc ->
        doc.toObject(Schedule::class.java)?.let {
          schedule = it
          schedule.id = queue.scheduleId
          polyclinicList.map { polyclinic ->
            if (polyclinic.id == schedule.polyclinicId) {
              binding.textPolyclinicName.text = polyclinic.name
              binding.textPolyclinicName.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
//                intent.putExtra(MapActivity.POLYCLINIC, polyclinic)
                startActivity(intent)
              }
            }
          }
        }
      }
  }

  private fun fetchPolyclinics() {
    binding.progressBarHome.visibility = View.VISIBLE
    polyclinicList = ArrayList()
    db.collection("polyclinics").get()
      .addOnSuccessListener { documents ->
        binding.progressBarHome.visibility = View.GONE
        for (document in documents) {
          val polyclinic = document.toObject(Polyclinic::class.java)
          polyclinic.id = document.id
          polyclinicList.add(polyclinic)
        }
        polyclinicAdapter.submitList(polyclinicList)
      }
      .addOnFailureListener { e ->
        binding.progressBarHome.visibility = View.GONE
        Toast.makeText(this, "Error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
      }
  }
}