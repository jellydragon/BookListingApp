package com.example.android.booklistingapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Book currentBook = getItem(position);

        String title = currentBook.getTitle();
        String author = currentBook.getAuthor();

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.textview_li_title);
        titleTextView.setText(title);

        TextView authorTextView = (TextView) listItemView.findViewById(R.id.textview_li_author);
        authorTextView.setText(author);

        return listItemView;
    }
}