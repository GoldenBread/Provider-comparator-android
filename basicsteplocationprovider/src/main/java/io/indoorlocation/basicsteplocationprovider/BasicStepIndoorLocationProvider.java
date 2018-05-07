package io.indoorlocation.basicsteplocationprovider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

public class BasicStepIndoorLocationProvider extends IndoorLocationProvider implements SensorEventListener {

    private boolean isStarted = false;

    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mMagnetometer;


    private final double LOW_PASS_TIME_CONSTANT = 0.15;
    private final double STEP_ACCELERATION_DEVIATION_THRESHOLD = 0.1;
    private final double STEP_LENGTH = 1.0;

    double lowPassXAcceleration;
    double lowPassYAcceleration;
    double lowPassZAcceleration;

    List<Double> accelerationBuffer;
    IndoorLocation lastIndoorLocation;

    double heading;
    private float currentDegree = 0f;

    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    /*NSMutableArray<NSNumber*>* accelerationBuffer;
    ILIndoorLocation* lastIndoorLocation;
    double heading;
    NSTimer* timer;*/
            
    
    public BasicStepIndoorLocationProvider(Object systemService) {
        super();
        mSensorManager = (SensorManager) systemService;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerationBuffer = new ArrayList<>();
    }

    public void setIndoorLocation(IndoorLocation indoorLocation) {
        lastIndoorLocation = indoorLocation;
        dispatchIndoorLocationChange(indoorLocation);
    }

    @Override
    public boolean supportsFloor() {
        return false;
    }

    @Override
    public void start() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void stop() {
        mSensorManager.unregisterListener(this);

    }

    @Override
    public boolean isStarted() {
        return false;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("basicStep","EVEeNT " + event.sensor.getType() + " deg" + currentDegree);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            processingSingleAccelerationMeasurement(event);
        /*else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = Math.round(event.values[0]);
            Log.d("Heading: " ,Float.toString(degree) + " degrees");
            // create a rotation animation (reverse turn degree degrees)
            //RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            // how long the animation will take place
            //ra.setDuration(210);
            // set the animation after the end of the reservation status
            //ra.setFillAfter(true);
            // Start the animation
            //image.startAnimation(ra);
            currentDegree = -degree;
        }*/

//https://www.wlsdevelop.com/index.php/en/blog?option=com_content&view=article&id=38
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            currentDegree = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            currentDegree = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        currentDegree = Math.round(currentDegree);
    }

    private void processingSingleAccelerationMeasurement(SensorEvent event) {
        lowPassXAcceleration = lowPassXAcceleration * LOW_PASS_TIME_CONSTANT + event.values[0] * (1 - LOW_PASS_TIME_CONSTANT);
        lowPassYAcceleration = lowPassYAcceleration * LOW_PASS_TIME_CONSTANT + event.values[1] * (1 - LOW_PASS_TIME_CONSTANT);
        lowPassZAcceleration = lowPassZAcceleration * LOW_PASS_TIME_CONSTANT + event.values[2] * (1 - LOW_PASS_TIME_CONSTANT);

        double x2 = lowPassXAcceleration * lowPassXAcceleration;
        double y2 = lowPassYAcceleration * lowPassYAcceleration;
        double z2 = lowPassZAcceleration * lowPassZAcceleration;
        double lowPassAccelerationNorm = sqrt(x2 + y2 + z2);
        Log.d("basicStep", "lowPassAccelerationNorm> " + lowPassAccelerationNorm);

        if (accelerationBuffer.size() < 10)
            accelerationBuffer.add(lowPassAccelerationNorm);
        else
            processAccelerationOverOneSecond();
    }

    private void processAccelerationOverOneSecond() {
        double accelerateSum = 0.0;
        for (Double x: accelerationBuffer) {
            accelerateSum += x.doubleValue();
        }
        double accelerationMean = accelerateSum / accelerationBuffer.size();
        double accelerationSquaredDeviation = 0.0;
        for (Double x: accelerationBuffer) {
            accelerationSquaredDeviation += (x - accelerationMean) * (x - accelerationMean);
        }
        accelerationSquaredDeviation = accelerationSquaredDeviation / accelerationBuffer.size();

        accelerationBuffer.clear();

        if (accelerationSquaredDeviation > STEP_ACCELERATION_DEVIATION_THRESHOLD) {
            makeAStep();
        }
    }

    private void makeAStep() {
        if (lastIndoorLocation != null) {
            lastIndoorLocation = locationWithBearing(currentDegree, STEP_LENGTH, lastIndoorLocation);
            dispatchIndoorLocationChange(lastIndoorLocation);
        }
    }

    private IndoorLocation locationWithBearing(double bearing, double distance, IndoorLocation origin) {
        double bearingRad = bearing * Math.PI / 180.0;
        double distRadians = distance / 6372797.6;
        double lat1 = origin.getLatitude() * Math.PI / 180.0;
        double lon1 = origin.getLongitude() * Math.PI / 180.0;

        double lat2 = Math.asin((Math.sin(lat1) * Math.cos(distRadians)) + (Math.cos(lat1) * Math.sin(distRadians) * Math.cos(bearingRad)));
        double lon2 = lon1 + atan2(Math.sin(bearingRad) * Math.sin(distRadians) * Math.cos(lat1), Math.cos(distRadians) - Math.sin(lat1) * Math.sin(lat2));

        double targetLat = lat2 * 180.0 / Math.PI;
        double targetLon = lon2 * 180.0 / Math.PI;

        IndoorLocation indoorLocation = new IndoorLocation(getName(), targetLat, targetLon, origin.getFloor(), System.currentTimeMillis());

        return indoorLocation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("basicStep","onAccuracyChanged");

    }
}
