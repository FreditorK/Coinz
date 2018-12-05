package com.example.kelbel.frederik.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.truncate


class ProfileActivity : AppCompatActivity() , OnCompleted {//This activity is the main activity of action

    var bottomNavigationView: BottomNavigationView? = null //bottom navigation between depot, map and settings
    private var startFrame: FrameLayout? = null//frame to be filled by the fragments

    //fragments
    private var fragmentDepot: FragmentDepot? = null
    private var fragmentMap: FragmentMap? = null
    private var fragmentSettings: FragmentSettings? = null

    private lateinit var user: String//username plus @useless.com

    companion object {
        //collection of the users data
        var team: Int = 0//current team
        var downloadDate: String = ""//date of last map download
        var exchangedCount: Int = 0//number of coins exchanged today
        var gold: Float = 0.0f//amount of gold in the bank
        lateinit var wallet: Wallet//local wallet
        lateinit var nastycoins: ArrayList<NastyCoin>//coins from the map that have not been collected yet
        var coinExchangeRates: ArrayList<CoinExchangeRates> = ArrayList()//exchange rates this week

        //exclusively for test purposes
        @SuppressLint("StaticFieldLeak")
        @VisibleForTesting
        var h: BrowseOffers.TradeOfferHolder? = null//test-variable that enables espresso to swipe the swipe button
        @JvmField
        var isTest: Boolean = false //makes the set up for tests possible
        //-----------------------------

        fun collect(n: NastyCoin) {//called when coin is collected on the map
            when (n.currency) {
                "shil" -> wallet.shilCoins.add(n)
                "dolr" -> wallet.dolrCoins.add(n)
                "quid" -> wallet.quidCoins.add(n)
                "peny" -> wallet.penyCoins.add(n)
            }
        }

        fun catchMovingSac() {//called when moving sac is catched in moving sac event
            val r = Random()
            val i = 1 + r.nextInt(10)
            for (k in 0..i) {
                val l = r.nextInt(4)
                when (l) {
                    0 -> wallet.shilCoins.add(randomCoin(l, k))
                    1 -> wallet.dolrCoins.add(randomCoin(l, k))
                    2 -> wallet.quidCoins.add(randomCoin(l, k))
                    3 -> wallet.penyCoins.add(randomCoin(l, k))
                }
            }
        }

        private fun randomCoin(l: Int, k: Int): NastyCoin {//generates a random coin for the moving sac reward
            val r = Random()
            var s = "shil"
            when (l) {
                0 -> s = "shil"
                1 -> s = "dolr"
                2 -> s = "quid"
                3 -> s = "peny"
            }
            val v = 10 * r.nextDouble()
            return NastyCoin("cI-" + System.currentTimeMillis().toString() + k.toString(), v.toFloat(), s, truncate(v).toInt().toString(), Pair(0.0, 0.0))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        startFrame = findViewById(R.id.start_frame)
        bottomNavigationView = findViewById(R.id.navigation_bar)

        user = FirebaseAuth.getInstance().currentUser?.email.toString()

        if (isTest) {
            setUpTestEnvironment()
            setUpFragments()
        } else {
            getLocalAndWebData()//download exchange rates and new map
        }

    }

    private fun setUpFragments() {//set up fragments and bottom navigation
        fragmentDepot = FragmentDepot()
        fragmentMap = FragmentMap()
        fragmentSettings = FragmentSettings()
        displayFragmentDepot()//depot is the fragment visible on opening the app

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
    }

    private fun displayFragmentDepot() {//displays depot
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentDepot!!.isAdded) {
            ft.show(fragmentDepot)
        } else {
            ft.add(R.id.start_frame, fragmentMap, "B")//codependent fragments, otherwise we get null pointer exception for some variables
            ft.add(R.id.start_frame, fragmentDepot, "A")
        }
        if (fragmentMap!!.isAdded) {
            ft.hide(fragmentMap)
        }
        if (fragmentSettings!!.isAdded) {
            ft.hide(fragmentSettings)
        }
        ft.commit()
    }

    private fun displayFragmentMap() {//displays map
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentMap!!.isAdded) {
            ft.show(fragmentMap)
        } else {
            ft.add(R.id.start_frame, fragmentMap, "B")
        }
        if (fragmentDepot!!.isAdded) {
            ft.hide(fragmentDepot)
        }
        if (fragmentSettings!!.isAdded) {
            ft.hide(fragmentSettings)
        }
        ft.commit()
    }

    private fun displayFragmentSettings() {//displays settings
        val ft = supportFragmentManager.beginTransaction()
        if (fragmentSettings!!.isAdded) {
            ft.show(fragmentSettings)
        } else {
            ft.add(R.id.start_frame, fragmentSettings, "C")
        }
        if (fragmentMap!!.isAdded) {
            ft.hide(fragmentMap)
        }
        if (fragmentDepot!!.isAdded) {
            ft.hide(fragmentDepot)
        }
        ft.commit()
    }

    private fun getCurrentDate(): String {//gets current date as string in fitting format
        return SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Calendar.getInstance().time).toString()
    }

    override fun onPause() {
        super.onPause()
        saveProgress()//saves progress on pausing/quitting the game
    }

    private fun saveProgress() {//saves progress to firestore
        if(!isTest) {
            val gson = Gson()
            val sharedprefsEditor = getSharedPreferences("General", Context.MODE_PRIVATE).edit()
            sharedprefsEditor.putString("ER", gson.toJson(coinExchangeRates))
            sharedprefsEditor.apply()

            //firestore automatically retries to update if connection is bad, otherwise automatically stored in cache
            val db = FirebaseFirestore.getInstance().collection("users").document(user)
            db.update("lD", downloadDate)
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving downloadDate", e)
                    }
            db.update("exchangeCount", exchangedCount)
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving exchangeCount", e)
                    }
            db.update("gold", gold)
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving gold", e)
                    }
            db.update("wallet", gson.toJson(wallet))
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving wallet", e)
                    }
            db.update("nastycoins", gson.toJson(nastycoins))
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving nastycoins", e)
                    }
            db.update("movingSac", SubFragmentEvents.eventAvailability)
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed saving movingSac", e)
                    }
        }
    }

    override fun onTaskCompleted() {
        downloadDate = getCurrentDate()
        fragmentMap?.addMarkers()
        fragmentMap?.drawPolygon()
    }

    private fun getLocalAndWebData() {//retrieve data on event availability, exchange rates and map
        val gson = Gson()
        if (downloadDate != getCurrentDate()) {
            setUpFragments()
            DownloadFileTask(this).execute("http://homepages.inf.ed.ac.uk/stg/coinz/${getCurrentDate()}/coinzmap.geojson")
            SubFragmentEvents.eventAvailability = true
            exchangedCount = 0
        }else{
            val c = gson.fromJson<ArrayList<CoinExchangeRates>>(getSharedPreferences("General", Context.MODE_PRIVATE).getString("ER", ""), object : TypeToken<ArrayList<CoinExchangeRates>>() {}.type)
            if (c != null){
                coinExchangeRates = c
            }
            setUpFragments()
            fragmentMap?.addMarkers()
            fragmentMap?.drawPolygon()
        }
    }

    @VisibleForTesting
    fun setUpTestEnvironment() {//sets up the app for testing
        exchangedCount = 0
        gold = 100000.0f
        wallet = Wallet(arrayListOf(genCoin(0, 5.6)), arrayListOf(genCoin(1, 2.1)), arrayListOf(genCoin(2, 7.8)), arrayListOf(genCoin(3, 1.9)))
        downloadDate = "2018/11/24"
        DownloadFileTask(this).execute("http://homepages.inf.ed.ac.uk/stg/coinz/2018/11/24/coinzmap.geojson")
        exchangedCount = 0
    }

    @VisibleForTesting
    private fun genCoin(l: Int, k: Double): NastyCoin {//generates a coin of given value for given currency
        var s = "shil"
        when (l) {
            0 -> s = "shil"
            1 -> s = "dolr"
            2 -> s = "quid"
            3 -> s = "peny"
        }
        return NastyCoin("Test-" + System.currentTimeMillis().toString(), k.toFloat(), s, truncate(k).toInt().toString(), Pair(0.0, 0.0))
    }

    @VisibleForTesting
    fun postTestOffer() {//sets up an offer for testing purposes
        val offer = HashMap<String, Any>()
        val gold = 200.5
        val gson = Gson()
        offer["user"] = "testcase2@useless.com"
        offer["gold"] = gold
        val nastyCoin1 = genCoin(3, 9.6)
        val nastyCoin2 = genCoin(1, 3.2)
        nastyCoin1.id = "FirstTest1"
        nastyCoin2.id = "FirstTest2"
        offer["nastycoins"] = gson.toJson(arrayListOf(nastyCoin1, nastyCoin2))

        //upload offer to firestore and return to depot
        val db = FirebaseFirestore.getInstance()
        db.collection("trade_offers")
                .add(offer)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Test-offer placed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("ProfileActivity", "Error adding test-document", e)
                    Toast.makeText(applicationContext, "Test-offer was not placed. Check connection!", Toast.LENGTH_SHORT).show()
                }
    }

    @VisibleForTesting
    fun acceptOfferTest() {//used to accept an offer in BrowseOffers since Espresso does not seem to be able to do that
        h?.swipeButton?.toggleState()
    }

    @VisibleForTesting
    fun expandTest() {//used to click on expand button of offer in BrowseOffers since Espresso does not seem to be able to do that
        h?.expand?.performClick()
    }

    @VisibleForTesting
    fun makeAllZonesBlue() {
        /*makes all zones blue so that no problems are encountered in GatheringTest when gathering coins
        * remember: Coins can only be collected in a zone of your team*/
        for (i in 0..24) {
            FirebaseFirestore.getInstance().collection("zones").document(i.toString()).update("c", -1868855061)
        }
    }
}