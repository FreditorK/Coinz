package com.example.kelbel.frederik.coinz

import android.app.Activity
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast


import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.io.IOException

class FragmentSettings : Fragment() {
    private var profile_pic : ImageView? = null

    private var save_button : Button? = null

    private var uriProfileImage : Uri? = null

    private var v : View? = null

    private val P_PIC: Int = 1

    private var mStorageRef: StorageReference? = null

    private var profileImageUrl: Uri? = null

    private var firebaseAuth : FirebaseAuth? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.fragment_settings, container, false)

        profile_pic = v?.findViewById(R.id.profile_pic)
        save_button = v?.findViewById(R.id.save_button)
        mStorageRef = FirebaseStorage.getInstance().getReference()
        firebaseAuth = FirebaseAuth.getInstance()
        profile_pic?.setOnClickListener{
            choosePic()
        }

        loadInformation()

        save_button?.setOnClickListener{v ->
            saveInformation()
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === P_PIC && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            uriProfileImage = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uriProfileImage)
                profile_pic?.setImageBitmap(bitmap)
                uploadImageToFirebase()

            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
    }

    private fun choosePic(){
        val intent : Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), P_PIC)
    }

    private fun saveInformation(){

        val user = firebaseAuth?.currentUser

        if (user != null && profileImageUrl != null) {
            val profile = UserProfileChangeRequest.Builder()
                    .setPhotoUri(profileImageUrl)
                    .build()

            user.updateProfile(profile)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun uploadImageToFirebase(){
        val profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg")

        if (uriProfileImage != null) {
            profileImageRef.putFile(uriProfileImage!!)
                    .addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnCompleteListener{
                                task -> if(task.isSuccessful){
                                profileImageUrl = task.result
                            }else{
                                Log.d(tag, "Problem with downloadUrl")
                            }
                            }                        }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun loadInformation() {
        val user = firebaseAuth?.getCurrentUser()

        if (user != null) {
            if (user.photoUrl != null) {
                Glide.with(this)
                        .load(user.photoUrl.toString())
                        .into(profile_pic)
            }
        }
    }

}