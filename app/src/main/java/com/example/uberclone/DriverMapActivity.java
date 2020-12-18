package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleAPICLient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private final int LOCATION_REQUEST_CODE = 1;
    SupportMapFragment mapFragment;
    private Button mLogout;
    private String customerID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDriver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        mLogout=findViewById(R.id.logoutDriver);
        mLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(DriverMapActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        });

        getAssignedCustomer();
    }

    /**
     * this method fetches customer with customerRideID using current driver field in DB present.Fetching the ID and then fetching its location
     */
    private void getAssignedCustomer() {
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID);
        assignedustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Map<String,Object> map= (Map<String, Object>) snapshot.getValue();
                    if(map.get("customerRideID")!=null)
                    {
                        customerID=map.get("customerRideID").toString();
                        getAssignedCustomerPickUpLocation();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * method which fetches the customer req and then sets up a marker there
     */
    private void getAssignedCustomerPickUpLocation() {
      DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerID).child("l");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng latLng = new LatLng(locationLat, locationLong);
                    mMap.addMarker(new MarkerOptions().position(latLng).title("pickup location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleAPIClient();
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void buildGoogleAPIClient() {
        mGoogleAPICLient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleAPICLient.connect();
    }

    /**
     * @param location gives the location of driver
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference referenceAvailable= FirebaseDatabase.getInstance().getReference("driversAvailable");

        DatabaseReference referenceWorking= FirebaseDatabase.getInstance().getReference("driversWorking");

        GeoFire geoFireAvaiable=new GeoFire(referenceAvailable);
        GeoFire geoFireWorking=new GeoFire(referenceWorking);

        if ("".equals(customerID)) {
            geoFireAvaiable.removeLocation(userID);
            geoFireAvaiable.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        } else {
            geoFireWorking.removeLocation(userID);
            geoFireWorking.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }


    }

    /**
     * @param bundle shows the blue marker on the location
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPICLient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(this, "Please provide the perm", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire=new GeoFire(reference);
        geoFire.removeLocation(userID);
    }
}