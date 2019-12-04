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

import android.app.Activity;
import android.content.Context;
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
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView; //imported this for SearchView widget support; Website for help on searchviews: https://abhiandroid.com/ui/searchview
import android.widget.TextView;
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


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

	private DrawerLayout drawer;
    private GoogleMap mMap; //this mMap object is a google maps object, will call many methods from this object
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
    boolean isUserNavigating = false; //bool flag for keeping track if map camera should be stationary or follow user.
	private int currentlySelectedFloor = 0;
    public TextView NavigationTextViewObject = null;
    public GroundOverlay imageOverlaySBS1; //instanciating an map overlay here
    public GroundOverlay imageOverlaySBS2; //instanciating an map overlay here
    public GroundOverlay imageOverlaySBS3; //instanciating an map overlay here

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

        //Button CollegeOfEducationNav = (Button) findViewById(R.id.subitem1);
        switch (item.getItemId()){
            case R.id.action_building_directory:
                Toast.makeText(this, "To Buildings!", Toast.LENGTH_SHORT).show();
                return true;
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

        ImageButton findLocationButton = findViewById(R.id.current_location_button); //Button was created with help of Google Documentation here: https://developer.android.com/reference/android/widget/Button
        findLocationButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //Code inside here will execute on main thread after user presses button
                        getDeviceLocation(18);
                        Toast toast = Toast.makeText(MapsActivity.this, "Retrieving your GPS location...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        Button floor1button = (Button) findViewById(R.id.floor1_button); //Button was created with help of Google Documentation here: https://developer.android.com/reference/android/widget/Button
        floor1button.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //Code inside here will execute on main thread after user presses button

                        drawMapOverlay(1);
                        Toast toast = Toast.makeText(MapsActivity.this, "Displaying SBS Building Floor 1 indoor map...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        Button floor2button = (Button) findViewById(R.id.floor2_button); //Button was created with help of Google Documentation here: https://developer.android.com/reference/android/widget/Button
        floor2button.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //Code inside here will execute on main thread after user presses button

                        drawMapOverlay(2);
                        Toast toast = Toast.makeText(MapsActivity.this, "Displaying SBS Building Floor 2 indoor map...", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

		Button floor3button = (Button) findViewById(R.id.floor3_button); //Button was created with help of Google Documentation here: https://developer.android.com/reference/android/widget/Button
		floor3button.setOnClickListener(
				new Button.OnClickListener(){
					public void onClick(View v){
						//Code inside here will execute on main thread after user presses button

						drawMapOverlay(3);
						Toast toast = Toast.makeText(MapsActivity.this, "Displaying SBS Building Floor 3 indoor map...", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
		);

        NavigationTextViewObject = (TextView) findViewById(R.id.NavigationTextView); //this instantiates a textview object, reference to the Textview UI Element created in the activity_maps.xml file


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
			getDeviceLocation(15);
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

                    if (addressList != null){

						Address address = addressList.get(0);


						LatLng destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude()); //this stores latitude and longitude coordinates of DESTINATION (found by Geocoder) and stores in a LatLng object
						mMap.clear(); //this clears all previously searched (location) GPS markers from map


						getDeviceLocation(15); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
						if (isUserLocatable = false){
							LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

							LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
							//LatLng sydney = new LatLng(-34, 151);
							mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
							mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
						}

						//Add logic to choose either CSUDH center or Device live location as 'start' for navigation (need to add a bool and put it here to see which marker to use for start"


						Marker destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));  //this creates new Marker and instanciates marker object; is colored green

						// MarkerOptions destinationMarker = new MarkerOptions().position(destinationLatLng).title(location);

						// mMap.addMarker(destinationMarker); //add marker to destination
						calculateDirections(destinationMarker); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));


						//OLD FUNCTIONING DESTINATION MARKER HERE  //remove this..testing
                    /*
                    mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(location)); //add marker to destination
                    calculateDirections(mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(location))); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16)); */

					}
                    else
					{
						// Show error message toast. Let user know to check internet connection since Geocoder API is unable to successfully request information.
						// If there is an issue requesting Geocoder results, there must be internet connectivity issues
						hideKeyboard(MapsActivity.this); //this method forces Android system to hide keyboard (used so user can view error toast message). Go to method declaration for citation!
						Toast toast = Toast.makeText(MapsActivity.this, "Unable to obtain directions request. Please check your internet connection and try again!", Toast.LENGTH_LONG);
						toast.show();
					}
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
            case R.id.action_building_directory:
                Toast.makeText(this, "Going to the buildings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_findnearestfood:
                Toast.makeText(this, "Showing nearest Food spots", Toast.LENGTH_SHORT).show();

                getFoodSpots();

                //perhaps make a few map Markers (of restaurants, vending machines?, Grab N Go's) here and show them on map
                //Perhaps make them clickable and then start navigation to selected marker
                //then clear map of previous markers (other food results)
                break;
            case R.id.action_findnearestrestroom:
                Toast.makeText(this, "Showing nearest restrooms", Toast.LENGTH_SHORT).show();
                getNearestRestrooms();
                break;

            /******************************************************** FOR THE BUILDINGS
             *          case R.id.college_of_education:
             *                 Toast.makeText(this, "College Of Education", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("College of Education", 33.8654089,-118.2548097);
             *                 break;
             *             case R.id.leo_cain:
             *                 Toast.makeText(this, "Leo Cain Library", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Leo Cain Library", 33.8640928,-118.2558234); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Leo Cain Library
             *                 break;
             *             case R.id.james_welch:
             *                 Toast.makeText(this, "Welch Hall", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Welch Hall", 33.8662514,-118.2567457); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Welch Hall
             *                 break;
             *             case R.id.loker:
             *                 Toast.makeText(this, "Loker Student Union (LSU)", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Loker Student Union (LSU)", 33.8647679,-118.2559123); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for LSU
             *                 break;
             *             case R.id.social_behavioral:
             *                 Toast.makeText(this, "Social and Behavioral Sciences", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Social and Behavioral Sciences", 33.8646270,-118.2548612); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.lacorte:
             *                 Toast.makeText(this, "Lacorte Hall", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Lacorte Hall", 33.863861, -118.256968); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.theater:
             *                 Toast.makeText(this, "University Theater", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("University Theater", 33.865364, .118.256810); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.social_behavioral:
             *                 Toast.makeText(this, "Natural Sciences and Mathametics", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Natural Sciences and Mathematics", 33.863868, .118.254834); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.gym:
             *                 Toast.makeText(this, "Gymnasium", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("Gymnasium", 33.862817, -118.255899); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.complex2:
             *                 Toast.makeText(this, "South Academy Complex 2", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("South Academy Complex 2", 33.862835, -118.254652 ); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *             case R.id.complex3:
             *                 Toast.makeText(this, "South Academy Complex 3", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("South Academy Complex 3", 33.862494, -118.254773); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 //then set destination to (for now) hard coded coordinates for Social and Behavioral Sciences building
             *                 break;
             *****************************************************************************/
            /******************************************************************************* FOR FLOOR 1
             *             case R.id.a104:
             *                 Toast.makeText(this, "A104", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A104", 33.864782, -118.255159); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a110b:
             *                 Toast.makeText(this, "A110B", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A110B", 33.864707, -118.255040); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a110c:
             *                 Toast.makeText(this, "A110C", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A110C", 33.864724, -118254963); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a122:
             *                 Toast.makeText(this, "A122", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A22", 33.864782, -118.254860); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a126:
             *                 Toast.makeText(this, "A126", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A126", 33.864792, -118.254779); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a134:
             *                 Toast.makeText(this, "A134", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A134", 33.864803, -118.254666); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.a144:
             *                 Toast.makeText(this, "A144", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("A144", 33.864796, -118.254542); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b101:
             *                 Toast.makeText(this, "B101", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B101", 33864714, -118.255.147); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b110:
             *                 Toast.makeText(this, "B110", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B110", 33.864607, -118.255085); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b131:
             *                 Toast.makeText(this, "B131", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B131", 33.864703, -118.254714); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b137:
             *                 Toast.makeText(this, "B137", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B137", 33.864707, -118.254644); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b140:
             *                 Toast.makeText(this, "B140", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B140", 33.864633, -118.254613); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.b143:
             *                 Toast.makeText(this, "B143", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("B143", 33.864719, -118.254537); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.d121:
             *                 Toast.makeText(this, "D121", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("D121", 33.864506, -118.255083); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.d125:
             *                 Toast.makeText(this, "D125", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("D125", 33.864441, -118.255083); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.e116:
             *                 Toast.makeText(this, "E116", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("E166", 33.864602, -118.254945); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.e122:
             *                 Toast.makeText(this, "E122", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("E122", 33.864518, -118.254956); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.e126:
             *                 Toast.makeText(this, "E126", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("E126", 33.864441, -118.254959); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.f101:
             *                 Toast.makeText(this, "F101", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("F101", 33.864787, -118.254697); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.f117:
             *                 Toast.makeText(this, "F117", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("F117", 33.864616, -118.254694); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.f121:
             *                 Toast.makeText(this, "F121", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("F121", 33.864528, -118.254691); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.f125:
             *                 Toast.makeText(this, "F125", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("F125", 33.864443, -118.254694); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.g122:
             *                 Toast.makeText(this, "G122", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("G122", 33.864525, -118.254542); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             *             case R.id.g126:
             *                 Toast.makeText(this, "G126", Toast.LENGTH_SHORT).show();
             *                 getDeviceLocation(15); // first find device location (if GPS is available), set appropriate boolean flag for isUserLocatable
             *                 navigateToDestination("G126", 33.864443, -118.254548); // we are passing a the name of the desired destination, longitude and latitude coordinates. This method initiates navigation
             *                 break;
             ******************************
             */
        }
        return true;
    }

	//***CITATION*** This hideSoftKeyboard method was derived from the following website: https://guides.codepath.com/android/Working-with-the-Soft-Keyboard
	//remove this...testing
	//unused
	public void hideSoftKeyboard(View view){ //this forces Android system to hide keyboard whenever method is called
		InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}


	//***CITATION*** This hideKeyboard method was derived from the following website: https://stackoverflow.com/questions/1109022/how-to-set-visibility-android-soft-keyboard
	//this method forces Android system to hide keyboard whenever method is called. Useful for showing user toast messages, which usually appear below the keyboard
	public static void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}


    public boolean onMarkerClick(final Marker marker) {//***CITATION*** code to create clickable marker was derived from official Google Maps Marker documentation: https://developers.google.com/maps/documentation/android-sdk/marker
	//this method allows for user to click on a marker on the map (like nearby food spots or restrooms) and instantly start navigation to the selected marker (destination)
        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);

            if(((Integer) marker.getTag()).intValue() == 1){ //this will allow user to click on a marker once (to view the name) then click on it a 2nd time to start Navigation

                Toast.makeText(this,
                        marker.getTitle() +
                                " has been clicked. Click 2x to get walking directions.",
                        Toast.LENGTH_SHORT).show();

            }

            if(((Integer) marker.getTag()).intValue() == 2){ //this will allow user to click on a marker once (to view the name) then click on it a 2nd time to start Navigation
                navigateToDestination(marker.getTitle(), marker.getPosition().latitude ,marker.getPosition().longitude);
                clickCount = 0;
                marker.setTag(clickCount); //resets clickCount of each marker to 0 once user has started navigation to it
            }
        }



        //Start navigation here
        //When user clicks on a marker, we are passing the marker's longitude + latitude to navigateToDestination() method, which draws the start-to-destination route Polyline on map
        //navigateToDestination(marker.getTitle(), marker.getPosition().latitude ,marker.getPosition().longitude);
        //clickCount = 0;
        //marker.setTag(clickCount); //resets clickCount of each marker to 0 once user has started navigation to it

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }


    private void indoorNavigationInstructions(){

        //place markers on staircases
        // add text instructions to climb to specific floor (perhaps in middle of building)
        // add text instructions to head to certain direction (south, north) towards classroom
        //Possibly add an arrow image (left = west, right = east, up = north, down = south) indicating what direction to walk from central staircase.


        //** Figure out how to get bearing to destination relative to current location

        // Go to Floor 2, Head west, then turn left at hallway.
        //Destination will be to your right


        //hardcoded classrooms as test

        LatLng SBSE126 = new LatLng(33.8644736, -118.2549322); //E126, Floor 1 (Senior Design Classroom)
        LatLng SAC2102 = new LatLng(33.8628113, -118.2550489); //SAC2102, Floor 1


    }


    private void getNearestRestrooms(){

        //below is a list of some restrooms across the campus etc. Not a complete list for now, until database is implemented)
        //These are the LatLng objects (that will be used to plant markers on map)


        LatLng SBSBathrooms = new LatLng(33.864750, -118.254987); //bathrooms inside center of SBS (floors 1 + 2)
        LatLng SAC2Bathrooms = new LatLng(33.8628333, -118.2546607); //bathrooms inside center of South Academic Complex 2
        LatLng SAC3Bathrooms = new LatLng(33.8624775, -118.2543623); //bathrooms inside center of South Academic Complex 3
        LatLng LSUSouthBathrooms = new LatLng(33.8647005, -118.2558123); //bathrooms inside South side of LSU (near Bookstore) (Floors 1 + 2)
        LatLng LSUNorthBathrooms = new LatLng(33.8628333, -118.2546607); //bathrooms inside north side of LSU (near DH Sports Lounge)
        LatLng NSMBathrooms = new LatLng(33.8638700, -118.2548367); //bathrooms inside center of NSM (Floors 1 + 2)
        LatLng WelchHallSouthBathrooms = new LatLng(33.8661827, -118.2570317); //bathrooms inside Welch Hall (Floor 1) (near Grab N' Go)
        LatLng LaCorteHallBathrooms = new LatLng(33.8638297, -118.2570203); //bathrooms inside LCH (Floor 2)
        LatLng WeightRoomBathrooms = new LatLng(33.8625479, -118.2567568); //bathrooms outside Gym weight room

        NavigationTextViewObject.setText(" "); //way of clearing the textview object of previous navigation instructions
        mMap.clear(); // clears maps of any markers to prevent user confusion
        //all markers are being placed on map below, after map was cleared of old markers
        mMap.addMarker(new MarkerOptions().position(SBSBathrooms).title("SBS Restrooms (Floors 1 + 2)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(SAC2Bathrooms).title("SAC-2 Restrooms")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(SAC3Bathrooms).title("SAC-3 Restrooms")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(LSUNorthBathrooms).title("LSU Restrooms (Near DH Sports Lounge)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(LSUSouthBathrooms).title("LSU Restrooms (Floors 1 + 2)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(NSMBathrooms).title("NSM Bathrooms (Floors 1 + 2))")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(WelchHallSouthBathrooms).title("Welch Hall Restrooms (Near Grab N' Go!; Floor 1)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(LaCorteHallBathrooms).title("LaCorte Hall Restrooms (Floor 2)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(WeightRoomBathrooms).title("Weight Room Restrooms (across from Tennis Courts)")).setTag(0);

        mMap.setOnMarkerClickListener(this); //this allows markers to be clickable. In case user wants to select a potential point of interest as a destination


    }



//this method clears all old markers off maps + puts some major food / drink restaurants, stores, vending machines, etc. User can then choose from available
    private void getFoodSpots(){

        //below is a list of major food spots (restaurants, Grab N Go's, some vending machines, etc. Not a complete list for now, until database is implemented)
        //These are the LatLng objects (that will be used to plant markers on map)


        //---Restaurants + Grab N Go Stores---
        LatLng PandaExpress = new LatLng(33.8654717, -118.2558586); //inside LSU (along with Subway,
        LatLng UnionGrind = new  LatLng(33.8636456, -118.2560222); //cafe inside 1st floor library
        LatLng GrabNGoSBS = new  LatLng(33.8645933, -118.2549208); //Grab N Go in SBS building (floor 2)
        LatLng GrabNGoWH = new  LatLng(33.8661431, -118.2570532); //Grab N Go in Welch Hall building (floor 1)
        LatLng DHSportsLounge = new  LatLng(33.8652506, -118.2561738);

        // ---Vending Machines ----
        LatLng VendingMachineByGym = new  LatLng(33.8626693, -118.2555190); // Vending Machine by Gym entrance (facing SAC 2102 building)
        LatLng VendingMachineSAC2102 = new  LatLng(33.8628029, -118.2550885); //Vending Machines by SAC 2102 west entrance

        // ---


        NavigationTextViewObject.setText(" "); //way of clearing the textview object of previous navigation instructions
        mMap.clear(); // clears maps of any markers to prevent user confusion
        //all markers are being placed on map below, after map was cleared of old markers
        mMap.addMarker(new MarkerOptions().position(PandaExpress).title("Panda Express")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(UnionGrind).title("Union Grind Cafe")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(GrabNGoSBS).title("Grab N' Go! (2nd Floor)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(GrabNGoWH).title("Grab N' Go! (1st Floor)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(DHSportsLounge).title("DH Sports Lounge (1st Floor)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(VendingMachineByGym).title("Vending Machine (near Gym Entrance)")).setTag(0);
        mMap.addMarker(new MarkerOptions().position(VendingMachineSAC2102).title("Vending Machine (near SAC-2102 entrance)")).setTag(0);

        mMap.setOnMarkerClickListener(this); //this allows markers to be clickable. In case user wants to select a potential point of interest as a destination

    }

    //this method takes in following arguments (destination name (ex. LSU, Welch Hall, etc), latitude coordinate (in float), longitude coordinate (in float)
    private void navigateToDestination(String destinationName, double latitude, double longitude){ //this method will be used for navigating to a destination selected in slide out Hamburger menu
        LatLng destinationLatLng = new LatLng(latitude, longitude); //this stores latitude and longitude coordinates of DESTINATION (found by Geocoder) and stores in a LatLng object
        mMap.clear(); //this clears all previously searched (location) GPS markers from map

        getDeviceLocation(15); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
        if (isUserLocatable = false){
            LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

            LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
            //LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        }

        //Add logic to choose either CSUDH center or Device live location as 'start' for navigation (need to add a bool and put it here to see which marker to use for start"

        Marker destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));  //this creates new Marker and instanciates marker object; is colored green


        //mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName)); //add marker where user defined DESTINATION is located
        calculateDirections(destinationMarker); //this method is taking the marker as argument(created from user-inputted search) and creating navigation directions
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));

    }


    // ***CITATION*** the 'Calculate' method below is derived from the following YouTube Tutorial: (Calculating Directions with Google Directions API, Coding with Mitch) https://www.youtube.com/watch?v=f47L1SL5S0o
    private void calculateDirections(Marker marker) { // method accepts a marker object and sends origin + destination directions request to Google. Response can include distance, routes, polyline data, etc.
        Log.d(TAG, "calculateDirections: calculating directions.");
		getDeviceLocation(15);

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
            //getDeviceLocation(); //this method obtains live GPS location. If no live GPS location cannot be obtained by this method, the defaultUserLocation will not be changed at all and default will be CSUDH

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
                Log.d(TAG, "calculateDirections: Steps: " + result.routes[0].legs[0].steps[0].htmlInstructions);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                Log.d(TAG, "calculateDirections: overviewPolyline: " + result.routes[0].overviewPolyline.toString());

                //this following line sends the htmlInstructions to the TextView object (that will display text "turn-by-turn" directions to user).
                // Here it displays Google Maps Directions API-supplied directions, but will possibly be used with our own text based directions later on.
                //Html.fromHtml(result.routes[0].legs[0].steps[0].htmlInstructions).toString();

                //NavigationTextViewObject.setText(Html.fromHtml(result.routes[0].legs[0].steps[0].htmlInstructions).toString()); //this displays google Directions API-supplied routing directions to user.


              /*  for (DirectionsRoute route : result.routes){

                    NavigationTextViewObject.setText(Html.fromHtml(result.routes[0].legs[0].steps[0].htmlInstructions).toString());

                } */


                for (int i=0; i<result.routes.length; i++) //this goes through the "routes" directions array supplied by Google Maps API and prints out the route directions on screen (and in a log)
                {
                    NavigationTextViewObject.setText(Html.fromHtml(result.routes[i].legs[i].steps[i].htmlInstructions).toString());
                    Log.d(TAG, "NavigationTextView: " + Html.fromHtml(result.routes[i].legs[i].steps[i].htmlInstructions).toString());
                }


                    //  NavigationTextViewObject.setText(result.routes[0].legs[0].steps[0].htmlInstructions);


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
                    polyline.setColor(Color.rgb(239,0,56)); // set Polyline to CSUDH yellow color to contrast the burgundy status bar... trying to have a theme going on here! Source for school theme values is on CSUDH website
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


        getDeviceLocation(15); //this method will determine if user is locatable. If not, CSUDH center will be default user location for now (if statement below)
        if (isUserLocatable = false){
            LatLng defaultUserLocation = new LatLng(33.8636406, -118.2549980); //if user GPS location cannot be found (no signal, no permissions etc), set default user Location to center of CSUDh

            LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
            //LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH")); // only show if isUserLocatable == false
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        }


        //LatLng NEWARK = new LatLng(40.714086, -74.228697);
        LatLng csudhSBS = new LatLng(33.8645994, -118.2548179); //this is middle of SBS building; used to place mapOverlay at this exact GPS Coordinates

        LatLng southwest = new LatLng(33.857300, -118.260804); //remove this...testing
        LatLng northeast = new LatLng(33.867629, -118.247800);


		/*BitmapDescriptor floor1 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1); //remove this...testing
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

        /*
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions ();
		groundOverlayOptions.position(csudhSBS, 100, 100 )
				.image( BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1ds)).transparency((float)0.5);

		mMap.addGroundOverlay(groundOverlayOptions);
		//mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(csudhSBS));

*/



        drawMapOverlay(2); //remove this... testing

        getLocationPermission(); //remove this...testing

    }



    //this method will take in a number for what floor map to draw on the map. If a '0' is passed in, then no floor map will be drawn / other existing overlays are to be deleted. Otherwise, can select floors 1,2,3 or 4 (depending on building)
	//this method is to be used so user can select floors manually with buttons
    private void drawMapOverlay(int floorNumberSelection){

        LatLng csudhSBS = new LatLng(33.8645994, -118.2548179); //this is middle of SBS building; used to place mapOverlay at this exact GPS Coordinates

        LatLng southwest = new LatLng(33.8643183, -118.2552353);
        LatLng northeast = new LatLng(33.8648742, -118.2544119);
        LatLngBounds SBS_Bounds = new LatLngBounds(southwest, northeast); //we are creating LatLngBounds object so our jpg image of the floor plans can overlay right on top of the SBS building.
        BitmapDescriptor floor1 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1ds); //creating all of the .jpg files to be ready for map overlaying
        BitmapDescriptor floor2 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor2ds);
        BitmapDescriptor floor3 = BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor3ds);


        //derived from google documentation on GroundOverlays
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions (); //creating new groundOverlay object

        switch(floorNumberSelection){ //based on argument int value; either choose a floor (value 1 or greater) or remove all drawn overlays (value 0)

            case 0:
                //remove any existing overlays on map
                break;
            case 1:
                //REMOVE ANY OVERLAYS BEFORE DRAWING THIS ONE ON THE MAP (FOR MEMORY)
                if (imageOverlaySBS1 != null){
                    imageOverlaySBS1.remove();
                }
                if (imageOverlaySBS2 != null){
                    imageOverlaySBS2.remove();
                }
                if (imageOverlaySBS3 != null) {
                    imageOverlaySBS3.remove();
                }

                //this is old working overlay positioning (using one center coordinate as positioning
                // groundOverlayOptions.position(csudhSBS, 100, 100 )
                //.image( BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1ds)).transparency((float)0.3);

                groundOverlayOptions.positionFromBounds(SBS_Bounds).image(BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor1ds)).transparency((float)0.2).visible(true); //this uses LatLngbounds to position the overlay

                imageOverlaySBS1 = mMap.addGroundOverlay(groundOverlayOptions); //this line actually overlays the created groundOverlay onto the map; also stores it as GroundOverlay

               // mMap.addGroundOverlay(groundOverlayOptions); //remove this...testing   this is old working code to add overlay to map.
                break;

            case 2:
                //REMOVE ANY OVERLAYS BEFORE DRAWING THIS ONE ON THE MAP (FOR MEMORY)
                if (imageOverlaySBS1 != null){
                    imageOverlaySBS1.remove();
                }
                if (imageOverlaySBS2 != null){
                    imageOverlaySBS2.remove();
                }
                if (imageOverlaySBS3 != null) {
                    imageOverlaySBS3.remove();
                }

                //in case 1, I have commented out an older working way to position map overlay using only 1 central GPS Coordinate in middle of building
                groundOverlayOptions.positionFromBounds(SBS_Bounds).image(BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor2ds)).transparency((float)0.2).visible(true); //this uses LatLngbounds to position the overlay (uses 2 GPS coordinates @ corners of buildings for best overlay positioning)



                imageOverlaySBS2 = mMap.addGroundOverlay(groundOverlayOptions);
                // mMap.addGroundOverlay(groundOverlayOptions); //remove this...testing old working code to add overlay to map.
                break;

            case 3:
                //REMOVE ANY OVERLAYS BEFORE DRAWING THIS ONE ON THE MAP (FOR MEMORY)
                //REMOVE ANY OVERLAYS BEFORE DRAWING THIS ONE ON THE MAP (FOR MEMORY)
                if (imageOverlaySBS1 != null){
                    imageOverlaySBS1.remove();
                }
                if (imageOverlaySBS2 != null){
                    imageOverlaySBS2.remove();
                }
                if (imageOverlaySBS3 != null) {
                    imageOverlaySBS3.remove();
                }



                //in case 1, I have commented out an older working way to position map overlay using only 1 central GPS Coordinate in middle of building
                groundOverlayOptions.positionFromBounds(SBS_Bounds).image(BitmapDescriptorFactory.fromResource(R.drawable.sbsfloor3ds)).transparency((float)0.2).visible(true); //this uses LatLngbounds to position the overlay (uses 2 GPS coordinates @ corners of buildings for best overlay positioning)



                imageOverlaySBS3 = mMap.addGroundOverlay(groundOverlayOptions);
                // mMap.addGroundOverlay(groundOverlayOptions); //remove this...testing old working code to add overlay to map.
                break;

            default:

                break;

        }


	}


    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude() );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }


    //method below is to get GPS current location and set global variable defaultUserLocation marker to device current location. Also will determine boolean flag 'isUserLocatble'
    private void getDeviceLocation(final int zoomLevel) { //***CITATION*** this method was derived from the following YouTube Tutorial: Coding With Mitch Get Device Location - [Android Google Maps Course]  link: https://www.youtube.com/watch?v=fPFr0So1LmI
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

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentDeviceLocation, zoomLevel)); //moves map camera to where user is currently located, level 15 zoom

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

