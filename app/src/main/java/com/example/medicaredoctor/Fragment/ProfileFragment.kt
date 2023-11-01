package com.example.medicaredoctor.Fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.medicaredoctor.Models.Image
import com.example.medicaredoctor.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var storageRef: StorageReference // Declare storage reference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        storageRef = FirebaseStorage.getInstance().reference
        firebaseFirestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Load the user's profile image when the fragment is created
        loadUserProfileImage()

        binding.updateicon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imageLauncher.launch(intent)
        }

        binding.updateprofile.setOnClickListener {
            // Save user profile information here
            val doctorName = binding.doctorsName.text.toString()
            val email = binding.doctorEmail.text.toString()
            val mobile = binding.doctorPHone.text.toString()
            val speciality = binding.speciality.text.toString()
            val degree = binding.degree.text.toString()
            val hospital = binding.hospital.text.toString()
            val interest = binding.interest.text.toString()
            val about = binding.about.text.toString()
        }

        return binding.root
    }

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val fileUri = result.data!!.data
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    val imageRef = storageRef.child("profileImages/$userId.jpg")
                    val uploadTask = imageRef.putFile(fileUri!!)
                    uploadTask.addOnSuccessListener { _ ->
                        // Successfully uploaded
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val DoctorsImage = Image(imageUrl)
                            val db = firebaseFirestore.collection("DoctorsImage")
                            db.document(userId).set(DoctorsImage)
                                .addOnCompleteListener(requireActivity()) { it ->
                                    if (it.isSuccessful) {
                                        Toast.makeText(requireContext(), "Image Uri Uploaded", Toast.LENGTH_SHORT).show()
                                        // Load the updated image
                                        loadUserProfileImage()
                                    }
                                }
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "User is not authenticated.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "You have not selected any image", Toast.LENGTH_SHORT).show()
            }
        }

    private fun loadUserProfileImage() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDocRef = firebaseFirestore.collection("DoctorsImage").document(userId)
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val imageUrl = documentSnapshot.getString("profileImageUrl")
                    if (!imageUrl.isNullOrBlank()) {
                        Picasso.get().load(imageUrl).into(binding.profileImage)
                    }
                }
            }
        }
    }
}
