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
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlarmManager
import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap


class FragmentSettings : Fragment() {
    private var profilepic: ImageView? = null//picture in settings

    private var savebutton: Button? = null//save button for new picture

    private var switchbutton: Button? = null//button to switch team

    private var checkswitch: CheckBox? = null//checkbox to agree to the conditions of a team switch

    private var currentTeam: ImageView? = null//picture of current team logo

    private var passwordbutton: Button? = null//button to change password

    private var username: TextView? = null//displays username

    private var oldpassword: EditText? = null//enter old password to change it

    private var newpassword: EditText? = null//enter new password to change it

    private var uriProfileImage: Uri? = null//uri for profile picture

    private val pPIC: Int = 1//needed for gallery intent

    private var mStorageRef: StorageReference? = null//firbase ref

    private var profileImageUrl: Uri? = null//uri for profile picture

    private var firebaseAuth: FirebaseAuth? = null//to get user

    private var pic: Bitmap? = null//profile pic


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profilepic = view.findViewById(R.id.profile_pic)
        savebutton = view.findViewById(R.id.save_button)
        passwordbutton = view.findViewById(R.id.save_lock)
        oldpassword = view.findViewById(R.id.text_view_lock)
        newpassword = view.findViewById(R.id.text_view_lock2)
        switchbutton = view.findViewById(R.id.switch_team)
        checkswitch = view.findViewById(R.id.check_switch)
        currentTeam = view.findViewById(R.id.current_team)
        username = view.findViewById(R.id.user_settings)
        mStorageRef = FirebaseStorage.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        profilepic?.setOnClickListener {
            //click on current profile picture to change it
            choosePic()
        }

        loadInformation()//load current profile picture

        savebutton?.setOnClickListener {
            //save new rofile picture
            uploadImageToFirebase()
        }
        val user = firebaseAuth?.currentUser
        passwordbutton?.setOnClickListener {
            val credential = EmailAuthProvider
                    .getCredential(user?.email.toString(), oldpassword?.text.toString())
            if (newpassword?.text.toString().length >= 5) {//check if new password is long enough
                user?.reauthenticate(credential)?.addOnSuccessListener {
                    //check credentials
                    user.updatePassword(newpassword?.text.toString()).addOnSuccessListener {
                        //update password
                        Toast.makeText(this.context, "Password updated!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this.context, "Password could not be updated. Retry later!", Toast.LENGTH_SHORT).show()
                    }
                }?.addOnFailureListener {
                    Toast.makeText(this.context, "Your old password is incorrect!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this.context, "Password too short!", Toast.LENGTH_SHORT).show()
            }
        }

        if (ProfileActivity.team == 1) {//change team logo if coincarver
            currentTeam?.setImageResource(R.mipmap.coincarver)
        }

        switchbutton?.setOnClickListener {
            //switch team
            if (checkswitch!!.isChecked) {
                FirebaseFirestore.getInstance().collection("users").document(user?.email.toString())
                        .update("gold", ProfileActivity.gold / 2)//halves your current gold
                        .addOnSuccessListener {
                            FirebaseFirestore.getInstance().collection("users").document(user?.email.toString())
                                    .update("team", (ProfileActivity.team + 1).rem(2)).addOnSuccessListener {
                                        //changes team
                                        restartApp()//restarts app
                                    }.addOnFailureListener {
                                        Toast.makeText(this.context, "Try again later!", Toast.LENGTH_SHORT).show()
                                    }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this.context, "Try again later!", Toast.LENGTH_SHORT).show()
                        }
            }
        }

        username?.text = user?.email?.substringBefore('@')
    }

    private fun restartApp() {//restarts app
        val intent = Intent(getApplicationContext(), MainActivity::class.java)
        val mPendingIntentId = System.currentTimeMillis().toInt()
        val mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//on activity result after selecting profile picture from gallery
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pPIC && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            uriProfileImage = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uriProfileImage)
            profilepic?.setImageBitmap(bitmap)//set profile picture
            try {
                pic = bitmap
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
    }

    private fun choosePic() {//intent for gallery
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), pPIC)
    }

    private fun uploadImageToFirebase() {//upload to firebase
        val profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + username?.text.toString() + ".jpg")
        Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()

        if (uriProfileImage != null) {
            profileImageRef.putFile(uriProfileImage!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                profileImageUrl = task.result

                                val user = firebaseAuth?.currentUser

                                if (user != null && profileImageUrl != null) {
                                    val profile = UserProfileChangeRequest.Builder()
                                            .setPhotoUri(profileImageUrl)
                                            .build()

                                    user.updateProfile(profile)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                }
                                //adds profile picture to account fragment
                                if ((fragmentManager?.findFragmentByTag("A") != null && pic != null)) {//sets pic in Depot
                                    (fragmentManager?.findFragmentByTag("A") as FragmentDepot).profilepic?.setImageBitmap(pic)
                                }

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

    private fun loadInformation() {//load profile picture from firebase and display it
        val user = firebaseAuth?.currentUser

        if (user != null) {
            if (user.photoUrl != null) {
                Glide.with(this)
                        .load(user.photoUrl.toString())
                        .into(profilepic)
            }
        }
    }

}