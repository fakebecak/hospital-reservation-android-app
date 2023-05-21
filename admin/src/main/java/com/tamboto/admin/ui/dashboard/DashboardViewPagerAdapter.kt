package com.tamboto.admin.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tamboto.admin.ui.dashboard.polyclinic.PolyclinicFragment
import com.tamboto.hospitalreservation.model.Polyclinic

class DashboardViewPagerAdapter(
  activity: FragmentActivity,
  private val polyclinics: ArrayList<Polyclinic>
) :
  FragmentStateAdapter(activity) {

  override fun getItemCount(): Int = polyclinics.size

  override fun createFragment(position: Int): Fragment {
    return PolyclinicFragment.newInstance(polyclinics[position])
  }
}