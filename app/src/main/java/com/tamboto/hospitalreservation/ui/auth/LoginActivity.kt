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
import com.tamboto.hospitalreservation.databinding.ActivityLoginBinding
import com.tamboto.hospitalreservation.helper.SharedPreferenceHelper
import com.tamboto.hospitalreservation.helper.isValidEmail
import com.tamboto.hospitalreservation.model.User
import com.tamboto.hospitalreservation.ui.dashboard.DashboardActivity
import com.tamboto.hospitalreservation.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {
  private lateinit var binding: ActivityLoginBinding
  private lateinit var auth: FirebaseAuth
  private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    auth = Firebase.auth
    db = Firebase.firestore

    binding.textRegister.setOnClickListener {
      startActivity(Intent(this, RegisterActivity::class.java))
      finish()
    }

    binding.btnLogin.setOnClickListener {
      val inputEmail = binding.edEmail.text.toString().trim()
      val inputPassword = binding.edPassword.text.toString().trim()

      var isInvalid = false

      when {
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
        login(inputEmail, inputPassword)
      }
    }
  }

  private fun login(inputEmail: String, inputPassword: String) {
    binding.progressLogin.visibility = View.VISIBLE
    auth.signInWithEmailAndPassword(inputEmail, inputPassword)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          // Sign in success, update UI with the signed-in user's information
          val user = auth.currentUser

          user?.let {
            db.collection("users").document(user.uid).get()
              .addOnSuccessListener { documentReference ->
                binding.progressLogin.visibility = View.GONE
                val userDocument = documentReference.toObject(User::class.java)
                userDocument?.let {
                  userDocument.id = user.uid

                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.ID, userDocument.id
                  )
                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.NAME, userDocument.name
                  )
                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.EMAIL, userDocument.email
                  )
                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.PHONE, userDocument.phone
                  )
                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.ADDRESS, userDocument.address
                  )
                  SharedPreferenceHelper.setSharedPreferenceString(
                    this, SharedPreferenceHelper.ROLE, userDocument.role
                  )
                  Toast.makeText(
                    this,
                    "Selamat datang ${userDocument.name}",
                    Toast.LENGTH_SHORT
                  ).show()
                  updateUI(user)
                }
              }
              .addOnFailureListener { e ->
                binding.progressLogin.visibility = View.GONE
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT)
                  .show()
              }
          }
        } else {
          // If sign in fails, display a message to the user.
          binding.progressLogin.visibility = View.GONE
          Toast.makeText(
            baseContext, "Autentikasi gagal",
            Toast.LENGTH_SHORT
          ).show()
          updateUI(null)
        }
      }
      .addOnFailureListener {
        binding.progressLogin.visibility = View.GONE
        Toast.makeText(
          baseContext, "Autentikasi gagal",
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