package com.example.googlemapstest0918;
import java.util.List;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Camera;
import android.location.Geocoder; // Geocoder is for transforming address --> GPS coordinates (could be used for planting markers on maps for navigation)
import android.location.Address;
import android.os.Bundle;

import android.widget.SearchView; //imported this for SearchView widget support; Website for help on searchviews: https://abhiandroid.com/ui/searchview

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SearchView searchView; //instanciating Searchview object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
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

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // this method changes map to hybrid satellite view
        // Add a marker in Sydney and move the camera //CHANGED TO CSUDH
        LatLng csudh = new LatLng(33.8636406, -118.2549980); //rough coordinates of CSUDH center
        //LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(csudh).title("Marker in CSUDH"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(csudh, 16)); //used to be newLatLng(csudh)
        // used newLatLngZoom method to specify to zoom (level 16) into 'CSUDH' marker by default
        //mMap.moveCamera(CameraUpdateFactory.zoomBy(4, csudh));
    }
}
