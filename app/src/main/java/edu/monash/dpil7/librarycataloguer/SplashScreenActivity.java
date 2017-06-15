package edu.monash.dpil7.librarycataloguer;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

import java.util.ArrayList;
import java.util.Date;

import static android.view.View.GONE;

/**
 * Created by AvocadoCake on 2/06/2017.
 * In addition to being a splash screen to introduce the user to the app, this activity is used to load books from the firebase and add them to the local db
 */


public class SplashScreenActivity extends AppCompatActivity {

    DatabaseHelper db;
    FirebaseHelper fb;
    Button continueButton;
    Intent i;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        db = new DatabaseHelper(this);

        boolean firstTime = (db.getAllBooksFromTable("books").isEmpty());
        TextView howTo = (TextView) findViewById(R.id.howToText);
        if(firstTime) {
            howTo.setText(getHowToText()); //give the user some tips on how to use the app if this is their first time
        }
        else { //hide the how to objects if this isn't the user's first time using the app
            NestedScrollView nsv = (NestedScrollView) findViewById(R.id.nestedScrollView);
            TextView howTitle = (TextView) findViewById(R.id.howToTitle);

            nsv.setVisibility(GONE);
            howTitle.setVisibility(GONE);
            howTo.setVisibility(GONE);
        }

        fb = new FirebaseHelper(SplashScreenActivity.this);
        continueButton = (Button) findViewById(R.id.enterButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
            }
        });



        //below is the code to generate books for firebase. To be commented out unless new books are needed.
        //you can uncomment this to get new books into the firebase, but this will remove all existing books from the firebase

        /*ArrayList<String> libs = new ArrayList<String>() {{
            add("Caulfield Library");
            add("Clayton Library");
            add("Peninsula Library");
        }};

        libSetup(100, 35, libs);*/

        //querying for external permission. We need internal, but one can't be allowed without the other.
        /*if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            ArrayList<Long> ids = new ArrayList<>();
            for (long id : db.getAllBooksFromTable("books").keySet()) {
                ids.add(id);
            }


            ImageDownloader imgDl = new ImageDownloader(SplashScreenActivity.this);
            imgDl.downloadImages(ids);
        }
        else {
            Toast.makeText(this, "Please turn on storage permissions to see cover images!", Toast.LENGTH_LONG).show();
        }*/
    }

    private String getHowToText() {
        return "Swipe across the top on the tabs to see more tabs, and select them to get to other screens" +
                "\n" +
                "\n" +
                "Long click on a book to get more options" +
                "\n" +
                "\n" +
                "Go to Settings->Applications->Permissions and turn Location on to get the most out of the app" +
                "\n" +
                "\n" +
                "When searching, use 'id=x' to search for a book with an id of x";

    }


    //methods called when generating firebase books

    /*private Book generateRandomBook(long id) {
        //creates some dummy data and returns it
        Random random = new Random();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz";


        String name = "";
        while (name.length() < 15) {
            name += alphabet.charAt((int) (random.nextFloat() * alphabet.length()));
        }

        String author = "";
        while (author.length() < 20) {
            author += alphabet.charAt((int) (random.nextFloat() * alphabet.length()));
        }

        String genre = "";
        while (genre.length() <8) {
            genre += alphabet.charAt((int) (random.nextFloat() * alphabet.length()));
        }

        String month = "May";

        Integer year =  1990 + (int) (random.nextFloat() * 27);

        return new Book(id, name, author, genre, month, year);
    }

    //only used for adding files to firebase
    private void libSetup(int bookAmount, int booksPerLib, ArrayList<String> libs) {
        assert bookAmount>=booksPerLib;


        ArrayList<Book> books = new ArrayList<Book>();
        for (int i=0;i<bookAmount;i++) {
            books.add(generateRandomBook((long) i));
        }


        Random rand = new Random();
        int n;
        ArrayList<Book> tempBooks;
        Book nextBook;
        ArrayList<Book> booksToAdd;

        for (String lib : libs) {
            tempBooks = (ArrayList<Book>) books.clone();
            booksToAdd = new ArrayList<Book>();
            for (int i=0; i<booksPerLib;i++) {
                n = rand.nextInt(tempBooks.size());
                booksToAdd.add(tempBooks.get(n));
                tempBooks.remove(n);
            }
            for (Book book : booksToAdd) {
                fb.addBookToLib(lib, book);
            }
        }

    }*/

}
