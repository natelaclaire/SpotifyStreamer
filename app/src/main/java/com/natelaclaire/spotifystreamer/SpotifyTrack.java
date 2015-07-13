package com.natelaclaire.spotifystreamer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Parcelable class to store information about individual tracks
 */
public class SpotifyTrack implements Parcelable {

    public String name;
    public String albumName;
    public Uri albumImageSmall = null;
    public Uri albumImageLarge = null;
    public Uri preview;

    /**
     * Constructor with parameter types that match field types
     * @param name track name
     * @param albumName album name
     * @param albumImageSmall small image URL
     * @param albumImageLarge large image URL
     * @param preview preview URL
     */
    SpotifyTrack(String name, String albumName, Uri albumImageSmall, Uri albumImageLarge, Uri preview) {
        this.name = name;
        this.albumName = albumName;
        this.albumImageSmall = albumImageSmall;
        this.albumImageLarge = albumImageLarge;
        this.preview = preview;
    }

    /**
     * Constructor that accepts all fields as String parameters
     * @param name track name
     * @param albumName album name
     * @param albumImageSmall small image URL
     * @param albumImageLarge large image URL
     * @param preview preview URL
     */
    SpotifyTrack(String name, String albumName, String albumImageSmall, String albumImageLarge, String preview) {
        this(name, albumName, Uri.parse(albumImageSmall), Uri.parse(albumImageLarge), Uri.parse(preview));
    }

    /**
     * Constructor that can take an AlbumSimple object for identifying the album name and images
     * @param name track name
     * @param album AlbumSimple object representing track's album
     * @param preview String containing preview URL
     */
    SpotifyTrack(String name, AlbumSimple album, String preview) {
        this.name = name;
        this.albumName = album.name;
        this.preview = Uri.parse(preview);

        // if the album has images, identify large and small image URLs
        if (album.images!=null && !album.images.isEmpty()) {
            this.albumImageSmall = this.findImageSize(album.images, 200);
            this.albumImageLarge = this.findImageSize(album.images, 640);
        }
    }

    /**
     * Constructor used by CREATOR to recreate object from Parcel
     * @param parcel Parcel object containing the values to restore
     */
    SpotifyTrack(Parcel parcel) {
        this.name = parcel.readString();
        this.albumName = parcel.readString();

        // if no image, Parcel will contain an empty String
        String albumImageSmall = parcel.readString();

        if (albumImageSmall.compareTo("")!=0) {
            this.albumImageSmall = Uri.parse(albumImageSmall);
        }

        String albumImageLarge = parcel.readString();

        if (albumImageLarge.compareTo("")!=0) {
            this.albumImageLarge = Uri.parse(albumImageLarge);
        }

        this.preview = Uri.parse(parcel.readString());
    }

    /**
     * Method to return a Uri for an image. Attempts to return an image with width matching
     * the size parameter. If that size isn't availabe, seeks an image with width within
     * 100 pixels of size parameter. Falls back to using largest image (first in list).
     * @param images List of Image objects from Spotify API
     * @param size target image size
     * @return Uri ubject for the image
     */
    public Uri findImageSize(List<Image> images, int size) {

        // loop through the images, looking for one the requested size and return that if found
        for (Image i : images) {
            if (i.width==size) {
                return Uri.parse(i.url);
            }
        }

        // if exact size wasn't found, look for an image within 100 pixels of the requested size,
        // either larger or smaller
        for (Image i : images) {
            if (i.width >= size - 100 || i.width <= size + 100) {
                return Uri.parse(i.url);
            }
        }

        // last resort, return the largest size, which is the first in the list
        return Uri.parse(images.get(0).url);
    }

    /**
     * Returns String representation of object
     * @return String representation of object
     */
    @Override
    public String toString() {
        return name + ", album " + albumName;
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
        parcel.writeString(this.name);
        parcel.writeString(this.albumName);

        // if album image is null, store empty String
        if (this.albumImageSmall==null) {
            parcel.writeString("");
        } else {
            parcel.writeString(this.albumImageSmall.toString());
        }

        if (this.albumImageLarge==null) {
            parcel.writeString("");
        } else {
            parcel.writeString(this.albumImageLarge.toString());
        }

        parcel.writeString(this.preview.toString());
    }

    public static final Parcelable.Creator<SpotifyTrack> CREATOR = new Parcelable.Creator<SpotifyTrack>() {
        @Override
        public SpotifyTrack createFromParcel(Parcel parcel) {
            return new SpotifyTrack(parcel);
        }

        @Override
        public SpotifyTrack[] newArray(int i) {
            return new SpotifyTrack[i];
        }

    };
}
