package com.example.kelbel.frederik.coinz

import android.app.Activity
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.io.IOException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import android.app.PendingIntent
import android.content.Context


class FragmentSettings : Fragment() {
    private var profile_pic: ImageView? = null

    private var save_button: Button? = null

    private var switch_button: Button? = null

    private var check_switch: CheckBox? = null

    private var currentTeam: ImageView? = null

    private var password_button: Button? = null

    private var username: TextView? = null

    private var old_password: EditText? = null

    private var new_password: EditText? = null

    private var uriProfileImage: Uri? = null

    private val P_PIC: Int = 1

    private var mStorageRef: StorageReference? = null

    private var profileImageUrl: Uri? = null

    private var firebaseAuth: FirebaseAuth? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profile_pic = view.findViewById(R.id.profile_pic)
        save_button = view.findViewById(R.id.save_button)
        password_button = view.findViewById(R.id.save_lock)
        old_password = view.findViewById(R.id.text_view_lock)
        new_password = view.findViewById(R.id.text_view_lock2)
        switch_button = view.findViewById(R.id.switch_team)
        check_switch = view.findViewById(R.id.check_switch)
        currentTeam = view.findViewById(R.id.current_team)
        username = view.findViewById(R.id.user_settings)
        mStorageRef = FirebaseStorage.getInstance().getReference()
        firebaseAuth = FirebaseAuth.getInstance()

        profile_pic?.setOnClickListener {
            choosePic()
        }

        loadInformation()

        save_button?.setOnClickListener {
            saveInformation()
        }
        val user = firebaseAuth?.currentUser
        password_button?.setOnClickListener {
            val credential = EmailAuthProvider
                    .getCredential(user?.email.toString(), old_password?.text.toString())
            if (new_password?.text.toString().length >= 5) {
                user?.reauthenticate(credential)?.addOnSuccessListener {
                    user.updatePassword(new_password?.text.toString()).addOnSuccessListener {
                        Toast.makeText(this.context, "Password updated!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{
                        Toast.makeText(this.context, "Password could not be updated. Retry later!", Toast.LENGTH_SHORT).show()
                    }
                }?.addOnFailureListener {
                    Toast.makeText(this.context, "Your old password is incorrect!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this.context, "Password too short!", Toast.LENGTH_SHORT).show()
            }
        }

        if(ProfileActivity.team == 1){
            currentTeam?.setImageResource(R.mipmap.coincarver)
        }

        switch_button?.setOnClickListener{
            if(check_switch!!.isChecked()){
                FirebaseFirestore.getInstance().collection("users").document(user?.email.toString())
                        .update("gold", ProfileActivity.gold/2)
                        .addOnSuccessListener({
                            FirebaseFirestore.getInstance().collection("users").document(user?.email.toString())
                                    .update("team", (ProfileActivity.team + 1).mod(2)).addOnSuccessListener {
                                        restartApp()
                                    }.addOnFailureListener{
                                        Toast.makeText(this.context, "Try again later!", Toast.LENGTH_SHORT).show()
                                    }                        })
                        .addOnFailureListener({
                            Toast.makeText(this.context, "Try again later!", Toast.LENGTH_SHORT).show()
                        })
            }
        }

        username?.text = user?.email?.substringBefore('@')
        Log.d("Giraffe", user?.email?.substringBefore('@'))
    }

    private fun restartApp() {
        val intent = Intent(getApplicationContext(), MainActivity::class.java)
        val mPendingIntentId = System.currentTimeMillis().toInt()
        val mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
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

    private fun choosePic() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), P_PIC)
    }

    private fun saveInformation() {

        val user = firebaseAuth?.currentUser

        if (user != null && profileImageUrl != null) {
            val profile = UserProfileChangeRequest.Builder()
                    .setPhotoUri(profileImageUrl)
                    .build()

            user.updateProfile(profile)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun uploadImageToFirebase() {
        val profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg")

        val user = firebaseAuth?.getCurrentUser()

        if (user != null) {
            if (user.photoUrl != null) {
                profileImageRef.delete().addOnSuccessListener {
                    if (uriProfileImage != null) {
                        profileImageRef.putFile(uriProfileImage!!)
                                .addOnSuccessListener { taskSnapshot ->
                                    taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            profileImageUrl = task.result
                                        } else {
                                            Log.d(tag, "Problem with downloadUrl")
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                    }
                }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }

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