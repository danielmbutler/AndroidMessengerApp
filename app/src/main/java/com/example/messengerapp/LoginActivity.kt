package com.example.messengerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        SignInButton.setOnClickListener {
            val email = LoginActivityEmail.text.toString()
            val password = LoginActivityPassword.text.toString()

            Log.d("Login", "Attempt login with email/pw: $email/****")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
               .addOnCompleteListener {
                   if (!it.isSuccessful) return@addOnCompleteListener
                   //else if successful
                   Log.d("Main", "successfully signed in user with id: ${it.result?.user?.uid}")
               }
               .addOnFailureListener {
                   Log.d("Main", "Failed To Create User: ${it.message}")
               }
        }

        BackToRegTextView.setOnClickListener {
            finish()
        }
    }
}