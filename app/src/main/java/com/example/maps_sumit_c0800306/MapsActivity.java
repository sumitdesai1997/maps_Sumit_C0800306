package com.example.maps_sumit_c0800306;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps_sumit_c0800306.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = "MapsActivity";
    public static final int REQUEST_CODE = 1;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLatLng;

    Marker currentMarker;

    Polygon polygon;
    private static final int POLYGON_SIDES = 4;
    Marker base = null;
    // MarkerOptions baseMarker = new MarkerOptions().position(new LatLng(43.6532, -79.3832)).title("Your base").icon(BitmapDescriptorFactory.fromResource(R.drawable.messiresize));

    ArrayList<Marker> markerList = new ArrayList<Marker>(){{
        add(base);
        add(base);
        add(base);
        add(base);

    }};
    ArrayList<String> cityList = new ArrayList<String>(){{
        add("");
        add("");
        add("");
        add("");
    }};
    ArrayList<Integer> cityFillList = new ArrayList<Integer>() {{
        add(0);
        add(0);
        add(0);
        add(0);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                setCurrentLocationMarker(location);
            }
        };

        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startUpdateLocation();
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                int indexMarker = 0;
                for(Marker marker: markerList){
                    if(marker != null){
                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(marker.getPosition().latitude);
                        markerLocation.setLongitude(marker.getPosition().longitude);

                        Location newMarkerLocation = new Location("");
                        newMarkerLocation.setLatitude(latLng.latitude);
                        newMarkerLocation.setLongitude(latLng.longitude);

                        double distanceInKM =  markerLocation.distanceTo(newMarkerLocation)/1000;
                        Log.d(TAG, "for index: "+ indexMarker + ", distanceInKM: " + distanceInKM);
                        if(distanceInKM < 5.0){
                            marker.remove();

                            if(cityFillList.get(0) != 0 && cityFillList.get(1) != 0 && cityFillList.get(2) != 0 && cityFillList.get(3) != 0){
                                polygon.remove();
                                polygon = null;
                            }

                            cityFillList.set(indexMarker, 0);
                            cityList.set(indexMarker,"");
                            markerList.set(indexMarker,null);

                            Log.d(TAG, " after remove cityFillList: "+ cityFillList);
                            Log.d(TAG, " after remove cityList: "+ cityList);
                            Log.d(TAG, " after remove markerList: "+ markerList);

                            return;
                        }
                        indexMarker += 1;
                    }
                }

                if(cityFillList.get(0) != 0 && cityFillList.get(1) != 0 && cityFillList.get(2) != 0 && cityFillList.get(3) != 0){
                    for(int i=0; i<markerList.size();i++){
                        if(markerList.get(i) != null) {
                            markerList.get(i).remove();
                            markerList.set(i,null);
                        }
                    }
                    markerList.clear();
                    markerList.add(base);
                    markerList.add(base);
                    markerList.add(base);
                    markerList.add(base);

                    polygon.remove();
                    polygon = null;

                    cityList.clear();
                    cityList.add("");
                    cityList.add("");
                    cityList.add("");
                    cityList.add("");

                    cityFillList.clear();
                    cityFillList.add(0);
                    cityFillList.add(0);
                    cityFillList.add(0);
                    cityFillList.add(0);
                }

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String newCity = "";
                try{
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if(addressList != null  && addressList.size() > 0){
                        if (addressList.get(0).getLocality() != null)
                            newCity = addressList.get(0).getLocality();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "newCity: "+ newCity);
                if(newCity.equals("")){
                    return;
                }

                Boolean cityExist = false;
                for(String city: cityList){
                    if(city.equalsIgnoreCase(newCity)){
                        cityExist = true;
                        Toast.makeText(getBaseContext(),"City "+ newCity+" already exist!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "City "+ newCity+" already exist!");
                        return;
                    }
                }

                Log.d(TAG, "cityExist: "+ cityExist);
                if(!cityExist && !newCity.equals("")){
                    if(cityList.get(0).equals("")){
                        cityList.set(0,newCity);

                    } else if(cityList.get(1).equals("")){
                        cityList.set(1,newCity);

                    }
                    else if(cityList.get(2).equals("")){
                        cityList.set(2,newCity);

                    }
                    else if(cityList.get(3).equals("")){
                        cityList.set(3,newCity);

                    }
                }

                Log.d(TAG, "cityList: "+ cityList);

                if (polygon != null) {
                    polygon.remove();
                    polygon = null;
                }

                adjustPolygonWithRespectTo(latLng);

                PolygonOptions polygonOptions = new PolygonOptions()
                        .strokeColor(Color.RED)
                        .strokeWidth(5f)
                        .fillColor(0x5900AA00);
                for (int i = 0; i < markerList.size(); i++) {
                    if(markerList.get(i) != null) {
                        polygonOptions.add(markerList.get(i).getPosition());
                    }
                }
                if(cityFillList.get(0) != 0 && cityFillList.get(1) != 0 && cityFillList.get(2) != 0 && cityFillList.get(3) != 0) {
                    polygon = mMap.addPolygon(polygonOptions);
                    polygon.setClickable(true);
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull @NotNull Marker marker) {

                String address = "Could not find the address";
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                try{
                    List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    if(addressList != null  && addressList.size() > 0){
                        address = "\n";

                        // street name
                        if (addressList.get(0).getThoroughfare() != null)
                            address += addressList.get(0).getThoroughfare() + ",";
                        if (addressList.get(0).getLocality() != null)
                            address += addressList.get(0).getLocality() + " ";
                        if (addressList.get(0).getPostalCode() != null)
                            address += addressList.get(0).getPostalCode() + " ";
                        if (addressList.get(0).getAdminArea() != null)
                            address += addressList.get(0).getAdminArea();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),address, Toast.LENGTH_LONG).show();
                Log.d(TAG, "onMarkerClick: "+ address);
                return false;
            }
        });
        
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull @NotNull Marker marker) {
                Log.d(TAG, "onMarkerDragStart: marker: " + marker);
                String title = marker.getTitle();
                if(title.equals("A")){
                    markerList.set(0, null);
                    cityList.set(0, "");
                    cityFillList.set(0, 0);
                } else if(title.equals("B")){
                    markerList.set(1, null);
                    cityList.set(1, "");
                    cityFillList.set(1, 0);
                } else if(title.equals("C")){
                    markerList.set(2, null);
                    cityList.set(2, "");
                    cityFillList.set(2, 0);
                } else if(title.equals("D")){
                    markerList.set(3, null);
                    cityList.set(3, "");
                    cityFillList.set(3, 0);
                }

                if(polygon != null){
                    polygon.remove();
                    polygon = null;
                }

                Log.d(TAG, " onMarkerDragStart cityFillList: "+ cityFillList);
                Log.d(TAG, " onMarkerDragStart cityList: "+ cityList);
                Log.d(TAG, " onMarkerDragStart markerList: "+ markerList);
            }

            @Override
            public void onMarkerDrag(@NonNull @NotNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull @NotNull Marker marker) {
                Log.d(TAG, "onMarkerDragEnd: marker: "+ marker);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String draggedCity = "";
                try{
                    List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    if(addressList != null  && addressList.size() > 0){
                        if (addressList.get(0).getLocality() != null)
                            draggedCity = addressList.get(0).getLocality();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(markerList.get(0) == null){
                    markerList.set(0,marker);
                    cityList.set(0,draggedCity);
                    cityFillList.set(0,1);
                } else if(markerList.get(1) == null){
                    markerList.set(1,marker);
                    cityList.set(1,draggedCity);
                    cityFillList.set(1,1);
                } else if(markerList.get(2) == null){
                    markerList.set(2,marker);
                    cityList.set(2,draggedCity);
                    cityFillList.set(2,1);
                } else if(markerList.get(3) == null){
                    markerList.set(3,marker);
                    cityList.set(3,draggedCity);
                    cityFillList.set(3,1);
                }

                Location userLocation = new Location("");
                userLocation.setLatitude(userLatLng.latitude);
                userLocation.setLongitude(userLatLng.longitude);

                Location markerLocation = new Location("");
                markerLocation.setLatitude(marker.getPosition().latitude);
                markerLocation.setLongitude(marker.getPosition().longitude);

                double distanceInKM =  userLocation.distanceTo(markerLocation)/1000;

                marker.setSnippet(String.format("%.2f",distanceInKM)+ "Km");

                PolygonOptions polygonOptions = new PolygonOptions()
                        .strokeColor(Color.RED)
                        .strokeWidth(5f)
                        .fillColor(0x5900AA00);
                Log.d(TAG, " onMarkerDragEnd markerList.size(): "+ markerList.size());
                for (int i = 0; i < markerList.size(); i++) {
                    if(markerList.get(i) != null) {
                        polygonOptions.add(markerList.get(i).getPosition());
                    }
                }
                if(cityFillList.get(0) != 0 && cityFillList.get(1) != 0 && cityFillList.get(2) != 0 && cityFillList.get(3) != 0) {
                    polygon = mMap.addPolygon(polygonOptions);
                    polygon.setClickable(true);
                }

                Log.d(TAG, " onMarkerDragEnd cityFillList: "+ cityFillList);
                Log.d(TAG, " onMarkerDragEnd cityList: "+ cityList);
                Log.d(TAG, " onMarkerDragEnd markerList: "+ markerList);
            }
        });
        
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull @NotNull Polygon polygon) {

                double AB = calculateDistance(markerList.get(0), markerList.get(1));
                double BC = calculateDistance(markerList.get(1), markerList.get(2));
                double CD = calculateDistance(markerList.get(2), markerList.get(3));
                double DE = calculateDistance(markerList.get(3), markerList.get(0));

                double totalDistance = AB + BC + CD +DE;

                Toast.makeText(getBaseContext(),"Total distance is: "+ String.format("%.2f", totalDistance) + "Km", Toast.LENGTH_LONG).show();

                Log.d(TAG, "onPolygonClick: totalDistance: "+ String.format("%.2f", totalDistance) + "Km");
            }
        });
    }

    public double calculateDistance(Marker marker1, Marker marker2){
        Location location1 = new Location("");
        location1.setLatitude(marker1.getPosition().latitude);
        location1.setLongitude(marker1.getPosition().longitude);

        Location location2 = new Location("");
        location2.setLatitude(marker2.getPosition().latitude);
        location2.setLongitude(marker2.getPosition().longitude);

        return location1.distanceTo(location2)/1000;
    }

    ArrayList<Double> distancesFromMidPointsOfPolygonEdges = new ArrayList<>();
    private void adjustPolygonWithRespectTo(LatLng point) {
        double minDistance = 0;

        if (markerList.get(0) != null && markerList.get(1) != null && markerList.get(2) != null) {
            distancesFromMidPointsOfPolygonEdges.clear();

            for (int i = 0; i < markerList.size(); i++) {
                if (markerList.get(i) != null && markerList.get(i+1) != null) {
                    // 1. Find the mid points of the edges of polygon
                    ArrayList<LatLng> list = new ArrayList<>();

                    if (i == (markerList.size() - 1)) {
                        list.add(markerList.get(markerList.size() - 1).getPosition());
                        list.add(markerList.get(0).getPosition());
                    } else {
                        list.add((markerList.get(i).getPosition()));
                        list.add((markerList.get(i + 1).getPosition()));
                    }

                    LatLng midPoint = computeCentroid(list);

                    // 2. Calculate the nearest coordinate by finding distance between mid point of each edge and the coordinate to be drawn
                    Location startPoint = new Location("");
                    startPoint.setLatitude(point.latitude);
                    startPoint.setLongitude(point.longitude);

                    Location endPoint = new Location("");
                    endPoint.setLatitude(midPoint.latitude);
                    endPoint.setLongitude(midPoint.longitude);

                    double distance = startPoint.distanceTo(endPoint);

                    distancesFromMidPointsOfPolygonEdges.add(distance);

                    if (i == 0) {
                        minDistance = distance;
                    } else {

                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                    }
                }
            }

            // 3. The nearest coordinate = the edge with minimum distance from mid point to the coordinate to be drawn
            int position = minIndex(distancesFromMidPointsOfPolygonEdges);

            // 4. move the nearest coordinate at the end by shifting array right
            int shiftByNumber = (markerList.size() - position - 1);

            if (shiftByNumber != markerList.size()) {
                markerList = rotate(markerList, shiftByNumber);
            }
        }

        // 5. Now add coordinated to be drawn
        Location userLocation = new Location("");
        userLocation.setLatitude(userLatLng.latitude);
        userLocation.setLongitude(userLatLng.longitude);

        Location markerLocation = new Location("");
        markerLocation.setLatitude(point.latitude);
        markerLocation.setLongitude(point.longitude);

        double distanceInKM =  userLocation.distanceTo(markerLocation)/1000;

        MarkerOptions newMarker = new MarkerOptions().position(point).draggable(true).snippet(String.format("%.2f",distanceInKM)+ "Km");

        if(cityFillList.get(0) == 0){
            newMarker.title("A");
            cityFillList.set(0,1);
        } else if(cityFillList.get(1) == 0){
            newMarker.title("B");
            cityFillList.set(1,1);
        } else if(cityFillList.get(2) == 0){
            newMarker.title("C");
            cityFillList.set(2,1);
        } else if(cityFillList.get(3) == 0){
            newMarker.title("D");
            cityFillList.set(3,1);
        }

        if(markerList.get(0) == null){
            markerList.set(0,mMap.addMarker(newMarker));
            cityFillList.set(0,1);
        } else if(markerList.get(1) == null){
            markerList.set(1,mMap.addMarker(newMarker));
            cityFillList.set(1,1);
        } else if(markerList.get(2) == null){
            markerList.set(2,mMap.addMarker(newMarker));
            cityFillList.set(2,1);
        } else if(markerList.get(3) == null){
            markerList.set(3,mMap.addMarker(newMarker));
            cityFillList.set(3,1);
        }
        Log.d(TAG, "cityFillList: "+ cityFillList);
        Log.d(TAG, "markerList: "+ markerList);
    }

    public static int minIndex(ArrayList<Double> list) {
        return list.indexOf(Collections.min(list));
    }

    public static <T> ArrayList<T> rotate(ArrayList<T>  shiftedArrayList, int shift) {
        if (shiftedArrayList.size() == 0)
            return shiftedArrayList;

        T element = null;
        for (int i = 0; i < shift; i++) {
            // remove last element, add it to front of the ArrayList
            element = shiftedArrayList.remove(shiftedArrayList.size() - 1);
            shiftedArrayList.add(0, element);
        }

        return shiftedArrayList;
    }

    private LatLng computeCentroid(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }

        return new LatLng(latitude / n, longitude / n);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void setCurrentLocationMarker(Location location) {
        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(userLatLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.messiresize))
                .snippet("Your location");
        currentMarker = mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setMessage("The permission is mandatory")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                                }
                            }).create().show();
                } else
                    startUpdateLocation();
            }
        }
    }
}