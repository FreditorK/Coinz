package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebanx.swipebtn.OnStateChangeListener
import com.ebanx.swipebtn.SwipeButton
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.collections.ArrayList

class SubFragmentEvents : Fragment() {

    companion object {
        var eventAvailability : Boolean = false
    }

    private lateinit var catchMeIfyouCan: SwipeButton
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            catchMeIfyouCan = view.findViewById(R.id.swipe_btn2)
            catchMeIfyouCan.setOnStateChangeListener(object : OnStateChangeListener {
                override fun onStateChange(active: Boolean) {
                    if (eventAvailability == true) {
                        val r = Random()
                        val list = ArrayList<LatLng>()
                        val durations = ArrayList<Long>()
                        var k = LatLng(55.942617 + (55.946233 - 55.942617) * r.nextDouble(), -3.192473 + (-3.184319 + 3.192473) * r.nextDouble())
                        list.add(k)
                        var i = 0L
                        while (i < 150000) {
                            val k2 = generatek2(k, r)
                            list.add(k2)
                            val d = (k2.distanceTo(k) / 0.0016).toLong()
                            durations.add(d)//moderatly fast walking speed
                            k = k2
                            i += d
                        }
                        (this@SubFragmentEvents.activity as ProfileActivity).displayFragmentMap()
                        (fragmentManager?.findFragmentByTag("B") as FragmentMap).addMovingSac(list, durations)
                    }else{
                        catchMeIfyouCan.setEnabledDrawable(getDrawable(this@SubFragmentEvents.context!!, R.mipmap.denied))
                    }
                }
            })
    }

    fun generatek2(k : LatLng, r : Random) : LatLng{
        val k2 = LatLng(k.latitude - 0.002917 + (2*0.002917) * r.nextDouble(), k.longitude - 0.002973 + (2* 0.002973) * r.nextDouble())
        if(k2.latitude < 55.946233 && k2.latitude > 55.942617 && k2.longitude < -3.184319 && k2.longitude > -3.192473){
            return k2
        }else{
            return LatLng(55.942617 + (55.946233 - 55.942617) * r.nextDouble(), -3.192473 + (-3.184319 + 3.192473) * r.nextDouble())
        }
    }
}