package com.example.messengerapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        RegisterButton.setOnClickListener {
            PerformRegister()
        }


        SelectPhotoButtonRegister.setOnClickListener {
            Log.d("RegisterActivity","Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        NavToLoginPage.setOnClickListener {
            Log.d("RegisterActivity","show login activity")

            // launch login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }

    var selectedPhoto: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //proceed an check what is selected image was


            selectedPhoto = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)

           //val bitmapDrawble = BitmapDrawable(bitmap)
           //SelectPhotoButtonRegister.setBackground(bitmapDrawble)

            Select_Photo_Imageview_Regsiter.setImageBitmap(bitmap)
            SelectPhotoButtonRegister.alpha = 0f //changing float to 0
            Log.d("RegisterActivity", "$selectedPhoto Was Selected")




        }
    }

    private fun PerformRegister(){

        val email = Email_EditText_RegScreen.text.toString()
        val password = Password_EditText_RegScreen.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please Enter Text in Email/Password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is " + email)
        Log.d("RegisterActivity", "Password : $password")

        // authenticate with firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Log.d("RegisterActivity", "successfully created user with id: ${it.result?.user?.uid}")

                UploadImageToFireBaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Failed To Create User: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d("RegisterActivity", "Failed To Create User: ${it.message}")
            }
    }
    private fun UploadImageToFireBaseStorage(){
        if (selectedPhoto == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhoto!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully Uploaded Image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register Activity", "File Location : $it")

                    saveUserToFireBaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Register Activty", "Photo Upload Failed")
            }
    }

    private fun saveUserToFireBaseDatabase(ProfileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, Username_EditText_RegScreen.text.toString(),ProfileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user to firebaseDB")

                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("Register Activty", "DB Put Failed")
            }
    }
}

class User(val uid: String,val username: String, val profileImageUrl: String) {
    constructor() : this("","","")
}