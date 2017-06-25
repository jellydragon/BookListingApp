package com.example.android.booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    private String mTitle;
    private String mAuthors;
    private String mUrl;

    public Book(String title, String authors, String url) {
        mTitle = title;
        mAuthors = authors;
        mUrl = url;
    }

    private Book(Parcel in) {
        mTitle = in.readString();
        mAuthors = in.readString();
        mUrl = in.readString();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthors;
    }

    public String getUrl() {
        return mUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mAuthors);
        out.writeString(mUrl);
    }

}
