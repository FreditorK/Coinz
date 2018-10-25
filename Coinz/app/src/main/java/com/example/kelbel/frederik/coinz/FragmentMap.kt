package com.example.kelbel.frederik.coinz

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode

class FragmentMap : Fragment(), LocationEngineListener, PermissionsListener{

    private lateinit var v : View
    private lateinit  var mapView: MapView

    private lateinit var map : MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin : LocationLayerPlugin? = null

    private val t = "FragmentMap"

    private lateinit var shilview: TextView
    private lateinit var dolrview: TextView
    private lateinit var quidview: TextView
    private lateinit var penyview: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map, container, false)
        return v
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiZnJlZGRvaXNoZXJlIiwiYSI6ImNqbWdsNThyYjI0ODIzcWxoZG9xdDVwNGYifQ.w2TSSM7Issr2fQdrhxwsBw")
        mapView = v.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{mapboxMap ->
            map = mapboxMap
            map.uiSettings.isCompassEnabled = true
            for (n in ProfileActivity.nastycoins!!) {
                val icon : Icon = IconFactory.getInstance(activity!!.applicationContext).fromBitmap(BitmapFactory.decodeResource(resources, getFittingIconId(n)))
                map.addMarker(MarkerOptions().setIcon(icon)
                        .position(LatLng(n.coordinates.second, n.coordinates.first))
                        .title(n.id)
                        .snippet(n.value.toString()))
            }
            enableLocation()
        }
        shilview = view.findViewById(R.id.shil_textview)
        dolrview = view.findViewById(R.id.dolr_textview)
        quidview = view.findViewById(R.id.quid_textview)
        penyview = view.findViewById(R.id.peny_textview)
        displayWalletValues()
    }

    fun displayWalletValues(){//update displayed wallet
        shilview.text = ProfileActivity.wallet?.shilCoins?.size.toString()
        dolrview.text = ProfileActivity.wallet?.dolrCoins?.size.toString()
        quidview.text = ProfileActivity.wallet?.quidCoins?.size.toString()
        penyview.text = ProfileActivity.wallet?.penyCoins?.size.toString()
    }

    fun getFittingIconId(n : NastyCoin): Int{//Get the Icon to display on the map from Coin
        return  resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", context?.packageName)
    }

    //Coin collection
    fun checkForCoin(location: Location){//check if a coin can be collected and collect it
        val a = ProfileActivity.nastycoins?.indexOfFirst { i -> compareCoordinates(Pair(location.longitude, location.latitude), i.coordinates)}
        if(a!! > -1){
            ProfileActivity.collect(ProfileActivity.nastycoins!![a])
            ProfileActivity.nastycoins?.removeAt(a)
            map.removeMarker(map.markers[a])
            displayWalletValues()
        }
    }

    fun compareCoordinates(a : Pair<Double, Double>, b : Pair<Double, Double>): Boolean {//test if given location is in range
        if (a.first < b.first + 0.0002f && a.first > b.first - 0.0002f && a.second < b.second + 0.0002f && a.second > b.second - 0.0002f) {
            return true
        } else {
            return false
        }
    }

    //Location, Map
    private fun setCameraPosition(location: Location){
        map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
    }

    private fun enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this.context)){
            Log.d(t, "Permissions granted")
            initLocationEngine()
            initLocationLayer()
        }else{
            Log.d(t, "Permissions denied")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this.activity)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if(PermissionsManager.areLocationPermissionsGranted(this.context)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        displayWalletValues()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(t, "[onConnected] requesting location updates")
        locationEngine?.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(t, "Permissions: $permissionsToExplain")
    }

    override fun onLocationChanged(location: Location?) {
        location?.let{
            setCameraPosition(location)
            checkForCoin(location)
        }
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(t, "[onPermissionResult] granted == $granted")
        if(granted){
            enableLocation()
        }else{
            Toast.makeText(context, "Let me know where you are. Hide and seek is not my game.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)//bug
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationEngine(){
        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        locationEngine?.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine?.lastLocation
        if(lastLocation != null){
            setCameraPosition(lastLocation)
        }else{
            locationEngine?.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationLayer(){
        locationLayerPlugin = LocationLayerPlugin(mapView, map, locationEngine)
        locationLayerPlugin?.apply{
            setLocationLayerEnabled(true)
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.NORMAL
        }
    }
}