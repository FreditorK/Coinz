package com.example.kelbel.frederik.coinz

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable

class TradeOffer : Serializable {
    var id : DocumentReference? = null
    var user: String? = null
    var gold: Double? = null
    var worth: Double = 0.0
    var children: ArrayList<SubTradeOffer>? = null
}