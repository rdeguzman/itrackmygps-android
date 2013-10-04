package com.twormobile.gpslogger;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapUtils {

    public static boolean isLatLngVisible(GoogleMap map, LatLng pos){
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        return bounds.contains(pos);
    }
}
