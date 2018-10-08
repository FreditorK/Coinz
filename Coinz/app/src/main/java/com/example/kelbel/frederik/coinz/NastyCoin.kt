package com.example.kelbel.frederik.coinz

import java.util.*
import kotlin.collections.ArrayList

data class NastyCoin (
        var value: Float,
        var currency: String,
        var marker_symbol: Int,
        var marker_color: String,
        var coordinates: Pair<Float, Float>
)