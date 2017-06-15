package edu.monash.dpil7.librarycataloguer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by AvocadoCake on 6/05/2017.
 */

public class ViewBookActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
    private TextView title;
    private TextView author;
    private TextView release;
    private TextView genre;
    private ImageView cover;

    private Book book;
    private DatabaseHelper dbh;

    private Button findLocs;
    private Button favButton;
    private Button wishButton;


    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.view_book);

        //get any information passed from other activity
        Bundle extras = getIntent().getExtras();
        //make new book from extras
        String bookStr = extras.getString("Book");
        book = new Gson().fromJson(bookStr, Book.class);

        //associate variables with ui elements
        title = (TextView) findViewById(R.id.title);
        author = (TextView) findViewById(R.id.author);
        release = (TextView) findViewById(R.id.releaseDate);
        genre = (TextView) findViewById(R.id.genre);
        favButton = (Button) findViewById(R.id.addToFavButton);
        wishButton = (Button) findViewById(R.id.addToWishButton);
        findLocs = (Button) findViewById(R.id.findLocButton);
        cover = (ImageView) findViewById(R.id.bookCover);

        dbh = new DatabaseHelper(this);


        findLocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewBookActivity.this, MapsActivity.class);
                i.putExtra("Book", new Gson().toJson(book));
                startActivity(i);
            }
        });

        boolean inFav = dbh.isBookInTable(book, "favourites");
        if(inFav) {
            favButton.setText("Remove from favourites");
        }

        final boolean inWish = dbh.isBookInTable(book, "wish");
        if(inWish) {
            wishButton.setText("Remove from wishlist");
        }


        favButton.setOnClickListener(new View.OnClickListener() {
            boolean inFav = dbh.isBookInTable(book, "favourites");
            @Override
            public void onClick(View v) {
                if(inFav) {
                    dbh.removeBookFromTable(book, "favourites");
                    inFav = false;
                    favButton.setText("Add to favourites");
                }
                else {
                    dbh.addBook(book, "favourites");
                    inFav = true;
                    favButton.setText("Remove from favourites");
                }
            }
        });

        wishButton.setOnClickListener(new View.OnClickListener() {
            boolean inWish = dbh.isBookInTable(book, "wish");
            @Override
            public void onClick(View v) {
                if(inWish) {
                    dbh.removeBookFromTable(book, "wish");
                    inWish = false;
                    wishButton.setText("Add to wishlist");
                }
                else {
                    dbh.addBook(book, "wish");
                    inWish = true;
                    wishButton.setText("Remove from wishlist");
                }
            }
        });


        //update screen
        title.setText(book.getName());
        author.setText("by " + book.getAuthor());
        genre.setText("Genre: " + book.getGenre());
        release.setText("Release date: " + book.getReleaseMonth() + ", " + book.getReleaseYear().toString());

        Bitmap image = getImage(book.get_id());
        if (image != null) {
            cover.setImageBitmap(image);
        }

    }


    //gets image from local files
    private Bitmap getImage(long id) {
        String path = this.getFilesDir()+"/"+id+".jpg";
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
