package com.twormobile.trackble;

public enum GpsFix {

    IDLE("Idle", R.drawable.ball_red),
    ACQUIRING_FIX("Acquiring", R.drawable.ball_orange),
    CONNECTED("Connected", R.drawable.ball_green);

    private int icon;
    private String title;

    GpsFix(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    @Override
    public String toString(){
        return title;
    }

    public int icon(){
        return icon;
    }
}
