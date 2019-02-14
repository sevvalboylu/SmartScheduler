
   package com.sabanciuniv.smartschedule.app;

   import android.content.Intent;
   import android.os.Bundle;
   import android.app.Activity;
   import android.view.View;

   import com.yandex.mapkit.Animation;
   import com.yandex.mapkit.MapKitFactory;
   import com.yandex.mapkit.geometry.Point;
   import com.yandex.mapkit.map.CameraPosition;
   import com.yandex.mapkit.mapview.MapView;

public class MapViewActivity extends Activity {

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        // Now MapView can be created.
        setContentView(R.layout.activity_map_view);
        super.onCreate(savedInstanceState);
        mapView = (MapView)findViewById(R.id.mapview);

        // And to show what can be done with it, we move the camera to the center of Istanbul
        mapView.getMap().move(
                new CameraPosition(TARGET_LOCATION, 8.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null);
    }

    @Override
    protected void onStop() {
        // Activity onStop call must be passed to both MapView and MapKit instance.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
    public void goToSearch(View view)
    {
        Intent intent = new Intent(MapViewActivity.this, SearchActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onStart() {
        // Activity onStart call must be passed to both MapView and MapKit instance.
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
}