package edu.monash.dpil7.librarycataloguer;

import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;

//contains information about a book. All this does is hold information and have it changed by other classes.
public class Book implements Parcelable {

    protected Book(Parcel in) {
        _id = in.readLong(); //if we were recording real books instead of randomly generated ones, this would be the book's ISBN, not its id in our database.
        name = in.readString();
        author = in.readString();
        genre = in.readString();
        releaseMonth = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseMonth() {
        return releaseMonth;
    }

    public void setReleaseMonth(String releaseMonth) {
        this.releaseMonth = releaseMonth;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }


    private String name;
    private String author;
    private String genre;
    private String releaseMonth;
    private Integer releaseYear;
    private long _id;

    Book(long id, String name, String author, String genre, String month, Integer year) {
        this._id = id;
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.releaseMonth = month;
        this.releaseYear = year;
    }

    Book() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(genre);
        dest.writeString(releaseMonth);
    }
}
