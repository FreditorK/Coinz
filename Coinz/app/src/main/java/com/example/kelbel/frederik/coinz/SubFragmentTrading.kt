package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView

class SubFragmentTrading : Fragment(), View.OnClickListener {
    //start activities to browse and make offers here
    private lateinit var makeOffer: ImageView//click to start makeOffer activity
    private lateinit var browseOffers: ImageView//click to start browseOffers activity
    private lateinit var filterby: EditText//filter browsing results by username, if you only want to trade with a specific user

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_trading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        makeOffer = view.findViewById(R.id.make_an_offer)
        browseOffers = view.findViewById(R.id.browse_offers_img)
        filterby = view.findViewById(R.id.filter_by)
        makeOffer.setOnClickListener(this)
        browseOffers.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {//start respective activities on imageview click
        when (p0?.id) {
            R.id.browse_offers_img -> {
                val i = Intent(this.context, BrowseOffers::class.java)
                i.putExtra("filter", filterby.text.toString().trim())
                startActivity(i)
            }
            R.id.make_an_offer -> startActivity(Intent(this.context, MakeanOffer::class.java))
        }
    }
}