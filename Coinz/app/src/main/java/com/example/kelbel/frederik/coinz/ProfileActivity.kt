package com.example.kelbel.frederik.coinz

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.annotations.Icon
import java.io.File
import kotlin.collections.ArrayList
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import android.provider.SyncStateContract.Helpers.update
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.truncate


class ProfileActivity : AppCompatActivity() {

    private var bottomNavigationView : BottomNavigationView? = null
    private var startFrame : FrameLayout? = null

    private var fragmentDepot : FragmentDepot? = null
    private var fragmentMap : FragmentMap? = null
    private var fragmentSettings : FragmentSettings? = null

    private lateinit var user : String

    companion object {
        var downloadDate : String = ""
        var exchangedCount : Int = 0
        var gold : Float = 0.0f
        lateinit var wallet : Wallet
        lateinit var nastycoins : ArrayList<NastyCoin>
        var coinExchangeRates : ArrayList<CoinExchangeRates>? = null
        fun collect(n : NastyCoin){
                when(n.currency){
                    "shil" -> wallet.shilCoins.add(n)
                    "dolr" -> wallet.dolrCoins.add(n)
                    "quid" -> wallet.quidCoins.add(n)
                    "peny" -> wallet.penyCoins.add(n)
                }
        }
        fun catchMovingSac(){
            val r = Random()
            val i = 1 + r.nextInt(10)
            for(k in 0..i) {
                val l = r.nextInt(4)
                when (l) {
                    0 -> wallet.shilCoins.add(randomCoin(l, k))
                    1 -> wallet.dolrCoins.add(randomCoin(l, k))
                    2 -> wallet.quidCoins.add(randomCoin(l, k))
                    3 -> wallet.penyCoins.add(randomCoin(l, k))
                }
            }
        }
        private fun randomCoin(l : Int, k : Int) : NastyCoin{
            val r = Random()
            var s  = "shil"
            when(l){
                0 -> s = "shil"
                1 -> s = "dolr"
                2 -> s = "quid"
                3 -> s = "peny"
            }
            val v = 10*r.nextDouble()
            val n =NastyCoin("cI-" + System.currentTimeMillis().toString() + k.toString(), v.toFloat(), s, truncate(v).toInt().toString(), Pair(0.0, 0.0))
            return n
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        startFrame = findViewById(R.id.start_frame)
        bottomNavigationView = findViewById(R.id.navigation_bar)

        user = FirebaseAuth.getInstance().currentUser?.email.toString()

        getLocalAndWebData()

    }

    fun setUpFragments(){
        fragmentDepot = FragmentDepot()
        fragmentMap = FragmentMap()
        fragmentSettings = FragmentSettings()

        bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.depot_tab -> {
                    displayFragmentDepot()
                    fragmentDepot!!.displayValues()
                }
                R.id.map_tab -> displayFragmentMap()
                R.id.settings_tab -> displayFragmentSettings()
            }
            return@setOnNavigationItemSelectedListener true
        }
        displayFragmentDepot()
    }

    fun displayFragmentDepot() {
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentDepot!!.isAdded()) {
            ft.show(fragmentDepot)
        } else {
            ft.add(R.id.start_frame, fragmentMap, "B")//codependent fragments
            ft.add(R.id.start_frame, fragmentDepot, "A")
        }
        if (fragmentMap!!.isAdded()) {
            ft.hide(fragmentMap)
        }
        if (fragmentSettings!!.isAdded()) {
            ft.hide(fragmentSettings)
        }
        ft.commit()
    }
    fun displayFragmentMap() {
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentMap!!.isAdded()) {
            ft.show(fragmentMap)
        } else {
            ft.add(R.id.start_frame, fragmentMap, "B")
        }
        if (fragmentDepot!!.isAdded()) {
            ft.hide(fragmentDepot)
        }
        if (fragmentSettings!!.isAdded()) {
            ft.hide(fragmentSettings)
        }
        ft.commit()
    }
    fun displayFragmentSettings() {
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentSettings!!.isAdded()) {
            ft.show(fragmentSettings)
        } else {
            ft.add(R.id.start_frame, fragmentSettings, "C")
        }
        if (fragmentMap!!.isAdded()) {
            ft.hide(fragmentMap)
        }
        if (fragmentDepot!!.isAdded()) {
            ft.hide(fragmentDepot)
        }
        ft.commit()
    }

    fun getCurrentDate() : String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Calendar.getInstance().getTime()).toString()
    }

    override fun onPause() {
        super.onPause()
        saveProgress()
    }
    private fun saveProgress(){
        /*val mPrefs = getSharedPreferences(user ,Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        val gson = Gson()
        var json = gson.toJson(wallet)
        prefsEditor.putString("Wallet", json)
        json = gson.toJson(nastycoins)
        prefsEditor.putString("NC", json)
        prefsEditor.putFloat("Gold", gold)
        prefsEditor.putInt("EC", exchangedCount)
        prefsEditor.apply()*/

        val gson = Gson()
        val sharedprefsEditor = getSharedPreferences("General", Context.MODE_PRIVATE).edit()
        sharedprefsEditor.putString("ER", gson.toJson(coinExchangeRates))
        sharedprefsEditor.apply()

        val db = FirebaseFirestore.getInstance().collection("users").document(user)
        db.update("lD", downloadDate)
                .addOnSuccessListener({})
                .addOnFailureListener({})
        db.update("exchangeCount", exchangedCount)
                .addOnSuccessListener({})
                .addOnFailureListener({})
        db.update("gold", gold)
                .addOnSuccessListener({})
                .addOnFailureListener({})
        db.update("wallet", gson.toJson(wallet))
                .addOnSuccessListener({})
                .addOnFailureListener({})
        db.update("nastycoins", gson.toJson(nastycoins))
                .addOnSuccessListener({})
                .addOnFailureListener({})
        db.update("movingSac", SubFragmentEvents.eventAvailability)
                .addOnSuccessListener({})
                .addOnFailureListener({})
    }

    /*private fun uploadToFirebase(){
        val f = File(this.applicationInfo.dataDir + "/shared_prefs/" + user + ".xml")
        val profileRef = FirebaseStorage.getInstance().getReference("profiledata/" + user  + ".xml")
        if (f.exists()) {
            val file = Uri.fromFile(f)
            profileRef.putFile(file).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnCompleteListener{
                    task -> if(!task.isSuccessful){
                    Toast.makeText(this, "Check Connection! Progress locally saved!", Toast.LENGTH_SHORT).show()
                }
                }                        }
                    .addOnFailureListener { e ->
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "Check Connection! Progress locally saved!", Toast.LENGTH_SHORT).show()
                    }
        }
    }*/

    private fun getLocalAndWebData(){
        val gson = Gson()
        if (downloadDate != getCurrentDate()) {
            downloadDate = getCurrentDate()
            DownloadFileTask(this).execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson")
            SubFragmentEvents.eventAvailability = true
        }
        coinExchangeRates = gson.fromJson<ArrayList<CoinExchangeRates>>(getSharedPreferences("General", Context.MODE_PRIVATE).getString("ER", ""), object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
        setUpFragments()
    }

    /*private fun retrieveProgress(){
        //val mPrefs = getSharedPreferences(user, Context.MODE_PRIVATE)
        val msharedPrefs = getSharedPreferences("General", Context.MODE_PRIVATE)
        downloadDate = msharedPrefs.getString("lD", "").toString()

        val gson = Gson()
        var json : String
        if (downloadDate != getCurrentDate()){
            downloadDate = getCurrentDate()
            //exchangedCount = 0
            json = msharedPrefs.getString("ER", "")
            coinExchangeRates = gson.fromJson<ArrayList<CoinExchangeRates>>(json, object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
            if(coinExchangeRates == null){
                coinExchangeRates = ArrayList()
            }
            DownloadFileTask(this).execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson")
        }else{
            /*if(mPrefs.contains("NC")) {
                json = mPrefs.getString("NC", "")
                nastycoins = gson.fromJson<ArrayList<NastyCoin>>(json, object : TypeToken<ArrayList<NastyCoin>>() {}.type)
            }else{
                DownloadFileTask.loadGeoJson(this)
            }*/
            json = msharedPrefs.getString("ER", "")
            coinExchangeRates = gson.fromJson<ArrayList<CoinExchangeRates>>(json, object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
            //exchangedCount = mPrefs.getInt("EC", 0)
        }

        /*val json2 = mPrefs.getString("Wallet", "")
        if(json2 != ""){
            wallet = gson.fromJson<Wallet>(json2, Wallet::class.java)
        }else{
            wallet = Wallet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())
        }*/

        //gold = mPrefs.getFloat("Gold", 0.0f)
        setUpFragments()
    }*/
}