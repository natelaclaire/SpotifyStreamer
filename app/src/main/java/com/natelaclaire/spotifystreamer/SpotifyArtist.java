package com.natelaclaire.spotifystreamer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable class to store information about individual artists
 */
public class SpotifyArtist implements Parcelable {

    public String id;
    public String name;
    public Uri photo = null;

    /**
     * Constructor accepting String representations of all fields - used so that task doesn't have
     * to deal with parsing the Uri
     * @param id Spotify artist ID
     * @param name Artist name
     * @param photo URL of artist photo
     */
    public SpotifyArtist(String id, String name, String photo) {
        this(id, name, Uri.parse(photo));
    }

    /**
     * Constructor with parameters that match field types
     * @param id Spotify artist ID
     * @param name Artist name
     * @param photo URL of artist photo (Uri object)
     */
    public SpotifyArtist(String id, String name, Uri photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }

    /**
     * Constructor for artists with no photo
     * @param id Spotify artist ID
     * @param name artist name
     */
    public SpotifyArtist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor used by CREATOR to recreate object from Parcel
     * @param parcel Parcel object containing the values to restore
     */
    public SpotifyArtist(Parcel parcel) {
        this.id = parcel.readString();
        this.name = parcel.readString();

        // if no photo, Parcel will contain an empty String
        String photo = parcel.readString();

        if (photo.compareTo("")!=0) {
            this.photo = Uri.parse(photo);
        }
    }

    /**
     * Returns String representation of object
     * @return String representation of object
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Not used
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write field values to Parcel
     * @param parcel
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeString(this.name);

        // if artist photo is null, store empty String
        if (this.photo==null) {
            parcel.writeString("");
        } else {
            parcel.writeString(this.photo.toString());
        }

    }

    public static final Parcelable.Creator<SpotifyArtist> CREATOR = new Parcelable.Creator<SpotifyArtist>() {
        @Override
        public SpotifyArtist createFromParcel(Parcel parcel) {
            return new SpotifyArtist(parcel);
        }

        @Override
        public SpotifyArtist[] newArray(int i) {
            return new SpotifyArtist[i];
        }

    };
}
