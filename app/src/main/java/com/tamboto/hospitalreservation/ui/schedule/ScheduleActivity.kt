package com.tamboto.hospitalreservation.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.ActivityScheduleBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.helper.isToday
import com.tamboto.hospitalreservation.model.Queue
import com.tamboto.hospitalreservation.ui.auth.LoginActivity

class ScheduleActivity : AppCompatActivity() {
  private lateinit var binding: ActivityScheduleBinding
  private lateinit var db: FirebaseFirestore
  private lateinit var queues: ArrayList<Queue>
  private lateinit var queueAdapter: QueueAdapter
  private var scheduleId: String? = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityScheduleBinding.inflate(layoutInflater)
    setContentView(binding.root)

    db = Firebase.firestore

    setSupportActionBar(binding.toolbarSchedule)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = "Daftar Antrian"

    queueAdapter = QueueAdapter()
    binding.recyclerQueue.layoutManager = GridLayoutManager(this, 3)
    binding.recyclerQueue.adapter = queueAdapter

    scheduleId = intent.getStringExtra(SCHEDULE_ID)
    scheduleId?.let {
      fetchQueues(it)
    }

    binding.fabCreateReservation.setOnClickListener {
      createReservation()
    }
  }

  private fun fetchQueues(scheduleId: String) {
    binding.progressSchedule.visibility = View.VISIBLE
    queues = ArrayList()
    db.collection("queues")
      .whereEqualTo("scheduleId", scheduleId)
      .get()
      .addOnSuccessListener { documents ->
        binding.progressSchedule.visibility = View.GONE
        for (document in documents) {
          val queue = document.toObject(Queue::class.java)
          queue.id = document.id
          if (isToday(queue.queuedAt.toDate()))
            queues.add(queue)
        }
        val sortedQueues = queues.sortedWith(compareBy { it.orderNumber })
        queueAdapter.submitList(sortedQueues)
        binding.fabCreateReservation.visibility = View.VISIBLE
      }
      .addOnFailureListener { e ->
        binding.progressSchedule.visibility = View.GONE
        Toast.makeText(this, "Error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
      }
  }

  private fun createReservation() {
    var isQueued = false
    queues.map { queue ->
      if (queue.userId == SharedPreferenceHelper.getSharedPreferenceString(
          this,
          SharedPreferenceHelper.ID,
          ""
        )
      ) isQueued = true
    }
    if (!isQueued) {
      if (SharedPreferenceHelper.getSharedPreferenceString(this, SharedPreferenceHelper.ID, "")
          .isEmpty()
      ) {
        Firebase.auth.signOut()
        SharedPreferenceHelper.clearPreferences(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
      } else {
        binding.progressSchedule.visibility = View.VISIBLE
        val data = hashMapOf(
          "scheduleId" to scheduleId,
          "userId" to SharedPreferenceHelper.getSharedPreferenceString(
            this,
            SharedPreferenceHelper.ID,
            ""
          ),
          "orderNumber" to queues.size + 1,
          "status" to "waiting",
          "queuedAt" to Timestamp.now()
        )
        db.collection("queues").add(data)
          .addOnSuccessListener { document ->
            binding.progressSchedule.visibility = View.GONE
            Toast.makeText(this, "Berhasil membuat reservasi", Toast.LENGTH_SHORT).show()
            document?.let {
              document.get().addOnSuccessListener { doc ->
                val queue = doc.toObject(Queue::class.java)
                queue?.let {
                  queue.id = doc.id
                  queues.add(queue)
                  val sortedQueues = queues.sortedWith(compareBy { it.orderNumber })
                  queueAdapter.submitList(sortedQueues)
                  queueAdapter.notifyDataSetChanged()
                }
              }
            }
          }
          .addOnFailureListener {
            binding.progressSchedule.visibility = View.GONE
            Toast.makeText(this, "Gagal membuat reservasi", Toast.LENGTH_SHORT).show()
          }
      }
    } else {
      Toast.makeText(this, "Anda sudah dalam antrian", Toast.LENGTH_SHORT).show()
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
    const val SCHEDULE_ID = "schedule_id"
  }
}