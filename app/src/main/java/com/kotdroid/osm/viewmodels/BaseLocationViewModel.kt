package com.kotdroid.osm.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

abstract class BaseLocationViewModel(application: Application) : BaseViewModel(application) {

    private var userLocation = MutableLiveData<LatLng>()

    fun setUserLocation(latLng: LatLng) {
        userLocation.postValue(latLng)
    }

    fun getUserLocation(): LiveData<LatLng> = userLocation
}