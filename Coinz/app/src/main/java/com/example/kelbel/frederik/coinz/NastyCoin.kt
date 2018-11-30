package com.example.kelbel.frederik.coinz

data class NastyCoin(//object storing data of coin
        var id: String,
        var value: Float,
        var currency: String,
        var markersymbol: String,
        var coordinates: Pair<Double, Double>
)