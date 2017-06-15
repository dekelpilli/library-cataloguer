package edu.monash.dpil7.librarycataloguer;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Library> libs;
    private DatabaseHelper db;
    private Book book;

    private boolean inCaul;
    private boolean inClay;
    private boolean inPen;
    private ProgressDialog pd;

    private TextView libDistanceText;

    //private LocationListener locListen;
    private double longitude;
    private double latitude;

    Library caulLib;
    Library clayLib;
    Library penLib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        pd = new ProgressDialog(this, 0);
        libs = new ArrayList<>();

        //get book from intent
        Bundle extras = getIntent().getExtras();
        String bookStr = extras.getString("Book");
        book = new Gson().fromJson(bookStr, Book.class);

        //check which libraries the book is in
        db = new DatabaseHelper(MapsActivity.this);
        inCaul = db.isBookInTable(book, "caulfield");
        inClay = db.isBookInTable(book, "clayton");
        inPen = db.isBookInTable(book, "peninsula");

        caulLib = new Library("Caulfield Library", 145.0443, -37.8770, new ArrayList<Book>());
        clayLib = new Library("Clayton Library", 145.1300, -37.9150, new ArrayList<Book>());
        penLib = new Library("Peninsula Library", 145.1344, -38.1536, new ArrayList<Book>());

        if (inCaul) {
            libs.add(caulLib);
        }
        if (inClay) {
            libs.add(clayLib);
        }
        if (inPen) {
            libs.add(penLib);
        }

        libDistanceText = (TextView) findViewById(R.id.libDistText);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * adds library markers where book is available, gets user location and calculates distances*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        showProgressDialog("Preparing map...");
        mMap = googleMap;
        LatLng libLoc;
        ArrayList<LatLng> locs = new ArrayList<>();
        for (Library library : libs) {
            libLoc = new LatLng(library.getLatitude(), library.getLongitude());
            mMap.addMarker(new MarkerOptions().position(libLoc).title(library.getLibName()));
            locs.add(libLoc);
        }
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) { //checks location permissions
            mMap.setMyLocationEnabled(true);//add button to go to current location, and icon on map with user location
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false)); //get user location
            if(location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            else{
                if(libs.size()>0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(libs.get(0).getLatitude(), libs.get(0).getLongitude()), 10.0f)); //if no location services, move camera to first available library
                }
                else {
                    libDistanceText.setText(book.getName() + " is currently out of stock in all of our libraries :(");
                }
            }
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude) , 10.0f) ); //sets default camera position to be on the user's location, and sets appropriate zoom

            DecimalFormat df = new DecimalFormat("#.##"); //round to 2 decimal places
            df.setRoundingMode(RoundingMode.CEILING);

            //show distances in km for each available library
            libDistanceText.setText("");
            if (inCaul) {
                libDistanceText.setText("Caulfield: " + df.format((distance(latitude, clayLib.getLatitude(), longitude, caulLib.getLongitude())) / 1000) + "km\n");
            }
            if (inClay) {
                libDistanceText.setText(libDistanceText.getText() + "Clayton: " + df.format((distance(latitude, clayLib.getLatitude(), longitude, clayLib.getLongitude())) / 1000) + "km\n");
            }
            if (inPen) {
                libDistanceText.setText(libDistanceText.getText() + "Peninsula: " + df.format((distance(latitude, clayLib.getLatitude(), longitude, penLib.getLongitude())) / 1000) + "km");
            }
            if(!inCaul && !inPen && !inClay) {
                libDistanceText.setText(book.getName() + " is currently out of stock in all of our libraries :(");
            }
        }
        else {
            if(libs.size()>0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(libs.get(0).getLatitude(), libs.get(0).getLongitude()), 10.0f)); //if no location services, move camera to first available library
                libDistanceText.setText("Please turn on location permissions to see your distance from the libraries");
            }
            else {
                libDistanceText.setText(book.getName() + " is currently out of stock in all of our libraries :(");
            }
            Toast.makeText(this, "Please turn on location permissions!", Toast.LENGTH_LONG).show(); //ask user to turn on location services

        }
        hideProgressDialog();

        mMap.setMinZoomPreference(8);
        mMap.setMaxZoomPreference(17);
    }


    private void showProgressDialog(String message) {
        pd.setMessage(message);
        pd.show();
    }

    private void hideProgressDialog() {
        if(pd.isShowing()) {
            pd.hide();
        }
    }

    //calculates distance as the crow flies base on two pairs of latitude and longitude values
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}
