package io.indoorlocation.comparator.demoapp;

import android.graphics.Color;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.commons.utils.PolylineUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;
import io.indoorlocation.core.IndoorLocationProviderListener;

public class Comparator extends IndoorLocationProvider implements IndoorLocationProviderListener {
    private List<IndoorLocationProvider> indoorLocationProviderList;
    private List<Trajectory> trajectories;
    private MapboxMap mapboxMap;
    private boolean isStarted = false;
    List<String> colorList;

    public Comparator(MapboxMap mapboxMap) {
        super();
        this.indoorLocationProviderList = new ArrayList<>();
        this.mapboxMap = mapboxMap;

        this.trajectories = new ArrayList<>();

        colorList = Arrays.asList(
                "#F44336", "#3F51B5", "#9C27B0",
                "#2196F3", "#00BCD4", "#009688",
                "#8BC34A", "#CDDC39", "#FFC107");

    }

    public void addIndoorLocationProvider(IndoorLocationProvider indoorLocationProvider) {
        this.indoorLocationProviderList.add(indoorLocationProvider);
        indoorLocationProvider.addListener(this);

        String color = colorList.get(trajectories.size() % colorList.size());

        Polyline polyline = mapboxMap.addPolyline(new PolylineOptions()
                .color(Color.parseColor(color))
                .width(2));


        this.trajectories.add(new Trajectory(indoorLocationProvider.getName(), polyline, color));



        if (this.isStarted) {
            indoorLocationProvider.start();
        }
    }

    public void removeIndoorLocationProvider(IndoorLocationProvider indoorLocationProvider) {
        this.indoorLocationProviderList.remove(indoorLocationProvider);
        indoorLocationProvider.stop();
    }

    @Override
    public boolean supportsFloor() {
        for (IndoorLocationProvider indoorLocationProvider : this.indoorLocationProviderList) {
            if (indoorLocationProvider.supportsFloor()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        for (IndoorLocationProvider indoorLocationProvider : this.indoorLocationProviderList) {
            indoorLocationProvider.start();
        }
    }

    @Override
    public void stop() {
        for (IndoorLocationProvider indoorLocationProvider : this.indoorLocationProviderList) {
            indoorLocationProvider.stop();
        }
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }


    @Override
    public void onProviderStarted() {
        Log.d("Comparator", "providerStarted");
        if (!this.isStarted) {
            this.dispatchOnProviderStarted();
        }
        this.isStarted = true;
    }

    @Override
    public void onProviderStopped() {
        Log.d("Comparator", "providerStarted");
        boolean allAreStopped = true;
        for (IndoorLocationProvider provider : indoorLocationProviderList) {
            if (provider.isStarted()) {
                allAreStopped = false;
            }
        }
        if (allAreStopped) {
            this.dispatchOnProviderStopped();
        }
    }

    @Override
    public void onProviderError(Error error) {
        this.dispatchOnProviderError(error);
    }

    @Override
    public void onIndoorLocationChange(IndoorLocation indoorLocation) {
        //this.indoorLocationMap.put(indoorLocation.getProvider(), indoorLocation);
        //IndoorLocation selectedIndoorLocation = this.selectIndoorLocation(new ArrayList<>(this.indoorLocationMap.values()));
        //this.dispatchIndoorLocationChange(selectedIndoorLocation);
        Log.d("Location updated", indoorLocation.getLatitude() + " " + indoorLocation.getLongitude());
        //indoorLocationByProvider[[provider getName]] = location;
/*    ILIndoorLocation* selectedLocation = [self selectIndoorLocation:[indoorLocationByProvider allValues]];
    if (selectedLocation) {
        [self dispatchDidUpdateLocation:selectedLocation];
    }*/

/*
        this.dispatchIndoorLocationChange(indoorLocation);


        List<LatLng> points;

        points = new ArrayList<>();

        points.add(new LatLng(50.632853, 3.019985));
        points.add(new LatLng(50.633309, 3.022267));


        mapboxMap.addPolyline(new PolylineOptions()
                .color(Color.parseColor("#3bb2d0"))
                .width(2));*/

        for (Trajectory trajectory: trajectories) {
            if (trajectory.getProviderName().equals(indoorLocation.getProvider())) {
                drawPoint(trajectory, indoorLocation);
            }
        }
        /*for (int i = 0; i < _lines.count; ++i) {
            NSLog(@"|%@| |%@|", provider.getName, _lines[i].polyline.title);
            if ([provider.getName isEqualToString:_lines[i].polyline.title]) {
                NSLog(@"addElement");
            [self addElement:CLLocationCoordinate2DMake(location.latitude, location.longitude) Index:i];
                break;
            }
        }*/

    }

    private void drawPoint(Trajectory trajectory, IndoorLocation indoorLocation) {
        Log.d("drawPoint", "addPoingue " + trajectory.getProviderName());
/*        trajectory.polyline.addPoint(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()));
        mapboxMap.updatePolyline(trajectory.polyline);*/

        if (indoorLocation.getLatitude() != 0 && indoorLocation.getLongitude() != 0) {
            if (trajectory.getLastCoordinate() != null) {
                if (indoorLocation.getLatitude() != trajectory.getLastCoordinate().getLatitude() &&
                        indoorLocation.getLongitude() != trajectory.getLastCoordinate().getLongitude()) {
                    ArrayList<LatLng> points = new ArrayList<>();

                    points.add(trajectory.getLastCoordinate());
                    points.add(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()));
                    mapboxMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .color(Color.parseColor(trajectory.getColor()))
                            .width(2));

                }

            }
            trajectory.setLastCoordinate(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()));



            if (trajectory.getPoint() == null) {
                Marker marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()))
                        .title(trajectory.getProviderName()));
                trajectory.setPoint(marker);

            } else {
                Marker marker = trajectory.getPoint();
                marker.setPosition(new LatLng(indoorLocation.getLatitude(), indoorLocation.getLongitude()));

            }
        }

    }
}
