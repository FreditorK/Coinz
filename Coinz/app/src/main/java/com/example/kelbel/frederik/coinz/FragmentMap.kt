package com.example.kelbel.frederik.coinz

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FragmentMap : Fragment(), OnMapReadyCallback, LocationEngineListener, PermissionsListener{

    private var v : View? = null
    private var mapView: MapView? = null
    private var map : MapboxMap? = null

    private lateinit var originLocation : Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin : LocationLayerPlugin

    private val t = "FragmentMap"
    private var currentDate : String = ""
    private var downloadDate : String = ""
    private val preferencesFile : String = "MyPrefsFile"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_map, container, false)
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiZnJlZGRvaXNoZXJlIiwiYSI6ImNqbWdsNThyYjI0ODIzcWxoZG9xdDVwNGYifQ.w2TSSM7Issr2fQdrhxwsBw")
        mapView = v?.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.setStyleUrl("mapbox://styles/freddoishere/cjmwbc84k78jw2snx54cx7mlv")
        mapView?.getMapAsync(this)

        currentDate = getCurrentDate()

        return v
    }

    fun getCurrentDate() : String {
        val calendar : Calendar = Calendar.getInstance()
        val mdformat : SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        return mdformat.format(calendar.getTime()).toString()
    }

    fun loadGeoJson(): String {
        val stream: InputStream = context!!.applicationContext.openFileInput("coinzmap.geojson")
        val writer = StringWriter()
        IOUtils.copy(stream, writer, "UTF-8")
        val result = writer.toString()
        return result
    }

    override fun onStart() {
        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        downloadDate = settings?.getString("lD", "").toString()
        if (downloadDate != currentDate){
            downloadDate = currentDate
            DownloadFileTask(DownloadCompleteRunner, context).execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson")
        }
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
        //Log.d(t, "[onStop] Storing lastDownloadDate of $downloadDate")
        val settings = context?.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings?.edit()
        editor?.putString("lD", downloadDate)
        editor?.apply()
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

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(t, "[onMapReady] mapboxMap is null")
        } else {
            map = mapboxMap

            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            enableLocation()

            //val obj : GeoJsonSource = GeoJsonSource("geojson", ka)
            //val features : List<Feature> = obj.querySourceFeatures(Expression.eq(Expression.get("type"), "Feature"))
            //map?.addSource(obj)
            //val pointLayer : FillLayer = FillLayer("pointLayer", "geojson")

            // map?.addLayer(pointLayer)
            val json = JSONObject(loadGeoJson())
            val nastycoins : ArrayList<NastyCoin> = retrieveCoins(json)
            var routeCoordinates = ArrayList<Point>()
            Log.d("giraffe", nastycoins[0].coordinates.first.toDouble().toString())
            routeCoordinates.add(Point.fromLngLat(nastycoins[0].coordinates.first.toDouble(), nastycoins[0].coordinates.second.toDouble()))
            routeCoordinates.add(Point.fromLngLat(nastycoins[1].coordinates.first.toDouble(), nastycoins[1].coordinates.second.toDouble()))

            val lineString: LineString = LineString.fromLngLats(routeCoordinates)
            val featureCollection = FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(lineString)))
            val geoJsonSource: GeoJsonSource = GeoJsonSource("line-source", featureCollection)
            mapboxMap.addSource(geoJsonSource)
            val lineLayer: LineLayer = LineLayer("linelayer", "line-source")
             lineLayer.setProperties(
                     PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                     PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                     PropertyFactory.lineWidth(5f),
                     PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
             )

            mapboxMap.addLayer(lineLayer);
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(t, "[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(t, "Permissions: $permissionsToExplain")
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null){
            Log.d(t, "[onLocationChanged] location is null")
        }else{
            originLocation = location
            setCameraPosition(originLocation)
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
        val latIng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latIng))
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationEngine(){
        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        locationEngine.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if(lastLocation != null){
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        }else{
            locationEngine.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationLayer(){
        if (mapView == null){
            Log.d(t, "mapView is null")
        }else{
            if(map == null){
                Log.d(t, "map is null")
            }else{
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin.apply{
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    fun retrieveCoinExchangerates(json : JSONObject) : CoinExchangeRates{
        var values : CoinExchangeRates = CoinExchangeRates(0f, 0f, 0f, 0f)
        values.SHIL = json.getJSONObject("rates").getString("SHIL").toFloat()
        values.DOLR = json.getJSONObject("rates").getString("DOLR").toFloat()
        values.QUID = json.getJSONObject("rates").getString("QUID").toFloat()
        values.PENY = json.getJSONObject("rates").getString("PENY").toFloat()
        return values
    }

    fun retrieveCoins(json: JSONObject) : ArrayList<NastyCoin>{
        var nastycoins: ArrayList<NastyCoin> = ArrayList<NastyCoin>()
        var jsonarray : JSONArray = json.getJSONArray("features")
        var k = JSONObject()
        for(i in 0..49){
            var n = NastyCoin(0f, "", 0, "", Pair(0f, 0f))
            k = jsonarray.getJSONObject(i)
            n.coordinates = stringToCoordinates(k.getJSONObject("geometry").getString("coordinates"))
            n.currency = k.getJSONObject("properties").getString("currency")
            n.marker_color = k.getJSONObject("properties").getString("marker-color")
            n.value = k.getJSONObject("properties").getString("value").toFloat()
            n.marker_symbol = k.getJSONObject("properties").getString("marker-symbol").toInt()
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