package com.twormobile.itrackmygps;

import junit.framework.TestCase;

public class MainActivityTest extends TestCase{
    MainActivity activity;

    public MainActivityTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        activity = new MainActivity();
    }

    public void testIsBetterLocation(){
        assertEquals(true, false);
    }

}

