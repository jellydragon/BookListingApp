package com.example.android.booklistingapp;

public class Book {

    private String mTitle;
    private String mAuthors;
    private String mUrl;

    public Book(String title, String authors, String url) {
        mTitle = title;
        mAuthors = authors;
        mUrl = url;
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
}
