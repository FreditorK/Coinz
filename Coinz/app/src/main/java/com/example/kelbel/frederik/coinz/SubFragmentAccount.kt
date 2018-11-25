package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.view.ViewGroup
import android.widget.*


class SubFragmentAccount : Fragment() {//fragment visible on opening

    private lateinit var supportButton: Button//on button click open zone purchase activity (teamzone)

    //simple barplot of zones owned by each team
    private var creeperbar: LinearLayout? = null
    private var carverbar: LinearLayout? = null

    companion object {
        //height of the bars
        var creepersize: Int = 0
        var carversize: Int = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportButton = view.findViewById(R.id.support_button)
        creeperbar = view.findViewById(R.id.creeperbar)
        carverbar = view.findViewById(R.id.carverbar)
        if (ProfileActivity.team == 1) {//change color of support team button according to your current team
            supportButton.setBackgroundResource(R.color.coinBlueAccent)
        }
        supportButton.setOnClickListener { startActivity(Intent(context, TeamZone::class.java)) }
    }

    fun updateBars() {//update barplot
        val layoutParamscr = LinearLayout.LayoutParams(200, creepersize * 20)
        val layoutParamsca = LinearLayout.LayoutParams(200, carversize * 20)
        layoutParamsca.gravity = Gravity.CENTER_HORIZONTAL
        layoutParamscr.gravity = Gravity.CENTER_HORIZONTAL
        creeperbar?.layoutParams = layoutParamscr
        carverbar?.layoutParams = layoutParamsca
    }
}