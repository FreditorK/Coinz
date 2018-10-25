package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.exchange_pop_up.view.*


class Exchange_Pop_Up : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var aList : ArrayList<HashMap<String, String>>
    private lateinit var broke_text : TextView
    private lateinit var back_button : Button
    private lateinit var simpleAdapter : SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exchange_pop_up)

        aList = ArrayList()

        when(intent.getStringExtra("currency")){
            "shil" ->{
                for(i in ProfileActivity.wallet.shilCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].SHIL).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "dolr" ->{
                for(i in ProfileActivity.wallet.dolrCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].DOLR).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "quid" ->{
                for(i in ProfileActivity.wallet.quidCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].QUID).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "peny" ->{
                for(i in ProfileActivity.wallet.penyCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].PENY).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
        }
        val from = arrayOf("listview_image", "listview_title", "listview_discription")
        val to = intArrayOf(R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description)
        simpleAdapter = SimpleAdapter(baseContext, aList, R.layout.list_item, from, to)
        val androidListView = findViewById<ListView>(R.id.list_view)
        androidListView.adapter = simpleAdapter
        androidListView.setOnItemClickListener(this)
        if(aList.size == 0){
            setContentView(R.layout.exchange_cover_up)
        }
        broke_text = findViewById(R.id.broke_text)
        broke_text.text = "You have an exchange capacity of " + (25 - ProfileActivity.exchangedCount).toString()
        back_button = findViewById(R.id.back_button)
        back_button.setOnClickListener{
            val i = Intent(applicationContext, ProfileActivity :: class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(i, 0)
            finish()
        }
    }

    fun getFittingIconId(n : NastyCoin): Int{//Get the Icon to display on the map from Coin
        return  resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", packageName)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (ProfileActivity.exchangedCount < 25) {
            when (intent.getStringExtra("currency")) {
                "shil" -> {
                    ProfileActivity.gold += ProfileActivity.wallet.shilCoins[p2].value * ProfileActivity.coinExchangeRates!![0].SHIL
                    ProfileActivity.wallet.shilCoins.removeAt(p2)
                }
                "dolr" -> {
                    ProfileActivity.gold += ProfileActivity.wallet.dolrCoins[p2].value * ProfileActivity.coinExchangeRates!![0].DOLR
                    ProfileActivity.wallet.dolrCoins.removeAt(p2)
                }
                "quid" -> {
                    ProfileActivity.gold += ProfileActivity.wallet.quidCoins[p2].value * ProfileActivity.coinExchangeRates!![0].QUID
                    ProfileActivity.wallet.quidCoins.removeAt(p2)
                }
                "peny" -> {
                    ProfileActivity.gold += ProfileActivity.wallet.penyCoins[p2].value * ProfileActivity.coinExchangeRates!![0].PENY
                    ProfileActivity.wallet.penyCoins.removeAt(p2)
                }
            }
            ProfileActivity.exchangedCount++
            broke_text.text = "You have an exchange capacity of " + (25 - ProfileActivity.exchangedCount).toString()
            aList.removeAt(p2)
            p0?.removeViewInLayout(p1)
            simpleAdapter.notifyDataSetChanged()
        }else{
            Toast.makeText(this, "You can only exchange a capacity of 25 a day!", Toast.LENGTH_SHORT).show()
        }
    }
}