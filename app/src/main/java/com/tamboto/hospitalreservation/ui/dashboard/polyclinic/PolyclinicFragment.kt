package com.tamboto.hospitalreservation.ui.dashboard.polyclinic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.FragmentPolyclinicBinding
import com.tamboto.hospitalreservation.helper.isToday
import com.tamboto.hospitalreservation.model.Polyclinic
import com.tamboto.hospitalreservation.model.Queue
import com.tamboto.hospitalreservation.model.Schedule
import java.util.*
import kotlin.collections.ArrayList

class PolyclinicFragment() : Fragment() {
  private lateinit var binding: FragmentPolyclinicBinding
  private lateinit var adapter: QueueAdapter
  private lateinit var db: FirebaseFirestore
  private lateinit var scheduleList: ArrayList<Schedule>
  private lateinit var queueList: ArrayList<Queue>
  private lateinit var sortedQueueList: List<Queue>
  private var polyclinicId: String? = ""

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentPolyclinicBinding.inflate(layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (activity != null) {
      db = Firebase.firestore
      polyclinicId = arguments?.getString(POLYCLINIC_ID)
      adapter = QueueAdapter()

      adapter.onSuccessButtonClicked = { queue, pos ->
        binding.progressBarPolyclinic.visibility = View.VISIBLE
        db.collection("queues").document(queue.id).update("status", "success")
          .addOnSuccessListener {
            binding.progressBarPolyclinic.visibility = View.GONE
            val mQueue = sortedQueueList[pos]
            mQueue.status = "success"

            adapter.submitList(sortedQueueList)
            adapter.notifyItemChanged(pos)
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_SHORT)
              .show()
          }
          .addOnFailureListener {
            binding.progressBarPolyclinic.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${it.message.toString()}", Toast.LENGTH_SHORT)
              .show()
          }
      }

      adapter.onPassButtonClicked = { queue, pos ->
        binding.progressBarPolyclinic.visibility = View.VISIBLE
        db.collection("queues").document(queue.id).update("status", "passed")
          .addOnSuccessListener {
            binding.progressBarPolyclinic.visibility = View.GONE
            val mQueue = sortedQueueList[pos]
            mQueue.status = "passed"

            adapter.submitList(sortedQueueList)
            adapter.notifyItemChanged(pos)
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_SHORT)
              .show()
          }
          .addOnFailureListener {
            binding.progressBarPolyclinic.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${it.message.toString()}", Toast.LENGTH_SHORT)
              .show()
          }
      }

      binding.recyclerQueue.adapter = adapter
    }

    fetchSchedules()
  }

  private fun fetchSchedules() {
    scheduleList = ArrayList()
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_WEEK)
    binding.progressBarPolyclinic.visibility = View.VISIBLE
    db.collection("schedules")
      .whereEqualTo("polyclinicId", polyclinicId)
      .whereEqualTo("day", day)
      .get()
      .addOnSuccessListener { documents ->
        binding.progressBarPolyclinic.visibility = View.GONE
        for (doc in documents) {
          val schedule = doc.toObject(Schedule::class.java)
          schedule.id = doc.id
          scheduleList.add(schedule)
        }
        fetchQueues()
      }
      .addOnFailureListener {
        binding.progressBarPolyclinic.visibility = View.GONE
      }
  }

  private fun fetchQueues() {
    queueList = ArrayList()
    binding.progressBarPolyclinic.visibility = View.VISIBLE
    db.collection("queues").whereIn("scheduleId", scheduleList.map { it.id }).get()
      .addOnSuccessListener { documents ->
        binding.progressBarPolyclinic.visibility = View.GONE
        for (doc in documents) {
          val queue = doc.toObject(Queue::class.java)
          queue.id = doc.id
          if (isToday(queue.queuedAt.toDate())) {
            queueList.add(queue)
          }
        }
        sortedQueueList = queueList.sortedWith(compareBy { it.orderNumber })
        adapter.submitList(sortedQueueList)
      }
      .addOnFailureListener {
        binding.progressBarPolyclinic.visibility = View.GONE
      }
  }

  companion object {
    fun newInstance(polyclinic: Polyclinic): PolyclinicFragment {
      val args = Bundle()
      args.putString(POLYCLINIC_ID, polyclinic.id)

      val fragment = PolyclinicFragment()
      fragment.arguments = args
      return fragment
    }

    private const val POLYCLINIC_ID = "polyclinic_id"
  }
}