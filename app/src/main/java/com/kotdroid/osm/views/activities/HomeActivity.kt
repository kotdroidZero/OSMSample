package com.kotdroid.osm.views.activities

import com.kotdroid.osm.R
import com.kotdroid.osm.views.fragments.HomeMapFragment

class HomeActivity : BaseAppCompactActivity() {


    override val layoutId: Int
        get() = R.layout.activity_main

    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override fun init() {
        doFragmentTransaction(containerViewId = R.id.flHome, fragment = HomeMapFragment())
    }
}
