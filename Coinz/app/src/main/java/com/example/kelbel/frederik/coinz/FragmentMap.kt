package com.example.kelbel.frederik.coinz

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.*
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import kotlin.collections.ArrayList

class FragmentMap : Fragment(), LocationEngineListener, PermissionsListener {

    private lateinit var mapView: MapView
    private var lastLocation: Location? = null

    private var marker: Marker? = null //marker for movingsac event
    private var markerAnimator: ValueAnimator? = null//markeranimator for movingsac event
    private var countDownTimer: CountDownTimer? = null//countdowntimer for movingsac event

    private lateinit var map: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null

    private val t = "FragmentMap"

    //currently gathered coins
    private lateinit var shilview: TextView
    private lateinit var dolrview: TextView
    private lateinit var quidview: TextView
    private lateinit var penyview: TextView
    private lateinit var timer: TextView

    //grid coords for team zones
    private val rows = arrayOf(55.946233, 55.9455098, 55.9447866, 55.9440634, 55.9433402, 55.942617)
    private val columns = arrayOf(-3.192473, -3.1908422, -3.1892114, -3.1875806, -3.1859498, -3.184319)
    //blue color values for coords
    private val cbs = arrayOf(Color.parseColor("#90020051"), Color.parseColor("#9004008e"), Color.parseColor("#900700cc"), Color.parseColor("#90514cdb"), Color.parseColor("#909b88eb"))

    @VisibleForTesting var counter : Int = 71//counts down for tests, one counter value corresponds to one location

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Mapbox.getInstance(this.context!!, "pk.eyJ1IjoiZnJlZGRvaXNoZXJlIiwiYSI6ImNqbWdsNThyYjI0ODIzcWxoZG9xdDVwNGYifQ.w2TSSM7Issr2fQdrhxwsBw")
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            //load map
            map = mapboxMap
            map.uiSettings.isCompassEnabled = true
            drawPolygon(map)
            for (n in ProfileActivity.nastycoins) {//add coins to map with individual icons displaying them as groups of coins according to value
                val icon: Icon = IconFactory.getInstance(activity!!.applicationContext).fromBitmap(BitmapFactory.decodeResource(resources, getFittingIconId(n)))
                map.addMarker(MarkerOptions().setIcon(icon)
                        .position(LatLng(n.coordinates.second, n.coordinates.first))
                        .title(n.id)
                        .snippet(n.value.toString()))
            }
            enableLocation()
        }
        //display currently gathered coins
        shilview = view.findViewById(R.id.shil_textview)
        dolrview = view.findViewById(R.id.dolr_textview)
        quidview = view.findViewById(R.id.quid_textview)
        penyview = view.findViewById(R.id.peny_textview)
        timer = view.findViewById(R.id.timer)
        displayWalletValues()
    }

    private fun drawPolygon(mapboxMap: MapboxMap) {//draw zones grid
        for (r in 0..4) {
            for (c in 0..4) {
                val polygon = ArrayList<LatLng>()
                polygon.add(LatLng(rows[r], columns[c]))
                polygon.add(LatLng(rows[r], columns[c + 1]))
                polygon.add(LatLng(rows[r + 1], columns[c + 1]))
                polygon.add(LatLng(rows[r + 1], columns[c]))
                mapboxMap.addPolygon(PolygonOptions().alpha(0.5f).strokeColor(Color.parseColor("#00ffffff"))
                        .addAll(polygon))
            }
        }

        val ref = FirebaseFirestore.getInstance()

        for (num in 0..24) {//retrieve colors for the respective zones and shape barplot in subfragmentaccount
            ref.collection("zones").document(num.toString())
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document!!.exists()) {
                                val c = document.getLong("c")!!.toInt()
                                mapboxMap.polygons[num].fillColor = c
                                if (cbs.contains(c)) {
                                    SubFragmentAccount.carversize += 1
                                } else {
                                    SubFragmentAccount.creepersize += 1
                                }
                                (fragmentManager?.findFragmentByTag("A") as FragmentDepot).getAccount()?.updateBars()
                            } else {
                                Toast.makeText(this.context, "Check your Connection!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this.context, "Check your Connection!", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

        //listen for updates in zone colors, in case someone buys a zone for their team and shape barplot accordingly
        FirebaseFirestore.getInstance().collection("zones")
                .addSnapshotListener(EventListener { documentSnapshots, e ->
                    if (e != null) {
                        Log.e("TeamZone", "Listen failed!", e)
                        return@EventListener
                    }

                    if (documentSnapshots != null) {
                        for (doc in documentSnapshots.documentChanges) {
                            if (doc.type == DocumentChange.Type.MODIFIED) {
                                val c = doc.document.getLong("c")!!.toInt()
                                mapboxMap.polygons[doc.document.id.toInt()].fillColor = c
                                if (cbs.contains(c)) {
                                    SubFragmentAccount.carversize += 1
                                    SubFragmentAccount.creepersize -= 1
                                } else {
                                    SubFragmentAccount.creepersize += 1
                                    SubFragmentAccount.carversize -= 1
                                }
                                (fragmentManager?.findFragmentByTag("A") as FragmentDepot).getAccount()?.updateBars()
                            }
                        }
                    }
                })
    }

    private fun displayWalletValues() {//update displayed wallet
        shilview.text = ProfileActivity.wallet.shilCoins.size.toString()
        dolrview.text = ProfileActivity.wallet.dolrCoins.size.toString()
        quidview.text = ProfileActivity.wallet.quidCoins.size.toString()
        penyview.text = ProfileActivity.wallet.penyCoins.size.toString()
    }

    private fun getFittingIconId(n: NastyCoin): Int {//Get the Icon to display on the map from Coin
        return resources.getIdentifier(n.currency + n.marker_symbol, "mipmap", context?.packageName)
    }

    fun addMovingSac(list: ArrayList<LatLng>, durations: ArrayList<Long>) {//adds moving sac to the map
        val icon: Icon = IconFactory.getInstance(activity!!.applicationContext).fromBitmap(BitmapFactory.decodeResource(resources, R.mipmap.movingsac))
        marker = map.addMarker(MarkerOptions().setIcon(icon)
                .position(list[0])
                .title("Catch me!"))
        markerAnimator = ObjectAnimator.ofObject(marker, "position", LatLngEvaluator(), list[0], list[1])
        updateMovingSac(list, durations, 0)
        timer.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(durations.sum(), 1000) {
            //sets timer for event
            override fun onTick(p0: Long) {
                timer.text = "Event finishes in: " + p0 / 1000 + "s"
                if (p0 / 1000 < 10) {
                    changeColor()
                }
            }

            override fun onFinish() {//failing to collect it
                timer.setTextColor(Color.WHITE)
                timer.text = "You missed your chance for today."
                SubFragmentEvents.eventAvailability = false
                timer.visibility = View.GONE
            }

            fun changeColor() {//change color of timer in final seconds
                if (timer.currentTextColor == Color.RED) {
                    timer.setTextColor(Color.WHITE)
                } else {
                    timer.setTextColor(Color.RED)
                }
            }
        }.start()
    }

    fun updateMovingSac(list: ArrayList<LatLng>, durations: ArrayList<Long>, count: Int) {//recursive function so that the sac changes direction of movement
        markerAnimator?.duration = durations[count]//walking speed, calculated realtive to distance
        markerAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                if (count + 2 < list.size) {//change movement direction
                    markerAnimator = ObjectAnimator.ofObject(marker, "position", LatLngEvaluator(), list[count + 1], list[count + 2])
                    updateMovingSac(list, durations, count + 1)
                } else {//finish
                    markerAnimator?.end()
                    map.removeMarker(marker!!)
                }
            }
        })
        markerAnimator?.start()
    }

    //Coin collection
    private fun checkForCoin(location: Location) {//check if a coin can be collected and collect it
        lastLocation = location
        val a = ProfileActivity.nastycoins.indexOfFirst { i -> compareCoordinates(Pair(location.longitude, location.latitude), i.coordinates) }
        if (a > -1) {
            if (checkIfValidZone(ProfileActivity.nastycoins[a].coordinates)) {//check if coin is in zone of your team
                ProfileActivity.collect(ProfileActivity.nastycoins[a])
                ProfileActivity.nastycoins.removeAt(a)
                map.removeMarker(map.markers[a])
                displayWalletValues()
            } else {
                Toast.makeText(this.context, "These coins are not in a zone your team controls!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compareCoordinates(a: Pair<Double, Double>, b: Pair<Double, Double>): Boolean {//test if given location is in range
        return a.first < b.first + 0.0002f && a.first > b.first - 0.0002f && a.second < b.second + 0.0002f && a.second > b.second - 0.0002f
    }

    private fun checkIfValidZone(b: Pair<Double, Double>): Boolean {//checks if coin is within one of your zones
        //rows = arrayOf(55.946233, 55.9455098, 55.9447866, 55.9440634, 55.9433402, 55.942617)
        //columns = arrayOf(-3.192473, -3.1908422, -3.1892114, -3.1875806, -3.1859498, -3.184319)
        for (i in 0..4) {
            if (b.second < rows[i] && b.second >= rows[i + 1]) {
                for (k in 0..4) {
                    if (b.first > columns[k] && b.first <= columns[k + 1]) {
                        return if (cbs.contains(map.polygons[i * 5 + k].fillColor)) {
                            ProfileActivity.team == 1
                        } else {
                            ProfileActivity.team == 0
                        }
                    }
                }
            }
        }
        return false
    }

    //Location, Map
    private fun setCameraPosition(location: Location) {//adjusts camera location according to current location
        map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
            Log.d(t, "Permissions granted")
            initLocationEngine()
            initLocationLayer()
        } else {
            Log.d(t, "Permissions denied")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this.activity)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
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

    override fun onStop() {//stop listening
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

    override fun onLocationChanged(location: Location?) {//check for location changes
        location?.let {
            setCameraPosition(location)
            checkForCoin(location)
        }
    }

    override fun onPermissionResult(granted: Boolean) {//enable location if permission granted
        Log.d(t, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
        } else {
            Toast.makeText(context, "Let me know where you are. Hide and seek is not my game.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        locationEngine?.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            setCameraPosition(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationLayer() {
        locationLayerPlugin = LocationLayerPlugin(mapView, map, locationEngine)
        locationLayerPlugin?.apply {
            setLocationLayerEnabled(true)
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.NORMAL
        }
    }

    inner class LatLngEvaluator : TypeEvaluator<LatLng> {//for moving sac animation purposes

        private var latLng: LatLng = LatLng()

        override fun evaluate(fraction: Float, startValue: LatLng?, endValue: LatLng?): LatLng {// Method is used to interpolate the marker animation.
            latLng.latitude = startValue!!.latitude + (endValue!!.latitude - startValue.latitude) * fraction
            latLng.longitude = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            if (latLng.latitude < lastLocation!!.latitude + 0.0002f && latLng.latitude > lastLocation!!.latitude - 0.0002f && latLng.longitude > lastLocation!!.longitude - 0.0002f && latLng.longitude < lastLocation!!.longitude + 0.0002f) {
                markerAnimator?.end()
                map.removeMarker(marker!!)
                ProfileActivity.catchMovingSac()
                displayWalletValues()
                countDownTimer?.cancel()
                timer.text = "Congratulations on your catch!"
                SubFragmentEvents.eventAvailability = false
            }
            return latLng
        }
    }
    @VisibleForTesting
    fun loc(){//called iteratively in GatheringTest, mocks location
        val locs = arrayOf(Pair(-3.187363,55.944453), Pair(-3.188036,55.944773), Pair(-3.187855,55.944723),
                Pair(-3.187695,55.944656), Pair(-3.187552,55.944582), Pair(-3.187458,55.944541),
                Pair(-3.187335,55.944489), Pair(-3.187436,55.944461), Pair(-3.187561,55.944438),
                Pair(-3.187679,55.944423), Pair(-3.187793,55.944407), Pair(-3.187902,55.944394),
                Pair(-3.188013,55.944358), Pair(-3.188136,55.944355), Pair(-3.188262,55.944351),
                Pair(-3.188382,55.944343), Pair(-3.188500,55.944333), Pair(-3.188618,55.944313),
                Pair(-3.188740,55.944301), Pair(-3.188841,55.944262), Pair(-3.188958,55.944234),
                Pair(-3.189076,55.944215), Pair(-3.189188,55.944198), Pair(-3.189318,55.944182),
                Pair(-3.189447,55.944168), Pair(-3.189559,55.944148), Pair(-3.189686,55.944139),
                Pair(-3.189816,55.944128), Pair(-3.189933,55.944109), Pair(-3.190063,55.944099),
                Pair(-3.190181,55.944091), Pair(-3.190303,55.944108), Pair(-3.190420,55.944100),
                Pair(-3.190537,55.944089), Pair(-3.190655,55.944066), Pair(-3.190767,55.944037),
                Pair(-3.190895,55.944023), Pair(-3.191013,55.944023), Pair(-3.191129,55.944018),
                Pair(-3.191255,55.943996), Pair(-3.191350,55.944035), Pair(-3.191373,55.944107),
                Pair(-3.191396,55.944174), Pair(-3.191430,55.944244), Pair(-3.191422,55.944319),
                Pair(-3.191422,55.944385), Pair(-3.191468,55.944447), Pair(-3.191466,55.944520),
                Pair(-3.191470,55.944594), Pair(-3.191472,55.944658), Pair(-3.191447,55.944738),
                Pair(-3.191432,55.944802), Pair(-3.191414,55.944871), Pair(-3.191410,55.944940),
                Pair(-3.191401,55.945004), Pair(-3.191387,55.945067), Pair(-3.191209,55.945243),
                Pair(-3.191251,55.945309), Pair(-3.191258,55.945380), Pair(-3.191222,55.945440),
                Pair(-3.191249,55.945504), Pair(-3.191263,55.945569), Pair(-3.191245,55.945640),
                Pair(-3.191223,55.945709), Pair(-3.191193,55.945780), Pair(-3.191181,55.945845),
                Pair(-3.191189,55.945908), Pair(-3.191191,55.945971), Pair(-3.191295,55.946011),
                Pair(-3.191425,55.946024), Pair(-3.191542,55.946026), Pair(-3.191648,55.946052))//mock locations/mock path

        //adds a mock-location-provider to the location manager
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val mocLocationProvider = LocationManager.GPS_PROVIDER
        lm.addTestProvider(mocLocationProvider, false, false,
                false, false, true, true, true, 0, 5)
        lm.setTestProviderEnabled(mocLocationProvider, true)

        val loc = Location(mocLocationProvider)
        val mockLocation = Location(mocLocationProvider)

        if(counter >= 0){//do nothing if counter dropped below zero
            //set next mock location
            mockLocation.latitude = locs[counter].second
            mockLocation.longitude = locs[counter].first
            mockLocation.altitude = loc.altitude
            mockLocation.time = System.currentTimeMillis()
            mockLocation.accuracy = 1.0f

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
            lm.setTestProviderLocation(mocLocationProvider, mockLocation)
            counter--
        }
    }
}