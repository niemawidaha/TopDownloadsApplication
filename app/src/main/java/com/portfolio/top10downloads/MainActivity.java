package com.portfolio.top10downloads;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCacheUrl = "INVALIDATESD";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // determine if the bundle has been saved
        if(savedInstanceState != null){
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }

        // Connect data to list view"
        listApps = findViewById(R.id.xmlListView);
        // Async Task: Perform download on a different thread
        // - the UI thread will reserve the updates

       downloadURL(String.format(feedUrl,feedLimit));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // retrieve the menu from the view!
        getMenuInflater().inflate(R.menu.feeds_menu, menu);

        if(feedLimit == 10){
            menu.findItem(R.id.menuTop10).setChecked(true);
        } else {
            menu.findItem(R.id.menuTop25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // utilize the id to retrieve the specific menu item
        int id = item.getItemId();

        switch(id){
            case R.id.menuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menuTop10:
            case R.id.menuTop25:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35-feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: "+ item.getTitle() + "setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + "feedLimit unchanged.");
                }
                break;
            case R.id.menuRefresh:
                feedCacheUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        downloadURL(String.format(feedUrl,feedLimit));
        return true;
    }

    private void downloadURL(String feedUrl) {

        if(!feedUrl.equals(feedCacheUrl)){
            // create instance of download data:
            Log.d(TAG, "onCreate: Starting AsyncTask with Download Data");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);

            // update string
            feedCacheUrl = feedUrl;
            Log.d(TAG, "onCreate: Download complete");
        } else {
            Log.d(TAG, "downloadURL: URL NOT CHANGED");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        // before:
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT,feedLimit);

        // this saves the bundle you must add data before this is called:
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // after - retrieve the data

    }

// INNER CLASS: FOR ASYNC TASK
    // String -> url is the string of the RSS feed
    // Void   -> No progress bar update (Gives user indication of the progress of data download)
    // String -> the result received
    private class DownloadData extends AsyncTask<String,Void,String>
    {

        private static final String TAG = "DownloadData";

        // Run on UI thread:
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        // Run on UI thread: after executing
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);

            // initialize the data stream
            ParseTop10Applications parseTop10Applications = new ParseTop10Applications();
            parseTop10Applications.parse(s);

            // send data to the array adapter:
            RSSFeedAdapter rssFeedAdapter = new RSSFeedAdapter(MainActivity.this, R.layout.list_record,parseTop10Applications.getApplications());

            // pass this adapter to the list
            listApps.setAdapter(rssFeedAdapter);
        }

        // Does the processing of data in the background
        // Strings... -> Several URL's can be passed
        // - It will be better to create multiple instances of the download data class
        @Override
        protected String doInBackground(String... strings) {

            // Retrieve the data from the RSS feed
            Log.d(TAG, "doInBackground - starts with " + strings[0]);

            String rssFeed = downloadXML(strings[0]);

            if(rssFeed == null){
                Log.e(TAG,"doInBackGround: Error downloading");
            }
            return rssFeed;
        }

        // Runs on the background thread:
        private String downloadXML(String urlPath) {

            // Open a HTTPConnection to access the data from the internet:
            // BufferedReader - Data is read from an input stream from the buffer of the system
            StringBuilder xmlResult = new StringBuilder();

            try{
                URL url = new URL(urlPath);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                int response = httpURLConnection.getResponseCode();
                Log.d(TAG, "downloadXML: the response code was " + response);

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];

                while(true){
                    charsRead = bufferedReader.read(inputBuffer);

                    if(charsRead < 0){
                        break; // this is used to get outside of the loop once there's nothing left to read
                    }

                    if(charsRead > 0){
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charsRead));
                    }
                }
                // Closing the buffered reader will close all other objects
                bufferedReader.close();
                // when you get an exception you receive a response code:
                // EX: 404 - NOT FOUND
                return xmlResult.toString();
            // when an exception is thrown, java will catch the exception in the following:
            } catch (MalformedURLException e) {
                Log.d(TAG, "downloadXML: the response code was: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "downloadXML: IO exception reading data:" + e.getMessage());
            } catch (SecurityException e){
                Log.e(TAG, "downloadXML: Security Exception needs permission? "+ e.getMessage());
            }

            return null;
        }
    } // ends inner class: DownloadData
} // ends class: MainActivity