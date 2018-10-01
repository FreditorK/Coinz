package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView



class FragmentMap : Fragment(){

    private var v : View? = null
    private var mapView: MapView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map, container, false)
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiZnJlZGRvaXNoZXJlIiwiYSI6ImNqbWdsNThyYjI0ODIzcWxoZG9xdDVwNGYifQ.w2TSSM7Issr2fQdrhxwsBw");
        mapView = v?.findViewById(R.id.mapView);
        mapView?.onCreate(savedInstanceState);
        return v
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onDestroyView() {
        mapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}