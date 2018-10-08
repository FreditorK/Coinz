package com.example.kelbel.frederik.coinz

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout

class ProfileActivity : AppCompatActivity() {

    private var bottomNavigationView : BottomNavigationView? = null
    private var startFrame : FrameLayout? = null

    private var fragmentDepot : FragmentDepot? = null
    private var fragmentMap : FragmentMap? = null
    private var fragmentSettings : FragmentSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        startFrame = findViewById(R.id.start_frame)
        bottomNavigationView = findViewById(R.id.navigation_bar)

        fragmentDepot = FragmentDepot()
        fragmentMap = FragmentMap()
        fragmentSettings = FragmentSettings()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.start_frame, fragmentDepot)
        transaction.commit()

        bottomNavigationView?.setOnNavigationItemSelectedListener {
            var selectedFragment: Fragment? = fragmentDepot
            when (it.itemId) {
                R.id.depot_tab -> selectedFragment = fragmentDepot
                R.id.map_tab -> selectedFragment = fragmentMap
                R.id.settings_tab -> selectedFragment = fragmentSettings
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.start_frame, selectedFragment)
            transaction.commit()
            return@setOnNavigationItemSelectedListener true
        }
    }
}