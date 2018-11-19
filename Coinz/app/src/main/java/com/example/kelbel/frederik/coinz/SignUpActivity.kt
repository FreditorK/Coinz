package com.example.kelbel.frederik.coinz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import android.util.Log
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson


class SignUpActivity : AppCompatActivity(), View.OnClickListener{

    var progressBar : ProgressBar? = null
    var edit_username : EditText? = null
    var edit_password : EditText? = null
    var repeat_password : EditText? = null

    private var firebaseAuth : FirebaseAuth? = null

    private var selectedTeam = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        edit_username = findViewById(R.id.edit_username)
        edit_password = findViewById(R.id.edit_password)
        repeat_password = findViewById(R.id.repeat_password)
        progressBar = findViewById(R.id.progress_bar)

        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.sign_up_button).setOnClickListener(this)
        findViewById<TextView>(R.id.log_in_textView).setOnClickListener(this)

        val listItemsTxt = arrayOf(" Gold-Creeper", " Coin-Carver")
        val images = arrayOf(R.mipmap.goldcreeper, R.mipmap.coincarver)

        val spinnerAdapter: CustomDropDownAdapter = CustomDropDownAdapter(this, listItemsTxt, images)
        val spinner: Spinner = this.findViewById(R.id.team_spinner) as Spinner
        spinner.adapter = spinnerAdapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedTeam = p2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedTeam = 0
            }
        })
    }

    private fun register(){
        val username : String = edit_username?.text.toString().trim() + "@useless.com"
        val password : String = edit_password?.text.toString().trim()
        val rpassword : String = repeat_password?.text.toString().trim()

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
        if (password != rpassword){
            edit_password?.error = "Password does not match password confirmation"
            edit_password?.requestFocus()
            return
        }

        progressBar?.visibility = View.VISIBLE

        firebaseAuth?.createUserWithEmailAndPassword(username, password)?.addOnCompleteListener({ task ->
            when {
                task.isSuccessful -> {
                    setUpUserAccount(username)
                }
                else -> {
                    when{
                        task.exception is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(applicationContext, "Already registered. Please Log in", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE}
                        else -> {Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE}
                    }
                }
            }
        })
    }

    private fun setUpUserAccount(username : String){
        // Create a new user with a first, middle, and last name
        val user = HashMap<String, Any>()
        val gson = Gson()
        user.put("user", username)
        user.put("lD", "")
        user.put("exchangeCount", 0)
        user.put("gold", 0.0f)
        user.put("wallet", gson.toJson(Wallet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())))
        user.put("nastycoins", gson.toJson(arrayListOf<NastyCoin>()))
        user.put("movingSac", true)
        user.put("team", selectedTeam)

        // Add a new document with a generated ID
        FirebaseFirestore.getInstance().collection("users").document(username)
                .set(user)
                .addOnSuccessListener({
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
                    startActivity(Intent(this, ProfileActivity() :: class.java))})
                .addOnFailureListener({ e -> Log.w("SignUpActivity", "Error adding document", e) })
    }
    override fun onClick(p0: View?) {//not done here
        when (p0?.id){
            R.id.sign_up_button -> register()
            R.id.log_in_textView -> {finish()
                                     startActivity(Intent(this, MainActivity() :: class.java))}
        }
    }
}