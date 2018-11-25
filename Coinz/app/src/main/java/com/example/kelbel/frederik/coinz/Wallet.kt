package com.example.kelbel.frederik.coinz

data class Wallet(//local wallet
        var shilCoins: ArrayList<NastyCoin>,
        var dolrCoins: ArrayList<NastyCoin>,
        var quidCoins: ArrayList<NastyCoin>,
        var penyCoins: ArrayList<NastyCoin>)