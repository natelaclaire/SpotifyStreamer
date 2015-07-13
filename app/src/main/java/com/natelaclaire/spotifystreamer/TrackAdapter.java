package com.natelaclaire.spotifystreamer;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter for SpotifyTrack objects
 */
public class TrackAdapter extends ArrayAdapter<SpotifyTrack> {
    private static final String LOG_TAG = TrackAdapter.class.getSimpleName();

    private ArrayList<SpotifyTrack> tracks;

    /**
     * Custom constructor
     *
     * @param context   The current context.
     * @param tracks   A List of Track objects to display in a list.
     */
    public TrackAdapter(Activity context, ArrayList<SpotifyTrack> tracks) {
        // The second argument is not going to be used in this adapter,
        // so any value is acceptable.
        super(context, 0, tracks);

        this.tracks = tracks;
    }

    /**
     * Getter for the list of tracks - used when saving instance state
     * @return ArrayList of SpotifyTrack objects
     */
    public ArrayList<SpotifyTrack> getTracks() {
        return tracks;
    }

    /**
     * Provides a view for the AdapterView
     *
     * @param position      The position that is requesting a view.
     * @param convertView   The recycled view to populate.
     * @param parent        The parent viewGroup used for inflation.
     * @return              The view.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpotifyTrack track = getItem(position);

        Log.v(LOG_TAG, track.name);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }

        ImageView trackThumbnailView = (ImageView) convertView.findViewById(R.id.track_thumbnail);
        trackThumbnailView.setImageResource(R.mipmap.track_placeholder);

        // check to see if there is a thumbnail image and load it using Picasso
        if (track.albumImageSmall!=null) {
            Picasso.with(getContext()).load(track.albumImageSmall).into(trackThumbnailView);
        }

        TextView albumNameView = (TextView) convertView.findViewById(R.id.track_album_name);
        albumNameView.setText(track.albumName);

        TextView trackNameView = (TextView) convertView.findViewById(R.id.track_name);
        trackNameView.setText(track.name);

        return convertView;
    }

}
