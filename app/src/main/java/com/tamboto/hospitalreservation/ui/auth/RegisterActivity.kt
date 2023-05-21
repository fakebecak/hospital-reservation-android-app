package com.tamboto.hospitalreservation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tamboto.hospitalreservation.databinding.ActivityRegisterBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.helper.isValidEmail
import com.tamboto.hospitalreservation.ui.dashboard.DashboardActivity
import com.tamboto.hospitalreservation.ui.home.HomeActivity

class RegisterActivity : AppCompatActivity() {
  private lateinit var binding: ActivityRegisterBinding
  private lateinit var auth: FirebaseAuth
  private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRegisterBinding.inflate(layoutInflater)
    setContentView(binding.root)

    auth = Firebase.auth
    db = Firebase.firestore

    binding.textLogin.setOnClickListener {
      startActivity(Intent(this, LoginActivity::class.java))
      finish()
    }

    binding.btnRegister.setOnClickListener {
      val inputName = binding.edName.text.toString().trim()
      val inputPhone = binding.edPhone.text.toString().trim()
      val inputAddress = binding.edAddress.text.toString().trim()
      val inputEmail = binding.edEmail.text.toString().trim()
      val inputPassword = binding.edPassword.text.toString().trim()

      var isInvalid = false

      when {
        inputName.isEmpty() -> {
          isInvalid = true
          binding.edName.error = "Nama tidak boleh kosong"
        }
        inputPhone.isEmpty() -> {
          isInvalid = true
          binding.edPhone.error = "No. telepon tidak boleh kosong"
        }
        inputAddress.isEmpty() -> {
          isInvalid = true
          binding.edAddress.error = "Alamat tidak boleh kosong"
        }
        inputEmail.isEmpty() -> {
          isInvalid = true
          binding.edEmail.error = "Email tidak boleh kosong"
        }
        inputPassword.isEmpty() -> {
          isInvalid = true
          binding.edPassword.error = "Password ini tidak boleh kosong"
        }
        !isValidEmail(inputEmail) -> {
          isInvalid = true
          binding.edEmail.error = "Email tidak valid"
        }
      }

      if (!isInvalid) {
        register(inputName, inputPhone, inputAddress, inputEmail, inputPassword)
      }
    }
  }

  private fun register(
    inputName: String,
    inputPhone: String,
    inputAddress: String,
    inputEmail: String,
    inputPassword: String
  ) {
    binding.progressBarRegister.visibility = View.VISIBLE
    auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          // Sign in success, update UI with the signed-in user's information
          val user = auth.currentUser

          val data = hashMapOf(
            "name" to inputName,
            "email" to inputEmail,
            "phone" to inputPhone,
            "address" to inputAddress,
            "role" to "user"
          )

          user?.let {
            db.collection("users").document(user.uid).set(data)
              .addOnSuccessListener {
                binding.progressBarRegister.visibility = View.GONE
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.ID, user.uid
                )
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.NAME, inputName
                )
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.EMAIL, inputEmail
                )
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.PHONE, inputPhone
                )
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.ADDRESS, inputAddress
                )
                SharedPreferenceHelper.setSharedPreferenceString(
                  this, SharedPreferenceHelper.ROLE, "user"
                )
                Toast.makeText(
                  this, "Berhasil mendaftar, selamat datang $inputName", Toast.LENGTH_SHORT
                ).show()
                updateUI(user)
              }
              .addOnFailureListener { e ->
                binding.progressBarRegister.visibility = View.GONE
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT)
                  .show()
              }
          }
        } else {
          // If sign in fails, display a message to the user.
          binding.progressBarRegister.visibility = View.GONE
          Toast.makeText(
            baseContext, "Registrasi gagal.",
            Toast.LENGTH_SHORT
          ).show()
          updateUI(null)
        }
      }
      .addOnFailureListener {
        binding.progressBarRegister.visibility = View.GONE
        Toast.makeText(
          baseContext, "Registrasi gagal.",
          Toast.LENGTH_SHORT
        ).show()
      }
  }

  private fun updateUI(user: FirebaseUser?) {
    user?.let {
      val isAdmin = SharedPreferenceHelper.getSharedPreferenceString(
        this, SharedPreferenceHelper.ROLE, ""
      ) == "admin"
      if (isAdmin) {
        startActivity(Intent(this, DashboardActivity::class.java))
      } else {
        startActivity(Intent(this, HomeActivity::class.java))
      }
      finish()
    }
  }

  override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    if (currentUser != null) {
      updateUI(currentUser)
    }
  }
}