package com.natelaclaire.spotifystreamer;

import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * Fragment for the Top 10 Tracks activitys
 */
public class TopTenTracksActivityFragment extends Fragment {

    private final String LOG_TAG = TopTenTracksActivityFragment.class.getSimpleName();

    private TrackAdapter trackAdapter;
    private SpotifyArtist artist;

    public TopTenTracksActivityFragment() {
    }

    /**
     * Store the List of tracks in the Bundle
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", trackAdapter.getTracks());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    /**
     * Set up the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return root View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);
        Intent intent = getActivity().getIntent();

        ArrayList<SpotifyTrack> tracks;

        // if a saved instance state Bundle was supplied and contains a List of tracks,
        // use it; otherwise, create an empty List
        if (savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            tracks = new ArrayList<SpotifyTrack>();
        } else {
            tracks = savedInstanceState.getParcelableArrayList("tracks");
        }

        // instantiate the TrackAdapter
        trackAdapter = new TrackAdapter(
                getActivity(),
                tracks
        );

        ListView lv = (ListView)rootView.findViewById(R.id.track_list);
        lv.setAdapter(trackAdapter);

        // when an item is clicked on, we're currently just showing a Toast,
        // to be fixed in part 2
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SpotifyTrack track = trackAdapter.getItem(position);

                Toast.makeText(getActivity(), track.name, Toast.LENGTH_SHORT).show();
            }
        });

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            // the intent contains the Spotify artist object, so use the ID to execute the task
            // that queries the Spotify API
            artist = intent.getParcelableExtra(Intent.EXTRA_TEXT);

            Log.v(LOG_TAG, artist.id);
            FetchTracksTask task = new FetchTracksTask();
            task.execute(artist.id);

            // use the artist's name as the activity's subtitle
            ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
            actionBar.setSubtitle(artist.name);
        } else {
            Log.v(LOG_TAG, "No Artist ID supplied");
        }

        return rootView;
    }

    /**
     * AsyncTask to query the Spotify API for artist's top 10 tracks
     */
    public class FetchTracksTask extends AsyncTask<String, Void, List<Track>> {

        private final String LOG_TAG = FetchTracksTask.class.getSimpleName();

        // dialog to show Loading message
        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /**
         * Before executing the task, display Loading dialog
         */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.show();
        }

        /**
         * Query the Spotify API for artist's top 10 tracks in the background
         * @param strings array containing artist's Spotify ID
         * @return List of found Tracks
         */
        @Override
        protected List<Track> doInBackground(String... strings) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();

            // load country from shared preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String country = sharedPref.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default));

            try {
                // getArtistTopTrack method requires the country, which must be supplied through
                // a Map object
                Map<String, Object> options = new HashMap();
                options.put("country", country);

                // perform query
                Tracks results = service.getArtistTopTrack(strings[0], options);

                // return tracks found
                List<Track> tracks = results.tracks;

                return tracks;
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, "Error ", spotifyError);
            }

            return null;

        }

        /**
         * After the query has run, update the custom array adapter
         * @param tracks List of found Tracks
         */
        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null) {
                // remove all previous tracks in the List
                trackAdapter.clear();

                // loop through found tracks, adding them to the List
                for (Track t : tracks) {
                    Log.v(LOG_TAG, t.name);
                    trackAdapter.add(new SpotifyTrack(
                            t.name,
                            t.album,
                            t.preview_url
                    ));
                }

                // notify the user if no tracks were found
                if (tracks.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.no_tracks_message), Toast.LENGTH_SHORT).show();
                }
            }

            // close Loading dialog
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            super.onPostExecute(tracks);
        }
    }
}
