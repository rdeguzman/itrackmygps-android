package com.twormobile.gpslogger;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapUtils {

    public static boolean isLatLngNotVisible(LatLng pos, GoogleMap map){
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        return bounds.contains(pos) == false;
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }
}
