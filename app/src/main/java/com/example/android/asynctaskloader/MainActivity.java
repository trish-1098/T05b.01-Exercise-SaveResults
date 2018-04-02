/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.asynctaskloader;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.asynctaskloader.utilities.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{
    private  static final String SEARCH_URL_QUERY_EXTRA = "query";
    private static final String RAW_JSON = "result";

    // TODO (2) Create a static final key to store the search's raw JSON

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    private String githubUrlString;

    private static final int GITHUB_SEARCH_LOADER = 11;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SEARCH_URL_QUERY_EXTRA)){
                mUrlDisplayTextView.setText(savedInstanceState.getString(SEARCH_URL_QUERY_EXTRA));
            }
        }
    }

    /**
     * This method retrieves the search text from the EditText, constructs the
     * URL (using {@link NetworkUtils}) for the github repository you'd like to find, displays
     * that URL in a TextView, and finally fires off an AsyncTask to perform the GET request.
     */
    private void makeGithubSearchQuery() {
        String githubQuery = mSearchBoxEditText.getText().toString();
        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        githubUrlString = githubSearchUrl.toString();
        mUrlDisplayTextView.setText(githubUrlString);
        Bundle inputBundle = new Bundle();
        inputBundle.putCharSequence(SEARCH_URL_QUERY_EXTRA,githubUrlString);

        //For now, otherwise getSupportLoaderManager() must be used
        LoaderManager loaderManager = getLoaderManager();
        Loader<String> loader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if(loader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, inputBundle, this).forceLoad();
        } //else {
            //loaderManager.restartLoader(GITHUB_SEARCH_LOADER,inputBundle,this);
        //}
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the View for the JSON data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showJsonDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the JSON data is visible */
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<String> onCreateLoader(int i, final Bundle bundle) {
        Log.i("InsideonCreateLoader-->","onCreateLoader() called");
        return new GithubQueryTaskLoader(this,bundle);
                /*new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(bundle == null){
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
            }
            @Override
            public String loadInBackground() {
                String githubSearchResults = null;
                URL searchUrl = null;
                String searchUrlString = bundle.getString(SEARCH_URL_QUERY_EXTRA);
                if(searchUrlString == null || TextUtils.isEmpty(searchUrlString)){
                    return null;
                } else {
                    try{
                        searchUrl = new URL(searchUrlString);
                    } catch (MalformedURLException e){
                        e.printStackTrace();
                    }
                }
                try {
                    if(searchUrl != null) {
                        githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return githubSearchResults;
            }
        };*/
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String githubSearchResults) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (githubSearchResults != null && !githubSearchResults.equals("")) {
            showJsonDataView();
            mSearchResultsTextView.setText(githubSearchResults);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
    public static class GithubQueryTaskLoader extends AsyncTaskLoader<String>{
        private Bundle bundle;
        private String githubSearchResults;

        @Override
        public void deliverResult(String githubSearchResults) {
            super.deliverResult(githubSearchResults);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(githubSearchResults == null){
                forceLoad();
            } else {
                deliverResult(githubSearchResults);
            }
            Log.i("Start Loading --->","Inside onStartLoading()");
            //forceLoad();
            //if(bundle == null){
                //return;
            //}
            //mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        public GithubQueryTaskLoader(Context context,Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Override
        public String loadInBackground() {
            Log.i("background ----> ","Inside loadInBackground() method");
            githubSearchResults = null;
            URL searchUrl = null;
            String searchUrlString = bundle.getString(SEARCH_URL_QUERY_EXTRA);
            Log.i("String of URL ---->",searchUrlString);
            if(searchUrlString == null || TextUtils.isEmpty(searchUrlString)){
                return null;
            } else {
                try{
                    searchUrl = new URL(searchUrlString);
                } catch (MalformedURLException e){
                    e.printStackTrace();
                }
            }
            Log.i("URL is ------>",searchUrl+"");
            try {
                if(searchUrl != null) {
                    githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Log.i("Got JSON --------->",githubSearchResults);
            return githubSearchResults;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SEARCH_URL_QUERY_EXTRA,githubUrlString);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLoadingIndicator = null;
    }
}