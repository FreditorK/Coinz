package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class SubFragmentTrading : Fragment(), View.OnClickListener {
    private lateinit var makeOffer : ImageView
    private lateinit var browseOffers : ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_trading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        makeOffer = view.findViewById(R.id.make_an_offer)
        browseOffers = view.findViewById(R.id.browse_offers_img)
        makeOffer.setOnClickListener(this)
        browseOffers.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.browse_offers_img -> startActivity(Intent(this.context, Browse_Offers :: class.java))
            R.id.make_an_offer -> startActivity(Intent(this.context, Make_an_Offer :: class.java))
        }
    }
}