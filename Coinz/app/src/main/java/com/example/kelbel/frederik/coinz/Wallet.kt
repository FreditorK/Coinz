package com.example.kelbel.frederik.coinz

import android.content.Context
import com.mapbox.mapboxsdk.annotations.Icon

data class Wallet (
        var shilCoins : ArrayList<NastyCoin>,
        var dolrCoins : ArrayList<NastyCoin>,
        var quidCoins : ArrayList<NastyCoin>,
        var penyCoins : ArrayList<NastyCoin>)