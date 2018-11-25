package com.example.kelbel.frederik.coinz

import java.util.*

data class CoinExchangeRates(//instances hold exchange rates of given date
        var SHIL: Float,
        var DOLR: Float,
        var QUID: Float,
        var PENY: Float,
        var date: Date
)