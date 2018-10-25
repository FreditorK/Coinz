package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//import com.google.firebase.storage.FirebaseStorage
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth: FirebaseAuth? = null
    var enter_username: EditText? = null
    var enter_password: EditText? = null
    var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.sign_up_textView).setOnClickListener(this)
        findViewById<Button>(R.id.log_in_button).setOnClickListener(this)

        enter_username = findViewById(R.id.enter_username)
        enter_password = findViewById(R.id.enter_password)
        progressBar = findViewById(R.id.progress_bar)

        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun login() {

        val username: String = enter_username?.text.toString().trim() + "@useless.com"
        val password: String = enter_password?.text.toString().trim()

        if (username.isEmpty()) {
            enter_username?.error = "Please enter a username"
            enter_username?.requestFocus()
            return
        }
        if (password.length < 5) {
            enter_password?.error = "Password is to short"
            enter_password?.requestFocus()
            return
        }

        progressBar?.visibility = View.VISIBLE

        firebaseAuth?.signInWithEmailAndPassword(username, password)?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    getUserAccount(username)
                }
                else -> {
                    when {
                        task.exception is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(applicationContext, "Already registered. Please Log in", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                        }
                        else -> {
                            Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.sign_up_textView -> startActivity(Intent(this, SignUpActivity::class.java))
            R.id.log_in_button -> login()
        }
    }

    private fun getUserAccount(username : String){
        // Create a new user with a first, middle, and last name
        val db = FirebaseFirestore.getInstance()
        val gson = Gson()
        db.collection("users").document(username)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {
                            ProfileActivity.downloadDate = document.getString("lD")!!
                            ProfileActivity.exchangedCount = document.getLong("exchangeCount")!!.toInt()
                            ProfileActivity.gold = document.getDouble("gold")!!.toFloat()
                            ProfileActivity.wallet = gson.fromJson<Wallet>(document.getString("wallet"), object : TypeToken<Wallet>() {}.type)
                            ProfileActivity.nastycoins = gson.fromJson<ArrayList<NastyCoin>>(document.getString("nastycoins"), object : TypeToken<ArrayList<NastyCoin>>() {}.type)
                            Toast.makeText(applicationContext, "Log in successful", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                            finish()
                            startActivity(Intent(this, ProfileActivity()::class.java))
                        } else {
                            Toast.makeText(this, "Check your Connection!", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, "Check your Connection!", Toast.LENGTH_SHORT).show()
                        progressBar?.visibility = View.GONE
                    }
                })
    }

    /*private fun downloadFile() {
        val storageRef = FirebaseStorage.getInstance().getReference("profiledata/" + FirebaseAuth.getInstance().currentUser?.email.toString() + ".xml")

        val rootPath = File(this.applicationInfo.dataDir + "/shared_prefs")
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        val localFile = File(rootPath, firebaseAuth?.currentUser?.email.toString() + ".xml")

        storageRef.getFile(localFile).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Updated Game Progress", Toast.LENGTH_SHORT).show()
                    progressBar?.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, ProfileActivity()::class.java))
                } else {
                    Toast.makeText(this, "Bad connection. Playing on local storage.", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, ProfileActivity()::class.java))
                }
            }
        }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "No Profile could be retrieved. Playing on local storage.", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, ProfileActivity()::class.java))

                }
    }*/
}
