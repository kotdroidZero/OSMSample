package com.kotdroid.osm.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.kotdroid.osm.utils.MarshMallowPermissions
import com.kotdroid.osm.viewmodels.BaseLocationViewModel
import com.kotdroid.osm.views.activities.BaseAppCompactActivity

abstract class BaseLocationFragment : BaseFragment() {

    companion object {

        const val REQUEST_LOCATION = 89
        const val LOCATION_INTERVAL: Long = 1000
        const val FASTEST_LOCATION_INTERVAL: Long = 10000
    }

    private val mMarshmallowPermissions: MarshMallowPermissions by lazy {
        MarshMallowPermissions(this)
    }

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback

    var userCurrentLatLng: LatLng? = null
    var isLocationGranted = false


    @SuppressLint("MissingPermission")
    override fun init() {

        // Get FusedLocationClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activityContext)

        // Initialize ViewModel
        setViewModel()

        // Location permission related methods
        checkLocationAvailability()

        //do your routine work after the location permission has been granted or denied
        setData()

        //request for location
        requestLocation()
    }

     private fun requestLocation(){
//         mFusedLocationProviderClient.requestLocationUpdates()
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
                        getViewModel?.setUserLocation(userCurrentLatLng!!)
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
        if (RESULT_OK == resultCode && REQUEST_LOCATION == requestCode) {
            checkLocationAvailability()
        }
    }

    abstract fun setData()
    abstract fun setViewModel()
    abstract val getViewModel: BaseLocationViewModel?
//    abstract fun getLastLocation(currentLatLng: LatLng)
}