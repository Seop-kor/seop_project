package com.one.geoar;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.one.geoar.util.location.LocationProvider;
import com.wikitude.architect.ArchitectView;

public class SimpleGeoArActivity extends SimpleArActivity implements LocationListener {
    private LocationProvider locationProvider;

    private LocationProvider.ErrorCallback errorCallback = new LocationProvider.ErrorCallback() {
        @Override
        public void noProvidersEnabled() {
            Toast.makeText(SimpleGeoArActivity.this, R.string.no_location_provider,Toast.LENGTH_LONG).show();
        }
    };


    private ArchitectView.SensorAccuracyChangeListener sensorAccuracyChangeListener = new ArchitectView.SensorAccuracyChangeListener() {
        @Override
        public void onCompassAccuracyChanged(int i) {
            if(i < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM){
                Toast.makeText(SimpleGeoArActivity.this, R.string.compass_accuracy_low,Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = new LocationProvider(this,this,errorCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationProvider.onResume();
        architectView.registerSensorAccuracyChangeListener(sensorAccuracyChangeListener);
    }

    @Override
    protected void onPause() {
        locationProvider.onPause();
        super.onPause();
        architectView.unregisterSensorAccuracyChangeListener(sensorAccuracyChangeListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        float accuracy = location.hasAccuracy() ? location.getAccuracy() : 500;
        if(location.hasAltitude()){
            architectView.setLocation(location.getLatitude(),location.getLongitude(),location.getAltitude(),accuracy);
        }else{
            architectView.setLocation(location.getLatitude(),location.getLongitude(),accuracy);
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
