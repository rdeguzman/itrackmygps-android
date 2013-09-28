package com.example.gpslogger;

import android.app.Activity;
import android.location.GpsSatellite;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SatteliteListAdapter extends BaseAdapter {
    public ArrayList list;
    Activity activity;

    public SatteliteListAdapter(Activity activity, ArrayList list) {
        super();
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView txtFirst;
        TextView txtSecond;
        TextView txtThird;
        TextView txtFourth;
        TextView txtFifth;
        TextView txtSixth;
        TextView txtSeventh;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater =  activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_satellite, null);

            holder = new ViewHolder();
            holder.txtFirst = (TextView) convertView.findViewById(R.id.tv_satellite_id);

            holder.txtSecond = (TextView) convertView.findViewById(R.id.tv_satellite_azimuth);
            holder.txtThird = (TextView) convertView.findViewById(R.id.tv_satellite_elevation);
            holder.txtFourth = (TextView) convertView.findViewById(R.id.tv_satellite_snr);
            holder.txtFifth = (TextView) convertView.findViewById(R.id.tv_satellite_almanac);
            holder.txtSixth = (TextView) convertView.findViewById(R.id.tv_satellite_ephemeris);
            holder.txtSeventh = (TextView) convertView.findViewById(R.id.tv_satellite_fix);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        GpsSatellite sat = (GpsSatellite)list.get(position);

        holder.txtFirst.setText(Integer.toString(sat.getPrn()));

        holder.txtSecond.setText(Float.toString(sat.getAzimuth()));
        holder.txtThird.setText(Float.toString(sat.getElevation()));
        holder.txtFourth.setText(Float.toString(sat.getSnr()));
        holder.txtFifth.setText(sat.hasAlmanac() ? "T" : "F");
        holder.txtSixth.setText(sat.hasEphemeris() ? "T" : "F");
        holder.txtSeventh.setText(sat.usedInFix() ? "T" : "F");

        return convertView;
    }
}
