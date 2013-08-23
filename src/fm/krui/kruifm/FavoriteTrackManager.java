package fm.krui.kruifm;

/**
 * fm.krui.kruifm - FavoriteTrackManager
 *
 * @author Tony Andrys
 *         Created: 08/22/2013
 *         (C) 2013 - Tony Andrys
 */

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * All operations involving storing or retrieving the user's favorite tracks are encapsulated in this class.
 */
public class FavoriteTrackManager {

    final private String TAG = FavoriteTrackManager.class.getName();
    final private String FAVORITE_TRACKS_FILENAME = "krui-favorite-track-arraylist";

    // HashMap keys
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
            Toast.makeText(activity, "Added " + trackName + " by " + artistName + "to favorites.", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(activity, "Removed " + lastTrack.get(KEY_TRACK) + " by " + lastTrack.get(KEY_ARTIST) + "from favorites.", Toast.LENGTH_SHORT).show();
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

}
