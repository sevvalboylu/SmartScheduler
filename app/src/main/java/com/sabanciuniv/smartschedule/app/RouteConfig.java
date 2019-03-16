package com.sabanciuniv.smartschedule.app;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.List;
public interface RouteConfig {

    public interface RouteConfigExample {
        @NonNull
        LatLng getOrigin();

        @NonNull
        LatLng getDestination();

        @StringRes
        int getDestinationAddress();

        @StringRes
        int getRouteDescription();

        @Nullable
        List<LatLng> getWaypoints();
    }

}
