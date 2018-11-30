package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*


class ExchangePopUp : AppCompatActivity(), AdapterView.OnItemClickListener {//activity to exchange coins you're currently holding

    private lateinit var aList : ArrayList<HashMap<String, String>>//contains coin descriptions
    private lateinit var broketext : TextView//dispalys that you do not have any coins to exchange
    private lateinit var backbutton : Button//go back to depot
    private lateinit var simpleAdapter : SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exchange_pop_up)

        aList = ArrayList()

        when(intent.getStringExtra("currency")){//button in exchange tab defines what is gonna get exchanged
            "shil" ->{//add all shil to list
                for(i in ProfileActivity.wallet.shilCoins){
                    val hm = HashMap<String, String>()
                    hm["listview_title"] = i.id
                    hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].SHIL).toString()
                    hm["listview_image"] = getFittingIconId(i).toString()
                    aList.add(hm)
                }
            }
            "dolr" ->{//add all dolr to list
                for(i in ProfileActivity.wallet.dolrCoins){
                    val hm = HashMap<String, String>()
                    hm["listview_title"] = i.id
                    hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].DOLR).toString()
                    hm["listview_image"] = getFittingIconId(i).toString()
                    aList.add(hm)
                }
            }
            "quid" ->{//add all quid to list
                for(i in ProfileActivity.wallet.quidCoins){
                    val hm = HashMap<String, String>()
                    hm["listview_title"] =  i.id
                    hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].QUID).toString()
                    hm["listview_image"] = getFittingIconId(i).toString()
                    aList.add(hm)
                }
            }
            "peny" ->{//add all peny to list
                for(i in ProfileActivity.wallet.penyCoins){
                    val hm = HashMap<String, String>()
                    hm["listview_title"] = i.id
                    hm["listview_discription"] = "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].PENY).toString()
                    hm["listview_image"] = getFittingIconId(i).toString()
                    aList.add(hm)
                }
            }
        }
        val from = arrayOf("listview_image", "listview_title", "listview_discription")
        val to = intArrayOf(R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description)
        simpleAdapter = SimpleAdapter(baseContext, aList, R.layout.list_item, from, to)
        val androidListView = findViewById<ListView>(R.id.list_view)
        androidListView.adapter = simpleAdapter//set adapter for listview
        androidListView.onItemClickListener = this
        if(aList.size == 0){//display message if no coins are in the lsit
            setContentView(R.layout.exchange_cover_up)
        }
        broketext = findViewById(R.id.broke_text)
        val s = "You have an exchange capacity of " + (25 - ProfileActivity.exchangedCount).toString()
        broketext.text = s
        backbutton = findViewById(R.id.back_button)
        backbutton.setOnClickListener{
            val i = Intent(applicationContext, ProfileActivity :: class.java)
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(i, 0)
            finish()
        }
    }

    private fun getFittingIconId(n : NastyCoin): Int{//Get the Icon to display on the map from Coin
        return  resources.getIdentifier(n.currency + n.markersymbol, "mipmap", packageName)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {//exchange onclick of coin
        var bool = false
        when (intent.getStringExtra("currency")) {//get which currency should be exchanged
            "shil" -> {
                bool = ProfileActivity.wallet.shilCoins[p2].id.matches(Regex(".*TRADED"))//traded coins can always get exchanged
                if (ProfileActivity.exchangedCount < 25 || bool) {
                    ProfileActivity.gold += ProfileActivity.wallet.shilCoins[p2].value * ProfileActivity.coinExchangeRates!![0].SHIL
                    ProfileActivity.wallet.shilCoins.removeAt(p2)
                    aList.removeAt(p2)
                    p0?.removeViewInLayout(p1)
                    simpleAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this, "You can only exchange a capacity of 25 a day!", Toast.LENGTH_SHORT).show()
                }
            }
            "dolr" -> {
                bool = ProfileActivity.wallet.dolrCoins[p2].id.matches(Regex(".*TRADED"))//traded coins can always get exchanged
                if (ProfileActivity.exchangedCount < 25 || bool) {//check if 25 coins have been exchanged already
                    ProfileActivity.gold += ProfileActivity.wallet.dolrCoins[p2].value * ProfileActivity.coinExchangeRates!![0].DOLR
                    ProfileActivity.wallet.dolrCoins.removeAt(p2)
                    aList.removeAt(p2)
                    p0?.removeViewInLayout(p1)
                    simpleAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this, "You can only exchange a capacity of 25 a day!", Toast.LENGTH_SHORT).show()
                }
            }
            "quid" -> {
                bool = ProfileActivity.wallet.quidCoins[p2].id.matches(Regex(".*TRADED"))//traded coins can always get exchanged
                if (ProfileActivity.exchangedCount < 25 || bool) {//check if 25 coins have been exchanged already
                    ProfileActivity.gold += ProfileActivity.wallet.quidCoins[p2].value * ProfileActivity.coinExchangeRates!![0].QUID
                    ProfileActivity.wallet.quidCoins.removeAt(p2)
                    aList.removeAt(p2)
                    p0?.removeViewInLayout(p1)
                    simpleAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this, "You can only exchange a capacity of 25 a day!", Toast.LENGTH_SHORT).show()
                }
            }
            "peny" -> {
                bool = ProfileActivity.wallet.penyCoins[p2].id.matches(Regex(".*TRADED"))//traded coins can always get exchanged
                if (ProfileActivity.exchangedCount < 25 || bool) {//check if 25 coins have been exchanged already
                    ProfileActivity.gold += ProfileActivity.wallet.penyCoins[p2].value * ProfileActivity.coinExchangeRates!![0].PENY
                    ProfileActivity.wallet.penyCoins.removeAt(p2)
                    aList.removeAt(p2)
                    p0?.removeViewInLayout(p1)
                    simpleAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(this, "You can only exchange a capacity of 25 a day!", Toast.LENGTH_SHORT).show()
                }
            }
        }
           if(!bool) {//If the coin is not a traded/messaged coin
               ProfileActivity.exchangedCount++
               val s = "You have an exchange capacity of "  + (25 - ProfileActivity.exchangedCount).toString()
               broketext.text = s
           }
    }
}