package com.example.android.parkme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Maps extends FragmentActivity implements OnMapReadyCallback {
    private static final int LOCATION_REQUEST = 500;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;
    private GoogleMap mMap;
//    private TextView hintForUsers;
    private LatLng locationLatLng;
    private LocationManager locationManager;

    private TextView parkingName;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    //    private LatLng lviv = new LatLng(49.8411222, 24.02800039);
    private LatLng parking1 = new LatLng(49.84118611, 24.02551979);
    private LatLng parking2 = new LatLng(49.83978843, 24.0327926);
    private LatLng parking3 = new LatLng(49.84382049, 24.02835622);
    private LatLng parking4 = new LatLng(49.822717, 23.9853437);

    private Marker park1;
    private Marker park2;
    private Marker park3;
    private Marker park4;

    private List<Polyline> mPolylines = new ArrayList<>();

    /*public void nearestParkingButtonClick() {
        theNearestParking = findViewById(R.id.theNearestParking);
        theNearestParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapsMaps2 = new Intent(Maps.this, Maps2.class);
                startActivity(mapsMaps2);
            }
        });
    }*/

    public void nearestParkingButtonClick() {
        Button nearestParking = findViewById(R.id.nearestParking);
        nearestParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDistanceLess(parking1, parking2, parking3, parking4)){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(parking1));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(parking2, parking1, parking3, parking4)){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(parking2));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(parking3, parking2, parking1, parking4)){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(parking3));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(parking4, parking2, parking1, parking3)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(parking4));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                }
            }
        });
    }

    private boolean isDistanceLess(LatLng distance, LatLng distance2, LatLng distance3, LatLng distance4) {
        return getDistance(distance) < getDistance(distance2) && getDistance(distance) < getDistance(distance3)
                && getDistance(distance) < getDistance(distance4);
    }

    public float getDistance(LatLng latLng) {
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location dest = new Location(LocationManager.GPS_PROVIDER);
        dest.setLatitude(latLng.latitude);
        dest.setLongitude(latLng.longitude);
        return location.distanceTo(dest);
    }

    public void menuButtonClick() {
        FloatingActionButton info = findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapsAboutUs = new Intent(Maps.this, AboutUs.class);
                startActivity(mapsAboutUs);
            }
        });
    }

    /*public void choosingButtonClick(){
        choosing = findViewById(R.id.choosingParking);
        choosing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapsChoosing = new Intent(Maps.this, ChoosingActivity.class);
                startActivity(mapsChoosing);
            }
        });
    }*/

    private void videoButtonClick() {
        Button videoButton = findViewById(R.id.video);

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapsVideo = new Intent("https://parkme-main.firebaseapp.com/translation.html");
                startActivity(mapsVideo);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        choosingButtonClick();
        menuButtonClick();
        nearestParkingButtonClick();
        videoButtonClick();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        parkingName = findViewById(R.id.parkingName);
        final View contentInfo = findViewById(R.id.contentInfo);
        bottomSheetBehavior = BottomSheetBehavior.from(contentInfo);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(384);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        drawMarkers();

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        askMyLocationPermissions();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void askMyLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            com.example.android.parkme.PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    public void drawMarkers() {

        // Add a marker in parkings and move the camera
        park1 = mMap.addMarker(new MarkerOptions()
                .position(parking1)
                .title("Parking \"Sichovykh Strilʹtsiv Street\""));
        park1.setTag(1);
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(parking1));

        park2 = mMap.addMarker(new MarkerOptions()
                .position(parking2)
                .title("Parking \"Valova Street\""));
        park2.setTag(1);
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(parking2));

        park3 = mMap.addMarker(new MarkerOptions()
                .position(parking3)
                .title("Parking zone \"Vernissage\""));
        park3.setTag(1);
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(parking3));

        park4 = mMap.addMarker(new MarkerOptions()
                .position(parking4)
                .title("Parking \"SoftServe Lviv HQ\""));
        park4.setTag(1);
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sadova));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                DirectionsParser directionsParser = new DirectionsParser();
                JSONObject jsonObject = null;

                parkingName = findViewById(R.id.parkingName);
                String title = marker.getTitle();
                if ("Parking \"Sichovykh Strilʹtsiv Street\"".equals(title)) {
                    createMarker(park1, parking1);
                    parkingName.setText(R.string.Sschovykh_strilʹtsiv_street);
                } else if ("Parking \"Valova Street\"".equals(title)) {
                    createMarker(park2, parking2);
                    parkingName.setText(R.string.valova_street);
                } else if ("Parking zone \"Vernissage\"".equals(title)) {
                    createMarker(park3, parking3);
                    parkingName.setText(R.string.vernissage);
                } else if ("Parking \"SoftServe Lviv HQ\"".equals(title)) {
                    createMarker(park4, parking4);
                    parkingName.setText(R.string.softserve_lviv_hq);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }, 300);
                return true;
            }
        });
        /*// Retrieve the data from the marker.
        Integer clickCount = (Integer) park1.getTag();
        clickCount += (Integer) park2.getTag();
        clickCount += (Integer) park3.getTag();
        clickCount += (Integer) park4.getTag();
        // Check if a click count was set, then display the click count.
        if (clickCount == 4) {
            hintForUsers = findViewById(R.id.infoForUsers);
            hintForUsers.setText("For choosing parking click on markers");
        } else if (clickCount > 4){
            hintForUsers.setText("Good");
        }*/
    }

    public void createMarker(Marker marker, LatLng latLng) {
        removeMarkers();
        drawMarkers();
        marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(latLng));
        String url = getRequestUrl(latLng, locationLatLng);
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);
    }

    public void removeMarkers() {
        park1.remove();
        park2.remove();
        park3.remove();
        park4.remove();
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode;
        //Output format
        String output = "json";
        //Create url to request
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            responseString = stringBuilder.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList<LatLng> points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList<>();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            for (Polyline line : mPolylines) {
                line.remove();
            }
            mPolylines.clear();

            if (polylineOptions != null) {
                mPolylines.add(mMap.addPolyline(polylineOptions));
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
