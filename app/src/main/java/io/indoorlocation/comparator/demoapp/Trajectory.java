package io.indoorlocation.comparator.demoapp;


import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class Trajectory {
    private String providerName;
    public Polyline polyline;
    private LatLng lastCoordinate;
    private String color;
    private Marker point;

    public Trajectory(String providerName, Polyline polyline, String color) {
        this.providerName = providerName;
        this.polyline = polyline;
        this.color = color;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public LatLng getLastCoordinate() {
        return lastCoordinate;
    }

    public void setLastCoordinate(LatLng lastCoordinate) {
        this.lastCoordinate = lastCoordinate;
    }

    public Marker getPoint() {
        return point;
    }

    public void setPoint(Marker point) {
        this.point = point;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
