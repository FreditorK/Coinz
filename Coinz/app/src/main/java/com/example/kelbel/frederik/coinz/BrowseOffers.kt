package com.example.kelbel.frederik.coinz

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import android.view.LayoutInflater
import android.view.ViewGroup
import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.*
import com.ebanx.swipebtn.SwipeButton
import com.google.firebase.auth.FirebaseAuth




class BrowseOffers : AppCompatActivity() {//Browse to offers to exchange gold for coins, coin messaging

    private lateinit var backbutton: Button//go back to depot
    private lateinit var currentgold: TextView//displays your gold

    private lateinit var mRecyclerView: RecyclerView
    private var fRecyclerAdapter: FirestoreRecyclerAdapter<TradeOffer, TradeOfferHolder>? = null

    private lateinit var db: Query//query to retrieve offers
    var offerList = mutableListOf<TradeOffer>()//list of offers
    private var firestoreListener: ListenerRegistration? = null

    private lateinit var filter: String//filters by username
    private var bool: Boolean = false//true if filter == ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browse_offers)

        mRecyclerView = findViewById(R.id.rec_list_view)
        backbutton = findViewById(R.id.back_button)
        currentgold = findViewById(R.id.current_gold)
        currentgold.text = ProfileActivity.gold.toString()
        filter = intent.getStringExtra("filter")//get username filter

        backbutton.setOnClickListener {
            val i = Intent(applicationContext, ProfileActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(i, 0)
            finish()
        }

        db = FirebaseFirestore.getInstance().collection("trade_offers")
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.setHasFixedSize(true)

        loadOffers()

        firestoreListener = db//listens for new or executed offers
                .addSnapshotListener(EventListener { documentSnapshots, e ->
                    if (e != null) {
                        Log.e("Browse_Offers", "Listen failed!", e)
                        return@EventListener
                    }

                    offerList = mutableListOf()

                    if (documentSnapshots != null) {
                        for (doc in documentSnapshots) {
                            //retrieve offer, suboffer(coins in offer) details
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
                                s.picRef = resources.getIdentifier(n.currency + n.markersymbol, "mipmap", this.packageName)
                                s.markersymbol = n.markersymbol
                                s.coordinates = n.coordinates
                                offer.children?.add(s)
                            }
                            offerList.add(offer)
                        }
                    }

                    fRecyclerAdapter!!.notifyDataSetChanged()
                    mRecyclerView.adapter = fRecyclerAdapter
                })

        if (filter == "") {
            bool = true
        }
    }

    private fun getCurrencyValue(s: String): Double {//return current exchange rates for coins
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

    private fun roundToDecimals(number: Double, numDecimalPlaces: Int): Double {//rounds to given number of decimal places
        val factor = Math.pow(10.0, numDecimalPlaces.toDouble())
        return Math.round(number * factor) / factor
    }

    private fun loadOffers() {

        val user = FirebaseAuth.getInstance().currentUser?.email//get current user
        val options = FirestoreRecyclerOptions.Builder<TradeOffer>()
                .setQuery(db, TradeOffer::class.java)
                .build()
        fRecyclerAdapter = object : FirestoreRecyclerAdapter<TradeOffer, TradeOfferHolder>(options) {
            override fun onBindViewHolder(holder: TradeOfferHolder, position: Int, offer: TradeOffer) {
                if (offerList.size > 0) {
                    if (offerList[position].user != user && (offerList[position].user?.substringBefore('@') == filter || bool)) {//filter by given username filter and exclude offers with own username
                        val o = offerList[position]
                        //fill in viewholder
                        holder.user?.text = "Offered by: " + o.user?.substringBefore('@')
                        holder.gold?.text = "Price: " + roundToDecimals(o.gold!!, 1).toString()
                        holder.worth?.text = "Current worth: " + roundToDecimals(o.worth, 1).toString()
                        holder.swipeButton?.setOnStateChangeListener {
                            if (o.gold!! <= ProfileActivity.gold) {//execute trade
                                executeOrder66(o)
                            } else {//not enough gold to execute
                                holder.swipeButton?.setEnabledDrawable(getDrawable(R.mipmap.denied))
                                Toast.makeText(baseContext, "Not enough gold in depot!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        //exclusively for test purposes
                        ProfileActivity.h = holder
                        //-----------------------------
                        //add coins to expandable list in layout
                        val noOfChildTextViews = holder.childitems?.childCount
                        val noOfChild = o.children!!.size
                        if (noOfChild < noOfChildTextViews!!) {
                            for (index in noOfChild until noOfChildTextViews) {
                                val currentView = holder.childitems?.getChildAt(index) as LinearLayout
                                currentView.visibility = View.GONE
                            }
                        }
                        for (ViewIndex in 0 until noOfChild) {
                            val currentView = holder.childitems?.getChildAt(ViewIndex) as LinearLayout
                            (currentView.getChildAt(0) as ImageView).setImageResource(o.children?.get(ViewIndex)?.picRef!!)
                            val sublayout = currentView.getChildAt(1) as LinearLayout
                            (sublayout.getChildAt(0) as TextView).text = o.children?.get(ViewIndex)?.id
                            (sublayout.getChildAt(1) as TextView).text = "Value: " + o.children?.get(ViewIndex)?.value.toString()
                        }
                    } else {
                        //do not display if filtered out
                        holder.itemView.visibility = View.GONE
                        holder.itemView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeOfferHolder {//creates viewholder
                val view = LayoutInflater.from(parent.context).inflate(R.layout.browse_item, parent, false)
                return TradeOfferHolder(view, offerList, baseContext)
            }

            override fun onError(e: FirebaseFirestoreException) {
                super.onError(e)
                Log.e("error", e.message)
            }

            fun executeOrder66(offer: TradeOffer) {//executes trade
                FirebaseFirestore.getInstance().collection("users").document(offer.user.toString())
                        .get()
                        .addOnSuccessListener {
                            //update gold of trade offerer
                            val currentPlus = it.getDouble("plus")
                            FirebaseFirestore.getInstance().collection("users").document(offer.user.toString())
                                    .update("plus", currentPlus!! + offer.gold!!)
                                    .addOnSuccessListener {
                                        offer.id?.delete()!!
                                                .addOnSuccessListener {
                                                    Toast.makeText(applicationContext, "Trade executed", Toast.LENGTH_SHORT).show()
                                                    for (n in offer.children!!) {//retrieve coins and mark them as traded so that they can be exchanged even when exchanged 25 coins already
                                                        if (n.id!!.matches(Regex(".*TRADED"))) {
                                                            ProfileActivity.collect(NastyCoin(n.id!!, n.value!!.toFloat(), n.currency!!, n.markersymbol!!, n.coordinates!!))
                                                        } else {
                                                            ProfileActivity.collect(NastyCoin(n.id!! + "TRADED", n.value!!.toFloat(), n.currency!!, n.markersymbol!!, n.coordinates!!))
                                                        }
                                                    }
                                                    ProfileActivity.gold -= offer.gold!!.toFloat()//decrease gold of offer acceptor
                                                    currentgold.text = ProfileActivity.gold.toString()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w("Browse_Offers", "Error deleting offer", e)
                                                    Toast.makeText(applicationContext, "Trade could not be executed. Try again later.", Toast.LENGTH_SHORT).show()
                                                }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Browse_Offers", "Error updating gold status", e)
                                    }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Browse_Offers", "Error receiving gold status", e)
                        }
            }
        }

        fRecyclerAdapter!!.notifyDataSetChanged()
        mRecyclerView.adapter = fRecyclerAdapter
    }

    class TradeOfferHolder(itemView: View, offerList: MutableList<TradeOffer>, context: Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //Viewholder for the offers
        var expand: ImageView? = null//expand arrow
        var swipeButton: SwipeButton? = null
        var user: TextView? = null//offerer
        var gold: TextView? = null//price
        var worth: TextView? = null//current worth of the coins
        var childitems: LinearLayout? = null//contains coins in offer

        init {
            if (offerList.size > 0) {
                expand = itemView.findViewById(R.id.expand_coins)
                swipeButton = itemView.findViewById(R.id.swipe_btn)
                user = itemView.findViewById(R.id.listview_item_title)
                gold = itemView.findViewById(R.id.listview_item_short_description)
                worth = itemView.findViewById(R.id.worth)
                childitems = itemView.findViewById(R.id.child_items)
                childitems?.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in 0 until offerList.size) {//getting size of longest expandeable list for coin items
                    val intMaxSizeTemp = offerList[index].children!!.size
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
                }
                for (indexView in 0 until intMaxNoOfChild) {//assign values to expandable list containing coin items
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
                    childitems?.addView(linearlay, layoutParams)
                }
                expand?.setOnClickListener(this)
            }
        }

        override fun onClick(view: View) {//changes expand arrow to unexpand and reverse wise
            if (childitems?.visibility == View.VISIBLE) {
                childitems?.visibility = View.GONE
                (view as ImageView).setImageResource(R.drawable.ic_keyboard_arrow_up)
            } else {
                childitems?.visibility = View.VISIBLE
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