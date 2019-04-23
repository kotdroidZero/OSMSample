package com.kotdroid.osm.views.utils

import android.content.Context
import android.os.AsyncTask
import com.kotdroid.osm.R
import org.osmdroid.bonuspack.routing.*
import org.osmdroid.util.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class RouteAsyncLoader : AsyncTask<ArrayList<GeoPoint>, Void, Array<Road>?>() {


    companion object {
        const val OSRM = 0
        const val GRAPHHOPPER_FASTEST = 1
        const val GRAPHHOPPER_BICYCLE = 2
        const val GRAPHHOPPER_PEDESTRIAN = 3
        const val GOOGLE_FASTEST = 4
        private lateinit var mContext: Context

        fun newInstance(mContext: Context): RouteAsyncLoader {
            this.mContext = mContext
            return RouteAsyncLoader()
        }
    }

    private var routeCallback: RouteCallback? = null
    private var routeProviderType = -1

    fun setCallback(routeCallback: RouteCallback, provider: Int) {
        this.routeCallback = routeCallback
        routeProviderType = provider
    }


    override fun doInBackground(vararg params: ArrayList<GeoPoint>?): Array<Road>? {
        val waypoints = params[0] as ArrayList<GeoPoint>
        val roadManager: RoadManager
        val locale = Locale.getDefault()
        when (routeProviderType) {
            OSRM -> {
                roadManager = OSRMRoadManager(mContext)
            }
            GRAPHHOPPER_FASTEST -> {
                roadManager = GraphHopperRoadManager(mContext.getString(R.string.graph_hopper_api_key), false)
                roadManager.addRequestOption("locale=" + locale.language)
            }
            GRAPHHOPPER_BICYCLE -> {
                roadManager = GraphHopperRoadManager(mContext.getString(R.string.graph_hopper_api_key), false)
                roadManager.addRequestOption("locale=" + locale.language)
                roadManager.addRequestOption("vehicle=bike")
            }
            GRAPHHOPPER_PEDESTRIAN -> {
                roadManager = GraphHopperRoadManager(mContext.getString(R.string.graph_hopper_api_key), false)
                roadManager.addRequestOption("locale=" + locale.language)
                roadManager.addRequestOption("vehicle=foot")
            }
            GOOGLE_FASTEST -> {
                roadManager = GoogleRoadManager()

            }
            else -> {
                return null
            }
        }
        return roadManager.getRoads(waypoints)
    }

    override fun onPostExecute(result: Array<Road>?) {

        val list = ArrayList<Road>()
        result?.forEach {
            list.add(it)
        }
        routeCallback?.updateUIWithRoads(list)

    }

    interface RouteCallback {
        fun updateUIWithRoads(result: ArrayList<Road>?)
    }
}