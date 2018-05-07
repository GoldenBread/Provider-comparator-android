package io.indoorlocation.comparator.demoapp;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.navisens.motiondnaapi.MotionDnaApplication;

import io.indoorlocation.basicsteplocationprovider.BasicStepIndoorLocationProvider;
import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;
import io.indoorlocation.gps.GPSIndoorLocationProvider;
import io.indoorlocation.manual.ManualIndoorLocationProvider;
import io.indoorlocation.navisens.NavisensIndoorLocationProvider;
import io.mapwize.mapwizeformapbox.MapOptions;
import io.mapwize.mapwizeformapbox.MapwizePlugin;
import io.mapwize.mapwizeformapbox.model.LatLngFloor;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private MapwizePlugin mapwizePlugin;

    private static final int REQUEST_MDNA_PERMISSIONS = 1;
    private Comparator comparatorIndoorLocation;
    private NavisensIndoorLocationProvider navisensIndoorLocationProvider;
    private IndoorLocationProvider manualIndoorLocationProvider;
    private GPSIndoorLocationProvider gpsIndoorLocationProvider;
    private BasicStepIndoorLocationProvider basicStepIndoorLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoibWFwd2l6ZSIsImEiOiJjamNhYnN6MjAwNW5pMnZvMnYzYTFpcWVxIn0.veTCqUipGXCw8NwM2ep1Xg");// PASTE YOU MAPBOX API KEY HERE !!! This is a demo key. It is not allowed to use it for production. The key might change at any time without notice. Get your key by signing up at mapbox.com
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);



        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, new MapOptions());

                comparatorIndoorLocation = new Comparator(mapboxMap);
                manualIndoorLocationProvider = new ManualIndoorLocationProvider();
                navisensIndoorLocationProvider = new NavisensIndoorLocationProvider(getApplicationContext(), manualIndoorLocationProvider,"uu6oF6dDdNsIBWBez4pw2GuMwNWGJlLpRjVjsa4c23XrT8wqT7BKnXS7WuWSyPfc");
                basicStepIndoorLocationProvider = new BasicStepIndoorLocationProvider(getSystemService(SENSOR_SERVICE));
                gpsIndoorLocationProvider = new GPSIndoorLocationProvider(getBaseContext());


                startLocationService();

                mapwizePlugin.setOnMapClickListener(new MapwizePlugin.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLngFloor latLngFloor) {

                        //Location location = new Location(navisensIndoorLocationProvider.getName());
                        Location location = new Location(manualIndoorLocationProvider.getName());
                        location.setLatitude(latLngFloor.getLatitude());
                        location.setLongitude(latLngFloor.getLongitude());
                        //IndoorLocation indoorLocation = new IndoorLocation(navisensIndoorLocationProvider.getName(), latLngFloor.getLatitude(), latLngFloor.getLongitude(), latLngFloor.getFloor(), System.currentTimeMillis());
                        IndoorLocation indoorLocation = new IndoorLocation(manualIndoorLocationProvider.getName(), latLngFloor.getLatitude(), latLngFloor.getLongitude(), latLngFloor.getFloor(), System.currentTimeMillis());
                        manualIndoorLocationProvider.dispatchIndoorLocationChange(indoorLocation);
                        IndoorLocation indoorLocation2 = new IndoorLocation(navisensIndoorLocationProvider.getName(), latLngFloor.getLatitude(), latLngFloor.getLongitude(), latLngFloor.getFloor(), System.currentTimeMillis());
                        navisensIndoorLocationProvider.dispatchIndoorLocationChange(indoorLocation2);
                        IndoorLocation indoorLocation3 = new IndoorLocation(basicStepIndoorLocationProvider.getName(), latLngFloor.getLatitude(), latLngFloor.getLongitude(), latLngFloor.getFloor(), System.currentTimeMillis());
                        basicStepIndoorLocationProvider.dispatchIndoorLocationChange(indoorLocation3);

                    }
                });
            }
        });
    }

    private void setupLocationProvider() {
        comparatorIndoorLocation.addIndoorLocationProvider(navisensIndoorLocationProvider);
        comparatorIndoorLocation.addIndoorLocationProvider(manualIndoorLocationProvider);
        comparatorIndoorLocation.addIndoorLocationProvider(gpsIndoorLocationProvider);
        comparatorIndoorLocation.addIndoorLocationProvider(basicStepIndoorLocationProvider);

        gpsIndoorLocationProvider.start();
        basicStepIndoorLocationProvider.start();
        navisensIndoorLocationProvider.start();
        comparatorIndoorLocation.start();
        //mapwizePlugin.setLocationProvider(navisensIndoorLocationProvider);
        //mapwizePlugin.setLocationProvider(manualIndoorLocationProvider);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (MotionDnaApplication.checkMotionDnaPermissions(this) == true) {
            Log.d("Activity", "Permissions");
            setupLocationProvider();
        }
    }

    private void startLocationService() {
        if  (
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, MotionDnaApplication.needsRequestingPermissions(), REQUEST_MDNA_PERMISSIONS);
        }
        else {
            setupLocationProvider();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
