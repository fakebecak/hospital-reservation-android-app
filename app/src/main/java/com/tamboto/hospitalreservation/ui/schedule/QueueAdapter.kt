package com.tamboto.hospitalreservation.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tamboto.hospitalreservation.App
import com.tamboto.hospitalreservation.R
import com.tamboto.hospitalreservation.databinding.ItemQueueBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.model.Queue

class QueueAdapter : ListAdapter<Queue, QueueAdapter.ViewHolder>(DiffCallback()) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(ItemQueueBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(private val binding: ItemQueueBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(queue: Queue) {
      if (queue.userId == SharedPreferenceHelper.getSharedPreferenceString(
          App.getContext(),
          SharedPreferenceHelper.ID,
          ""
        )
      ) {
        binding.cardQueueRoot.setCardBackgroundColor(
          ContextCompat.getColor(
            App.getContext(),
            R.color.primary
          )
        )
      }
      binding.textQueueOrder.text = queue.orderNumber.toString()
      binding.textQueueStatus.text = queue.status
      when (queue.status) {
        "waiting" -> {
          binding.cardQueueOrder.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.blue
            )
          )
        }
        "passed" -> {
          binding.cardQueueOrder.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.accent
            )
          )
        }
        "success" -> {
          binding.cardQueueOrder.setCardBackgroundColor(
            ContextCompat.getColor(
              App.getContext(),
              R.color.green
            )
          )
        }
      }
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