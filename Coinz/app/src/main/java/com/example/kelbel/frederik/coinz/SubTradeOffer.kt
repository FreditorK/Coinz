package com.example.kelbel.frederik.coinz

import java.io.Serializable
import java.util.*

class SubTradeOffer : Serializable{
    var id: String? = null
    var picRef: Int? = null
    var value: Double? = null
    var currency: String? = null
    var marker_symbol: String? = null
    var coordinates: Pair<Double, Double>? = null
}