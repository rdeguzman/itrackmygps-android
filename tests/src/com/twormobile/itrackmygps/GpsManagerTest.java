package com.twormobile.itrackmygps;

import android.location.Location;
import junit.framework.TestCase;

public class GpsManagerTest extends TestCase{
    GpsManager gpsManager;

    public GpsManagerTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        gpsManager = new GpsManager();
    }

    public void testIsSameLocation(){
        long time = 1403241138983L;
        double lat = -37.9265958;
        double lng = 145.2287573;

        Location loc1 = new Location("fused");
        loc1.setLatitude(lat);
        loc1.setLongitude(lng);
        loc1.setTime(time);

        Location loc2 = new Location("fused");
        loc2.setLatitude(lat);
        loc2.setLongitude(lng);
        loc2.setTime(time);

        assertEquals(true, gpsManager.isSameLocation(loc1, loc2));
        assertEquals(false, gpsManager.isBetterLocation(loc1, loc2));
    }

}

