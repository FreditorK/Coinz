package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class MakeanOffer : AppCompatActivity(), AdapterView.OnItemClickListener, View.OnClickListener {

    private lateinit var aList: ArrayList<HashMap<String, String>>//contains coin data to be displayed
    private lateinit var selectedCoins: ArrayList<NastyCoin>//list of coins currently in the offer you want to make
    private lateinit var listCoins: ArrayList<NastyCoin>//contains the coins available for trade
    private lateinit var goldamount: EditText//contains amouny of gold you want to receive from the trade
    private lateinit var submitbutton: Button//submit offer
    private lateinit var backbutton: Button//cancel offer and go back to depot
    private lateinit var simpleAdapter: SimpleAdapter//adapter for the listview containing the coins

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.make_an_offer)

        aList = ArrayList()
        selectedCoins = ArrayList()
        listCoins = ArrayList()

        //retrieve all coins that are tradable and put them in the hashmaps ready for display
        for (i in ProfileActivity.wallet.shilCoins) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = i.id
            hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].SHIL).toString()
            hm["listview_image"] = getFittingIconId(i).toString()
            aList.add(hm)
            listCoins.add(i)
        }
        for (i in ProfileActivity.wallet.dolrCoins) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = i.id
            hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].DOLR).toString()
            hm["listview_image"] = getFittingIconId(i).toString()
            aList.add(hm)
            listCoins.add(i)
        }
        for (i in ProfileActivity.wallet.quidCoins) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = i.id
            hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].QUID).toString()
            hm["listview_image"] = getFittingIconId(i).toString()
            aList.add(hm)
            listCoins.add(i)
        }
        for (i in ProfileActivity.wallet.penyCoins) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = i.id
            hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].PENY).toString()
            hm["listview_image"] = getFittingIconId(i).toString()
            aList.add(hm)
            listCoins.add(i)
        }
        //set up adapter
        val from = arrayOf("listview_image", "listview_title", "listview_discription")
        val to = intArrayOf(R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description)
        simpleAdapter = SimpleAdapter(baseContext, aList, R.layout.list_item, from, to)
        //set up listview
        val androidListView = findViewById<ListView>(R.id.list_view)
        androidListView.adapter = simpleAdapter
        androidListView.onItemClickListener = this

        submitbutton = findViewById(R.id.submit_button)
        backbutton = findViewById(R.id.back_button)
        goldamount = findViewById(R.id.gold_amount)
        submitbutton.setOnClickListener(this)
        backbutton.setOnClickListener(this)
    }

    private fun getFittingIconId(n: NastyCoin): Int {//Get the Icon to display on the map from Coin
        return application.resources.getIdentifier(n.currency + n.markersymbol, "mipmap", application.packageName)
    }

    private fun selectOrUnselect(view: View?, pos: Int) {//manages selecting and unselecting coins for the offer
        if (selectedCoins.contains(listCoins[pos])) {//unselect
            view?.background = getDrawable(R.color.colorPrimaryDark)
            selectedCoins.remove(listCoins[pos])
        } else {//select
            view?.background = getDrawable(R.color.colorAccent)
            selectedCoins.add(listCoins[pos])
        }
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {//item in listview is clicked
        selectOrUnselect(p1, p2)
    }

    private fun removeCoinsFromWallet() {//on submitting offer remove selected coins from wallet
        for (n in selectedCoins) {
            when (n.currency) {
                "shil" -> ProfileActivity.wallet.shilCoins.remove(n)
                "dolr" -> ProfileActivity.wallet.dolrCoins.remove(n)
                "quid" -> ProfileActivity.wallet.quidCoins.remove(n)
                "peny" -> ProfileActivity.wallet.penyCoins.remove(n)
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.back_button -> {//cancel and return to depot
                val i = Intent(applicationContext, ProfileActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(i, 0)
                finish()
            }
            R.id.submit_button -> {//submit offer
                val offer = HashMap<String, Any>()
                val username = FirebaseAuth.getInstance().currentUser?.email.toString()
                val gold = goldamount.text.toString().toDoubleOrNull()//do nothing if no number is entered
                if (gold != null) {
                    val gson = Gson()
                    offer["user"] = username
                    offer["gold"] = gold
                    offer["nastycoins"] = gson.toJson(selectedCoins)

                    //upload offer to firestore and return to depot
                    val db = FirebaseFirestore.getInstance()
                    db.collection("trade_offers")
                            .add(offer)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "Offer placed", Toast.LENGTH_SHORT).show()
                                removeCoinsFromWallet()
                                val i = Intent(applicationContext, ProfileActivity::class.java)
                                i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                startActivityIfNeeded(i, 0)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("SignUpActivity", "Error adding document", e)
                                Toast.makeText(applicationContext, "Offer could not be placed. Try again later.", Toast.LENGTH_SHORT).show()
                                val i = Intent(applicationContext, ProfileActivity::class.java)
                                i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                startActivityIfNeeded(i, 0)
                                finish()
                            }
                } else {
                    Toast.makeText(applicationContext, "Please enter a number!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}