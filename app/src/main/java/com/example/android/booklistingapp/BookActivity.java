package com.example.android.booklistingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Book>> {

    public static final String LOG_TAG = Book.class.getName();

    /**
     * Constant value for the book loader ID.
     */
    private static final int BOOK_LOADER_ID = 1;

    /**
     * Base URL for fetching data from the Google Books API (without the query)
     */
    private static final String GOOGLE_BOOKS_BASE_URL =
            "https://www.googleapis.com/books/v1/volumes?q=";

    /**
     * URL ending that specifies the max number of entries to return
     */
    private static final String GOOGLE_BOOKS_URL_ENDING = "&maxResults=40";

    /**
     * Adapter for the list of books
     */
    private BookAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Progress bar
     */
    private ProgressBar mProgressBar;

    /**
     * Search query field
     */
    private EditText mQueryField;

    /**
     * ListView with books
     */
    private ListView bookListView;

    /**
     * Book list instance state
     */
    private Parcelable mListInstanceState;

    /**
     * List of books to persist between activities
     */
    private ArrayList<Parcelable> mBooks;

    /**
     * Helper method for hiding the keyboard
     */
    private static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Retrieve saved data if available
        if (savedInstanceState != null) {
            mListInstanceState = savedInstanceState.getParcelable("book_list");
            mBooks = savedInstanceState.getParcelableArrayList("books");
        }

        // Set touch listeners on views that will hide the keyboard when not needed
        setupUI(findViewById(R.id.main_layout));
        // Find a reference to the {@link ListView} in the layout
        bookListView = (ListView) findViewById(R.id.list);
        // Set empty view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_text_view);
        bookListView.setEmptyView(mEmptyStateTextView);
        // Hide progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // Search query Edit Text
        mQueryField = (EditText) findViewById(R.id.text);

        // Set empty state text to display the intro text
        if (connectionOk()) {
            mEmptyStateTextView.setText(R.string.intro_text);
        } else {
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        // Restore books data if available
        if (mBooks != null && !mBooks.isEmpty()) {
            mAdapter = new BookAdapter(this, (List) mBooks);
        } else {
            // Otherwise create a new adapter that takes an empty list of books as input
            mAdapter = new BookAdapter(this, new ArrayList<Book>());
        }

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(mAdapter);

        // Restore position
        if (mListInstanceState != null) {
            bookListView.onRestoreInstanceState(mListInstanceState);
        }

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected book.
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (connectionOk()) {
                    // Find the current book that was clicked on
                    Book currentBook = mAdapter.getItem(position);
                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri bookUri = Uri.parse(currentBook.getUrl());
                    // Create a new intent to view the book URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);
                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                } else {
                    Toast.makeText(BookActivity.this, "No Internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        final ImageButton mSearchButton = (ImageButton) findViewById(R.id.btn_search);
        // Set click listener on search button
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mAdapter.clear();
                mQueryField.setCursorVisible(false);
                populateUI(mQueryField.getText().toString());
            }
        });

        // Touch listener that shows cursor when the query Edit Text is touched
        mQueryField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mQueryField.setCursorVisible(true);
                return false;
            }
        });

        // Perform the search when the "Done" key is pressed
        mQueryField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard(BookActivity.this);
                    mSearchButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Load data and display it
     */
    private void populateUI(String query) {
        if (connectionOk()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getSupportLoaderManager();
            if (!query.equals("")) {
                String mRequestUrl = buildUrl(query);
                Bundle args = new Bundle();
                args.putString("url", mRequestUrl);
                if (loaderManager.getLoader(BOOK_LOADER_ID) == null) {
                    // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                    // the bundle. Pass in the BookActivity activity for the LoaderCallbacks parameter
                    loaderManager.initLoader(BOOK_LOADER_ID, args, BookActivity.this);
                } else {
                    // Restart the loader
                    loaderManager.restartLoader(BOOK_LOADER_ID, args, BookActivity.this);
                }
            }
        } else {
            // Set empty state text to display "No internet connection."
            mEmptyStateTextView.setText(R.string.no_internet_connection);
            // Hide progress bar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle args) {
        // Change empty state text
        mEmptyStateTextView.setText(R.string.loading);
        // Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);
        return new BookLoader(this, args.getString("url"));
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
        } else {
            // Set empty state text to display "No books found."
            mEmptyStateTextView.setText(R.string.no_books_found);
        }
        // Hide progress bar
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // persist adapter items
        List<Book> books = mAdapter.getItems();
        outState.putParcelableArrayList("books", new ArrayList<>(books));

        // persist position
        outState.putParcelable("book_list", bookListView.onSaveInstanceState());

    }

    private boolean connectionOk() {
        // Check if the device is connected to the Internet
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Build a URL string from user input
     */
    private String buildUrl(String query) {
        String url = GOOGLE_BOOKS_BASE_URL;
        try {
            url += URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("BookActivity", "Error creating URL");
        }
        url += GOOGLE_BOOKS_URL_ENDING;
        return url;
    }

    /**
     * Set up touch listener for non-text box views to hide keyboard and cursor
     */
    private void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(BookActivity.this);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}