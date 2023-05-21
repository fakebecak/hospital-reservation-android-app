package com.tamboto.hospitalreservation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tamboto.hospitalreservation.databinding.ItemPolyclinicBinding
import com.tamboto.hospitalreservation.model.Polyclinic

class PolyclinicAdapter : ListAdapter<Polyclinic, PolyclinicAdapter.ViewHolder>(DiffCallback()) {
  var onPolyclinicItemClicked: ((Polyclinic) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(ItemPolyclinicBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(private val binding: ItemPolyclinicBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(polyclinic: Polyclinic) {
      Picasso.get().load(polyclinic.icon).into(binding.imagePolyclinic)
      binding.textPolyclinicName.text = polyclinic.name
      binding.rootPolyclinicItem.setOnClickListener {
        onPolyclinicItemClicked?.invoke(polyclinic)
      }
    }
  }
}

class DiffCallback : DiffUtil.ItemCallback<Polyclinic>() {
  override fun areItemsTheSame(oldItem: Polyclinic, newItem: Polyclinic): Boolean {
    return oldItem == newItem
  }

  override fun areContentsTheSame(oldItem: Polyclinic, newItem: Polyclinic): Boolean {
    return oldItem.id == newItem.id
  }

}