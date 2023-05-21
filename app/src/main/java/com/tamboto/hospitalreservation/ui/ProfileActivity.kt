package com.tamboto.hospitalreservation.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.ActivityProfileBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {
  private lateinit var binding: ActivityProfileBinding
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityProfileBinding.inflate(layoutInflater)
    setContentView(binding.root)

    auth = Firebase.auth

    setSupportActionBar(binding.toolbarProfile)
    supportActionBar?.title = "Pengaturan"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.btnLogout.setOnClickListener {
      auth.signOut()
      SharedPreferenceHelper.clearPreferences(this)
      val intent = Intent(this, LoginActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
      finish()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}