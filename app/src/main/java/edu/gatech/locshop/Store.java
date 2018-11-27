package edu.gatech.locshop;

import android.location.Location;

import java.util.ArrayList;

public class Store {
    private Double latitude;
    private Double longitude;

    public Store() {
        latitude = 0.0;
        longitude = 0.0;
    }

    public Store(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
