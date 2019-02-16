
   package com.sabanciuniv.smartschedule.app;

   import android.content.Intent;
   import android.os.Bundle;
   import android.app.Activity;
   import android.support.annotation.NonNull;
   import android.util.Log;
   import android.view.View;

   import com.google.common.collect.MapMaker;
   import com.google.type.LatLng;
   import com.yandex.mapkit.Animation;
   import com.yandex.mapkit.MapKitFactory;
   import com.yandex.mapkit.geometry.Point;
   import com.yandex.mapkit.map.CameraPosition;
   import com.yandex.mapkit.map.MapObject;
   import com.yandex.mapkit.map.MapObjectCollection;
   import com.yandex.mapkit.map.MapObjectDragListener;
   import com.yandex.mapkit.map.PlacemarkMapObject;
   import com.yandex.mapkit.mapview.MapView;
   import com.yandex.runtime.image.ImageProvider;

   public class MapViewActivity extends Activity {

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private MapView mapView;
    private MapObjectCollection mapObjects;

    final Point[] selectedPoint = new Point[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        selectedPoint[0] = TARGET_LOCATION;
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        // Now MapView can be created.
        setContentView(R.layout.activity_map_view);
        super.onCreate(savedInstanceState);
        mapView = (MapView)findViewById(R.id.mapview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        // And to show what can be done with it, we move the camera to the center of Istanbul
        mapView.getMap().move(
                new CameraPosition(TARGET_LOCATION, 8.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null);

        final PlacemarkMapObject mark = mapObjects.addPlacemark(TARGET_LOCATION);
        mark.setIcon(ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));
        mark.setDraggable(true);
        mark.setDragListener(new MapObjectDragListener() {
            @Override
            public void onMapObjectDragStart(@NonNull MapObject mapObject) {

            }

            @Override
            public void onMapObjectDrag(@NonNull MapObject mapObject, @NonNull Point point) {

            }

            @Override
            public void onMapObjectDragEnd(@NonNull MapObject mapObject) {
                selectedPoint[0] = mark.getGeometry();
            }
        });
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

    public void selectPoint(View view)
    {
        Intent intent = new Intent( MapViewActivity.this, AddTask.class);
        Bundle b = new Bundle();
        b.putDouble("PointLatitude", selectedPoint[0].getLatitude());
        b.putDouble("PointLongitude", selectedPoint[0].getLongitude());
        intent.putExtras(b);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onStart() {
        // Activity onStart call must be passed to both MapView and MapKit instance.
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

}