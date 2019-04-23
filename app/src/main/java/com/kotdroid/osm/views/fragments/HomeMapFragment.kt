package com.kotdroid.osm.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.location.Address
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.kotdroid.osm.R
import com.kotdroid.osm.utils.MarshMallowPermissions
import com.kotdroid.osm.viewmodels.HomeMapViewModel
import com.kotdroid.osm.views.activities.BaseAppCompactActivity
import com.kotdroid.osm.views.utils.RouteAsyncLoader
import kotlinx.android.synthetic.main.fragmnet_home_map.*
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.NetworkLocationIgnorer
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.*
import java.util.*
import kotlin.collections.ArrayList


class HomeMapFragment : BaseFragment(), IMyLocationConsumer, MapEventsReceiver, LocationListener, RouteAsyncLoader.RouteCallback {


//    override val getViewModel: BaseLocationViewModel?


    companion object {

        val OSRM = 0
        val GRAPHHOPPER_FASTEST = 1
        val GRAPHHOPPER_BICYCLE = 2
        val GRAPHHOPPER_PEDESTRIAN = 3
        val GOOGLE_FASTEST = 4
        internal val userAgent = "OsmNavigator/2.2"
        const val REQUEST_LOCATION = 89
        const val LOCATION_INTERVAL: Long = 1000
        const val FASTEST_LOCATION_INTERVAL: Long = 10000
    }

    override val layoutId: Int
        get() = R.layout.fragmnet_home_map


    override val isNavigationBarEnabled: Boolean
        get() = false

    private val mGeoPoints = arrayListOf<GeoPoint>()
    protected var startPoint: GeoPoint? = null
    protected var destinationPoint: GeoPoint? = null
    private var mHomeMapViewModel: HomeMapViewModel? = null
    private var mRotationGestureOverLey: RotationGestureOverlay? = null
    private var directedLocationOverlay: DirectedLocationOverlay? = null
    private var myLocationNewOverlay: MyLocationNewOverlay? = null
    private var mLocationManager: LocationManager? = null
    private var mAzimuthAngleSpeed = 0.0f
    private var mKmlDocument: KmlDocument? = null //made static to pass between activities
    private var mClickedGeoPoint: GeoPoint? = null
    protected var START_INDEX = -2
    protected var DEST_INDEX = -1

    private val mMarshmallowPermissions: MarshMallowPermissions by lazy {
        MarshMallowPermissions(this)
    }

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback

    var userCurrentLatLng: LatLng? = null
    var isLocationGranted = false

    override fun init() {
        mHomeMapViewModel = ViewModelProviders.of(this)[HomeMapViewModel::class.java]

        // Get FusedLocationClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activityContext)

        // Location permission related methods
        checkLocationAvailability()

        // Set Tile for OpenStreetMap
        osmMapView.setTileSource(TileSourceFactory.MAPNIK)

        // Add GeoPoints
//        addGeoPoints()

        // click listener
        ivRefresh.setOnClickListener { calcRoute() }
        btnSearch.setOnClickListener {

        }

        ivRouteProvider.setOnClickListener {
            val popupMenu = android.support.v7.widget.PopupMenu(activityContext, it)
            popupMenu.inflate(R.menu.menu)

            popupMenu.setOnMenuItemClickListener {

                when (it?.itemId) {
                    R.id.menuOSRM -> mWhichRouteProvider = RouteAsyncLoader.OSRM
                    R.id.menuGHFastest -> mWhichRouteProvider = RouteAsyncLoader.GRAPHHOPPER_FASTEST
                    R.id.menuGHPedestrian -> mWhichRouteProvider = RouteAsyncLoader.GRAPHHOPPER_PEDESTRIAN
                    R.id.menuGHBicycle -> mWhichRouteProvider = RouteAsyncLoader.GRAPHHOPPER_BICYCLE
                    R.id.menuGoogleProvider -> mWhichRouteProvider = RouteAsyncLoader.GOOGLE_FASTEST
                }

                popupMenu.dismiss()
                val routeLoader = RouteAsyncLoader.newInstance(activityContext)
                routeLoader.setCallback(this@HomeMapFragment, mWhichRouteProvider!!)

                val waypoints = ArrayList<GeoPoint>() //Build up list of geopoints
                val startPoint = GeoPoint(30.7110795, 76.6861879)
                waypoints.add(startPoint)
                val endPoint = GeoPoint(30.7231355, 76.7763967)
                waypoints.add(endPoint)

                routeLoader.execute(waypoints)
                true
            }

            popupMenu.show()

        }


        // map setting
        osmMapView.isTilesScaledToDpi = true
        osmMapView.setMultiTouchControls(true)
        osmMapView.minZoomLevel = 1.0
        osmMapView.maxZoomLevel = 21.0
        osmMapView.setZoomRounding(true)
        osmMapView.isVerticalMapRepetitionEnabled = false
        osmMapView.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0)


        //To use MapEventsReceiver methods, we add a MapEventsOverlay:
        val overlay = MapEventsOverlay(this)
        osmMapView.overlays.add(overlay)


        // My Location OverLey
        val locationProvider = GpsMyLocationProvider(activityContext)
        val myLocationOverLey = MyLocationNewOverlay(locationProvider, osmMapView)
        myLocationOverLey.enableMyLocation()
        myLocationOverLey.enableFollowLocation()
        osmMapView.overlays.add(myLocationOverLey)

        mLocationManager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        directedLocationOverlay = DirectedLocationOverlay(activityContext)
        osmMapView.overlays.add(directedLocationOverlay)
        myLocationNewOverlay = MyLocationNewOverlay(osmMapView)
        mKmlDocument = KmlDocument()


        // add location listener code
        var location: Location? = null
        if (ContextCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = mLocationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location == null)
                location = mLocationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (location != null) {
            //location known:
            onLocationChanged(location)
        } else {
            //no location known: hide directedLocationOverlay
            directedLocationOverlay?.isEnabled = false
        }


//        locationProvider.onLocationChanged()


        // Set RotationGesture
        mRotationGestureOverLey = RotationGestureOverlay(osmMapView)
        mRotationGestureOverLey?.isEnabled = true
        osmMapView.setMultiTouchControls(true)
        osmMapView.overlays.add(mRotationGestureOverLey)

//        osmMapView.controller.setCenter(myLocationOverLey?.myLocation)

//         Draw Polyline with  Road Manager
        //TODO commented

//        Thread(Runnable {
//            val roadManager = OSRMRoadManager(activityContext)
//            val road = roadManager.getRoad(mGeoPoints)
//            val polyline = RoadManager.buildRoadOverlay(road)
//            polyline.width = 15.0f
//            polyline.color = ContextCompat.getColor(activityContext, R.color.colorAccent)
//            osmMapView.overlays.add(polyline)
//            osmMapView.invalidate()
//        }).start()
//
//
//        // Add Marker
//        val startMarker = Marker(osmMapView)
//        startMarker.position = mGeoPoints[0]
//        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//        osmMapView.overlays.add(startMarker)
    }


    override fun observeData() {}

    override fun onResume() {
        super.onResume()
        val isOneProviderEnabled = startLocationUpdates()
        directedLocationOverlay?.isEnabled = isOneProviderEnabled
        osmMapView.onResume()
        //TODO: not used currently
        //mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        //sensor listener is causing a high CPU consumption...
    }


    // map events receiver
    override fun longPressHelper(p: GeoPoint?): Boolean {
        mClickedGeoPoint = p
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        InfoWindow.closeAllInfoWindowsOn(osmMapView)
        return true
    }

    private fun startLocationUpdates(): Boolean {
        var result = false
        if (mLocationManager != null && mLocationManager!!.getProviders(true) != null) {
            for (provider in mLocationManager!!.getProviders(true)) {
                if (ContextCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager?.requestLocationUpdates(provider, (2 * 1000).toLong(), 0.0f, this)
                    result = true
                }
            }
        }
        return result
    }


    //------------ LocationListener implementation
    private val mIgnorer = NetworkLocationIgnorer()
    internal var mLastTime: Long = 0 // milliseconds
    internal var mSpeed = 0.0 // km/h
    var count = 0
    override fun onLocationChanged(location: Location?) {
        val currentTime = System.currentTimeMillis()
        if (mIgnorer.shouldIgnore(location?.provider, currentTime))
            return
        val dT = (currentTime - mLastTime).toDouble()
        if (dT < 100.0) {
            //Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
            return
        }
        mLastTime = currentTime

        val newLocation = GeoPoint(location)
        if (directedLocationOverlay?.isEnabled == false) {
            //we get the location for the first time:
            directedLocationOverlay?.isEnabled = true
            osmMapView.controller.animateTo(newLocation)
        }

        val prevLocation = directedLocationOverlay?.location
        directedLocationOverlay?.location = newLocation
        directedLocationOverlay?.setAccuracy(location?.accuracy?.toInt()!!)

        if (prevLocation != null && location?.provider == LocationManager.GPS_PROVIDER) {
            mSpeed = location.speed * 3.6
            val speedInt = Math.round(mSpeed)

            tvSpeed.text = "$speedInt km/h"

            //TODO: check if speed is not too small
            if (mSpeed >= 0.1) {
                mAzimuthAngleSpeed = location.bearing
                directedLocationOverlay?.setBearing(mAzimuthAngleSpeed)
            }
        }

        //keep the map view centered on current location:
        osmMapView.controller.animateTo(newLocation)
        osmMapView.mapOrientation = -mAzimuthAngleSpeed

        if (count < 10) {
//            getRoadAsync(newLocation)
            count = 0
        } else {
            count++
        }

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    /**
     * Calculates a route from your start location to the next bus stop
     */
    var roadOverlay: Polyline? = null

    private fun calcRoute() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()//Strict mode for ignore NetworkOnMainExceptions (!for anytime, have to put in threading)
        StrictMode.setThreadPolicy(policy)

        if (roadOverlay != null) {
            osmMapView.overlays.remove(roadOverlay)
            Log.e("tag", "Deleted")
            Toast.makeText(activityContext, "Deleted", Toast.LENGTH_SHORT).show()
        }
        val waypoints = ArrayList<GeoPoint>() //Build up list of geopoints

        val startPoint = GeoPoint(30.7110795, 76.6861879)
        waypoints.add(startPoint)
        val endPoint = GeoPoint(30.7231355, 76.7763967)
        waypoints.add(endPoint)
        val roadManager = GraphHopperRoadManager(getString(R.string.graph_hopper_api_key), false)
        roadManager.addRequestOption("locale=" + Locale.getDefault().language)
        roadManager.setElevation(true)
        val road = roadManager.getRoad(waypoints)
        val d = road?.mDuration // get duration
        val x = road.mLength // get length
        val timeLeft = "${(myRound(d!!.div(60), 3))} min"//format string
        val distance = "${(myRound(x * 1000, 2))}m"
//
        tvTimeLeft.text = timeLeft
        tvDistanceLeft.text = distance
        roadOverlay = RoadManager.buildRoadOverlay(road)// draw route
        roadOverlay?.width = 30f
        roadOverlay?.color = ContextCompat.getColor(activityContext, R.color.colorPrimary)
        osmMapView.overlays.add(roadOverlay)
        osmMapView.invalidate()
        Toast.makeText(activityContext, "Refreshed", Toast.LENGTH_SHORT).show()
    }


    /**
     * Round Method for rounding doubles by number of digit
     * @param wert Value to round
     * @param stellen Numer of digit
     * @return rounded value
     */
    fun myRound(wert: Double, stellen: Int): Double {
        return Math.round(wert * Math.pow(10.0, stellen.toDouble())) / Math.pow(10.0, stellen.toDouble())
    }


    protected var mRoadOverlays: ArrayList<Polyline>? = null
    private var mWhichRouteProvider: Int? = RouteAsyncLoader.GRAPHHOPPER_FASTEST
    private var mSelectedRoad = -1
    var mRoads: Array<Road>? = null  //made static to pass between activities

    override fun updateUIWithRoads(roads: ArrayList<Road>?) {
        val mapOverlays = osmMapView.overlays
        if (mRoadOverlays != null) {
            for (i in mRoadOverlays?.indices!!)
                mapOverlays.remove(mRoadOverlays!![i])
            mRoadOverlays = null
        }

        mRoadOverlays = java.util.ArrayList()

        if (roads == null)
            return
        if (roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE)
            Toast.makeText(activityContext, "Technical issue when getting the route", Toast.LENGTH_SHORT).show()
        else if (roads[0].mStatus > Road.STATUS_TECHNICAL_ISSUE)
        //functional issues
            Toast.makeText(activityContext, "No possible route here", Toast.LENGTH_SHORT).show()

        var count = 0
        roads.forEach {
            val roadPolyline = RoadManager.buildRoadOverlay(it) // draw route
            roadPolyline.width = 40f
            roadPolyline.color = ContextCompat.getColor(activityContext, R.color.colorPrimary)

            val d = it.mDuration // get duration
            val x = it.mLength // get length
            val timeLeft = "${(myRound(d.div(60), 3))} min"//format string
            val distance = "${(myRound(x * 1000, 2))}m"
            tvTimeLeft.text = timeLeft
            tvDistanceLeft.text = distance
            Toast.makeText(activityContext, "Refreshed", Toast.LENGTH_SHORT).show()


            mRoadOverlays?.add(roadPolyline)
            if (mWhichRouteProvider == GRAPHHOPPER_BICYCLE || mWhichRouteProvider == GRAPHHOPPER_PEDESTRIAN) {
                val p = roadPolyline.paint
                p.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            }
            val routeDesc = it.getLengthDurationText(activityContext, -1)
            roadPolyline.title = getString(R.string.route) + " - " + routeDesc
            roadPolyline.infoWindow = BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, osmMapView)
            roadPolyline.relatedObject = count++
            roadPolyline.setOnClickListener(RoadOnClickListener())
            roadPolyline.paint.strokeCap = Paint.Cap.ROUND
            showingNodes(it)
            mapOverlays.add(1, roadPolyline)
        }

        selectRoad(0)
    }


    internal inner class RoadOnClickListener : Polyline.OnClickListener {
        override fun onClick(polyline: Polyline, mapView: MapView, eventPos: GeoPoint): Boolean {
            val selectedRoad = polyline.relatedObject as Int
            selectRoad(selectedRoad)
            polyline.infoWindowLocation = eventPos
            polyline.showInfoWindow()
            return true
        }
    }

    fun getRoadAsync() {
        mRoads = null
        var roadStartPoint: GeoPoint? = null
        if (startPoint != null) {
            roadStartPoint = startPoint
        } else if (directedLocationOverlay?.isEnabled!! && directedLocationOverlay?.location != null) {
            //use my current location as itinerary start point:
            roadStartPoint = directedLocationOverlay?.getLocation()
        }
        if (roadStartPoint == null || destinationPoint == null) {
            updateUIWithRoads(mRoads)
            return
        }
        val waypoints = java.util.ArrayList<GeoPoint>(2)
        waypoints.add(roadStartPoint)
        //add intermediate via points:
//        for (p in viaPoints) {
//            waypoints.add(p)
//        }
        waypoints.add(destinationPoint!!)

        val routeLoader = RouteAsyncLoader.newInstance(activityContext)
        routeLoader.setCallback(this@HomeMapFragment, mWhichRouteProvider!!)
        routeLoader.execute(waypoints)
    }

    fun getRoadAsync(roadStartPoin1t: GeoPoint) {
        mRoads = null
        var roadStartPoint: GeoPoint? = roadStartPoin1t
        if (startPoint != null) {
            roadStartPoint = startPoint
        } else if (directedLocationOverlay?.isEnabled!! && directedLocationOverlay?.location != null) {
            //use my current location as itinerary start point:
            roadStartPoint = directedLocationOverlay?.location
        }
        if (roadStartPoint == null || destinationPoint == null) {
            updateUIWithRoads(mRoads)
            return
        }
        val waypoints = java.util.ArrayList<GeoPoint>(2)
        waypoints.add(roadStartPoint)
        //add intermediate via points:
//        for (p in viaPoints) {
//            waypoints.add(p)
//        }
        waypoints.add(destinationPoint!!)

        val routeLoader = RouteAsyncLoader.newInstance(activityContext)
        routeLoader.setCallback(this@HomeMapFragment, mWhichRouteProvider!!)
        routeLoader.execute(waypoints)
    }


    internal fun selectRoad(roadIndex: Int) {
        mSelectedRoad = roadIndex
        for (i in mRoadOverlays!!.indices) {
            val p = mRoadOverlays!![i].paint
            if (i == roadIndex)
                p.color = -0x7fffff01 //blue
            else
                p.color = -0x6f99999a //grey
        }
        osmMapView.invalidate()
    }

    private inner class GeocodingTask : AsyncTask<Any, Void, List<Address>>() {
        internal var mIndex: Int = 0
        override fun doInBackground(vararg params: Any): List<Address>? {
            val locationAddress = params[0] as String
            mIndex = params[1] as Int
            val geocoder = GeocoderNominatim(userAgent)
            geocoder.setOptions(true) //ask for enclosing polygon (if any)
            //GeocoderGraphHopper geocoder = new GeocoderGraphHopper(Locale.getDefault(), graphHopperApiKey);
            try {
                val viewbox = osmMapView.getBoundingBox()
                return geocoder.getFromLocationName(locationAddress, 1,
                        viewbox.getLatSouth(), viewbox.getLonEast(),
                        viewbox.getLatNorth(), viewbox.getLonWest(), false)
            } catch (e: Exception) {
                return null
            }

        }

        override fun onPostExecute(foundAdresses: List<Address>?) {
            if (foundAdresses == null) {
                Toast.makeText(activityContext, "Geocoding error", Toast.LENGTH_SHORT).show()
            } else if (foundAdresses.isEmpty()) { //if no address found, display an error
                Toast.makeText(activityContext, "Address not found.", Toast.LENGTH_SHORT).show()
            } else {
                val address = foundAdresses[0] //get first address
                val addressDisplayName = address.extras.getString("display_name")
                if (mIndex == START_INDEX) {
                    startPoint = GeoPoint(address.latitude, address.longitude)
//                    markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
//                            R.string.departure, R.drawable.marker_departure, -1, addressDisplayName)
                    osmMapView.controller.setCenter(startPoint)
                } else if (mIndex == DEST_INDEX) {
                    destinationPoint = GeoPoint(address.latitude, address.longitude)
//                    markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
//                            R.string.destination, R.drawable.marker_destination, -1, addressDisplayName)
                    osmMapView.controller.setCenter(destinationPoint)
                }
                getRoadAsync()
                //get and display enclosing polygon:
//                val extras = address.extras
//                if (extras != null && extras.containsKey("polygonpoints")) {
//                    val polygon = extras.getParcelableArrayList<GeoPoint>("polygonpoints")
//                    //Log.d("DEBUG", "polygon:"+polygon.size());
//                    updateUIWithPolygon(polygon, addressDisplayName)
//                } else {
//                    updateUIWithPolygon(null, "")
//                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun locationPermissionGranted() {
        isLocationGranted = true
        Handler().postDelayed({
            //initializing location callback
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    userCurrentLatLng = LatLng(locationResult?.lastLocation?.latitude!!,
                            locationResult.lastLocation?.longitude!!)
                    if (null != userCurrentLatLng)


                        Log.e("location", "lat : ${userCurrentLatLng?.latitude} " +
                                "lng : ${userCurrentLatLng?.longitude}")


                }
            }

            //creating customized locationRequestObject
            val mLocationRequest = LocationRequest.create().apply {
                interval = LOCATION_INTERVAL
                fastestInterval = FASTEST_LOCATION_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            //requesting for location update from fusedLocationProvider
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper())
        }, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode && REQUEST_LOCATION == requestCode) {
            checkLocationAvailability()
        }
    }


    private fun checkLocationAvailability() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) openDialogToTurnOnLocation()
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mMarshmallowPermissions.requestPermissionForLocation()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == MarshMallowPermissions.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAvailability()
            } else {
//                showMessage(R.string.enable_location_permission, null,
//                        false)
            }
        }
    }

    private fun openDialogToTurnOnLocation() {

        //customizing location request
        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setNeedBle(true)
                .setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(activityContext
                as BaseAppCompactActivity).checkLocationSettings(builder.build())

        result.addOnSuccessListener {
            locationPermissionGranted()
        }
        result.addOnFailureListener {
            val statusCode = (it as ApiException).statusCode
            when (statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        val resolvableException = it as ResolvableApiException
                        resolvableException.startResolutionForResult(activity, REQUEST_LOCATION)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
//        directedLocationOverlay.enableMyLocation(source)

    }

    fun showingNodes(road: Road) {
        val nodeIcon = ContextCompat.getDrawable(activityContext, R.drawable.ic_follow_me)
        for (i in 0 until road.mNodes.size) {
            val node = road.mNodes[i]
            val nodeMarker = Marker(osmMapView)
            nodeMarker.icon = nodeIcon
            osmMapView.overlays.add(nodeMarker)
            nodeMarker.snippet = node.mInstructions

            nodeMarker.subDescription = Road.getLengthDurationText(activityContext, node.mLength, node.mDuration)


        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager?.removeUpdates(this)
        }
        osmMapView.onPause()
    }

}