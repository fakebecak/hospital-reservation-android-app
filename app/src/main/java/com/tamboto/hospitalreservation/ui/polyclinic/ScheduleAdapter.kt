package com.tamboto.hospitalreservation.ui.polyclinic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.App
import com.tamboto.hospitalreservation.R
import com.tamboto.hospitalreservation.databinding.ItemScheduleBinding
import com.tamboto.hospitalreservation.helper.getDay
import com.tamboto.hospitalreservation.model.Doctor
import com.tamboto.hospitalreservation.model.Schedule
import java.util.*

class ScheduleAdapter : ListAdapter<Schedule, ScheduleAdapter.ViewHolder>(DiffCallback()) {
  var onScheduleItemClicked: ((Schedule) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      ItemScheduleBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(private val binding: ItemScheduleBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(schedule: Schedule) {
      val db = Firebase.firestore
      db.collection("doctors").document(schedule.doctorId).get()
        .addOnSuccessListener { doc ->
          val mDoctor = doc.toObject(Doctor::class.java)
          mDoctor?.let {
            binding.textDoctor.text = it.name
          }
        }

      val calendar = Calendar.getInstance()
      if (calendar.get(Calendar.DAY_OF_WEEK) == schedule.day) {
        binding.cardScheduleRoot.setCardBackgroundColor(
          ContextCompat.getColor(
            App.getContext(),
            R.color.green
          )
        )
        binding.cardScheduleRoot.setOnClickListener {
          onScheduleItemClicked?.invoke(schedule)
        }
      } else {
        binding.cardScheduleRoot.setCardBackgroundColor(
          ContextCompat.getColor(
            App.getContext(),
            R.color.accent
          )
        )
      }

      val end = if (schedule.end.isEmpty()) {
        "Selesai"
      } else {
        schedule.end
      }
      binding.textSchedule.text =
        String.format("%s, %s-%s", getDay(schedule.day), schedule.start, end)
    }
  }
}

class DiffCallback : DiffUtil.ItemCallback<Schedule>() {
  override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
    return oldItem == newItem
  }

  override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
    return oldItem.id == newItem.id
  }

}