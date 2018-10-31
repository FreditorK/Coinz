package com.example.kelbel.frederik.coinz

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.list_item.view.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.annotation.NonNull
import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.*
import kotlin.coroutines.experimental.coroutineContext
import com.ebanx.swipebtn.OnActiveListener
import com.ebanx.swipebtn.OnStateChangeListener
import com.ebanx.swipebtn.SwipeButton
import com.google.firebase.auth.FirebaseAuth


class Browse_Offers : AppCompatActivity() {

    private lateinit var back_button: Button
    private lateinit var current_gold: TextView

    private lateinit var mRecyclerView: RecyclerView
    private var fRecyclerAdapter: FirestoreRecyclerAdapter<TradeOffer, TradeOfferHolder>? = null

    private lateinit var db: Query
    var offerList = mutableListOf<TradeOffer>()
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browse_offers)

        mRecyclerView = findViewById(R.id.rec_list_view)
        back_button = findViewById(R.id.back_button)
        current_gold = findViewById(R.id.current_gold)
        current_gold.text = ProfileActivity.gold.toString()
        back_button.setOnClickListener{
            val i = Intent(applicationContext, ProfileActivity :: class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(i, 0)
            finish()
        }

        db = FirebaseFirestore.getInstance().collection("trade_offers")
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.setHasFixedSize(true)

        loadOffers()

        firestoreListener = db
                .addSnapshotListener(EventListener { documentSnapshots, e ->
                    if (e != null) {
                        Log.e("Browse_Offers", "Listen failed!", e)
                        return@EventListener
                    }

                    offerList = mutableListOf()

                    if (documentSnapshots != null) {
                        for (doc in documentSnapshots) {
                                val offer = TradeOffer()
                                offer.id = doc.reference
                                offer.user = doc.get("user").toString()
                                offer.gold = doc.get("gold").toString().toDouble()
                                val gson = Gson()
                                val nastyCoins = gson.fromJson<ArrayList<NastyCoin>>(doc.getString("nastycoins"), object : TypeToken<ArrayList<NastyCoin>>() {}.type)
                                offer.children = ArrayList()
                                for (n in nastyCoins) {
                                    offer.worth += (n.value * getCurrencyValue(n.currency))
                                    val s = SubTradeOffer()
                                    s.id = n.id
                                    s.currency = n.currency
                                    s.value = n.value.toDouble()
                                    s.picRef = resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", packageName)
                                    s.marker_symbol = n.marker_symbol
                                    s.coordinates = n.coordinates
                                    offer.children?.add(s)
                                }
                                offerList.add(offer)
                        }
                    }

                    fRecyclerAdapter!!.notifyDataSetChanged()
                    mRecyclerView.adapter = fRecyclerAdapter
                })
    }

    private fun getCurrencyValue(s: String): Double {
        var d = 0.0
        when (s) {
            "shil" -> {
                d = ProfileActivity.coinExchangeRates!![0].SHIL.toDouble()
            }
            "dolr" -> {
                d = ProfileActivity.coinExchangeRates!![0].DOLR.toDouble()
            }
            "quid" -> {
                d = ProfileActivity.coinExchangeRates!![0].QUID.toDouble()
            }
            "peny" -> {
                d = ProfileActivity.coinExchangeRates!![0].PENY.toDouble()
            }
        }
        return d
    }

    private fun loadOffers() {

        val user = FirebaseAuth.getInstance().currentUser?.email
        val options = FirestoreRecyclerOptions.Builder<TradeOffer>()
                .setQuery(db, TradeOffer::class.java)
                .build()
        fRecyclerAdapter = object : FirestoreRecyclerAdapter<TradeOffer, TradeOfferHolder>(options) {
            override fun onBindViewHolder(holder: TradeOfferHolder, position: Int, offer: TradeOffer) {
                if(offerList.size > 0) {
                    if (offerList[position].user != user) {
                        val o = offerList[position]
                        holder.user?.text = "Offered by: " + o.user?.substringBefore('@')
                        holder.gold?.text = "Price: " + o.gold.toString()
                        holder.worth?.text = "Current worth: " + o.worth.toString()
                        holder.swipeButton?.setOnStateChangeListener(object : OnStateChangeListener {
                            override fun onStateChange(active: Boolean) {
                                if (o.gold!! <= ProfileActivity.gold) {
                                    executeOrder66(o)
                                } else {
                                    holder.swipeButton?.setEnabledDrawable(getDrawable(R.mipmap.denied))
                                    Toast.makeText(baseContext, "Not enough gold in depot!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                        val noOfChildTextViews = holder.child_items?.childCount
                        val noOfChild = o.children!!.size
                        if (noOfChild < noOfChildTextViews!!) {
                            for (index in noOfChild until noOfChildTextViews) {
                                val currentView = holder.child_items?.getChildAt(index) as LinearLayout
                                currentView.visibility = View.GONE
                            }
                        }
                        for (ViewIndex in 0 until noOfChild) {
                            val currentView = holder.child_items?.getChildAt(ViewIndex) as LinearLayout
                            (currentView.getChildAt(0) as ImageView).setImageResource(o.children?.get(ViewIndex)?.picRef!!)
                            val sublayout = currentView.getChildAt(1) as LinearLayout
                            (sublayout.getChildAt(0) as TextView).text = o.children?.get(ViewIndex)?.id
                            (sublayout.getChildAt(1) as TextView).text = "Value: " + o.children?.get(ViewIndex)?.value.toString()
                        }
                    }else{
                        holder.itemView.visibility = View.GONE
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeOfferHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.browse_item, parent, false)
                return TradeOfferHolder(view, offerList, baseContext)
            }

            override fun onError(e: FirebaseFirestoreException) {
                super.onError(e)
                Log.e("error", e.message)
            }

            fun executeOrder66(offer : TradeOffer){
                FirebaseFirestore.getInstance().collection("users").document(offer.user.toString())
                        .get()
                        .addOnSuccessListener {
                            val currentGold = it.getDouble("gold")
                            Log.d("goldig", offer.gold.toString() + "/" + currentGold.toString())
                            FirebaseFirestore.getInstance().collection("users").document(offer.user.toString())
                                    .update("gold", offer.gold!! + currentGold!!)
                                    .addOnSuccessListener { it2 ->
                                        offer.id?.delete()!!
                                                .addOnSuccessListener{it3 ->
                                                    Toast.makeText(applicationContext, "Trade executed", Toast.LENGTH_SHORT).show()
                                                    for(n in offer.children!!){
                                                        ProfileActivity.collect(NastyCoin(n.id!!, n.value!!.toFloat(), n.currency!!, n.marker_symbol!!, n.coordinates!!))
                                                    }
                                                    ProfileActivity.gold -= offer.gold!!.toFloat()
                                                    current_gold.text = ProfileActivity.gold.toString()
                                                }
                                                .addOnFailureListener{
                                                    e -> Log.w("Browse_Offers", "Error deleting offer", e)
                                                    Toast.makeText(applicationContext, "Trade could not be executed. Try again later.", Toast.LENGTH_SHORT).show()
                                                }
                                    }
                                    .addOnFailureListener{
                                        e -> Log.w("Browse_Offers", "Error updating gold status", e)
                                    }                        }
                        .addOnFailureListener{
                            e -> Log.w("Browse_Offers", "Error receiving gold status", e)
                        }
            }
        }

        fRecyclerAdapter!!.notifyDataSetChanged()
        mRecyclerView.adapter = fRecyclerAdapter
    }

    class TradeOfferHolder(itemView: View, offerList: MutableList<TradeOffer>, context: Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var expand: ImageView? = null
        var swipeButton: SwipeButton? = null
        var user: TextView? = null
        var gold: TextView? = null
        var worth: TextView? = null
        var child_items: LinearLayout? = null

        init {
            if(offerList.size > 0) {
                expand = itemView.findViewById(R.id.expand_coins)
                swipeButton = itemView.findViewById(R.id.swipe_btn)
                user = itemView.findViewById(R.id.listview_item_title)
                gold = itemView.findViewById(R.id.listview_item_short_description)
                worth = itemView.findViewById(R.id.worth)
                child_items = itemView.findViewById(R.id.child_items)
                child_items?.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in 0 until offerList.size) {
                    val intMaxSizeTemp = offerList[index].children!!.size
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
                }
                for (indexView in 0 until intMaxNoOfChild) {
                    val linearlay = LinearLayout(context)
                    val imageView = ImageView(context)
                    val linearlay2 = LinearLayout(context)
                    val textView = TextView(context)
                    val textView2 = TextView(context)
                    textView.setTextColor(Color.WHITE)
                    textView2.setTextColor(Color.WHITE)

                    linearlay.setPadding(0, 10, 0, 10)
                    linearlay.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearlay.orientation = LinearLayout.HORIZONTAL
                    linearlay2.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearlay2.orientation = LinearLayout.VERTICAL

                    linearlay2.addView(textView)
                    linearlay2.addView(textView2)

                    linearlay.addView(imageView)
                    linearlay.addView(linearlay2)
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    child_items?.addView(linearlay, layoutParams)
                }
                expand?.setOnClickListener(this)
            }
        }

        override fun onClick(view: View) {
            if (child_items?.visibility == View.VISIBLE) {
                child_items?.visibility = View.GONE
                (view as ImageView).setImageResource(R.drawable.ic_keyboard_arrow_up)
            } else {
                child_items?.visibility = View.VISIBLE
                (view as ImageView).setImageResource(R.drawable.ic_keyboard_arrow_down)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener!!.remove()
    }

    public override fun onStart() {
        super.onStart()

        fRecyclerAdapter!!.startListening()
    }

    public override fun onStop() {
        super.onStop()

        fRecyclerAdapter!!.stopListening()
    }

}