package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthUserCollisionException



class SignUpActivity : AppCompatActivity(), View.OnClickListener{

    var progressBar : ProgressBar? = null
    var edit_username : EditText? = null
    var edit_password : EditText? = null

    private var firebaseAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        edit_username = findViewById(R.id.edit_username)
        edit_password = findViewById(R.id.edit_password)
        progressBar = findViewById(R.id.progress_bar)

        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.sign_up_button).setOnClickListener(this)
        findViewById<TextView>(R.id.log_in_textView).setOnClickListener(this)
    }

    private fun register(){
        val username : String = edit_username?.text.toString().trim() + "@useless.com"
        val password : String = edit_password?.text.toString().trim()

        if (username.isEmpty()){
            edit_username?.error = "Please enter a username"
            edit_username?.requestFocus()
            return
        }
        if (password.length < 5){
            edit_password?.error = "Password is to short"
            edit_password?.requestFocus()
            return
        }

        progressBar?.visibility = View.VISIBLE

        firebaseAuth?.createUserWithEmailAndPassword(username, password)?.addOnCompleteListener({ task ->
            when {
                task.isSuccessful -> {
                    Toast.makeText(applicationContext, "Sign up successful", Toast.LENGTH_SHORT).show()
                    progressBar?.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, ProfileActivity() :: class.java))
                }
                else -> {
                    when{
                        task.exception is FirebaseAuthUserCollisionException -> Toast.makeText(applicationContext, "Already registered. Please Log in", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onClick(p0: View?) {//not done here
        when (p0?.id){
            R.id.sign_up_button -> register()
            R.id.log_in_textView -> {finish()
                                     startActivity(Intent(this, MainActivity() :: class.java))}
        }
    }
}