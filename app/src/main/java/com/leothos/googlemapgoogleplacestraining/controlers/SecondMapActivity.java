package com.leothos.googlemapgoogleplacestraining.controlers;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.leothos.googlemapgoogleplacestraining.R;
import com.leothos.googlemapgoogleplacestraining.models.PlaceInfo;
import com.leothos.googlemapgoogleplacestraining.models.ProximityPlace;

import java.util.ArrayList;
import java.util.List;

public class SecondMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    //CONSTANT
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final float DEFAULT_ZOOM = 15f;
    private static final String TAG = "SecondMapActivity";
    private static final int MAX_PLACES = 30;
    List<PlaceInfo> mPlaceInfoList = new ArrayList<>();
    ArrayList<ProximityPlace> mProximityPlaces;
    //VAR
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private LatLng mDefaultLocation = new LatLng(48.7927684, 2.3591994999999315);
    private GoogleApiClient mGoogleApiClient;
    private LatLng[] mLikelyPlaceLatlng;
    private String[] mLikelyPlaceName;
    private Integer[] mPlaceType;
    private LatLngBounds mLatLngBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_map);

        instantiatePlacesApiClients();
        initMap();

    }

    //---------------------------------------------------------------------------------------------//
    //                                          CONFIGURATION                                      //
    //---------------------------------------------------------------------------------------------//

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();
        getDeviceLocation();
        updateUI();
//        showProximityPlace();


    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.scd_map);
        mapFragment.getMapAsync(this);

    }

    private void instantiatePlacesApiClients() {
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    //---------------------------------------------------------------------------------------------//
    //                                          PERMISSION                                         //
    //---------------------------------------------------------------------------------------------//

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //Update Ui here
        updateUI();
    }

    //---------------------------------------------------------------------------------------------//
    //                                     UI & DEVICE LOCATION                                    //
    //---------------------------------------------------------------------------------------------//

    private void updateUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
//                showProximityPlace();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                //Try to obtain location permission
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "updateUI: SecurityException " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            //Move camera toward device position
                            if (mLastKnownLocation != null) {

                                //Define the bounds
                                mLatLngBounds = new LatLngBounds(
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                showProximityPlace();
                            } else {
                                Toast.makeText(SecondMapActivity.this, "Make sure your emulator device got map position on true", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "getPhoneLocation => Exception: %s" + task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());

        }
    }

    private void showProximityPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.

            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);

            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        PlaceLikelihoodBufferResponse likelyPlace = task.getResult();

                        //Store maximum entries
                        int count;
                        Place place = null;
                        if (likelyPlace.getCount() < MAX_PLACES) {
                            count = likelyPlace.getCount();
                        } else {
                            count = MAX_PLACES;
                        }

                        //Inside this loop we put marker at all position we find
                        int i = 0;
                        mLikelyPlaceName = new String[count];
                        mLikelyPlaceLatlng = new LatLng[count];
//                        mPlaceType = new Integer[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlace) {
                            place = placeLikelihood.getPlace();
                            //Check if the nearest place are restaurants
                            for (int j = 0; j < place.getPlaceTypes().size(); j++) {
                                if (place.getPlaceTypes().get(j) == Place.TYPE_RESTAURANT) {
                                    mLikelyPlaceName[i] = place.getName().toString();
                                    mLikelyPlaceLatlng[i] = place.getLatLng();

                                    //Add marker in every place found
                                    if (mLikelyPlaceLatlng.length != 0) {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(mLikelyPlaceLatlng[i])
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_custom_marker)));

                                        Log.d(TAG, "onComplete: show me Latlng marker" + mLikelyPlaceName[i]
                                                + " " + mLikelyPlaceName[i]
                                                + "\n" + place.getPlaceTypes());
                                    }
                                }
                            }


                            i++;
                            if (i > (count - 1)) {
                                break;
                            }

                        }
                        likelyPlace.release();

                    } else {
                        Log.e(TAG, "showProximityPlace: exception: %s", task.getException());
                    }
                }
            });
        } else {
            //User have not granted permission
            // The user has not granted permission.
            Log.d(TAG, "ShowProximityPlace => The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();

        }
    }

    //To finish
    private void autoCompleteFeature() {
        String query = "Restaurant";

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        PendingResult<AutocompletePredictionBuffer> result =
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query,
                        mLatLngBounds, autocompleteFilter);


    }
}
