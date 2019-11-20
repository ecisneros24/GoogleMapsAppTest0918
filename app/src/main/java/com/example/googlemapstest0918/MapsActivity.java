package com.example.googlemapstest0918;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Geocoder; // Geocoder is for transforming address --> GPS coordinates (could be used for planting markers on maps for navigation)
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView; //imported this for SearchView widget support; Website for help on searchviews: https://abhiandroid.com/ui/searchview
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest; //used for persistent GPS location tracking
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import android.view.Menu;
import android.view.MenuInflater;
import android.content.Intent;
import java.io.IOException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

	private DrawerLayout drawer;
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
    private LatLng defaultUserLocation = null; //rough coordinates of CSUDH center; default user location if GPS is not available
    //private LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //line above was this //Remove...testing
    private boolean isUserLocatable = false; //flag for keeping track if user GPS location is available or not


    private LocationRequest locationRequest;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu(): menu inflated.");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        Button CollegeOfEducationNav = (Button) findViewById(R.id.subitem1);
        switch (item.getItemId()){
            case R.id.action_findnearestfood:
                Toast.makeText(this, "Food to come", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onOptionsItemsSelected(): findNearestFood selected.");
                return true;
            case R.id.action_findnearestrestroom:
                Toast.makeText(this, "Restrooms to come", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onOptionsItemsSelected(): findNearestFood selected.");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getMenuInflater().inflate(R.menu.drawer_menu, menu); //remove this...testing
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); //sets a listener for when an item is clicked withing the navigationView (or slide-out hamburger menu)







        //***CITATION*** the Toolbar code below was derived from the following YouTube (Coding In Flow) tutorial: https://www.youtube.com/watch?v=zYVEMCiDcmY
        //Toolbar toolbar = (Toolbar) findViewById((R.id.toolbar)); //remove this...testing remove toolbar code may not implement
        //setActionBar(toolbar);
		//drawer = findViewById(R.id.drawer_layout);
		//ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		//drawer.addDrawerListener(toggle);
		//toggle.syncState();

        Button findLocationButton = (Button) findViewById(R.id.current_location_button); //Button was created with help of Google Documentation here: https://developer.android.com/reference/android/widget/Button
        findLocationButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //Code inside here will execute on main thread after user presses button
                        getDeviceLocation();
                        Toast toast = Toast.makeText(MapsActivity.this, "Retrieving your GPS location...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );


        // ***CITATION*** method below is derived from the following YouTube Tutorial: (Coding with Mitch) https://www.youtube.com/watch?v=f47L1SL5S0o
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build(); //has google maps directions API key value passed as argument
        }

        //concatDirectionsURL(); //this method concatenates necessary information required to send Google Directions URL + retrieve Google Directions //remove this...testing

        //***CITATION*** The following ~20 lines  are derived from the following website: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
        // Turn on the My Location layer and the related control on the map.
        testPermissionsRequest();
        getLocationPermission();
        updateLocationUI();
        // Get the current location of the device and set the position of the map.

		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) { //this makes sure that there are google Play Services installed on device; may integrate warning message later on

			System.out.println("getDeviceLocation() is working"); //for logging purposes
			getDeviceLocation();
			//add toast if no Google API is not available
		}
		else
		{
			System.out.println("getDeviceLocation() is NOT working"); //for logging purposes //add toast if no Google API is not available
		}



        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null); //remove this...testing
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null); //remove this...testing
        // Construct a FusedLocationProviderClient.
       mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        searchView = findViewById(R.id.search_bar); // searchview object is being by our "search_bar search view we created in our activity_maps.xml file

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { //when user types search query in search bar and hits search... do what is below:
                String location = searchView.getQuery().toString(); //this grabs user-entered text from SearchView search box; saves as string
                List<Address> addressList = null;

                if (location != null || !location.equals("")) { //makes sure there is no invalid user entry
                    Geocoder geocoder = new Geocoder(MapsActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1); // Google Geocoder converts user-entered text to GPS coordinates (we will use to place marker on map and (later) enable our own navigation
                        //Google Geocoder documentation here: https://developer.android.com/reference/android/location/Geocoder
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);


                    LatLng destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude()); //this stores latitude and longitude coordinates of DESTINATION (found by Geocoder) and stores in a LatLng object
                    mMap.clear(); //this clears all previously searched (location) GPS markers from map


                    getDeviceLocation(); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
                    if (isUserLocatable = false){
                        LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

                        LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
                        //LatLng sydney = new LatLng(-34, 151);
                        mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
                    }

                    //Add logic to choose either CSUDH center or Device live location as 'start' for navigation (need to add a bool and put it here to see which marker to use for start"

                    mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(location)); //add marker to destination
                    calculateDirections(mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(location))); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));
                }
                return false;
            }

            //********CITATION*** The code relating to searchView above was derived from the following YouTube video: https://www.youtube.com/watch?v=iWYsBDCGhGw
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
    }

    //The navigationView menu listener tutorial link (helped out with this onNavigationItemsSelected method): https://www.youtube.com/watch?v=bjYstsO1PgI
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch(menuItem.getItemId()){
            case R.id.subitem1:
                Toast.makeText(this, "College Of Education", Toast.LENGTH_SHORT).show();
                getDeviceLocation(); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
                navigateToDestination("College of Education", 33.8654089,-118.2548097); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
                //then set destination to (for now) hard coded coordinates for College of Education
                break;
            case R.id.subitem2:
                Toast.makeText(this, "Leo Cain Library", Toast.LENGTH_SHORT).show();
                getDeviceLocation(); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
                navigateToDestination("Leo Cain Library", 33.8640928,-118.2558234); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
                //then set destination to (for now) hard coded coordinates for Leo Cain Library
                break;
            case R.id.subitem3:
                Toast.makeText(this, "Welch Hall", Toast.LENGTH_SHORT).show();
                getDeviceLocation(); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
                navigateToDestination("Welch Hall", 33.8662514,-118.2567457); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
                //then set destination to (for now) hard coded coordinates for Welch Hall
                break;
            case R.id.subitem4:
                Toast.makeText(this, "Loker Student Union (LSU)", Toast.LENGTH_SHORT).show();
                getDeviceLocation(); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
                navigateToDestination("Loker Student Union (LSU)", 33.8647679,-118.2559123); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
                //then set destination to (for now) hard coded coordinates for LSU
                break;
            case R.id.subitem5:
                Toast.makeText(this, "Social and Behavioral Sciences", Toast.LENGTH_SHORT).show();
                getDeviceLocation(); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
                navigateToDestination("Social and Behavioral Sciences", 33.8646270,-118.2548612); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
                //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
                break;
            case R.id.action_findnearestfood:
                Toast.makeText(this, "Showing nearest Food spots", Toast.LENGTH_SHORT).show();
                //perhaps make a few map Markers (of restaurants, vending machines?, Grab N Go's) here and show them on map
                //Perhaps make them clickable and then start navigation to selected marker
                //then clear map of previous markers (other food results)
                break;
            case R.id.action_findnearestrestroom:
                Toast.makeText(this, "Showing nearest restrooms", Toast.LENGTH_SHORT).show();
                break;


        }
        return true;
    }

    //this method takes in following arguments (destination name (ex. LSU, Welch Hall, etc), latitude coordinate (in float), longitude coordinate (in float)
    private void navigateToDestination(String destinationName, double latitude, double longitude){ //this method will be used for navigating to a destination selected in slide out Hamburger menu
        LatLng destinationLatLng = new LatLng(latitude, longitude); //this stores latitude and longitude coordinates of DESTINATION (found by Geocoder) and stores in a LatLng object
        mMap.clear(); //this clears all previously searched (location) GPS markers from map

        getDeviceLocation(); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
        if (isUserLocatable = false){
            LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

            LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
            //LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        }

        //Add logic to choose either CSUDH center or Device live location as 'start' for navigation (need to add a bool and put it here to see which marker to use for start"

        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName)); //add marker to destination
        calculateDirections(mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName))); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));

    }


    // ***CITATION*** the 'Calculate' method below is derived from the following YouTube Tutorial: (Calculating Directions with Google Directions API, Coding with Mitch) https://www.youtube.com/watch?v=f47L1SL5S0o
    private void calculateDirections(Marker marker) { // method accepts a marker object and sends origin + destination directions request to Google. Response can include distance, routes, polyline data, etc.
        Log.d(TAG, "calculateDirections: calculating directions.");


        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng( //this marker.GetPosition().latitude (and longitude) will get the lat and long. of the DESTINATION, passed into this method with a marker object
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext); //this DirectionsApiRequest takes in a GeoApiContext with my Google Directions API key. Without valid key, request will not go through
        directions.mode(TravelMode.WALKING); //this method explicitly sets the DirectionsApi request object to return ONLY walking directions data, not driving directions from the API. We won't need driving directions as this app is primarily for on-campus use
        directions.alternatives(false); // this method shows us all possible routes from point a to point b. To show only one route result, set to FALSE


        if (isUserLocatable = false){
            LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

            directions.origin( // this sets the origin / starting point of navigation; want this to be device current location, but will likely default to center of CSUDH is GPS signal is not avail.
                    new com.google.maps.model.LatLng(
                            defaultUserLocation.latitude,
                            defaultUserLocation.longitude
                    )
            );
        }
        else{ //if usesIsLocatable, then obtain user Current GPS location and set that as default User Location (and to be used as origin for navigation)
            getDeviceLocation(); //this method obtains live GPS location. If no live GPS location cannot be obtained by this method, the defaultUserLocation will not be changed at all and default will be CSUDH
            getDeviceLocation();
            directions.origin( // this sets the origin / starting point of navigation; want this to be device current location, but will likely default to center of CSUDH is GPS signal is not avail.
                    new com.google.maps.model.LatLng(
                            defaultUserLocation.latitude,
                            defaultUserLocation.longitude
                    )
            );
        }


        // if isUserLocatable = true (if user has attainable GPS location), set their user location as what live Android device is reporting.

        //liveLocation = getDeviceLocation();

        //else if isUserLocatable = false, set their default location marker as CSUDH.
        //LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //this is center of CSUDH if user device cannot retrieve live GPS location info.



       /* directions.origin( // this sets the origin / starting point of navigation; want this to be device current location, but will likely default to center of CSUDH is GPS signal is not avail.
                new com.google.maps.model.LatLng(
                        defaultUserLocation.latitude,
                        defaultUserLocation.longitude
                )
        ); */


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


                    // addPolylinesToMap is called here because the result from Directionsresult object is returned in this onResult method

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    // ***CITATION*** the 'AddPolyLinesToMap' method below is derived from the following YouTube Tutorial: (Calculating Directions with Google Directions API, Coding with Mitch). link = https://www.youtube.com/watch?v=xl0GwkLNpNI&list=PLgCYzUzKIBE-SZUrVOsbYMzH7tPigT3gi&index=20
    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                //add comment on import statement;  DirectionsRoute
                for (DirectionsRoute route : result.routes) {  //this is taking all of the 'checkpoints' from the results object (returned from the request to DirectionsResult object) and
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath()); //this sums up all of the checkpoints along a route
                    //add comment on import statement; PolylineEncoding above

                    List<LatLng> newDecodedPath = new ArrayList<>(); //list will contain latitude + longitude coordinates for each 'checkpoint' along the route (ex, a point where the route/Polyline makes a turn, starts, or stops'


                    for (com.google.maps.model.LatLng latLng : decodedPath) { // This loops through all the LatLng coordinates of a polyline...

//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(new LatLng( //... and adds the coordinates for each 'checkpoint' on the route to this arrayl ist
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath)); //this actually adds the polyline (created from the sum of all the 'checkpoints' of all routes; obtained from a request to DirectionsResult object)
                    polyline.setColor(Color.rgb(239,186,8)); // set Polyline to CSUDH yellow color to contrast the burgundy status bar... trying to have a theme going on here! Source for school theme values is on CSUDH website
                    polyline.setClickable(true);

                }
            }
        });
    }


    public String concatDirectionsURL() { //this method creates the URL request with the following required attributes: origin, destination, traveling mode (walking or driving) and API key.
        //This URL is sent to Google Maps Directions API. The API then returns a set of waypoints, which we use to construct a 'polyline' on map between origin and destination

        String StudentOrigin = "Loker Student Union";
        String StudentDestination = "CSUDH Passport Hub";
        String APIkey = "AIzaSyC1KXH1tpPgdIK8Ln9PR8Ok8fdPOPNilLE"; //this API key is specific for Google MAPS Directions API. I generated a separate key for Google Maps API
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), // if adequate permissions are granted, set LocationPermissionGranted flag to true
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,  //else, request permissions from user with pop up dialog
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


//    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
//    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
//    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />



    private void testPermissionsRequest(){
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
// <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />


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
                    mLocationPermissionGranted = true; //was true
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
        } catch (SecurityException e) {
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


        getDeviceLocation(); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
        if (isUserLocatable = false){
            LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

            LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
            //LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        }














        //LatLng NEWARK = new LatLng(40.714086, -74.228697);
        LatLng csudh = new LatLng(33.8645994, -118.2548179);

        LatLng southwest = new LatLng(33.857300, -118.260804);
        LatLng northeast = new LatLng(33.867629, -118.247800);


		/*BitmapDescriptor floor1 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(floor1)
                .anchor(0, 1)
                .position(new LatLng(33.8636406, -118.2549980), 8600f, 6500f)
                .transparency((float)0.00).visible(true);
*/


		LatLngBounds bounds = new LatLngBounds(southwest,northeast); // get a bounds
		// Adds a ground overlay with 50% transparency.
		//add overlay

		/*BitmapDescriptor floor1 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1);

		GroundOverlayOptions test1= new GroundOverlayOptions()
				.image(floor1).anchor(0, 1)
				.position(csudh, 4300f, 3025f)
				.bearing(30);


		mMap.addGroundOverlay(test1); */



		//derived from google documentation on GroundOverlays
		GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions ();
		groundOverlayOptions.position(csudh, 100, 100 )
				.image( BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1ds)).transparency((float)0.5);

		mMap.addGroundOverlay(groundOverlayOptions);
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		mMap.moveCamera(CameraUpdateFactory.newLatLng(csudh));

		//mMap.addGroundOverlay(groundOverlay);





        // used newLatLngZoom method to specify to zoom (level 16) into 'CSUDH' marker by default
        //mMap.moveCamera(CameraUpdateFactory.zoomBy(4, csudh));

        getLocationPermission(); //remove this...testing


        // Add polylines and polygons to the map. This section shows just
        // a single polyline. Read the rest of the tutorial to learn more.
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions() //remove this..testing
                .clickable(true)
                .add(
                        new LatLng(-35.016, 143.321),
                        new LatLng(-34.747, 145.592),
                        new LatLng(-34.364, 147.891),
                        new LatLng(-33.501, 150.217),
                        new LatLng(-32.306, 149.248),
                        new LatLng(-32.491, 147.309)));
    }


    //method below is to get GPS current location and set global variable defaultUserLocation marker to device current location. Also will determine boolean flag 'isUserLocatble'
    private void getDeviceLocation() { //***CITATION*** this method was derived from the following YouTube Tutorial: Coding With Mitch Get Device Location - [Android Google Maps Course]  link: https://www.youtube.com/watch?v=fPFr0So1LmI
        Log.d(TAG, "getDeviceLocation() : getting device location"); //remove this...testing

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: foundLocation!"); //remove this...testing

                            if ((Location)task.getResult() != null){ //if there is a cached last known Location from Android System, do the following:

                                System.out.println("location task.getResult() successful");
                                Location currentLocation = (Location)task.getResult(); //save last known location of device

                                LatLng currentDeviceLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentDeviceLocation, 15)); //moves map camera to where user is currently located, level 15 zoom

                                mMap.addMarker(new MarkerOptions().position(currentDeviceLocation).title("U R HERE!"));
                                defaultUserLocation = currentDeviceLocation; //this sets the variable defaultUserLocation to the liveCurrentLocation reported by device.
                                isUserLocatable = true; //if the GPS location task is successful, set isUserLocatable boolean flag to true
                                //mFusedLocationProviderClient.requestLocationUpdates() // add occasional device location updates here
                            }else{
                                System.out.println("location task.getResult() FAILED");
                                // So far, app will crash if it doesn't find a current (or recent) location from the Android device. So we need to launch Google maps first, then app will open successfully.
                                //CREATE TOAST MESSAGE "unable to find current GPS location!"
                                //CREATE TOAST MESSAGE "User location will default to CSUDH Campus"
                                Toast toast = Toast.makeText(MapsActivity.this, "GPS location not found! Using CSUDH as default location...", Toast.LENGTH_LONG);
                                toast.show(); // show Android Toast error message when there is no GPS location available
                                defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh













                                //check this website for help! https://developer.android.com/training/location/receive-location-updates.html

                                // need to add a timer or an on-event listener(when device location has changed) to update location on map



                                //Add runtime permissions!!!!!




                                // Possibly create navigation where user manually chooses start and destination of route
                            }
                        } else {
                            Log.d(TAG, "onComplete: location is null!"); //remove this
                            Toast toast = Toast.makeText(MapsActivity.this, "GPS location permissions not granted!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation(): security exception" + e.getMessage());

        }

    }




    //***CITATION*** the following method was derived from the following Github page: https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
  /* private void getDeviceLocation() {

        // Get the best and most recent location of the device, which may be null in rare
        // cases when a location is not available.

        try {
            if (mLocationPermissionGranted) { //location permission has been granted at this point
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) { //called when the task completes

                        // if (task.isSuccessful() && task.getResult() != null)
                        if (task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            Location mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                            LatLng currentDeviceLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentDeviceLocation).title("U R HERE!")); // adds 'current' location marker on map, but really its just middle of CSUDH campus as starting point for now (until we figure out how to get actual GPS location)

                            //Place call to CalculateDirections(Marker) here //remove this and possibly lines above this


                            // place marker here with label "U R HERE!"
                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    } */



}

