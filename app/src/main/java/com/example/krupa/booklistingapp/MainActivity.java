package com.example.krupa.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;
    private ArrayList<Books> rBooksList;
    private BooksAdapter madapter;
    TextView textNoDataFound;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int NUMBER_OF_RESULTS = 10;
    private static final String GOOGLE_BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final String BOOKS_STATE = "bookList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textNoDataFound = (TextView) findViewById(R.id.text_no_data_found);

        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        SearchView search = (SearchView) findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                activeNetwork = cm.getActiveNetworkInfo();
                // Kick off an {@link AsyncTask} to perform the network request
                if ((activeNetwork != null) && activeNetwork.isConnectedOrConnecting()) {
                    BookAsyncTask task = new BookAsyncTask(query, NUMBER_OF_RESULTS);
                    task.execute();
                    return true;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No internet connection available.  Please connect and try again.", Toast.LENGTH_LONG);
                    toast.show();
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            rBooksList = savedInstanceState.getParcelableArrayList(BOOKS_STATE);
            updateUi();
        } else {
            // hide listview initially
            ListView lv = (ListView) findViewById(R.id.list_view);
            lv.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(BOOKS_STATE, rBooksList);

        super.onSaveInstanceState(outState);
    }


    // Update screen
    private void updateUi() {
        BooksAdapter adapter = new BooksAdapter(this, rBooksList);
        TextView tvInstructions = (TextView) findViewById(R.id.search_instructions);
        tvInstructions.setVisibility(View.GONE);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);

    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Books>> {
        private String mSearchQuery;
        private int mCount;

        public BookAsyncTask(String searchQuery, int count) {
            mSearchQuery = searchQuery.replaceAll(" ", "%20");
            mCount = count;
        }

        @Override
        protected ArrayList<Books> doInBackground(URL... params) {
            URL url = createUrl(GOOGLE_BOOKS_REQUEST_URL, mSearchQuery, mCount);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error when calling doInBackground: ", e);
            }

            ArrayList<Books> booksList = extractItemsFromJson(jsonResponse);

            return booksList;
        }

        @Override
        protected void onPostExecute(ArrayList<Books> books) {
            if (books == null) {
                return;
            }
            rBooksList = books;
            updateUi();
        }

        private URL createUrl(String stringUrl, String searchQuery, int count) {
            URL url = null;

            try {
                url = new URL(stringUrl + "?q=" + searchQuery + "&maxResults=" + count);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL: ", exception);
            }

            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error when calling makeHttpRequest: ", e);
            }

            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private ArrayList<Books> extractItemsFromJson(String booksJson) {
            try {
                JSONObject baseJsonResponse = new JSONObject(booksJson);
                if (!baseJsonResponse.isNull("items")) {
                    JSONArray itemsArray = baseJsonResponse.getJSONArray("items");
                    if (itemsArray.length() > 0) {
                        return Books.fromJson(itemsArray);
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing books JSON results: ", e);
            }
            return null;
        }
    }
}
