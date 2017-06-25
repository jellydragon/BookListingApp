package com.example.android.booklistingapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    private final List<Book> books;

    public BookAdapter(Activity context, List<Book> books) {
        super(context, 0, books);
        this.books = books;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        ViewHolder holder;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
            holder = new ViewHolder(listItemView);
            listItemView.setTag(holder);
        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        Book currentBook = getItem(position);
        String title = currentBook.getTitle();
        String author = currentBook.getAuthor();

        holder.titleTextView.setText(title);
        holder.authorTextView.setText(author);

        return listItemView;
    }

    public List<Book> getItems() {
        return books;
    }

    static class ViewHolder {
        TextView titleTextView;
        TextView authorTextView;

        public ViewHolder(@NonNull View view) {
            this.titleTextView = (TextView) view
                    .findViewById(R.id.textview_li_title);
            this.authorTextView = (TextView) view
                    .findViewById(R.id.textview_li_author);
        }
    }
}