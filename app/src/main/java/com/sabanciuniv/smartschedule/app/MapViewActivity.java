
   package com.sabanciuniv.smartschedule.app;

   import android.content.Intent;
   import android.location.Address;
   import android.location.Geocoder;
   import android.os.Bundle;
   import android.app.Activity;
   import android.support.annotation.NonNull;
   import android.view.View;
   import android.widget.Toast;

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

   import java.io.IOException;
   import java.util.List;
   import java.util.Locale;

   public class MapViewActivity extends Activity {
       private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
       //TODO: GET THE CURRENT LOCATION AND MAKE THE TARGET THAT LOCATION (LATER) //keep the comment don't delete
       private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);
       private MapView mapView;

       private MapObjectCollection mapObjects;

       final Point[] selectedPoint = new Point[1];
       String addressLine;

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           final Geocoder geocoder = new Geocoder(MapViewActivity.this, Locale.getDefault());

           selectedPoint[0] = TARGET_LOCATION;
           List<Address> address = null;
           try {
               address = geocoder.getFromLocation(selectedPoint[0].getLatitude(), selectedPoint[0].getLongitude(), 1);
               addressLine = address.get(0).getAddressLine(0);
           } catch (IOException e) {
               e.printStackTrace();
           }

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
                   try {
                       List<Address> address = geocoder.getFromLocation(selectedPoint[0].getLatitude(), selectedPoint[0].getLongitude(), 1);
                       addressLine = address.get(0).getAddressLine(0);
                   } catch (IOException e) {
                       Toast.makeText(MapViewActivity.this,"Sorry, can you select again?", Toast.LENGTH_SHORT);
                       e.printStackTrace();
                   }
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
           Intent current = getIntent();
           Intent intent = new Intent(MapViewActivity.this, SearchActivity.class);
           intent.putExtra("caller", current.getStringExtra("caller"));
           startActivity(intent);
       }

       public void selectPoint(View view)
       {
           Intent current = getIntent();
           Intent intent = null;
           if (current.getStringExtra("caller").equals("Profile.class"))
               intent = new Intent( MapViewActivity.this, Profile.class);
           else if (current.getStringExtra("caller").equals("AddTask.class"))
               intent = new Intent( MapViewActivity.this, AddTask.class);
           else     //let's hope we never enter this else here but need to be safe xx
           {
               //todo: if you need any more callers you may add them in else-if's
               Toast.makeText(this,"Cannot access the parent class, please restart app",Toast.LENGTH_SHORT);
               finish();
           }

           intent.putExtra("Address",addressLine);
           intent.putExtra("Longitude", selectedPoint[0].getLongitude());
           intent.putExtra("Latitude", selectedPoint[0].getLatitude());
           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
           startActivityForResult(intent, 1);
       }

       @Override
       protected void onStart() {
            // Activity onStart call must be passed to both MapView and MapKit instance.
           super.onStart();
           MapKitFactory.getInstance().onStart();
           mapView.onStart();
       }
   }