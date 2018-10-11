package com.example.kelbel.frederik.coinz

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FragmentMap : Fragment(), LocationEngineListener, PermissionsListener{

    private lateinit var v : View
    private lateinit  var mapView: MapView
    private lateinit var map : MapboxMap
    private lateinit var originLocation : Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var actionButton : FloatingActionButton
    private lateinit var button1 : SubActionButton
    private lateinit var button2 : SubActionButton
    private lateinit var button3 : SubActionButton
    private lateinit var button4 : SubActionButton

    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin : LocationLayerPlugin? = null

    private val t = "FragmentMap"
    private var currentDate : String = ""
    private var downloadDate : String = ""
    private val preferencesFile : String = "MyPrefsFile"
    private lateinit var shilview: TextView
    private lateinit var dolrview: TextView
    private lateinit var quidview: TextView
    private lateinit var penyview: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map, container, false)
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiZnJlZGRvaXNoZXJlIiwiYSI6ImNqbWdsNThyYjI0ODIzcWxoZG9xdDVwNGYifQ.w2TSSM7Issr2fQdrhxwsBw")
        mapView = v.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{mapboxMap ->
            map = mapboxMap
            map.uiSettings.isCompassEnabled = true
            val json = JSONObject(loadGeoJson())
            val coinExchangeRates = retrieveCoinExchangerates(json)
            val nastycoins : ArrayList<NastyCoin> = retrieveCoins(json)
            for (n in nastycoins) {
                val icon : Icon = IconFactory.getInstance(activity!!.applicationContext).fromBitmap(BitmapFactory.decodeResource(resources, getFittingIconId(n)))
                map.addMarker(MarkerOptions().setIcon(icon)
                        .position(LatLng(n.coordinates.second.toDouble(), n.coordinates.first.toDouble()))
                        .title(n.id)
                        .snippet(n.value.toString()))
            }
            enableLocation()
        }
        currentDate = getCurrentDate()
        setUpWalletMenu()

        return v
    }

    fun setUpWalletMenu(){

        shilview = TextView(this.context)
        dolrview = TextView(this.context)
        quidview = TextView(this.context)
        penyview = TextView(this.context)

        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        shilview.text = settings?.getString("shil", "").toString()
        dolrview.text = settings?.getString("dolr", "").toString()
        quidview.text = settings?.getString("quid", "").toString()
        penyview.text = settings?.getString("peny", "").toString()

        val icon : ImageView = ImageView(this.context)
        icon.setImageDrawable(resources.getDrawable(R.drawable.ic_account_balance_wallet))

        actionButton = FloatingActionButton.Builder(this.activity)
                .setBackgroundDrawable(R.mipmap.shil)
                .setContentView(icon)
                .setPosition(8)
                .build()
        val itemBuilder : SubActionButton.Builder = SubActionButton.Builder(this.activity)
        button1 = itemBuilder.setContentView(shilview).setBackgroundDrawable(resources.getDrawable(R.mipmap.shil)).build()
        button2 = itemBuilder.setContentView(dolrview).setBackgroundDrawable(resources.getDrawable(R.mipmap.dolr)).build()
        button3 = itemBuilder.setContentView(quidview).setBackgroundDrawable(resources.getDrawable(R.mipmap.quid)).build()
        button4 = itemBuilder.setContentView(penyview).setBackgroundDrawable(resources.getDrawable(R.mipmap.peny)).build()
        val actionMenu : FloatingActionMenu = FloatingActionMenu.Builder(this.activity)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .addSubActionView(button4)
                .attachTo(actionButton)
                .setStartAngle(0)
                .setEndAngle(90)
                .build()
    }

    fun getCurrentDate() : String {
        val calendar : Calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy/MM/dd")
        return mdformat.format(calendar.getTime()).toString()
    }

    fun loadGeoJson(): String {
        val stream: InputStream = context!!.applicationContext.openFileInput("coinzmap.geojson")
        val writer = StringWriter()
        IOUtils.copy(stream, writer, "UTF-8")
        return writer.toString()
    }

    fun setWalletVisible(){
        button1.visibility = View.VISIBLE
        button2.visibility = View.VISIBLE
        button3.visibility = View.VISIBLE
        button4.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
    }

    fun setWalletGone(){
        button1.visibility = View.GONE
        button2.visibility = View.GONE
        button3.visibility = View.GONE
        button4.visibility = View.GONE
        actionButton.visibility = View.GONE
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        setWalletVisible()
        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        downloadDate = settings?.getString("lD", "").toString()
        if (downloadDate != currentDate){
            downloadDate = currentDate
            DownloadFileTask(DownloadCompleteRunner, context).execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson")
            val editor = settings?.edit()
            shilview.text = "0"
            dolrview.text = "0"
            quidview.text = "0"
            penyview.text = "0"
            editor?.putString("shil", "0")
            editor?.putString("dolr", "0")
            editor?.putString("quid", "0")
            editor?.putString("peny", "0")
            editor?.apply()
        }
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
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        setWalletGone()
        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings?.edit()
        editor?.putString("lD", downloadDate)
        editor?.apply()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        actionButton.visibility = View.GONE
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        locationEngine?.deactivate()
        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings?.edit()
        editor?.putString("shil", shilview.text.toString())
        editor?.putString("dolr", dolrview.text.toString())
        editor?.putString("quid", quidview.text.toString())
        editor?.putString("peny", shilview.text.toString())
        editor?.apply()
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    fun getFittingIconId(n : NastyCoin): Int{
        val name: String = n.currency.toLowerCase() + n.marker_symbol
        return  resources.getIdentifier(name , "mipmap", context?.packageName)
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
            originLocation = location
            setCameraPosition(location)
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
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    private fun setCameraPosition(location: Location){
        map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
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
            originLocation = lastLocation
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

    fun retrieveCoinExchangerates(json : JSONObject) : CoinExchangeRates{
        val values : CoinExchangeRates = CoinExchangeRates(0f, 0f, 0f, 0f)
        values.SHIL = json.getJSONObject("rates").getString("SHIL").toFloat()
        values.DOLR = json.getJSONObject("rates").getString("DOLR").toFloat()
        values.QUID = json.getJSONObject("rates").getString("QUID").toFloat()
        values.PENY = json.getJSONObject("rates").getString("PENY").toFloat()
        return values
    }

    fun retrieveCoins(json: JSONObject) : ArrayList<NastyCoin>{
        val nastycoins: ArrayList<NastyCoin> = ArrayList<NastyCoin>()
        val jsonarray : JSONArray = json.getJSONArray("features")
        for(i in 0..49){
            val n = NastyCoin("", 0f, "", "", Pair(0f, 0f))
            val k = jsonarray.getJSONObject(i)
            n.id = k.getJSONObject("properties").getString("id")
            n.value = k.getJSONObject("properties").getString("value").toFloat()
            n.currency = k.getJSONObject("properties").getString("currency")
            n.marker_symbol = k.getJSONObject("properties").getString("marker-symbol")
            n.coordinates = stringToCoordinates(k.getJSONObject("geometry").getString("coordinates"))
            nastycoins.add(n)
        }
        return nastycoins
    }


    fun stringToCoordinates(s : String) : Pair<Float, Float>{
        val regex = "-?[0-9]*\\.[0-9]*".toRegex()
        val ss : Sequence<MatchResult> = regex.findAll(s)
        val ss1 : String = ss.first().value
        val ss2 : String = ss.last().value
        return Pair<Float, Float>(ss1.toFloat(), ss2.toFloat())
    }
}