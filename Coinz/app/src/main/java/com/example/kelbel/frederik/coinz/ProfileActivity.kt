package com.example.kelbel.frederik.coinz

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.annotations.Icon
import kotlin.collections.ArrayList


class ProfileActivity : AppCompatActivity() {

    private var bottomNavigationView : BottomNavigationView? = null
    private var startFrame : FrameLayout? = null

    private var fragmentDepot : FragmentDepot? = null
    private var fragmentMap : FragmentMap? = null
    private var fragmentSettings : FragmentSettings? = null

    private var downloadDate : String = ""

    private lateinit var user : String

    companion object {
        var gold : Float = 0.0f
        var wallet : Wallet? = null
        var nastycoins : ArrayList<NastyCoin>? = null
        var coinExchangeRates : ArrayList<CoinExchangeRates>? = null
        fun collect(n : NastyCoin, i : Icon){
                when(n.currency){
                    "shil" -> wallet!!.shilCoins.add(n)
                    "dolr" -> wallet!!.dolrCoins.add(n)
                    "quid" -> wallet!!.quidCoins.add(n)
                    "peny" -> wallet!!.penyCoins.add(n)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        startFrame = findViewById(R.id.start_frame)
        bottomNavigationView = findViewById(R.id.navigation_bar)

        user = FirebaseAuth.getInstance().currentUser?.email.toString()

        val mPrefs = getPreferences(Context.MODE_PRIVATE)
        downloadDate = mPrefs.getString("lD", "").toString()
        val gson = Gson()
        var json : String
        if (downloadDate != getCurrentDate()){
            downloadDate = getCurrentDate()
            json = mPrefs.getString("ER", "")
            coinExchangeRates = gson.fromJson<ArrayList<CoinExchangeRates>>(json, object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
            if(coinExchangeRates == null){
                coinExchangeRates = ArrayList<CoinExchangeRates>()
            }
            DownloadFileTask(DownloadCompleteRunner, this).execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson")
        }else{
            if(mPrefs.contains("NC" + user)) {
                json = mPrefs.getString("NC" + user, "")
                nastycoins = gson.fromJson<ArrayList<NastyCoin>>(json, object : TypeToken<ArrayList<NastyCoin>>() {}.type)
            }else{
                DownloadFileTask.loadGeoJson(this)
            }
            json = mPrefs.getString("ER", "")
            coinExchangeRates = gson.fromJson<ArrayList<CoinExchangeRates>>(json, object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
        }

        val json2 = mPrefs.getString("Wallet" + user, "")
        if(json2 != ""){
            wallet = gson.fromJson<Wallet>(json2, Wallet::class.java)
        }else{
            wallet = Wallet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())
        }

        gold = mPrefs.getFloat("Gold" + user, 0.0f)

        fragmentDepot = FragmentDepot()
        fragmentMap = FragmentMap()
        fragmentSettings = FragmentSettings()

        bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.depot_tab -> displayFragmentDepot()
                R.id.map_tab -> displayFragmentMap()
                R.id.settings_tab -> displayFragmentSettings()
            }
            return@setOnNavigationItemSelectedListener true
        }
        displayFragmentDepot()
    }

    protected fun displayFragmentDepot() {
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentDepot!!.isAdded()) {
            ft.show(fragmentDepot)
        } else {
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
    protected fun displayFragmentMap() {
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

    protected fun displayFragmentSettings() {
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

    override fun onStop() {
        super.onStop()
        //save Wallet
        val mPrefs = getPreferences(Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        val gson = Gson()
        var json = gson.toJson(wallet)
        prefsEditor.putString("Wallet" + user, json)
        json = gson.toJson(nastycoins)
        prefsEditor.putString("NC" + user, json)
        json = gson.toJson(coinExchangeRates)
        prefsEditor.putString("ER", json)
        prefsEditor.putString("lD", downloadDate)
        prefsEditor.putFloat("Gold" + user, gold)
        prefsEditor.apply()
    }
}