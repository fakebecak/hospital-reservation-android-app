package com.tamboto.hospitalreservation.ui.dashboard.polyclinic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.App
import com.tamboto.hospitalreservation.R
import com.tamboto.hospitalreservation.databinding.ItemQueueDashboardBinding
import com.tamboto.hospitalreservation.model.Queue
import com.tamboto.hospitalreservation.model.User

class QueueAdapter : ListAdapter<Queue, QueueAdapter.ViewHolder>(DiffCallback()) {
  var onSuccessButtonClicked: ((Queue, Int) -> Unit)? = null
  var onPassButtonClicked: ((Queue, Int) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(
      ItemQueueDashboardBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(private val binding: ItemQueueDashboardBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(queue: Queue) {
      val db = Firebase.firestore

      binding.textQueueOrder.text = queue.orderNumber.toString()
      binding.textQueueStatus.text = queue.status
      when (queue.status) {
        "waiting" -> {
          binding.cardSuccess.visibility = View.VISIBLE
          binding.cardPass.visibility = View.VISIBLE
          binding.cardQueueLeft.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.blue
            )
          )
        }
        "passed" -> {
          binding.cardSuccess.visibility = View.GONE
          binding.cardPass.visibility = View.GONE
          binding.cardQueueLeft.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.accent
            )
          )
        }
        "success" -> {
          binding.cardSuccess.visibility = View.GONE
          binding.cardPass.visibility = View.GONE
          binding.cardQueueLeft.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.green
            )
          )
        }
      }

      db.collection("users").document(queue.userId).get()
        .addOnSuccessListener { doc ->
          val user = doc.toObject(User::class.java)
          user?.id = doc.id
          user?.let {
            binding.textQueueName.text = user.name
          }
        }

      binding.cardSuccess.setOnClickListener {
        onSuccessButtonClicked?.invoke(
          queue,
          adapterPosition
        )
      }
      binding.cardPass.setOnClickListener { onPassButtonClicked?.invoke(queue, adapterPosition) }
    }
  }
}

class DiffCallback : DiffUtil.ItemCallback<Queue>() {
  override fun areItemsTheSame(oldItem: Queue, newItem: Queue): Boolean {
    return oldItem == newItem
  }

  override fun areContentsTheSame(oldItem: Queue, newItem: Queue): Boolean {
    return oldItem.id == newItem.id
  }

}