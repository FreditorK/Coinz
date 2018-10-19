package com.example.kelbel.frederik.coinz

data class NastyCoin (
        var id: String,
        var value: Float,
        var currency: String,
        var marker_symbol: String,
        var coordinates: Pair<Double, Double>
)