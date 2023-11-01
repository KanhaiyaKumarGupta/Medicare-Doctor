package com.example.medicaredoctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.medicaredoctor.DoctorsSignUpPage.TwilioConstants.ACCOUNT_SID
import com.example.medicaredoctor.Interface.createTwilioApiService
//import com.example.medicaredoctor.DoctorsSignUpPage.TwilioConstants.ACCOUNT_SID
//import com.example.medicaredoctor.Interface.createTwilioApiService
import com.example.medicaredoctor.databinding.ActivityDoctorsSignUpPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.random.Random

class DoctorsSignUpPage : AppCompatActivity() {
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding: ActivityDoctorsSignUpPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    object TwilioConstants {
        const val ACCOUNT_SID = "ACade57ae65880ce3b2511b46976c6e3f0"
        const val AUTH_TOKEN = "1bbf43a7e99968e916acf6e669024360"
        const val FROM_PHONE_NUMBER = "+14043345873"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorsSignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signin.setOnClickListener {
            startActivity(Intent(this, DoctorsLoginPage::class.java))
        }
        firebaseAuth = FirebaseAuth.getInstance()
        binding.signup.setOnClickListener {
            val mobileNumber = binding.phone.text.toString()
            val fullName = binding.name.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Enter you name", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_LONG).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password not matching", Toast.LENGTH_LONG).show()
            } else if (mobileNumber.isEmpty() || mobileNumber.length < 10) {
                Toast.makeText(this, "Please enter Valid Mobile Number", Toast.LENGTH_SHORT).show()
            } else {
                val doctorsId = generateRandomString(10)
                signup(fullName, email, password, doctorsId, mobileNumber)
            }


        }

    }

    private fun signup(
        fullName: String,
        email: String,
        password: String,
        doctorsId: String,
        mobileNumber: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, DoctorsLoginPage::class.java))
                    Toast.makeText(this, "Signup Successfully", Toast.LENGTH_SHORT).show()
                    val twilioApiServiceUser = createTwilioApiService()
                    val toPhoneNumberUser = "+91$mobileNumber"
                    val fromPhoneNumberUser =
                        "+14043345873" // Replace with your Twilio phone number
                    val messageUser = "Hey Your id is $doctorsId"

                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val response = twilioApiServiceUser.sendSMS(
                                ACCOUNT_SID,
                                toPhoneNumberUser,
                                fromPhoneNumberUser,
                                messageUser
                            ).execute()
                            if (response.isSuccessful) {
                                // SMS sent successfully
                            } else {
                                // Handle the failure case here
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            // Handle the exception here
                        }
                    }

                    addDatatoFirebase(
                        fullName,
                        email,
                        password,
                        firebaseAuth.currentUser?.uid!!,
                        doctorsId
                    )
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Weak password. Password should be at least 8 characters."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                        else -> "Signup failed. Please try again."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun addDatatoFirebase(
        fullName: String,
        email: String,
        password: String,
        uid: String,
        doctorsId: String
    ) {

        firebaseFirestore = FirebaseFirestore.getInstance()
        val doctorData = Doctors(fullName, email, password, uid, doctorsId)
        val db = firebaseFirestore.collection("DoctorsData")
        db.add(doctorData).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                Log.e("Ram Ram ", task.exception?.message.toString())
            }

        }
//        databaseReference = FirebaseDatabase.getInstance().reference
//        databaseReference.child("Doctor").child(doctorsId)
//            .setValue(Doctors(fullName, email, password, uid, doctorsId))
    }

    private fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars[Random.nextInt(0, allowedChars.length)] }
            .joinToString("")
    }
}