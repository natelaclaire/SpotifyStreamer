package com.natelaclaire.spotifystreamer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter for SpotifyArtist objects
 */
public class ArtistAdapter extends ArrayAdapter<SpotifyArtist> {
    private static final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    private ArrayList<SpotifyArtist> artists;

    /**
     * Custom constructor
     *
     * @param context   The current context.
     * @param artists   A List of Artist objects to display in a list.
     */
    public ArtistAdapter(Activity context, ArrayList<SpotifyArtist> artists) {
        // The second argument is not going to be used in this adapter,
        // so any value is acceptable.
        super(context, 0, artists);

        this.artists = artists;
    }

    /**
     * Getter for the list of artists - used when saving instance state
     * @return ArrayList of SpotifyArtist objects
     */
    public ArrayList<SpotifyArtist> getArtists() {
        return artists;
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
        SpotifyArtist artist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        ImageView artistPhotoView = (ImageView) convertView.findViewById(R.id.artist_photo);
        artistPhotoView.setImageResource(R.mipmap.artist_placeholder);

        // check to see if there is an image and load it with Picasso
        if (artist.photo!=null) {
            Picasso.with(getContext()).load(artist.photo).into(artistPhotoView);
        }


        TextView artistNameView = (TextView) convertView.findViewById(R.id.artist_name);
        artistNameView.setText(artist.name);

        return convertView;
    }

}
