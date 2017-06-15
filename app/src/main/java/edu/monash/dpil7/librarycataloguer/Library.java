package edu.monash.dpil7.librarycataloguer;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by AvocadoCake on 19/05/2017.
 */

//similar to the Book class, this class' only purpose it to hold information on Libraries
public class Library {
    private String libName;

    private double longitude; //in degrees, negative is west
    private double latitude; //in degrees, negative is south

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getLibName() {return libName;    }

    public Library(String name, double libLong, double libLat, ArrayList<Book> bookList) {
        this.libName = name;
        this.longitude = libLong;
        this.latitude = libLat;
    }
}
