package com.one.geoar.util;

import android.Manifest;

import com.wikitude.common.devicesupport.Feature;

import java.util.EnumSet;

public class PermissionUtil {

    private PermissionUtil(){}

    public static String[] getPermissionsForArFeatures(EnumSet<Feature> features) {
        return (features.contains(Feature.GEO)) ?
                new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION} :
                new String[]{Manifest.permission.CAMERA};
    }
}
