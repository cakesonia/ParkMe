package ua.lviv.iot.myparkme;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;
    private static final int LOCATION_REQUEST = 500;
    private GoogleMap mMap;
    private LatLng locationLatLng;
    private Location location;
    //Marker's latlng
    private LatLng sisLatlng = new LatLng(49.84118611, 24.02551979);       //Січових Стрільців
    private LatLng valovaLatlng = new LatLng(49.83978843, 24.0327926);     // Валова
    private LatLng vernisLatlng = new LatLng(49.84382049, 24.02835622);    // Вернісаж
    private LatLng sshqLatlng = new LatLng(49.822717, 23.9853437);         // Софт, Садова

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView parkingName;
    private Button buildRouteButton;

    private List<Polyline> mPolylines = new ArrayList<>();
    private PolylineOptions polylineOptions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearestParkingButtonClick();
        photoButtonClick();

        final View contentInfo = findViewById(R.id.content_info);
        bottomSheetBehavior = BottomSheetBehavior.from(contentInfo);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(384);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        parkingName = findViewById(R.id.parking_name);

        buildRouteButton = findViewById(R.id.build_route_button);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMarkers();

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        askMyLocationPermissions();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, new LocationListener() {
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

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }

        cleanMap();
    }

    public void nearestParkingButtonClick() {
        Button nearestParking = findViewById(R.id.nearest_parking_button);
        nearestParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDistanceLess(sisLatlng, valovaLatlng, vernisLatlng, sshqLatlng)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sisLatlng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(valovaLatlng, sisLatlng, vernisLatlng, sshqLatlng)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(valovaLatlng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(vernisLatlng, valovaLatlng, sisLatlng, sshqLatlng)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(vernisLatlng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                } else if (isDistanceLess(sshqLatlng, valovaLatlng, sisLatlng, vernisLatlng)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sshqLatlng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                }
            }
        });
    }

    public void photoButtonClick() {
        Button photoButton = findViewById(R.id.free_spots);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapsPhoto = new Intent(MapsActivity.this, PhotoParkingActivity.class);
                startActivity(mapsPhoto);
            }
        });
    }

    private boolean isDistanceLess(LatLng distance, LatLng distance2, LatLng distance3, LatLng distance4) {
        return getDistance(distance) < getDistance(distance2) && getDistance(distance) < getDistance(distance3)
                && getDistance(distance) < getDistance(distance4);
    }

    public float getDistance(LatLng latLng) {
        Location dest = new Location(LocationManager.GPS_PROVIDER);
        dest.setLatitude(latLng.latitude);
        dest.setLongitude(latLng.longitude);
        assert location != null;
        return location.distanceTo(dest);
    }

    public void buildRouteButtonClick(final LatLng latLng) {
        buildRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getRequestUrl(latLng, locationLatLng);
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
            }
        });
    }

    private void askMyLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    public void drawMarkers() {
        Marker sisMarker = mMap.addMarker(new MarkerOptions()
                .position(sisLatlng)
                .title("Parking \"Sichovykh Strilʹtsiv Street\""));
        sisMarker.setTag(1);

        Marker valovaMarker = mMap.addMarker(new MarkerOptions()
                .position(valovaLatlng)
                .title("Parking \"Valova Street\""));
        valovaMarker.setTag(1);

        Marker vernisMarker = mMap.addMarker(new MarkerOptions()
                .position(vernisLatlng)
                .title("Parking zone \"Vernissage\""));
        vernisMarker.setTag(1);

        Marker sshqMarker = mMap.addMarker(new MarkerOptions()
                .position(sshqLatlng)
                .title("Parking \"SoftServe Lviv HQ\""));
        sshqMarker.setTag(1);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                String title = marker.getTitle();
                if ("Parking \"Sichovykh Strilʹtsiv Street\"".equals(title)) {
                    parkingName.setText(R.string.Sschovykh_strilʹtsiv_street);
                    buildRouteButtonClick(sisLatlng);
                } else if ("Parking \"Valova Street\"".equals(title)) {
                    parkingName.setText(R.string.valova_street);
                    buildRouteButtonClick(valovaLatlng);
                } else if ("Parking zone \"Vernissage\"".equals(title)) {
                    parkingName.setText(R.string.vernissage);
                    buildRouteButtonClick(vernisLatlng);
                } else if ("Parking \"SoftServe Lviv HQ\"".equals(title)) {
                    parkingName.setText(R.string.softserve_lviv_hq);
//                    distanceValue.setText(String.valueOf(getDistance(sshqLatlng)));
                    buildRouteButtonClick(sshqLatlng);
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
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode;
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

    public void cleanMap() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
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
                polylineOptions.color(Color.CYAN);
                polylineOptions.geodesic(true);
            }

            for (Polyline line : mPolylines) {
                line.remove();
            }
            mPolylines.clear();

            if (polylineOptions != null) {
                mPolylines.add(mMap.addPolyline(polylineOptions));
            } else {
                Toast.makeText(getApplicationContext(), "DirectionParser not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
