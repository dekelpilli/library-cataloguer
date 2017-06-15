package edu.monash.dpil7.librarycataloguer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import static android.R.id.tabhost;
import static edu.monash.dpil7.librarycataloguer.R.id.aboutText;
import static edu.monash.dpil7.librarycataloguer.R.id.aboutTitle;
import static edu.monash.dpil7.librarycataloguer.R.id.howtoText;
import static edu.monash.dpil7.librarycataloguer.R.id.tabHost;


public class MainActivity extends AppCompatActivity {


    // http://androidexample.com/LAYOUTS/index.php?view=article_discription&aid=103&aaid=125

    private android.widget.SearchView searchView;

    private DatabaseHelper db;

    private Intent i;

    final private static String ALL_BOOKS_TABLE_NAME = "books";
    final private static String FAV_TABLE_NAME = "favourites";
    final private static String WISH_TABLE_NAME = "wish";


    //tab 1
    private BookAdapter allBooksAdapter;
    private ListView bookView;
    private ArrayList<Book> allBooks;
    private SearchView searchAll;

    //tab 2
    private BookAdapter favAdapter;
    private ListView favView;
    private ArrayList<Book> favBooks;
    private SearchView searchFavs;

    //tab 3
    private BookAdapter wishAdapter;
    private ListView wishView;
    private ArrayList<Book> wishBooks;
    private SearchView searchWishes;

    //tab 4
    TextView aboutContentTv;
    TextView aboutTitleTv;
    NestedScrollView nsv;

    //tab5
    TextView howTitle;
    TextView howText;
    NestedScrollView howNsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        //sets db
        db = new DatabaseHelper(this);

        final TabHost host = (TabHost)findViewById(tabHost);
        host.setup();

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                //clear search bars
                if (searchWishes != null){
                    searchWishes.setQuery("", false);
                }
                if (searchFavs != null ) {
                    searchFavs.setQuery("", false);
                }
                if (searchAll != null) {
                    searchAll.setQuery("", false);
                }

                //show newly selected tab and update information for it
                switch(tabId) {
                    case("Find Book"):
                        if(allBooksAdapter != null) {
                            allBooksAdapter.updateList(fetchBooks(ALL_BOOKS_TABLE_NAME), searchAll.getQuery());
                            updateListView(allBooksAdapter);
                        }

                        break;
                    case("My Favourites"):
                        favAdapter.notifyDataSetChanged();
                        favAdapter.updateList(fetchBooks(FAV_TABLE_NAME), searchFavs.getQuery());
                        updateListView(favAdapter);
                        break;
                    case("My Wishlist"):
                        wishAdapter.notifyDataSetChanged();
                        wishAdapter.updateList(fetchBooks(WISH_TABLE_NAME), searchWishes.getQuery());
                        updateListView(wishAdapter);
                        break;
                    case("About"):
                        setTitle("About LibraryCataloguer");
                        break;
                    case("How To"):
                        setTitle("How to use LibraryCataloguer");
                        break;
                    default:
                        setTitle("Library Cataloguer"); //should never get here
                        break;
                }
            }
        });

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Find Book");
        spec.setContent(R.id.findBook);
        spec.setIndicator("Find Book");
        host.addTab(spec);

        //associate with UI items
        bookView = (ListView) findViewById(R.id.bookSearchView);
        searchAll = (SearchView) findViewById(R.id.bookSearch);

        //populate listview
        allBooks = fetchBooks(ALL_BOOKS_TABLE_NAME);
        allBooksAdapter = new BookAdapter(this, allBooks);
        bookView.setAdapter(allBooksAdapter);
        updateListView(allBooksAdapter);

        //implement clicks
        bookView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int index, long id) {
                Book result = (Book) bookView.getAdapter().getItem(index);
                i = new Intent(MainActivity.this, ViewBookActivity.class);
                i.putExtra("Book", new Gson().toJson(result));

                startActivity(i);
            }
        });


        //long click listener for the first tab
        //brings up options to quickly perform actions with the book without needing to view book
        bookView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                final Book result = (Book) bookView.getAdapter().getItem(pos);
                final boolean inFav = db.isBookInTable(result, FAV_TABLE_NAME);
                String favStr = "Add to favourites";
                if(inFav) {
                    favStr = "Remove from favourites";
                }

                final boolean inWish = db.isBookInTable(result, WISH_TABLE_NAME);
                String wishStr = "Add to wishlist";
                if(inWish) {
                    wishStr = "Remove from wishlist";
                }


                CharSequence options[] = new CharSequence[] {favStr, wishStr, "View locations", "See more details", "Share on social media"};


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("What do you want to do?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch(which) {
                            case(0):
                                if(inFav) {
                                    db.removeBookFromTable(result, FAV_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been removed from your favourites", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    db.addBook(result, FAV_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been added to your favourites", Toast.LENGTH_SHORT).show();
                                }
                                favAdapter.updateList(fetchBooks(FAV_TABLE_NAME), searchAll.getQuery());
                                break;
                            case(1):
                                if(inWish) {
                                    db.removeBookFromTable(result, WISH_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been removed from your wishlist", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    db.addBook(result, WISH_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been added to your wishlist", Toast.LENGTH_SHORT).show();
                                }
                                wishAdapter.updateList(fetchBooks(WISH_TABLE_NAME), searchAll.getQuery());
                                break;
                            case(2):
                                i = new Intent(MainActivity.this, MapsActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(3):
                                i = new Intent(MainActivity.this, ViewBookActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(4):
                                String message = "Check out this awesome book! It's called " + result.getName() + " and it's written by my favourite author, " + result.getAuthor() + "!";
                                i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/plain");
                                i.putExtra(Intent.EXTRA_TEXT, message);

                                startActivity(Intent.createChooser(i, "Tell your friends about " + result.getName()));
                                break;
                        }
                    }
                });
                builder.show();

                Log.v("long clicked","pos: " + pos);

                return true;
            }
        });

        //implement search
        searchAll.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //submit does nothing to the textView as we already update on every change
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                allBooksAdapter.applyFilter(searchAll.getQuery()); //apply filter based on updated query

                return true;
            }
        });


        //Tab 2
        spec = host.newTabSpec("My Favourites");
        spec.setContent(R.id.myFavs);
        spec.setIndicator("My Favourites");
        host.addTab(spec);


        favView = (ListView) findViewById(R.id.favView);
        favBooks = fetchBooks(FAV_TABLE_NAME);
        searchFavs = (SearchView) findViewById(R.id.favSearch);

        favAdapter = new BookAdapter(this, favBooks);

        favView.setAdapter(favAdapter);
        updateListView(favAdapter);

        favView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int index, long id) {
                Book result = (Book) favView.getAdapter().getItem(index);
                i = new Intent(MainActivity.this, ViewBookActivity.class);
                i.putExtra("Book", new Gson().toJson(result));

                startActivity(i);
            }
        });

        favView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                final Book result = (Book) favView.getAdapter().getItem(pos);
                final boolean inWish = db.isBookInTable(result, WISH_TABLE_NAME);
                String wishStr = "Add to wishlist";
                if(inWish) {
                    wishStr = "Remove from wishlist";
                }

                CharSequence options[] = new CharSequence[] {"Remove from favourites", wishStr, "View locations", "See more details", "Share on social media"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("What do you want to do?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch(which) {
                            case(0):
                                db.removeBookFromTable(result, FAV_TABLE_NAME);
                                favAdapter.updateList(fetchBooks(FAV_TABLE_NAME), searchFavs.getQuery());
                                updateListView(favAdapter);
                                break;
                            case(1):
                                if(inWish) {
                                    db.removeBookFromTable(result, WISH_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been removed from your wishlist", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    db.addBook(result, WISH_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been added to your wishlist", Toast.LENGTH_SHORT).show();
                                }
                                wishAdapter.updateList(fetchBooks(WISH_TABLE_NAME), searchFavs.getQuery());
                                break;
                            case(2):
                                i = new Intent(MainActivity.this, MapsActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(3):
                                i = new Intent(MainActivity.this, ViewBookActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(4):
                                String message = "I really enjoyed " + result.getName() + " by " + result.getAuthor() + "! You should read it, too!";
                                i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/plain");
                                i.putExtra(Intent.EXTRA_TEXT, message);

                                startActivity(Intent.createChooser(i, "Tell your friends how much you enjoyed " + result.getName()));
                                break;
                        }
                    }
                });
                builder.show();

                Log.v("long clicked","pos: " + pos);

                return true;
            }
        });

        //triggers every time the user changes the text in the searchAll box
        searchFavs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //submit does nothing to the textView as we already update on every change
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                favAdapter.applyFilter(searchFavs.getQuery()); //apply filter based on updated query

                return true;
            }
        });

        //Tab 3
        spec = host.newTabSpec("My Wishlist");
        spec.setContent(R.id.myWishes);
        spec.setIndicator("My Wishlist");
        host.addTab(spec);


        //associate with UI items
        wishView = (ListView) findViewById(R.id.wishView);
        searchWishes = (SearchView) findViewById(R.id.wishSearch);

        //populate listview
        wishBooks = fetchBooks(WISH_TABLE_NAME);
        wishAdapter = new BookAdapter(this, wishBooks);
        wishView.setAdapter(wishAdapter);
        updateListView(wishAdapter);

        //implement clicks
        wishView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int index, long id) {
                Book result = (Book) wishView.getAdapter().getItem(index);
                i = new Intent(MainActivity.this, ViewBookActivity.class);
                i.putExtra("Book", new Gson().toJson(result));

                startActivity(i);
            }
        });

        wishView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                final Book result = (Book) wishView.getAdapter().getItem(pos);
                final boolean inFav = db.isBookInTable(result, FAV_TABLE_NAME);
                String favStr = "Add to favourites";
                if(inFav) {
                    favStr = "Remove from favourites";
                }


                CharSequence options[] = new CharSequence[] {"Remove from wishlist", favStr, "View locations","See more details", "Share on social media"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("What do you want to do?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch(which) {
                            case(0):
                                db.removeBookFromTable(result, WISH_TABLE_NAME);
                                wishAdapter.updateList(fetchBooks(WISH_TABLE_NAME), searchWishes.getQuery());
                                updateListView(wishAdapter);
                                break;
                            case(1):
                                if(inFav) {
                                    db.removeBookFromTable(result, FAV_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been removed from your favourites", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    db.addBook(result, FAV_TABLE_NAME);
                                    Toast.makeText(MainActivity.this, result.getName() + " has been added to your favourites", Toast.LENGTH_SHORT).show();
                                }
                                favAdapter.updateList(fetchBooks(FAV_TABLE_NAME), searchWishes.getQuery());
                                break;
                            case(2):
                                i = new Intent(MainActivity.this, MapsActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(3):
                                i = new Intent(MainActivity.this, ViewBookActivity.class);
                                i.putExtra("Book", new Gson().toJson(result));
                                startActivity(i);
                                break;
                            case(4):
                                String message = "I really wish I had " + result.getName() + " by " + result.getAuthor() + "!";
                                i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/plain");
                                i.putExtra(Intent.EXTRA_TEXT, message);

                                startActivity(Intent.createChooser(i, "Tell your friends how much you want " + result.getName()));
                                break;
                        }
                    }
                });
                builder.show();

                Log.v("long clicked","pos: " + pos);

                return true;
            }
        });

        //implement search
        searchWishes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //submit does nothing to the textView as we already update on every change
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                wishAdapter.applyFilter(searchWishes.getQuery()); //apply filter based on updated query

                return true;
            }
        });

        //tab 4
        spec = host.newTabSpec("About");
        spec.setContent(R.id.about);
        spec.setIndicator("About");
        host.addTab(spec);


        aboutContentTv = (TextView) findViewById(aboutText);
        aboutTitleTv = (TextView) findViewById(aboutTitle);
        aboutContentTv.setText(getAboutText());
        nsv = (NestedScrollView) findViewById(R.id.nestedScroll);

        //setAboutVisibility(false);



        //tab 5
        spec = host.newTabSpec("How to use");
        spec.setContent(R.id.howTo);
        spec.setIndicator("How to use");
        host.addTab(spec);

        howText = (TextView) findViewById(howtoText);
        howText.setText(getHowToText());


        //set title to tab 1's title
        setBookCount(allBooksAdapter);
    }


    @Override
    public void onBackPressed() {
        //disable the back button on this page
        //some apps opt to exit if the back button is pressed on the main activity, but I felt that it makes it too easy to exit accidentally
    }

    private void updateListView(BookAdapter adapter) {
        adapter.notifyDataSetChanged();
        setBookCount(adapter);
    }

    //sets the title to show the amount of books in the given listview
    private void setBookCount(BookAdapter adapter){
        setTitle("Books in list: " + adapter.getCount());
    }

    //gets an arraylist of books from a given table in the SQL
    private ArrayList<Book> fetchBooks(String tableName) {
        ArrayList<Book> books = new ArrayList<>();
        for (Book book : db.getAllBooksFromTable(tableName).values()) {
            books.add(book);
        }
        return books;
    }


    //seperated from initialiser for readability
    private String getAboutText() {
        return "Scrolling tabhost:\n" +
                "https://stackoverflow.com/questions/11904806/adding-scroll-in-tabhost-in-android \n" +
                "\n" +
                "\n" +
                "Getting location:\n" +
                "https://stackoverflow.com/questions/17591147/how-to-get-current-location-in-android \n" +
                "\n" +
                "\n" +
                "Calculating distances:\n" +
                "https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi\n" +
                "\n" +
                "\n" +
                "Saving images:\n" +
                "https://stackoverflow.com/questions/19462213/android-save-images-to-internal-storage\n" +
                "\n" +
                "https://github.com/sodhankit/Firebase-Storage-Upload-Download/blob/master/ViewDownload/ViewDownloadActivity.java\n" +
                "\n" +
                "\n" +
                "Using firebase database & storage:\n" +
                "https://firebase.google.com/docs/database/android/start/ \n" +
                "\n" +
                "https://firebase.google.com/docs/storage/android/start\n" +
                "\n" +
                "\n" +
                "Database manipulation:\n" +
                "https://stackoverflow.com/questions/9810430/get-database-path\n" +
                "\n" +
                "Clickable links:\n" +
                "https://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable\n"+
                "\n"+
                "\n" +
                "Popup with options:\n"+
                "https://stackoverflow.com/questions/16389581/android-create-a-popup-that-has-multiple-selection-options";
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
}
