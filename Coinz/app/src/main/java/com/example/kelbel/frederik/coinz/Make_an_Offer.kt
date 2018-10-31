package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class Make_an_Offer : AppCompatActivity(), AdapterView.OnItemClickListener, View.OnClickListener {

    private lateinit var aList : ArrayList<HashMap<String, String>>
    private lateinit var selectedCoins : ArrayList<NastyCoin>
    private lateinit var listCoins : ArrayList<NastyCoin>
    private lateinit var gold_amount : EditText
    private lateinit var submit_button : Button
    private lateinit var back_button : Button
    private lateinit var simpleAdapter : SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.make_an_offer)

        aList = ArrayList()
        selectedCoins = ArrayList()
        listCoins = ArrayList()

        for(i in ProfileActivity.wallet.shilCoins){
            val hm = HashMap<String, String>()
            hm.put("listview_title", i.id)
            hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].SHIL).toString())
            hm.put("listview_image", getFittingIconId(i).toString())
            aList.add(hm)
            listCoins.add(i)
        }
        for(i in ProfileActivity.wallet.dolrCoins){
            val hm = HashMap<String, String>()
            hm.put("listview_title", i.id)
            hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].DOLR).toString())
            hm.put("listview_image", getFittingIconId(i).toString())
            aList.add(hm)
            listCoins.add(i)
        }
        for(i in ProfileActivity.wallet.quidCoins){
            val hm = HashMap<String, String>()
            hm.put("listview_title", i.id)
            hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].QUID).toString())
            hm.put("listview_image", getFittingIconId(i).toString())
            aList.add(hm)
            listCoins.add(i)
        }
        for(i in ProfileActivity.wallet.penyCoins) {
            val hm = HashMap<String, String>()
            hm.put("listview_title", i.id)
            hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].PENY).toString())
            hm.put("listview_image", getFittingIconId(i).toString())
            aList.add(hm)
            listCoins.add(i)
        }
        val from = arrayOf("listview_image", "listview_title", "listview_discription")
        val to = intArrayOf(R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description)
        simpleAdapter = SimpleAdapter(baseContext, aList, R.layout.list_item, from, to)
        val androidListView = findViewById<ListView>(R.id.list_view)
        androidListView.adapter = simpleAdapter
        androidListView.setOnItemClickListener(this)
        submit_button = findViewById(R.id.submit_button)
        back_button = findViewById(R.id.back_button)
        gold_amount = findViewById(R.id.gold_amount)
        submit_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
    }

    fun getFittingIconId(n : NastyCoin): Int{//Get the Icon to display on the map from Coin
        return  resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", packageName)
    }

    fun selectOrUnselect(view : View?, pos : Int){
        if(selectedCoins.contains(listCoins[pos])){
            view?.background = getDrawable(R.color.colorPrimary)
            selectedCoins.remove(listCoins[pos])
        }else{
            view?.background = getDrawable(R.color.colorAccent)
            selectedCoins.add(listCoins[pos])
        }
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        selectOrUnselect(p1, p2)
    }

    fun removeCoinsFromWallet(){
        for(n in selectedCoins){
            when(n.currency){
                "shil" -> ProfileActivity.wallet.shilCoins.remove(n)
                "dolr" -> ProfileActivity.wallet.dolrCoins.remove(n)
                "quid" -> ProfileActivity.wallet.quidCoins.remove(n)
                "peny" -> ProfileActivity.wallet.penyCoins.remove(n)
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.back_button ->{
                val i = Intent(applicationContext, ProfileActivity :: class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivityIfNeeded(i, 0)
                finish()
            }
            R.id.submit_button -> {
                val offer = HashMap<String, Any>()
                val username = FirebaseAuth.getInstance().currentUser?.email.toString()
                val gold = gold_amount.text.toString().toDouble()
                val gson = Gson()
                offer.put("user", username)
                offer.put("gold", gold)
                offer.put("nastycoins", gson.toJson(selectedCoins))

                val db = FirebaseFirestore.getInstance()
                db.collection("trade_offers")
                        .add(offer)
                        .addOnSuccessListener({
                            Toast.makeText(applicationContext, "Offer placed", Toast.LENGTH_SHORT).show()
                            removeCoinsFromWallet()
                            val i = Intent(applicationContext, ProfileActivity :: class.java)
                            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            startActivityIfNeeded(i, 0)
                            finish()
                        })
                        .addOnFailureListener({
                            e -> Log.w("SignUpActivity", "Error adding document", e)
                            Toast.makeText(applicationContext, "Offer could not be placed. Try again later.", Toast.LENGTH_SHORT).show()
                            val i = Intent(applicationContext, ProfileActivity :: class.java)
                            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            startActivityIfNeeded(i, 0)
                            finish()
                        })
            }
        }
    }
}