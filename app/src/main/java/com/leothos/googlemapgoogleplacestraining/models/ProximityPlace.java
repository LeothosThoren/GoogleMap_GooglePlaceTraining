package com.leothos.googlemapgoogleplacestraining.models;

import com.google.android.gms.maps.model.LatLng;

public class ProximityPlace {
    private CharSequence mName;
    private LatLng mLatLng;

    public ProximityPlace(CharSequence name, LatLng latLng) {
        mName = name;
        mLatLng = latLng;
    }

    public ProximityPlace() {
        //empty constructor
    }

    public CharSequence getName() {
        return mName;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }
}
