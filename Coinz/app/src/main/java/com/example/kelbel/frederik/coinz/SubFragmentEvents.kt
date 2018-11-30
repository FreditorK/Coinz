package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebanx.swipebtn.SwipeButton
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*
import kotlin.collections.ArrayList

class SubFragmentEvents : Fragment() {

    companion object {
        //is event available (once a day)
        var eventAvailability: Boolean = false
    }

    private lateinit var catchMeIfyouCan: SwipeButton//start event

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catchMeIfyouCan = view.findViewById(R.id.swipe_btn2)
        catchMeIfyouCan.setOnStateChangeListener {
            //if swiped check for availability and start event
            if (eventAvailability) {
                val r = Random()
                val list = ArrayList<LatLng>()
                val durations = ArrayList<Long>()
                var k = LatLng(55.942617 + (55.946233 - 55.942617) * r.nextDouble(), -3.192473 + (-3.184319 + 3.192473) * r.nextDouble())//place sac at random pos in between given bounds
                list.add(k)
                var i = 0L
                while (i < 150000) {//generate new check points the sac moves towards until given duration of movement is reached
                    val k2 = generatek2(k, r)
                    list.add(k2)
                    val d = (k2.distanceTo(k) / 0.0016).toLong()//calculate time needed to reach checkpoint at walking speed
                    durations.add(d)//moderatly fast walking speed
                    k = k2
                    i += d
                }
                val v = (this@SubFragmentEvents.activity as ProfileActivity).bottomNavigationView?.findViewById<View>(R.id.map_tab)//go to map fragment
                v?.performClick()
                (fragmentManager?.findFragmentByTag("B") as FragmentMap).addMovingSac(list, durations)//start event
                eventAvailability = false
            } else {
                catchMeIfyouCan.setEnabledDrawable(getDrawable(this@SubFragmentEvents.context!!, R.mipmap.denied))//event has already been run today
            }
        }
    }

    private fun generatek2(k: LatLng, r: Random): LatLng {//generate new checkpoint for the sac
        //place new checkpoint near old checkpoint
        val k2 = LatLng(k.latitude - 0.002917 + (2 * 0.002917) * r.nextDouble(), k.longitude - 0.002973 + (2 * 0.002973) * r.nextDouble())
        return if (k2.latitude < 55.946233 && k2.latitude > 55.942617 && k2.longitude < -3.184319 && k2.longitude > -3.192473) {
            k2
        } else {
            //if checkpoint is outside of bounds place somewhere within bounds
            LatLng(55.942617 + (55.946233 - 55.942617) * r.nextDouble(), -3.192473 + (-3.184319 + 3.192473) * r.nextDouble())
        }
    }
}