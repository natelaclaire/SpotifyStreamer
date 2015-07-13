package com.natelaclaire.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * Fragment for Main activity
 */
public class MainActivityFragment extends Fragment {

    private ArtistAdapter artistAdapter;

    public MainActivityFragment() {
    }

    /**
     * Store the List of artists in the Bundle
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artists", artistAdapter.getArtists());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    /**
     * Set up the View
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return root View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<SpotifyArtist> artists;

        // if a saved instance state Bundle was supplied and contains a List of artists,
        // use it; otherwise, create an empty List
        if (savedInstanceState == null || !savedInstanceState.containsKey("artists")) {
            artists = new ArrayList<SpotifyArtist>();
        } else {
            artists = savedInstanceState.getParcelableArrayList("artists");
        }

        // instantiate the ArtistAdapter
        artistAdapter = new ArtistAdapter(
                getActivity(),
                artists
        );

        ListView lv = (ListView)rootView.findViewById(R.id.artist_list);
        lv.setAdapter(artistAdapter);

        // when an item is clicked, we launch the TopTenTracksActivity, providing
        // the SpotifyArtist object in the Intent (it is Parcelable)
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SpotifyArtist artist = artistAdapter.getItem(position);

                Intent artistIntent = new Intent(getActivity(), TopTenTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist);
                startActivity(artistIntent);
            }
        });

        EditText searchBox = (EditText)rootView.findViewById(R.id.artist_search);

        // handle the "Search" key on the soft keyboard
        // based on http://stackoverflow.com/questions/3205339/android-how-to-make-keyboard-enter-button-say-search-and-handle-its-click
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView searchBox, int actionId, KeyEvent event) {

                // execute the search if the user tapped the "Search" key on the soft keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // close the keyboard
                    // based on http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    // populate the list using the entered search string
                    fetchArtists(searchBox);

                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    /**
     * fetch artists that match the search string
     * @param view TextView of the search box
     */
    public void fetchArtists(TextView view) {
        // check to see if the artist_search EditText is empty
        if (!TextUtils.isEmpty(view.getText())) {

            // if not empty, execute the AsyncTask to find the artist
            String artistName = view.getText().toString();

            FetchArtistsTask task = new FetchArtistsTask();
            task.execute(artistName);
        }
    }

    /**
     * AsyncTask to search the Spotify API for the artist name entered
     */
    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        // dialog to show Loading message
        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /**
         * Before executing the search, display Loading dialog
         */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.show();
        }

        /**
         * In the background, query the Spotify API
         * @param strings array containing String representing search query
         * @return List of artists found
         */
        @Override
        protected List<Artist> doInBackground(String... strings) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();

            try {
                // perform query
                ArtistsPager results = service.searchArtists(strings[0]);

                // return list of artists found
                List<Artist> artists = results.artists.items;

                return artists;
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, "Error ", spotifyError);
            }

            return null;

        }

        /**
         * After query completes, replace the current list of artists with the results of the query
         * @param artists List of found artists
         */
        @Override
        protected void onPostExecute(List<Artist> artists) {
            if (artists != null) {
                // clear the current list
                artistAdapter.clear();

                // loop through artists found to add them to the list
                for (Artist a : artists) {
                    Log.v(LOG_TAG, a.name);

                    if (a.images!=null && !a.images.isEmpty()) {
                        // there are images

                        if (a.images.size()>2) {
                            // we prefer not the smallest or largest image, so if there are at least 3
                            // images, take the second to last
                            artistAdapter.add(new SpotifyArtist(
                                    a.id,
                                    a.name,
                                    a.images.get(a.images.size() - 2).url
                            ));
                        } else {
                            // if there are only 1 or 2 images, take the smallest (the last)
                            artistAdapter.add(new SpotifyArtist(
                                    a.id,
                                    a.name,
                                    a.images.get(a.images.size() - 1).url
                            ));
                        }

                    } else {
                        // no images are available
                        artistAdapter.add(new SpotifyArtist(
                                a.id,
                                a.name
                        ));
                    }

                }

                // if no artists were found matching request, show a message in a Toast
                if (artists.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.no_artists_message), Toast.LENGTH_SHORT).show();
                }
            }

            // close Loading dialog
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            super.onPostExecute(artists);
        }
    }
}
