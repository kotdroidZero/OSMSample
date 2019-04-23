//package com.kotdroid.osm.views.newstart
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Location
//import android.location.LocationManager
//import android.os.Build
//import android.os.Handler
//import android.preference.PreferenceManager
//import android.support.design.widget.Snackbar
//import android.support.v4.app.DialogFragment
//import android.support.v4.content.ContextCompat
//import android.util.Log
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.widget.EditText
//import android.widget.Toast
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.common.api.ResolvableApiException
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationSettingsRequest
//import com.google.android.gms.location.LocationSettingsStatusCodes
//import com.graphhopper.directions.api.client.model.GeocodingLocation
//import com.kotdroid.osm.R
//import com.kotdroid.osm.views.activities.BaseAppCompactActivity
//import com.kotdroid.osm.views.fragments.HomeMapFragment
//import com.mapbox.api.directions.v5.DirectionsCriteria
//import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.api.directions.v5.models.DirectionsRoute
//import com.mapbox.core.constants.Constants
//import com.mapbox.geojson.LineString
//import com.mapbox.geojson.Point
//import com.mapbox.mapboxsdk.Mapbox
//import com.mapbox.mapboxsdk.annotations.MarkerOptions
//import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException
//import com.mapbox.mapboxsdk.geometry.LatLng
//import com.mapbox.mapboxsdk.geometry.LatLngBounds
//import com.mapbox.mapboxsdk.maps.MapView
//import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
//import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
//import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
//import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
//import com.mapbox.services.android.navigation.ui.v5.route.OnRouteSelectionChangeListener
//import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
//import kotlinx.android.synthetic.main.fragmnet_ghhome_map.*
//import kotlinx.android.synthetic.main.fragmnet_home_map.osmMapView
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//import org.osmdroid.util.GeoPoint
//import org.osmdroid.util.LocationUtils.getLastKnownLocation
//import org.osmdroid.util.TileSystem
//import org.osmdroid.views.overlay.Marker
//
//class GhOsmMapActivity : BaseAppCompactActivity(),
//        OnRouteSelectionChangeListener,
//        FetchGeocodingTaskCallbackInterface,
//        GeocodingInputDialog.NoticeDialogListener {
//
//
//    private var currentGeocodingInput = ""
//    private var markers: MutableList<Marker>? = null
//    private var currentMarker: Marker? = null
//    private var route: DirectionsRoute? = null
//    private var waypoints: MutableList<Point> = java.util.ArrayList()
//    private var mLocationManager: LocationManager? = null
//    private var locationLayer: LocationLayerPlugin? = null
//    private var mapRoute: NavigationMapRoute? = null
//
//    override val layoutId: Int
//        get() = R.layout.fragmnet_ghhome_map
//
//    override val isMakeStatusBarTransparent: Boolean
//        get() = false
//
//    override fun init() {
//// Location permission related methods
//        checkLocationAvailability()
//
//        // Set Tile for OpenStreetMap
//        osmMapView.setTileSource(TileSourceFactory.MAPNIK)
//
//        // map setting
//        osmMapView.isTilesScaledToDpi = true
//        osmMapView.setMultiTouchControls(true)
//        osmMapView.minZoomLevel = 1.0
//        osmMapView.maxZoomLevel = 21.0
//        osmMapView.setZoomRounding(true)
//        osmMapView.isVerticalMapRepetitionEnabled = false
//        osmMapView.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0)
//
//        //locationManager
//        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        initMapRoute()
//
//    }
//
//    private fun initMapRoute() {
//        mapRoute = NavigationMapRoute(osmMapView, mapRoute)
//        mapRoute?.setOnRouteSelectionChangeListener(this)
//    }
//
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.navigation_view_activity_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onStart() {
//        super.onStart()
//        osmMapView.onResume()
//        if (locationLayer != null) {
//            locationLayer?.onStart()
//        }
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        osmMapView.onResume()
//        handleIntent(intent)
//    }
//
//
//    private fun handleIntent(intent: Intent?) {
//        if (intent != null) {
//            val data = intent.data
//            if (data != null && "graphhopper.com" == data.host) {
//                if (data.path != null) {
//                    if (this.osmMapView == null) {
//                        //this happens when onResume is called at the initial start and we will call this method again in onMapReady
//                        return
//                    }
//                    if (data.path!!.contains("maps")) {
//                        clearRoute()
//                        //Open Map Url
////                        setRouteProfileToSharedPreferences(data.getQueryParameter("vehicle"))
//
//                        val points = data.getQueryParameters("point")
//                        for (point in points) {
//                            val pointArr = point.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                            addPointToRoute(java.lang.Double.parseDouble(pointArr[0]), java.lang.Double.parseDouble(pointArr[1]))
//                        }
//
////                        setStartFromLocationToSharedPreferences(false)
//                        updateRouteAfterWaypointChange()
//                    }
//                    // https://graphhopper.com/api/1/vrp/solution/e7fb8a9b-e441-4ec2-a487-20788e591bb3?vehicle_id=1&key=[KEY]
//                    if (data.path!!.contains("api/1/vrp/solution")) {
//                        clearRoute()
//                        //Open Vrp Url
//                        val pathSegments = data.pathSegments
////                        fetchVrpSolution(pathSegments[pathSegments.size - 1], data.getQueryParameter("vehicle_id"))
//                    }
//                }
//
//            }
//        }
//    }
//
//    private fun updateRouteAfterWaypointChange() {
//        if (this.waypoints.isEmpty()) {
//            hideLoading()
//        } else {
//            val lastPoint = this.waypoints[this.waypoints.size - 1]
//            val latLng = LatLng(lastPoint.latitude(), lastPoint.longitude())
//            setCurrentMarkerPosition(latLng)
//            if (this.waypoints.size > 0) {
//                fetchRoute()
//            } else {
//                hideLoading()
//            }
//        }
//    }
//
//    private fun setCurrentMarkerPosition(position: LatLng?) {
//        if (position != null) {
//            if (currentMarker == null) {
//                val markerOptions = MarkerOptions()
//                        .position(position)
//                val marker = Marker(osmMapView)
////                marker.icon = markerOptions.icon
////                marker.position = GeoPoint(markerOptions.position.latitude, markerOptions.position.longitude)
////                marker.snippet = markerOptions.snippet
////                marker.title = markerOptions.title
//                osmMapView.overlays.add(currentMarker)
//            } else {
//                currentMarker?.position = GeoPoint(position.latitude, position.longitude)
//            }
//        }
//    }
//
//
//    private fun addPointToRoute(lat: Double, lng: Double) {
//        waypoints.add(Point.fromLngLat(lng, lat))
//    }
//
//    private fun clearRoute() {
//        waypoints.clear()
//        mapRoute?.removeRoute()
//        route = null
//        if (currentMarker != null) {
//
//            osmMapView.overlays.remove(currentMarker)
//
//            currentMarker = null
//        }
//    }
//
//    override fun onNewPrimaryRouteSelected(directionsRoute: DirectionsRoute) {
//        route = directionsRoute
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.geocoding_search_btn -> {
//                showGeocodingInputDialog()
//            }
//            R.id.navigate_btn -> {
//                launchNavigationWithRoute()
//            }
//        }
//        return true
//    }
//
//
//    private fun checkLocationAvailability() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) openDialogToTurnOnLocation()
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Toast.makeText(this, "Please enable location permission from setting.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    private fun openDialogToTurnOnLocation() {
//
//        //customizing location request
//        val locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(1000)
//                .setFastestInterval(1000)
//
//        val builder = LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest)
//                .setNeedBle(true)
//                .setAlwaysShow(true)
//
//        val result = LocationServices.getSettingsClient(this
//                as BaseAppCompactActivity).checkLocationSettings(builder.build())
//
//        result.addOnSuccessListener {
//            locationPermissionGranted()
//        }
//        result.addOnFailureListener {
//            val statusCode = (it as ApiException).statusCode
//            when (statusCode) {
//                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
//                    try {
//                        val resolvableException = it as ResolvableApiException
//                        resolvableException.startResolutionForResult(this, HomeMapFragment.REQUEST_LOCATION)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun locationPermissionGranted() {
////        isLocationGranted = true
//        Handler().postDelayed({
//            //initializing location callback
////            mLocationCallback = object : LocationCallback() {
////                override fun onLocationResult(locationResult: LocationResult?) {
////                    super.onLocationResult(locationResult)
////                    userCurrentLatLng = LatLng(locationResult?.lastLocation?.latitude!!,
////                            locationResult.lastLocation?.longitude!!)
////                    if (null != userCurrentLatLng)
////
////
////                        Log.e("location", "lat : ${userCurrentLatLng?.latitude} " +
////                                "lng : ${userCurrentLatLng?.longitude}")
////
////
////                }
////            }
//
//            //creating customized locationRequestObject
//            val mLocationRequest = LocationRequest.create().apply {
//                interval = HomeMapFragment.LOCATION_INTERVAL
//                fastestInterval = HomeMapFragment.FASTEST_LOCATION_INTERVAL
//                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            }
//
//            //requesting for location update from fusedLocationProvider
////            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
////                    mLocationCallback, Looper.myLooper())
//        }, 200)
//    }
//
//    private fun showGeocodingInputDialog() {
//        // Create an instance of the dialog fragment and show it
//        val dialog = GeocodingInputDialog()
//        dialog.setGeocodingInput(currentGeocodingInput)
//        dialog.show(supportFragmentManager!!, "gh-example")
//    }
//
//    override fun onDialogPositiveClick(dialog: DialogFragment?) {
//
//        // Check if it's a geocoding search
//        val search = dialog?.dialog?.findViewById<EditText>(R.id.geocoding_input_id)
//        if (search != null) {
//            currentGeocodingInput = search.text.toString()
//
//            showLoading()
//            var point: String? = null
//            val pointLatLng = this.osmMapView.boundingBox.center
//            if (pointLatLng != null)
//                point = pointLatLng.latitude.toString() + "," + pointLatLng.longitude
//            FetchGeocodingTask(this, getString(R.string.graph_hopper_api_key)).execute(FetchGeocodingConfig(currentGeocodingInput, "en", 5, false, point, "default"))
//        }
//    }
//
//    private fun showLoading() {
//        if (loading.visibility == View.INVISIBLE) {
//            loading.visibility = View.VISIBLE
//        }
//    }
//
//    private fun hideLoading() {
//        if (loading.visibility == View.VISIBLE) {
//            loading.visibility = View.INVISIBLE
//        }
//    }
//
//
//    // FetchGeocodingTaskCallbackInterface callback
//    override fun onError(message: Int) {
//        Log.e("tag", "error")
//    }
//
//    override fun onPostExecuteGeocodingSearch(locations: MutableList<GeocodingLocation>?) {
//        Log.e("tag", "this is location you searched for !")
//        markers = ArrayList<Marker>(locations?.size!!)
//
//        if (locations.isEmpty()) {
//            onError(R.string.error_geocoding_no_location)
//            return
//        }
//
//        val bounds = java.util.ArrayList<LatLng>()
//        val lastKnownLocation = getLastKnownLocation(mLocationManager)
//
//        if (lastKnownLocation != null)
//            bounds.add(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude))
//
//        locations.forEach {
//            val point = it.point
//            val marker = Marker(osmMapView)
//            val latLng = LatLng(point.lat, point.lng)
//            marker.position = GeoPoint(latLng.latitude, latLng.longitude)
//            bounds.add(latLng)
//            marker.title = it.name
//
//            var snippet = ""
//            if (it.street != null) {
//                snippet += it.street
//                if (it.housenumber != null)
//                    snippet += " " + it.housenumber
//                snippet += "\n"
//            }
//            if (it.city != null) {
//                if (it.postcode != null)
//                    snippet += it.postcode + " "
//                snippet += it.city + "\n"
//            }
//
//            if (it.country != null)
//                snippet += it.country + "\n"
//
//            if (it.osmId != null) {
//                snippet += "OSM-Id: " + it.osmId + "\n"
//                if (it.osmKey != null)
//                    snippet += "OSM-Key: " + it.osmKey + "\n"
//                if (it.osmType != null)
//                    snippet += "OSM-Type: " + it.osmType + "\n"
//            }
//
//            snippet += "\n\n Tap on info window\n to add point to route"
//            if (!snippet.isEmpty())
//                marker.snippet = snippet
//            marker.icon = resources.getDrawable(R.drawable.ic_follow_me_on)
//            osmMapView.overlays.add(marker)
//
//        }
//
//        // For bounds we need at least 2 entries
//        if (bounds.size >= 2) {
//            val boundsBuilder = LatLngBounds.Builder()
//            boundsBuilder.includes(bounds)
//            animateCameraBbox(boundsBuilder.build())
//        } else if (bounds.size == 1) {
//            // If there is only 1 result (=>current location unknown), we just zoom to that result
//            animateCamera(bounds[0])
//        }
//        hideLoading()
//    }
//
//
//    private fun animateCameraBbox(bounds: LatLngBounds) {
//        val myPoint1 = GeoPoint(bounds.center.latitude, bounds.center.longitude)
//        osmMapView.controller.setCenter(myPoint1)
//
//        osmMapView.controller.zoomTo(15)
//        osmMapView.controller.animateTo(myPoint1)
//    }
//
//    private fun animateCamera(point: LatLng) {
//        val myPoint1 = GeoPoint(point.latitude, point.longitude);
//        osmMapView.controller.setCenter(myPoint1)
//        osmMapView.controller.zoomTo(15)
//        osmMapView.controller.animateTo(myPoint1)
//    }
//
//    private fun launchNavigationWithRoute() {
//        if (route == null) {
//            Log.e("tag", "Route is not available !")
//            return
//        }
//
//        val lastKnownLocation = getLastKnownLocation()
//        if (lastKnownLocation != null && waypoints.size > 1) {
//            val distance = FloatArray(1)
//            Location.distanceBetween(lastKnownLocation.latitude, lastKnownLocation.longitude, waypoints[0].latitude(), waypoints[0].longitude(), distance)
//
//            //Ask the user if he would like to recalculate the route from his current positions
//            if (distance[0] > 100) {
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle(R.string.error_too_far_from_start_title)
//                builder.setMessage(R.string.error_too_far_from_start_message)
//                builder.setPositiveButton(R.string.ok) { dialog, id ->
//                    waypoints[0] = (Point.fromLngLat(lastKnownLocation.longitude, lastKnownLocation.latitude))
//                    fetchRoute()
//                }
//                builder.setNegativeButton(R.string.cancel) { dialog, id -> _launchNavigationWithRoute() }
//
//                val dialog = builder.create()
//                dialog.show()
//            } else {
//                _launchNavigationWithRoute()
//            }
//        } else {
//            _launchNavigationWithRoute()
//        }
//    }
//
//    private fun _launchNavigationWithRoute() {
//        val optionsBuilder = NavigationLauncherOptions.builder()
//                .shouldSimulateRoute(getShouldSimulateRouteFromSharedPreferences())
//                .directionsProfile(getRouteProfileFromSharedPreferences())
//                .waynameChipEnabled(false)
//
//        optionsBuilder.directionsRoute(route)
//
//        NavigationLauncher.startNavigation(this, optionsBuilder.build())
//    }
//
//
//    private fun getRouteProfileFromSharedPreferences(): String? {
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        return sharedPreferences.getString(
//                getString(R.string.route_profile_key), DirectionsCriteria.PROFILE_DRIVING
//        )
//    }
//
//    private fun getShouldSimulateRouteFromSharedPreferences(): Boolean {
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        return sharedPreferences.getBoolean(getString(R.string.simulate_route_key), false)
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun getLastKnownLocation(): Location? {
//        return if (locationLayer != null) {
//            locationLayer?.lastKnownLocation
//        } else null
//    }
//
//
//    private fun fetchRoute() {
//        val builder = NavigationRoute.builder(this)
//                .accessToken("pk." + getString(R.string.graph_hopper_api_key))
//                .baseUrl(getString(R.string.base_url))
//                .user("gh")
//                .alternatives(true)
//
//        val startFromLocation = getStartFromLocationFromSharedPreferences()
//
//        if (!startFromLocation && waypoints.size < 2 || startFromLocation && waypoints.size < 1) {
//            onError(R.string.error_not_enough_waypoints)
//            return
//        }
//
//        if (startFromLocation) {
//            val lastKnownLocation = getLastKnownLocation()
//            if (lastKnownLocation == null) {
//                onError(R.string.error_location_not_found)
//                return
//            } else {
//                val location = Point.fromLngLat(lastKnownLocation.longitude, lastKnownLocation.latitude)
//                if (lastKnownLocation.hasBearing())
//                // 90 seems to be the default tolerance of the SDK
//                    builder.origin(location, lastKnownLocation.bearing.toDouble(), 90.0)
//                else
//                    builder.origin(location)
//            }
//        }
//
//        for (i in waypoints.indices) {
//            val p = waypoints[i]
//            if (i == 0 && !startFromLocation) {
//                builder.origin(p)
//            } else if (i < waypoints.size - 1) {
//                builder.addWaypoint(p)
//            } else {
//                builder.destination(p)
//            }
//        }
//
//        showLoading()
//
////        setFieldsFromSharedPreferences(builder)
//        builder.build().getRoute(object : SimplifiedCallback() {
//            override fun onResponse(call: retrofit2.Call<DirectionsResponse>, response: retrofit2.Response<DirectionsResponse>) {
//                if (validRouteResponse(response)) {
//                    route = response.body()?.routes()?.get(0)
//                    mapRoute?.addRoutes(response.body()?.routes()!!)
//                    boundCameraToRoute()
//                } else {
//                    Snackbar.make(osmMapView, R.string.error_calculating_route, Snackbar.LENGTH_LONG).show()
//                }
//                hideLoading()
//            }
//
//            override fun onFailure(call: retrofit2.Call<DirectionsResponse>, throwable: Throwable) {
//                super.onFailure(call, throwable)
//                hideLoading()
//            }
//        })
//    }
//
//
//    private fun validRouteResponse(response: retrofit2.Response<DirectionsResponse>): Boolean {
//        return response.body() != null && !response.body()!!.routes().isEmpty()
//    }
//
//    private fun boundCameraToRoute() {
//        if (route != null) {
//            val routeCoords = LineString.fromPolyline(route?.geometry()!!,
//                    Constants.PRECISION_6).coordinates()
//            val bboxPoints = java.util.ArrayList<LatLng>()
//            for (point in routeCoords) {
//                bboxPoints.add(LatLng(point.latitude(), point.longitude()))
//            }
//            if (bboxPoints.size > 1) {
//                try {
//                    val bounds = LatLngBounds.Builder().includes(bboxPoints).build()
//                    // left, top, right, bottom
//                    animateCameraBbox(bounds)
//                } catch (exception: InvalidLatLngBoundsException) {
//                    Toast.makeText(this, R.string.error_valid_route_not_found, Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        }
//    }
//
//    private fun getStartFromLocationFromSharedPreferences(): Boolean {
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        return sharedPreferences.getBoolean(getString(R.string.start_from_location_key), true)
//    }
//
//}