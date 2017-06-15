package edu.monash.dpil7.librarycataloguer;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static android.content.ContentValues.TAG;

/**
 * Created by AvocadoCake on 8/06/2017.
 */

public class ImageDownloader {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    private Context myContext;
    private ProgressDialog pd;


    public ImageDownloader(Context context) {
        //creates storage reference to tell the program where to fetch the images from
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://fit3027-dpil7.appspot.com");
        myContext = context;

        pd = new ProgressDialog(myContext, 0);
        pd.setCanceledOnTouchOutside(false); //don't let user dismiss the progress dialog

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        showProgressDialog("Authenticating...");
        auth.signInAnonymously(); //necessary for accessing storage
        hideProgressDialog();
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

    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) myContext.getSystemService(myContext.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    public void downloadImage(final long id) {
        if(!isInternetConnected()) {
            return; //if no internet connection, don't try to download. If this image also doesn't already exist, the app will use a placeholder image.
        }


        final String path = myContext.getFilesDir()+"/"+id+".jpg";
        File file = new File(path);
        if(file.exists() && file.length()>5) { //if file already exists, don't download
            return;
        }
        showProgressDialog("Downloading images...");
        storageRef.child("covers/"+id+".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                try {
                    //write file
                    FileOutputStream fos=new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();
                    //output success message to UI, currently disabled
                    //Toast.makeText(myContext, "Image successly downloaded", Toast.LENGTH_SHORT).show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //output error to UI, currently disabled
                    //Toast.makeText(myContext, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    //output error to UI, currently disabled
                    Toast.makeText(myContext, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //output error, currently disabled
                Toast.makeText(myContext, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        hideProgressDialog();
    }
}
