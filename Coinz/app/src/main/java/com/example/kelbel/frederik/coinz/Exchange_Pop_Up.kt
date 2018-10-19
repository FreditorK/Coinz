package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView


class Exchange_Pop_Up : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var listView: ListView
    private lateinit var aList : ArrayList<HashMap<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exchange_pop_up)

        val dm : DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val width : Int = dm.widthPixels
        val height : Int = dm.heightPixels

        window.setLayout((width*.8).toInt(), (height*.6).toInt())

        aList = ArrayList()

        when(intent.getStringExtra("currency")){
            "shil" ->{
                for(i in ProfileActivity.wallet!!.shilCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].SHIL).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "dolr" ->{
                for(i in ProfileActivity.wallet!!.dolrCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].DOLR).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "quid" ->{
                for(i in ProfileActivity.wallet!!.quidCoins){
                    val hm = HashMap<String, String>()
                    hm.put("listview_title", i.id)
                    hm.put("listview_discription", "Value: " + i.value.toString() + ", Gold: " + (i.value * ProfileActivity.coinExchangeRates!![0].QUID).toString())
                    hm.put("listview_image", getFittingIconId(i).toString())
                    aList.add(hm)
                }
            }
            "peny" ->{
                for(i in ProfileActivity.wallet!!.penyCoins){
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
        val simpleAdapter = SimpleAdapter(baseContext, aList, R.layout.list_item, from, to)
        val androidListView = findViewById<ListView>(R.id.list_view)
        androidListView.adapter = simpleAdapter
        androidListView.setOnItemClickListener(this)
    }

    fun getFittingIconId(n : NastyCoin): Int{//Get the Icon to display on the map from Coin
        return  resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", packageName)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when(intent.getStringExtra("currency")) {
            "shil" -> {
                ProfileActivity.gold += ProfileActivity.wallet!!.shilCoins[p2].value * ProfileActivity.coinExchangeRates!![0].SHIL
                ProfileActivity.wallet!!.shilCoins.removeAt(p2)
            }
            "dolr" -> {
                ProfileActivity.gold += ProfileActivity.wallet!!.dolrCoins[p2].value * ProfileActivity.coinExchangeRates!![0].DOLR
                ProfileActivity.wallet!!.dolrCoins.removeAt(p2)
            }
            "quid" -> {
                ProfileActivity.gold += ProfileActivity.wallet!!.quidCoins[p2].value * ProfileActivity.coinExchangeRates!![0].QUID
                ProfileActivity.wallet!!.quidCoins.removeAt(p2)
            }
            "peny" -> {
                ProfileActivity.gold += ProfileActivity.wallet!!.penyCoins[p2].value * ProfileActivity.coinExchangeRates!![0].PENY
                ProfileActivity.wallet!!.penyCoins.removeAt(p2)
            }
        }
        aList.removeAt(p2)
        p0?.removeViewInLayout(p1)
    }
}