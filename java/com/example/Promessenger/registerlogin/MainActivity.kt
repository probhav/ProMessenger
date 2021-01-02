package com.example.Promessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.Promessenger.messages.LatestMessagesActivity
import com.example.Promessenger.models.User
import com.example.Promessenger.registerlogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {


    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "ProMessenger"

        register_button_register.setOnClickListener {
            performRegister();
        }


        already_have_account_text_view.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

            selectphoto_button_register.alpha = 0f
        //
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {

        val email = email_edittext_register.text.toString();
        val password = password_edittext_register.text.toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }



        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "createUserWithEmail:success")
                    // val user = auth.currentUser
                    Log.d("MainActivity", "uid: ${it.result?.user?.uid}")

                    uploadImageToFirebaseStorage()
                    //  updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    //  Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                        Toast.makeText(
//                            baseContext, "Authentication failed.",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    //   updateUI(null)
                    return@addOnCompleteListener
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/ $filename ")


        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("MainAcivity", "Successfully uploaded image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                saveUsertoFirebaseDatabase(it.toString())
            }
        }
            .addOnFailureListener {
            Log.d("MainActivity", "Failed to upload image to storage: ${it.message}")



        }

    }
    private fun saveUsertoFirebaseDatabase(profileImageUrl: String){


        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")


        val user= User(uid, username_edittext_register.text.toString(),profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener{
               Log.d("MainAcivity","saved")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener{

            }
    }
}




//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//    }

