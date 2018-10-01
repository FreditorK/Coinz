package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth : FirebaseAuth? = null
    var enter_username : EditText? = null
    var enter_password : EditText? = null
    var progressBar : ProgressBar? = null


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

    private  fun login(){

        val username : String = enter_username?.text.toString().trim() + "@useless.com"
        val password : String = enter_password?.text.toString().trim()

        if (username.isEmpty()){
            enter_username?.error = "Please enter a username"
            enter_username?.requestFocus()
            return
        }
        if (password.length < 5){
            enter_password?.error = "Password is to short"
            enter_password?.requestFocus()
            return
        }

        progressBar?.visibility = View.VISIBLE

        firebaseAuth?.signInWithEmailAndPassword(username, password)?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    Toast.makeText(applicationContext, "Log in successful", Toast.LENGTH_SHORT).show()
                    progressBar?.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, ProfileActivity() :: class.java))
                }
                else -> {
                    when {
                        task.exception is FirebaseAuthUserCollisionException -> {Toast.makeText(applicationContext, "Already registered. Please Log in", Toast.LENGTH_SHORT).show()
                                                                                 progressBar?.visibility = View.GONE}
                        else -> {Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                                 progressBar?.visibility = View.GONE}
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.sign_up_textView -> startActivity(Intent(this, SignUpActivity::class.java))
            R.id.log_in_button -> login()
        }
    }
}
