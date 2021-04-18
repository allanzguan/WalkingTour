package com.example.walkingtour;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.walkingtour.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 111;
    private static final int ACCURACY_REQUEST = 222;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker walkerMarker;
    private Polyline llHistoryPolyline;
    private Polyline pathPolyLine;
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private HashMap<String, Building> buildingList = new HashMap<String, Building>();
    private boolean zooming = false;
    private float oldZoom;
    private FenceManager fenceMgr;
    private final List<PatternItem> pattern = Collections.singletonList(new Dot());
    private CheckBox tour, travel, address, fence;
    private TextView addressView;
    private Geocoder geocoder;
    private ArrayList<Circle> fenseList = new ArrayList<Circle>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        addressView = findViewById(R.id.addressText);

        tour = findViewById(R.id.cbTour);
        tour.setChecked(true);

        travel = findViewById(R.id.cbTravel);
        travel.setChecked(true);

        address = findViewById(R.id.cbAddress);
        address.setChecked(true);

        fence = findViewById(R.id.cbGeofence);
        fence.setChecked(true);

        geocoder = new Geocoder(this);

        initMap();
    }


    public void initMap() {

        fenceMgr = new FenceManager(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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

        zooming = true;

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        setupLocationListener();
        setupZoomListener();
//        addFence();
    }

    public void setBuildingList(HashMap<String, Building> bl){
        this.buildingList = bl;
    }

    public void addFence(){

        for(String key : buildingList.keySet()){
            fenceMgr.addFence(buildingList.get(key));
            int line = Color.parseColor(buildingList.get(key).getFenceColor()) ;
            int fill = ColorUtils.setAlphaComponent(line, 50);

            Circle temp;
            temp = mMap.addCircle(new CircleOptions()
                    .center(buildingList.get(key).getLatLng())
                    .radius(buildingList.get(key).getRadius())
                    .strokePattern(pattern)
                    .strokeColor(line)
                    .fillColor(fill));

            fenseList.add(temp);

        }
    }

    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocListener(this);

        if (checkPermission() && locationManager != null)
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 1, locationListener);


    }

    private void setupZoomListener() {
        mMap.setOnCameraIdleListener(() -> {
            if (zooming) {
//                Log.d(TAG, "onCameraIdle: DONE ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = false;
                oldZoom = mMap.getCameraPosition().zoom;
            }
        });

        mMap.setOnCameraMoveListener(() -> {
            if (mMap.getCameraPosition().zoom != oldZoom) {
//                Log.d(TAG, "onCameraMove: ZOOMING: " + mMap.getCameraPosition().zoom);
                zooming = true;
            }
        });
    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        if(address.isChecked()){
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                Address address = addresses.get(0);
                addressView.setText(address.getAddressLine(0));

            } catch (IOException e) {
                e.printStackTrace();
                addressView.setText("");
            }
        }

        if(!address.isChecked()){
            addressView.setText("");
        }


        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
            zooming = true;
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }

            if(travel.isChecked()) {
                llHistoryPolyline = mMap.addPolyline(polylineOptions);
                llHistoryPolyline.setEndCap(new RoundCap());
                llHistoryPolyline.setWidth(10);
                llHistoryPolyline.setColor(Color.parseColor("#0C753C"));
            }

            float r = getRadius();
            if (r > 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);;
                if (location.getBearing() >= 180) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                }

                if (location.getBearing() < 180) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                }

                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
//                options.rotation(location.getBearing());

                if (walkerMarker != null) {
                    walkerMarker.remove();
                }

                walkerMarker = mMap.addMarker(options);
            }
        }

        if (!zooming)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


    }

    public void addPath(ArrayList<LatLng> p){


        ArrayList<LatLng> pathList = p;
        PolylineOptions polylineOptions2 = new PolylineOptions();

        for (LatLng ll : pathList) {
            polylineOptions2.add(ll);
        }


        pathPolyLine = mMap.addPolyline(polylineOptions2);
        pathPolyLine.setEndCap(new RoundCap());
        pathPolyLine.setWidth(10);
        pathPolyLine.setColor(Color.parseColor("#FFD700"));


    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        return 15f * z - 145f;
    }




    private boolean checkPermission() {
        ArrayList<String> perms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (!perms.isEmpty()) {
            String[] array = perms.toArray(new String[0]);
            ActivityCompat.requestPermissions(this,
                    array, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    public void tourClick(View view){

        if(!tour.isChecked()){
            pathPolyLine.setVisible(false);
        }
        if(tour.isChecked()){
            pathPolyLine.setVisible(true);
        }
        Log.d("CBCB", view.toString());
    }

    public void travelClick(View view){
        if(!travel.isChecked()){
            llHistoryPolyline.setVisible(false);
        }

        if(travel.isChecked()){
            llHistoryPolyline.setVisible(true);
        }
    }

    public void fenceClick(View view){

        if(!fence.isChecked()){
           for(Circle c : fenseList){

               c.setVisible(false);

           }
        }

        if(fence.isChecked()){
            for(Circle c : fenseList){
                c.setVisible(true);
            }
        }
    }

    public void addressClick(View view){
        if(!address.isChecked()){
            addressView.setText("");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }
}//