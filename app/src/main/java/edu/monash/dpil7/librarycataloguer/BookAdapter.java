package edu.monash.dpil7.librarycataloguer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.util.ArrayList;
import java.util.List;

//adapter to be used by all listviews containing books in this app.
public class BookAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Book> books;
    private ArrayList<Book> filteredBooks;
    private int markedForDelete = -1;

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.context = context;
        this.books = books; //a list of all books in a given library or user list
        this.filteredBooks = books; //a list of all books that fit the search criteria
    }

    public static class ViewHolder {
        TextView bookName;
        TextView author;
        ImageView bookCover;
    }

    @Override
    public int getCount() {
        return filteredBooks.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredBooks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return filteredBooks.get(position).get_id();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;

        // Check if the view has been created for the row. If not, inflate it
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Reference list item layout here
            view = inflater.inflate(R.layout.book_item, null);

            // Setup ViewHolder and attach to view
            vh = new ViewHolder();
            vh.bookName = (TextView) view.findViewById(R.id.bookName);
            vh.author = (TextView) view.findViewById(R.id.author);
            vh.bookCover = (ImageView) view.findViewById(R.id.bookCover);

            view.setTag(vh);
        } else {
            // View has already been created, fetch our ViewHolder
            vh = (ViewHolder) view.getTag();
        }

        // Assign values to the TextViews using the Book object
        if(filteredBooks.size() > i) {
            //put book information in UI
            vh.bookName.setText(filteredBooks.get(i).getName());
            vh.author.setText(filteredBooks.get(i).getAuthor());

            //if available, update book cover. Else, book cover image stays as default.
            Bitmap image = getImage(filteredBooks.get(i).get_id());
            if (image != null) {
                vh.bookCover.setImageBitmap(image);
            }


        }
        // Return the completed View of the row being processed
        return view;
    }


    //gets the image for a book with a certain id
    private Bitmap getImage(long id) {
        String path = context.getFilesDir()+"/"+id+".jpg"; //stored in [default location for this app]/[idNumber].jpg
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f)); //get image from local storage
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean applyFilter(CharSequence constraint) {
        //empty out filteredBooks
        filteredBooks = new ArrayList<Book>(books);
        constraint = constraint.toString().toLowerCase();


        if(constraint != null && constraint.toString().length()>0) {
            //repopulate filteredBooks
            if(constraint.toString().indexOf("id=")==0 && constraint.toString().length()>3) { //enables the command to search for books with "id=x" instead of book name/author
                String id = constraint.toString().replace("id=", "");
                ArrayList<Book> found = new ArrayList<Book>();
                for (Book book : books) {
                    if (((Long) book.get_id()).toString().equals(id)) {
                        found.add(book); //check for matching book ids
                    }
                }
                filteredBooks = found;
            }
            else {
                ArrayList<Book> found = new ArrayList<Book>();
                for (Book book : books) {
                    if (book.getName().toLowerCase().contains(constraint)) {
                        found.add(book); //check for matching book titles
                    } else if (book.getAuthor().toLowerCase().contains(constraint)) {
                        found.add(book); //check for matching authors
                    }
                }
                filteredBooks = found;
            }
        }
        else {
            //if no filter, all books fit the criteria
            filteredBooks = books;
        }

        notifyDataSetChanged();
        return true;
    }

    public void updateList(ArrayList<Book> newBooks, CharSequence constraint) {
        books = newBooks;
        applyFilter(constraint);
        notifyDataSetChanged();
    }

}
