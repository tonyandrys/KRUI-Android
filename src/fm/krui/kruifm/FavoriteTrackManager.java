package fm.krui.kruifm;

/**
 * fm.krui.kruifm - FavoriteTrackManager
 *
 * @author Tony Andrys
 *         Created: 08/22/2013
 *         (C) 2013 - Tony Andrys
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * All operations involving storing or retrieving the user's favorite tracks are encapsulated in this class.
 */
public class FavoriteTrackManager {

    final private String TAG = FavoriteTrackManager.class.getName();
    final private String FAVORITE_TRACKS_FILENAME = "krui-favorite-track-arraylist";

    // Constants
    final static public String KEY_FAVORITE_FLAG = "favFlag";
    final private String KEY_ARTIST = "artist";
    final private String KEY_TRACK = "name";
    final private String KEY_ALBUM = "album";

    private ArrayList<HashMap<String, String>> favoriteList = new ArrayList<HashMap<String,String>>();
    private Activity activity;

    public FavoriteTrackManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Retrieves favorite tracks from internal memory. If it does not exist, a blank map will be created.
     * @return HashMap of all favorite tracks, each track being stored in its own HashMap.
     */
    public void loadFavoriteTracks() {
        ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String,String>>();
        File file = new File(activity.getFilesDir(), FAVORITE_TRACKS_FILENAME);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            arrayList = (ArrayList<HashMap<String,String>>)objectInputStream.readObject();
            objectInputStream.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "ERROR WHEN RETRIEVING FAVORITE TRACKS: FileNotFoundException!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ERROR WHEN RETRIEVING FAVORITE TRACKS: ClassNotFoundException!");

            e.printStackTrace();
        } catch (OptionalDataException e) {
            Log.e(TAG, "ERROR WHEN RETRIEVING FAVORITE TRACKS: OptionalDataException!");

            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            Log.e(TAG, "ERROR WHEN RETRIEVING FAVORITE TRACKS: StreamCorruptedException!!");

            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "ERROR WHEN RETRIEVING FAVORITE TRACKS: IOException!");
            e.printStackTrace();
        }
        favoriteList = arrayList;
        Log.v(TAG, favoriteList.size() + " favorite tracks retrieved from memory.");
    }

    /**
     * Writes the favorite tracks to internal memory for later retrieval.
     */
    public void storeFavoriteTracks() {
        File file = new File(activity.getFilesDir(), FAVORITE_TRACKS_FILENAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(favoriteList);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "ERROR WHEN WRITING FAVORITE TRACKS: FileNotFoundException!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "ERROR WHEN WRITING FAVORITE TRACKS: IOException!");
            e.printStackTrace();
        }

        Log.v(TAG, "Stored " + favoriteList.size() + " favorite tracks into memory.");
    }

    /**
     * Adds the currently playing track to the user's favorites.
     */
    public void addTrackToFavorites(Track track) {
        // Ensure the map is in scope before writing to it.
            String trackName = "";
            String artistName = "";
            String albumName = "";

            // Get track name, album, and artist of currently playing track.
            try {
                trackName = track.getTitle();
                artistName = track.getArtist();
                albumName = track.getAlbum();
            } catch (NullPointerException e) {
                Log.e(TAG, "NullPointerException caught when saving favorite track!");
            }

            // Write the information to the track map.
            HashMap<String, String> trackMap = new HashMap<String, String>();
            trackMap.put(KEY_ARTIST, artistName);
            trackMap.put(KEY_ALBUM, albumName);
            trackMap.put(KEY_TRACK, trackName);
            // Add this track to the favorite hashMap.
            favoriteList.add(trackMap);

            Log.v(TAG, "----------");
            Log.v(TAG, "Favorite list is now:");
            for (int i=0; i<favoriteList.size(); i++) {
                HashMap<String, String> map = favoriteList.get(i);
                Log.v(TAG, "** Track " + i);
                Log.v(TAG, "Artist: " + map.get(KEY_ARTIST));
                Log.v(TAG, "Name: " + map.get(KEY_ALBUM));
                Log.v(TAG, "Album: " + map.get(KEY_TRACK));

            }
            Log.v(TAG, "----------");

        }

    /**
     * Removes last added track from favorites.
     */
    public void removeTrackFromFavorites() {
        HashMap<String, String> lastTrack = favoriteList.get(favoriteList.size()-1);

        // Ensure there is something in the list to remove before removing a favorite track.
        if (favoriteList.size()>0) {
            favoriteList.remove(favoriteList.size()-1);
        }

        Log.v(TAG, "----------");
        Log.v(TAG, "Favorite list is now:");
        for (int i=0; i<favoriteList.size(); i++) {
            HashMap<String, String> map = favoriteList.get(i);
            Log.v(TAG, "** Track " + i);
            Log.v(TAG, "Artist: " + map.get(KEY_ARTIST));
            Log.v(TAG, "Name: " + map.get(KEY_ALBUM));
            Log.v(TAG, "Album: " + map.get(KEY_TRACK));
        }
        Log.v(TAG, "----------");
    }

    /**
     * Compares an arbitrary track object to the last favorited track.
     * @param track Track object to compare
     * @return true if tracks match, false if not.
     */
    public boolean compareToLastFavoritedTrack(Track track) {
        Track favTrack = getLatestTrackFromFavorites();

        /* Tracks are identical if their name, artist, and album entries are identical. So,
        * check the passed track's fields against the retrieved track's fields. This comparison
        * can short circuit since all three fields must match to return true. */
        if (!favTrack.getTitle().equals(track.getTitle())) {
            Log.v(TAG, "Titles do not match!");
            Log.v(TAG, "Last favorited title: " + favTrack.getTitle());
            Log.v(TAG, "Track title: " + track.getTitle());
            return false;
        } else if (!favTrack.getArtist().equals(track.getArtist())) {
            Log.v(TAG, "Artists do not match!");
            Log.v(TAG, "Last favorited artist: " + favTrack.getArtist());
            Log.v(TAG, "Track artist: " + track.getArtist());
            return false;
        } else if (!favTrack.getAlbum().equals(track.getAlbum())) {
            Log.v(TAG, "Albums do not match!");
            Log.v(TAG, "Last favorited album: " + favTrack.getAlbum());
            Log.v(TAG, "Track title: " + track.getAlbum());
            return false;
        } else {
            // All fields are identical if we make it this far, so return true.
            return true;
        }

    }

    /**
     * Returns the last track that was favorited by the user.
     * @return HashMap<String, String> Artist/Album/TrackName
     */
    public Track getLatestTrackFromFavorites() {
        HashMap<String, String> lastTrack;
        Track track = new Track();
        try {
            lastTrack = favoriteList.get(favoriteList.size()-1);
            track.setTitle(lastTrack.get(KEY_TRACK));
            track.setAlbum(lastTrack.get(KEY_ALBUM));
            track.setArtist(lastTrack.get(KEY_ARTIST));
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Could not retrieve the user's last favorited track! Do they have any?");
            return null;
        }
        return track;
    }

    /**
     * Removes a specific track from the favorites list.
     * @param i index of track to remove
     */
    public void removeThisTrackFromFavorites(int i) {
        // Ensure the list has enough elements to allow the removal of the requested index.
        if (i<favoriteList.size() && i>=0) {
            favoriteList.remove(i);
        }
        Log.v(TAG, "Removed favorite track at position " + i);
    }

    public ArrayList<HashMap<String,String>> getFavoriteList() {
        return favoriteList;
    }

    /**
     * Reads the value of the favorite flag.
     * @return true if flag is set, false if not
     */
    public static boolean isFavoriteFlagSet(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(StreamService.PREFS_NAME, 0);
        return prefs.getBoolean(KEY_FAVORITE_FLAG, false);
    }

    /**
     * Sets the value of the favorite flag.
     * @param context
     * @param setFavoriteFlag true to set, false to unset
     */
    public static void setFavoriteFlag(Context context, boolean setFavoriteFlag) {
        SharedPreferences prefs = context.getSharedPreferences(StreamService.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FAVORITE_FLAG, setFavoriteFlag);
        editor.commit();
    }

}
