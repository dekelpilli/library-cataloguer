package edu.monash.dpil7.librarycataloguer;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static edu.monash.dpil7.librarycataloguer.R.drawable.book;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "BookDB";
    private static final int DATABASE_VERSION = 1;

    //table names
    private static String BOOK_TABLE_NAME = "books";
    private static String CAULFIELD_TABLE_NAME = "caulfield";
    private static String CLAYTON_TABLE_NAME = "clayton";
    private static String PENINSULA_TABLE_NAME = "peninsula";
    private static String FAVOURITES_TABLE_NAME = "favourites";
    private static String WISH_TABLE_NAME = "wish";

    //creation strings for each table
    private static final String CREATE_BOOK_TABLE = "CREATE TABLE " + BOOK_TABLE_NAME;
    private static final String CREATE_CAULFIELD_TABLE = "CREATE TABLE " + CAULFIELD_TABLE_NAME;
    private static final String CREATE_CLAYTON_TABLE = "CREATE TABLE " + CLAYTON_TABLE_NAME;
    private static final String CREATE_PENINSULA_TABLE = "CREATE TABLE " + PENINSULA_TABLE_NAME;
    private static final String CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FAVOURITES_TABLE_NAME;
    private static final String CREATE_WISH_TABLE = "CREATE TABLE " + WISH_TABLE_NAME;

    //column names
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_GENRE = "genre";

    //creation strings for the columns
    private static final String CREATE_COLUMNS = "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_AUTHOR + " TEXT, " +
            COLUMN_GENRE + " TEXT, " +
            COLUMN_MONTH + " TEXT, " +
            COLUMN_YEAR + " INTEGER" +
            ");";



    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    //remove all books from the three library databases
    public void clearLibraries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + CAULFIELD_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CLAYTON_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PENINSULA_TABLE_NAME);

        db.execSQL(CREATE_CAULFIELD_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_CLAYTON_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_PENINSULA_TABLE + CREATE_COLUMNS);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_CAULFIELD_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_CLAYTON_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_PENINSULA_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_FAVOURITES_TABLE + CREATE_COLUMNS);
        db.execSQL(CREATE_WISH_TABLE + CREATE_COLUMNS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BOOK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CAULFIELD_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CLAYTON_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PENINSULA_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FAVOURITES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WISH_TABLE_NAME);

        onCreate(db);
    }

    //add book to given table
    //all books in the database are also in the "books" table
    public void addBook(Book book, String tableName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, book.getName());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_YEAR, book.getReleaseYear());
        values.put(COLUMN_MONTH, book.getReleaseMonth());
        values.put(COLUMN_GENRE, book.getGenre());
        values.put(COLUMN_ID, book.get_id());

        SQLiteDatabase db = this.getWritableDatabase();


        //only add if new
        if(tableName != "books" && !isBookInTable(book, "books")) {
            db.insert("books", null, values); //all values in other tables will also be added to the books table
        }

        else if(!isBookInTable(book, tableName)) {
            db.insert(tableName, null, values);
        }
        db.close();
    }

    //check if book is in a given table.
    //book is considered to be in table if there's a book with matching id, author and title
    public boolean isBookInTable(Book book, String tableName) {
        HashMap<Long, Book> bookHashMap = getAllBooksFromTable(tableName);
        if (bookHashMap.get(book.get_id()) != null) { //can be merged into one if statement, but cleaner this way
            return (bookHashMap.get(book.get_id()).getName().equals(book.getName()) && bookHashMap.get(book.get_id()).getAuthor().equals(book.getAuthor()));
        }
        else {
            return false;
        }
    }

    public void removeBookFromTable(Book book, String tableName) {
        if(isBookInTable(book, tableName)) {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM " + tableName +
            " WHERE " + COLUMN_ID +
            "=" + book.get_id());
        }
    }

    //fetch hashmap with all books in a given table
    //key is the book's id, value is a book object
    public HashMap<Long, Book> getAllBooksFromTable(String tableName) {
        HashMap<Long, Book> books = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        if(cursor.moveToFirst()) {
            do {
                Book book = new Book(cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5));
                books.put(book.get_id(), book);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return books;
    }

}
