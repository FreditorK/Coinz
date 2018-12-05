package com.example.kelbel.frederik.coinz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth: FirebaseAuth? = null//for retrievig information from firestore
    private var enterusername: EditText? = null//username to login with
    private var enterpassword: EditText? = null//password to login with
    private var progressBar: ProgressBar? = null//loading circle
    private lateinit var remember: CheckBox // remembers your password and username


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.sign_up_textView).setOnClickListener(this)//link to sign up activity
        findViewById<Button>(R.id.log_in_button).setOnClickListener(this)//log in button

        enterusername = findViewById(R.id.enter_username)
        enterpassword = findViewById(R.id.enter_password)
        progressBar = findViewById(R.id.progress_bar)
        remember = findViewById(R.id.remember_me)

        firebaseAuth = FirebaseAuth.getInstance()

        //get password and username from remembered data
        enterusername?.setText(getSharedPreferences("General", Context.MODE_PRIVATE).getString("username", ""))
        enterpassword?.setText(getSharedPreferences("General", Context.MODE_PRIVATE).getString("password", ""))

    }

    private fun login() {//called when log in button is clicked

        //retrieve info from edittexts
        val username: String = enterusername?.text.toString().trim().toLowerCase() + "@useless.com"
        val password: String = enterpassword?.text.toString().trim()

        //check if username meets requirements
        if (enterusername?.text.toString().trim().isEmpty()) {
            enterusername?.error = "Please enter a username"
            enterusername?.requestFocus()
            return
        }
        if (password.length < 5) {
            enterpassword?.error = "Password is to short"
            enterpassword?.requestFocus()
            return
        }

        if(remember.isChecked){//store if wished
            val sharedprefsEditor = getSharedPreferences("General", Context.MODE_PRIVATE).edit()
            sharedprefsEditor.putString("username", enterusername?.text.toString().trim())
            sharedprefsEditor.putString("password", password)
            sharedprefsEditor.apply()
        }

        progressBar?.visibility = View.VISIBLE//loading circle appears

        firebaseAuth?.signInWithEmailAndPassword(username, password)?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    getUserAccount(username)//load user data
                }
                else -> when {
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

    override fun onClick(p0: View?) {//for login button and sign up link
        when (p0?.id) {
            R.id.sign_up_textView -> startActivity(Intent(this, SignUpActivity::class.java))
            R.id.log_in_button -> login()
        }
    }

    private fun getUserAccount(username: String) {//retrieve user data
        val db = FirebaseFirestore.getInstance()
        val gson = Gson()
        db.collection("users").document(username)//retrieve from user collection in firestore
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {
                            //load data into companion object in ProfileActivity and start it
                            ProfileActivity.downloadDate = document.getString("lD")!!
                            ProfileActivity.exchangedCount = document.getLong("exchangeCount")!!.toInt()
                            ProfileActivity.gold = document.getDouble("gold")!!.toFloat()
                            ProfileActivity.wallet = gson.fromJson<Wallet>(document.getString("wallet"), object : TypeToken<Wallet>() {}.type)
                            ProfileActivity.nastycoins = gson.fromJson<ArrayList<NastyCoin>>(document.getString("nastycoins"), object : TypeToken<ArrayList<NastyCoin>>() {}.type)
                            ProfileActivity.team = document.getLong("team")!!.toInt()
                            SubFragmentEvents.eventAvailability = document.getBoolean("movingSac")!!
                            Toast.makeText(applicationContext, "Log in successful", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                            finish()
                            startActivity(Intent(this, ProfileActivity()::class.java))
                        } else {
                            Toast.makeText(this, "User does not exist!", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, "Check your Connection!", Toast.LENGTH_SHORT).show()
                        progressBar?.visibility = View.GONE
                    }
                }
    }
}