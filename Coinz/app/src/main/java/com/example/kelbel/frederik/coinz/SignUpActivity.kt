package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import android.util.Log
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson


class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private var progressBar: ProgressBar? = null//loading circle
    private var editusername: EditText? = null//username to sign up with
    private var editpassword: EditText? = null//password to sign up with
    private var repeatpassword: EditText? = null//password confirmation

    private var firebaseAuth: FirebaseAuth? = null

    private var selectedTeam = 0//team to sign up with

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        editusername = findViewById(R.id.edit_username)
        editpassword = findViewById(R.id.edit_password)
        repeatpassword = findViewById(R.id.repeat_password)
        progressBar = findViewById(R.id.progress_bar)

        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.sign_up_button).setOnClickListener(this)//sign up
        findViewById<TextView>(R.id.log_in_textView).setOnClickListener(this)//back to login page

        val listItemsTxt = arrayOf(" Gold-Creeper", " Coin-Carver")//array of team names
        val images = arrayOf(R.mipmap.goldcreeper, R.mipmap.coincarver)//array of team logos

        //set up spinner for team selection
        val spinnerAdapter = CustomDropDownAdapter(this, listItemsTxt, images)
        val spinner: Spinner = this.findViewById(R.id.team_spinner) as Spinner
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedTeam = p2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {//goldcreeper is default team
                selectedTeam = 0
            }
        }
    }

    private fun register() {//called on sign up button click
        val username: String = editusername?.text.toString().trim() + "@useless.com"
        val password: String = editpassword?.text.toString().trim()
        val rpassword: String = repeatpassword?.text.toString().trim()

        //check conditions for password and username
        if (username.isEmpty()) {
            editusername?.error = "Please enter a username"
            editusername?.requestFocus()
            return
        }
        if (password.length < 5) {
            editpassword?.error = "Password is to short"
            editpassword?.requestFocus()
            return
        }
        if (password != rpassword) {
            editpassword?.error = "Password does not match password confirmation"
            editpassword?.requestFocus()
            return
        }

        progressBar?.visibility = View.VISIBLE//loading circle becomes visible

        firebaseAuth?.createUserWithEmailAndPassword(username, password)?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    setUpUserAccount(username)//set up  new user account
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

    private fun setUpUserAccount(username: String) {// Creates a new user profile
        val user = HashMap<String, Any>()
        val gson = Gson()
        user["user"] = username
        user["lD"] = "" //last date of map download
        user["exchangeCount"] = 0 //number of exchanged coins today
        user["gold"] = 0.0f //gold in the bank
        user["wallet"] = gson.toJson(Wallet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf()))//contains all coins collected
        user["nastycoins"] = gson.toJson(arrayListOf<NastyCoin>())//coins available for collection
        user["movingSac"] = true//moving sac event available
        user["team"] = selectedTeam//set selected team
        user["plus"] = 0 //plus in gold from trade, increases when offer accepted

        // Add user to firestore and set up variables in ProfileActivity
        FirebaseFirestore.getInstance().collection("users").document(username)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Sign up successful", Toast.LENGTH_SHORT).show()
                    ProfileActivity.downloadDate = ""
                    ProfileActivity.exchangedCount = 0
                    ProfileActivity.gold = 0.0f
                    ProfileActivity.nastycoins = arrayListOf()
                    ProfileActivity.wallet = Wallet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())
                    ProfileActivity.team = selectedTeam
                    SubFragmentEvents.eventAvailability = true
                    progressBar?.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, ProfileActivity()::class.java))
                }
                .addOnFailureListener { e -> Log.w("SignUpActivity", "Error adding document", e) }
    }

    override fun onClick(p0: View?) {//on click of button or link
        when (p0?.id) {
            R.id.sign_up_button -> register()
            R.id.log_in_textView -> {
                finish()
                startActivity(Intent(this, MainActivity()::class.java))
            }
        }
    }
}