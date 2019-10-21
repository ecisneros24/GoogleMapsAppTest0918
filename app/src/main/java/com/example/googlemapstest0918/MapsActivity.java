package com.example.googlemapstest0918;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Geocoder; // Geocoder is for transforming address --> GPS coordinates (could be used for planting markers on maps for navigation)
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.SearchView; //imported this for SearchView widget support; Website for help on searchviews: https://abhiandroid.com/ui/searchview

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.Places;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;


import java.io.IOException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //private FusedLocationProviderClient fusedLocationProviderClient; //need this fusedlocationprovider for current device loc. //remove this... testing
    SearchView searchView; //instanciating Searchview object
    //Location mLocationPermissionGranted; //REMOVE LATER...testing
    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient; //this is used for retrieving devices reported current location
    boolean mLocationPermissionGranted = false;
    public Location mLastKnownLocation;
    //private UserLocation mUserPosition; //remove later...testing
    public LatLng mDefaultLocation = new LatLng(33.8636406, -118.2549980); //set default location coordinates to center of CSUDH, in case cellular device cannot locate user with GPS
    static public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "MainActivity"; //used for logging purposes when retrieving device location // remove later...testing
    private GeoApiContext mGeoApiContext = null;
    private LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center; default user location if GPS is not available


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // ***CITATION*** method below is derived from the following YouTube Tutorial: (Coding with Mitch) https://www.youtube.com/watch?v=f47L1SL5S0o
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }

        concatDirectionsURL(); //this method concatenates necessary information required to send Google Directions URL + retrieve Google Directions

        //***CITATION*** The following ~20 lines  are derived from the following website: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
        // Turn on the My Location layer and the related control on the map.
        getLocationPermission();
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        //getDeviceLocation();


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);



        searchView = findViewById(R.id.search_bar); // searchview object is being by our "search_bar search view we created in our activity_maps.xml file

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = searchView.getQuery().toString(); //this grabs user-entered text from SearchView search box; saves as string
                List<Address> addressList = null;

                if(location != null || !location.equals("")){ //makes sure there is no invalid user entry
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 1); // Google Geocoder converts user-entered text to GPS coordinates (we will use to place marker on map and (later) enable our own navigation
                        //Google Geocoder documentation here: https://developer.android.com/reference/android/location/Geocoder
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.clear(); //this clears all previously searched (location) GPS markers from map
                    LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
                    //LatLng sydney = new LatLng(-34, 151);
                    mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // adds 'current' location marker on map, but really its just middle of CSUDH campus as starting point for now (until we figure out how to get actual GPS location)
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    calculateDirections(mMap.addMarker(new MarkerOptions().position(latLng).title(location))); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
                return false;
            }
            //********CITATION*** The code relating to searchView above was derived from the following YouTube video: https://www.youtube.com/watch?v=iWYsBDCGhGw
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        //mapFragment.getMapAsync(this);
    }



    // ***CITATION*** the 'Calculate' method below is derived from the following YouTube Tutorial: (Calculating Directions with Google Directions API, Coding with Mitch) https://www.youtube.com/watch?v=f47L1SL5S0o
    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        defaultUserLocation.latitude,
                        defaultUserLocation.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) { //this is returning a navigation result from our default marker to a destination (made from user input in SearchBar)
                                                                // SEE LOGCAT in Android Studio to see direction results, need to display Polyline still on map (line from starting point to destination)
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                Log.d(TAG, "calculateDirections: overviewPolyline: " + result.routes[0].overviewPolyline.toString());

            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }


    public String concatDirectionsURL(){ //this method creates the URL request with the following required attributes: origin, destination, traveling mode (walking or driving) and API key.
                                        //This URL is sent to Google Maps Directions API. The API then returns a set of waypoints, which we use to construct a 'polyline' on map between origin and destination

        String StudentOrigin = "Loker Student Union";
        String StudentDestination = "CSUDH Passport Hub";
        String APIkey = "AIzaSyC1KXH1tpPgdIK8Ln9PR8Ok8fdPOPNilLE"; //this API key is specific for Google MAPS Directions API. I use a separate key for Google Maps API
      // Google Directions API accepts a URL format like the following: "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=YOUR_API_KEY";
        String URL2 = "https://maps.googleapis.com/maps/api/directions/json?origin=" + StudentOrigin + "&destination=" + StudentDestination + "&mode=walking&key=" + APIkey;
        System.out.println(URL2);
        return URL2;
    }


    //***CITATION*** This method below is derived from Google Maps Location documentation at this website: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
	//Used to ask location permission from user, before showing current location on map
	private void getLocationPermission() {
		/*
		 * Request location permission, so that we can get the location of the
		 * device. The result of the permission request is handled by a callback,
		 * onRequestPermissionsResult.
		 */
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
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

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // this method changes map to hybrid satellite view (satellite + 2D map + labels hybrid)
       // Add a marker in Sydney and move the camera //CHANGED TO CSUDH
        LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
        //LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        // used newLatLngZoom method to specify to zoom (level 16) into 'CSUDH' marker by default
        //mMap.moveCamera(CameraUpdateFactory.zoomBy(4, csudh));

		getLocationPermission();


        // Add polylines and polygons to the map. This section shows just
        // a single polyline. Read the rest of the tutorial to learn more.
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-35.016, 143.321),
                        new LatLng(-34.747, 145.592),
                        new LatLng(-34.364, 147.891),
                        new LatLng(-33.501, 150.217),
                        new LatLng(-32.306, 149.248),
                        new LatLng(-32.491, 147.309)));
    }




    //***CITATION*** the following method was derived from the following Github page: https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            Location mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                            LatLng currentDeviceLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentDeviceLocation).title("U R HERE!")); // adds 'current' location marker on map, but really its just middle of CSUDH campus as starting point for now (until we figure out how to get actual GPS location)

                            //Place call to CalculateDirections(Marker) here //remove this and possibly lines above this



                            // place marker here with label "U R HERE!"
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    /*private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = GoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                    polyline.setClickable(true);

                }
            }
        });
    }

}*/
}
