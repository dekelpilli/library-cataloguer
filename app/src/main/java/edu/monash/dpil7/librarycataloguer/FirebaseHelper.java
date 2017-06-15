package edu.monash.dpil7.librarycataloguer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringDef;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import static android.content.ContentValues.TAG;

/**
 * Created by AvocadoCake on 19/05/2017.
 */

public class FirebaseHelper {
    private FirebaseDatabase db;

    private DatabaseReference caulRef;
    private DatabaseReference clayRef;
    private DatabaseReference penRef;

    private ArrayList<Book> caulBooks;
    private ArrayList<Book> clayBooks;
    private ArrayList<Book> penBooks;


    private DatabaseHelper dbh;

    private Context myContext;
    //private boolean loaded = false;

    private ProgressDialog pd;

    public FirebaseHelper(Context context) {
        db = FirebaseDatabase.getInstance();
        dbh = new DatabaseHelper(context);

        myContext = context;
        pd = new ProgressDialog(myContext, 0);
        pd.setCanceledOnTouchOutside(false); //don't let user dismiss the progress dialog

        caulBooks = new ArrayList<>();
        clayBooks = new ArrayList<>();
        penBooks = new ArrayList<>();


        if(isInternetConnected() && dbh.getAllBooksFromTable("books").values().size()>0) {
            //clear databases of each library if there is internet. This allows for books to be removed from a library's catalogue without removing it from the app
            //only performs this action if there is an internet connection
            dbh.clearLibraries();
        }
        caulRef = this.initialiseLibReference("Caulfield Library");
        clayRef = this.initialiseLibReference("Clayton Library");
        penRef = this.initialiseLibReference("Peninsula Library");

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

    //check if there is an internet connection
    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) myContext.getSystemService(myContext.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }


    //initliases the database reference for a given library
    //this includes fetching the books from firebase and their cover photos
    //progress dialog closes upon completion
    private DatabaseReference initialiseLibReference(String libName) {
        showProgressDialog("Loading books...");
        final DatabaseReference ret = db.getReference("Libraries").child(libName);
        if (!isInternetConnected()) {
            hideProgressDialog();
            return ret;
        }

        final ImageDownloader imgDler = new ImageDownloader(myContext);
        ret.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!isInternetConnected()) { //if app started with internet connection then lost it
                    hideProgressDialog();
                    return;
                }
                // This method is called once with the initial value and again whenever data at this location is updated.
                Book book;
                for (DataSnapshot bookSnapshot: dataSnapshot.getChildren()) {
                    book = bookSnapshot.getValue(Book.class);
                    imgDler.downloadImage(book.get_id()); //download image for this book
                    addBookToLocalLib(ret.getKey(), book); //adds to local list and db
                }
                hideProgressDialog();

                Log.d(TAG, "Value has been recorded");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.");
            }
        });

        return ret;
    }

    //adds book to the appropriate library in MySQL
    private void addBookToLocalLib(String lib, Book book)  {
        switch(lib) {
            case("Caulfield Library"):
                dbh.addBook(book, "caulfield");
                break;
            case("Clayton Library"):
                dbh.addBook(book, "clayton");
                break;
            case("Peninsula Library"):
                dbh.addBook(book, "peninsula");
                break;
            default:
                return;
        }
    }

    //only gets used when adding books to the firebase database
    public void addBookToLib(String lib, Book book) {

        DatabaseReference ref;
        switch(lib) {
            case("Caulfield Library"):
                ref = caulRef;
                break;
            case("Clayton Library"):
                ref = clayRef;
                break;
            case("Peninsula Library"):
                ref = penRef;
                break;
            default:
                return;
        }

        //book name and author to be used as 'primary keys'
        ref.child(String.valueOf(book.get_id())).setValue(book);
    }

}
