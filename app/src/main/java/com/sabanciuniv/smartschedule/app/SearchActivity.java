package com.sabanciuniv.smartschedule.app;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectDragListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.Session;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SearchActivity extends Activity implements Session.SearchListener, CameraListener {
    /**
     * Replace "your_api_key" with a valid developer key.
     * You can get it at the https://developer.tech.yandex.ru/ website.
     */
    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private EditText searchEdit;
    private SearchManager searchManager;
    private Session searchSession;
    PlacemarkMapObject mark; // initialized later on

    final Point[] selectedPoint = new Point[1];
    String addressLine;

    public SearchActivity() {
        mark = null;
    }

    private void submitQuery(String query) {
        searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                new SearchOptions(),
                this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        SearchFactory.initialize(this);

        setContentView(R.layout.search);
        super.onCreate(savedInstanceState);

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.getMap().addCameraListener(this);

        searchEdit = (EditText)findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    submitQuery(searchEdit.getText().toString());
                }

                return false;
            }
        });

        mapView.getMap().move(
                new CameraPosition(TARGET_LOCATION, 8.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null);


        submitQuery(searchEdit.getText().toString());
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onSearchResponse(Response response) {

        final Geocoder geocoder = new Geocoder(SearchActivity.this, Locale.getDefault());

        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
            Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(this, R.drawable.search_result));
            }
        }

        Point firstPoint = null;
        List<GeoObjectCollection.Item> firstResult = response.getCollection().getChildren();
        firstPoint = firstResult.get(0).getObj().getGeometry().get(0).getPoint();

        selectedPoint[0] = firstPoint; //by default selected point is the first query result
        try {
            List<Address> address = geocoder.getFromLocation(selectedPoint[0].getLatitude(), selectedPoint[0].getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            Toast.makeText(SearchActivity.this,"Sorry, can you select again?", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }

        mark = mapObjects.addPlacemark( firstPoint,
                ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));
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
                    Toast.makeText(SearchActivity.this,"Sorry, can you select again?", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSearchError(Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPositionChanged(
            Map map,
            CameraPosition cameraPosition,
            CameraUpdateSource cameraUpdateSource,
            boolean finished) {
        if (finished) {
            submitQuery(searchEdit.getText().toString());
        }
    }

    public void selectPoint(View view)
    {
        Intent intent = new Intent( SearchActivity.this, AddTask.class);
        intent.putExtra("Address",addressLine);
        intent.putExtra("Longitude", selectedPoint[0].getLongitude());
        intent.putExtra("Latitude", selectedPoint[0].getLatitude());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 1);
    }
}
