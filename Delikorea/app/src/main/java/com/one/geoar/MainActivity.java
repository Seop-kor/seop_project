package com.one.geoar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.one.geoar.util.PermissionUtil;
import com.one.geoar.util.SampleData;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.CallStatus;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.devicesupport.Feature;
import com.wikitude.common.permission.PermissionManager;

import java.util.Arrays;
import java.util.EnumSet;

public class MainActivity extends AppCompatActivity {

    private final PermissionManager permissionManager = ArchitectView.getPermissionManager();
    SampleData sampleData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EnumSet<Feature> features = EnumSet.noneOf(Feature.class);
        features.add(Feature.GEO);
        sampleData = new SampleData.Builder("Reloading Content","10_BrowsingPois_4_ReloadingContent/index.html")
                .activityClass(SimpleGeoArActivity.class)
                .extensions(null)
                .arFeatures(features)
                .cameraPosition(CameraSettings.CameraPosition.BACK)
                .cameraResolution(CameraSettings.CameraResolution.AUTO)
                .camera2Enabled(false)
                .build();

        CallStatus status = ArchitectView.isDeviceSupporting(this, sampleData.getArFeatures());
        if(status.isSuccess()){
            sampleData.isDeviceSupporting(true,"");
        }else{
            sampleData.isDeviceSupporting(false,status.getError().getMessage());
        }

        final String[] permissions = PermissionUtil.getPermissionsForArFeatures(sampleData.getArFeatures());

        if(!sampleData.getIsDeviceSupporting()){
            showDeviceMissingFeatures(sampleData.getIsDeviceSupportingError());
        }else{
            permissionManager.checkPermissions(MainActivity.this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
                @Override
                public void permissionsGranted(int i) {
                    final Intent it = new Intent(MainActivity.this, sampleData.getActivityClass());
                    it.putExtra("sampleData",sampleData);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(it);
                            finish();
                        }
                    },1000);
                }

                @Override
                public void permissionsDenied(@NonNull String[] strings) {
                    Toast.makeText(MainActivity.this,getString(R.string.permissions_denied)+ Arrays.toString(strings),Toast.LENGTH_LONG).show();
                }

                @Override
                public void showPermissionRationale(int i, @NonNull String[] strings) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(R.string.permission_rationale_title);
                    alertBuilder.setMessage(getString(R.string.permission_rationale_text)+Arrays.toString(permissions));
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            permissionManager.positiveRationaleResult(i,permissions);
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            });
        }
    }

    public void showDeviceMissingFeatures(String errorMessage) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.device_missing_features)
                .setMessage(errorMessage)
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArchitectView.getPermissionManager().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
