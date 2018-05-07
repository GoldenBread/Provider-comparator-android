package io.indoorlocation.navisens;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.navisens.motiondnaapi.MotionDna;
import com.navisens.motiondnaapi.MotionDnaApplication;
import com.navisens.motiondnaapi.MotionDnaInterface;

import java.util.Map;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;
import io.indoorlocation.core.IndoorLocationProviderListener;

public class NavisensIndoorLocationProvider extends IndoorLocationProvider implements MotionDnaInterface, IndoorLocationProviderListener {

    private boolean isStarted = false;

    private MotionDnaApplication motionDna;// = new MotionDnaApplication(this, false);

    private Context mContext;
    private Double currentFloor = null;

    private String key;

    private IndoorLocationProvider mSourceProvider;

    public NavisensIndoorLocationProvider(Context context, IndoorLocationProvider sourceProvider, String navisensDevKey) {
        super();

        mContext = context;

        motionDna = new MotionDnaApplication(this);

        mSourceProvider = sourceProvider;

        key = navisensDevKey;

        sourceProvider.addListener(this);
        //mSourceProvider.
    }

/*    public void setIndoorLocation(IndoorLocation indoorLocation) {
        dispatchIndoorLocationChange(indoorLocation);

        currentFloor = indoorLocation.getFloor();

        motionDna.setLocationLatitudeLongitude(indoorLocation.getLatitude(), indoorLocation.getLongitude());
        motionDna.setHeadingMagInDegrees();
    }*/

    @Override
    public boolean supportsFloor() {
        return true;
    }

    @Override
    public void start() {
        this.isStarted = true;

        motionDna.runMotionDna(key);

        motionDna.setCallbackUpdateRateInMs(1000);
        motionDna.setExternalPositioningState(MotionDna.ExternalPositioningState.HIGH_ACCURACY);
        motionDna.setPowerMode(MotionDna.PowerConsumptionMode.PERFORMANCE);

        motionDna.startUDP();
    }

    @Override
    public void stop() {
        this.isStarted = false;
    }

    @Override
    public boolean isStarted() {
        return this.isStarted;
    }

    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        Log.d("provider", "receiveMotionDna");

        MotionDna.Location location = motionDna.getLocation();

        IndoorLocation indoorLocation = new IndoorLocation(getName(), location.globalLocation.latitude, location.globalLocation.longitude,currentFloor, System.currentTimeMillis());

        dispatchIndoorLocationChange(indoorLocation);

    }


    @Override
    public void receiveNetworkData(MotionDna motionDna) {
        Log.d("Provider", "receiveNetworkData");
    }

    @Override
    public void receiveNetworkData(MotionDna.NetworkCode networkCode, Map<String, ?> map) {
        Log.d("Provider", "receiveNetworkData++");
    }

    @Override
    public void reportError(MotionDna.ErrorCode errorCode, String s) {
        Log.d("Provider", "reportError " + errorCode.toString());

    }

    @Override
    public Context getAppContext() {
        return mContext.getApplicationContext();
    }

    @Override
    public PackageManager getPkgManager() {
        return mContext.getPackageManager();
    }

    @Override
    public void onProviderStarted() {
        Log.d("Provider", "onProviderStarted");
    }

    @Override
    public void onProviderStopped() {
        Log.d("Provider", "onProviderStopped");
    }

    @Override
    public void onProviderError(Error error) {
        Log.d("Provider", "onProviderError");

        dispatchOnProviderError(error);
    }

    @Override
    public void onIndoorLocationChange(IndoorLocation indoorLocation) {
        Log.d("Provider", "onIndoorLocationChange");

        dispatchIndoorLocationChange(indoorLocation);

        currentFloor = indoorLocation.getFloor();

        motionDna.setLocationLatitudeLongitude(indoorLocation.getLatitude(), indoorLocation.getLongitude());
        motionDna.setHeadingMagInDegrees();//
    }
}
