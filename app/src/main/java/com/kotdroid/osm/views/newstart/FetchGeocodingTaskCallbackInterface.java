package com.kotdroid.osm.views.newstart;

import com.graphhopper.directions.api.client.model.GeocodingLocation;

import java.util.List;

public interface FetchGeocodingTaskCallbackInterface {

    void onError(int message);

    void onPostExecuteGeocodingSearch(List<GeocodingLocation> points);

}
