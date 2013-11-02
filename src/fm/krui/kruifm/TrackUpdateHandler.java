package fm.krui.kruifm;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TrackUpdateHandler extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = TrackUpdateHandler.class.getName();

    final public static String ALBUM_ART_FILENAME = "current_track_album_art";

    /* Constants */
    final private int NO_UPDATE = 0;
    final private int UPDATE_REQUESTED = 1;

    /* Class members */
    TrackUpdateListener listener;

	// Song info storage
	String[] trackInfo;
	String artUrl;
	Bitmap albumArt;
    boolean updateAlbumArt;
    String[] currentTrackInfo;
    Context context;


	public TrackUpdateHandler(Context context, TrackUpdateListener l, String[] currentTrackInfo, boolean updateAlbumArt) {
        this.context = context;
		this.listener = l;
        this.currentTrackInfo = currentTrackInfo;
        this.updateAlbumArt = updateAlbumArt;
	}
	
	@Override
	protected void onPreExecute() {
        Log.v(TAG, "Track Update Handler started!");
	}

	@Override
	protected Integer doInBackground(Void... arg0) {

		// Get song info and album art URL
		try {
            trackInfo = getSongInfo();
            Log.v(TAG, "---");
            Log.v(TAG, "Returned track name = " + trackInfo[0]);
            Log.v(TAG, "Returned track artist = " + trackInfo[1]);
            Log.v(TAG, "Returned track album = " + trackInfo[2]);
            Log.v(TAG, "---");
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException thrown when getting song info!");
            e.printStackTrace();
        }

        // Check if the returned info is a new track or the same as the previous track
        if ((!trackInfo[0].equals(currentTrackInfo[0])) || (!trackInfo[1].equals(currentTrackInfo[1])) || (!trackInfo[2].equals(currentTrackInfo[2]))) {
            Log.v(TAG, "Song information has changed. UI will be updated.");

            // UI UPDATE HERE
            listener.broadcastMessage(StreamService.BROADCAST_COMMAND_UPDATE_PENDING);

            // If either the song, artist, or album name is different than the current song playing, we have downloaded a new track and we need to update the UI.
            // If album art update is requested, get the location of the album art and download it.
            if (updateAlbumArt) {
                artUrl = getAlbumArtURL(trackInfo[1], trackInfo[2]);
                Log.v(TAG, "Returned album art = " + artUrl);
                Log.v(TAG, "Grabbing album art from " + artUrl);
                albumArt = downloadAlbumArt(artUrl);
            } else {
                Log.v(TAG, "User has elected to not download album art. Skipping...");
            }

        } else {
            // If the information is the same, don't waste resources updating information with itself.
            Log.v(TAG, "Song information is equivalent to the song currently playing. No UI update necessary.");
            return NO_UPDATE;
        }

        return UPDATE_REQUESTED;
	}

	protected void onPostExecute(Integer result) {

        if (result == UPDATE_REQUESTED) {

            // Write the new track information to SharedPrefs
            SharedPreferences prefs = context.getSharedPreferences(StreamService.PREFS_NAME, 0);
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString(StreamService.PREFKEY_TRACK, trackInfo[0]);
            prefEditor.putString(StreamService.PREFKEY_ARTIST, trackInfo[1]);
            prefEditor.putString(StreamService.PREFKEY_ALBUM, trackInfo[2]);
            prefEditor.commit();

            if (updateAlbumArt) {
                writeAlbumArtToFile(albumArt);
            }

            // Execute callback method of listener.
            listener.onTrackUpdate();
        }

	}

	/**
	 * Returns album art location for the passed song using the last.fm api and the artist/album name.
	 * @arg artist Artist of the song passed as a string.
	 * @arg albumName Name of the album/compilation the song appears on as a string.
	 * @return Album Art URL string. 
	 */
	protected String getAlbumArtURL(String artist, String albumName) {
		String key  = context.getString(R.string.lastfm_api_key);
		Log.v(TAG, "Web encoded artist: " + Utils.webEncodeString(artist));
		Log.v(TAG, "Web encoded album: " + Utils.webEncodeString(albumName));
		String apiQuery = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" + key + "&artist=" + Utils.webEncodeString(artist) + "&album=" + Utils.webEncodeString(albumName) + "&autocorrect=1&format=json";
		JSONObject obj = JSONFunctions.getJSONObjectFromURL(apiQuery);
		try {
			// TODO: Give user ability to stop downloading album art, or download at lower resolution.
			JSONObject album = obj.getJSONObject("album");
			JSONArray imageArray = album.getJSONArray("image");
			// Art is distributed in small, medium, large, extralarge, and mega
			JSONObject megaObj = imageArray.getJSONObject(4);
			String artUrl = megaObj.getString("#text");
			return artUrl;
		} catch (JSONException e) {
			Log.e(TAG, "Failed to retrive album art from JSON (Params: " + artist + ", " + albumName + ": ");
			e.printStackTrace();
			return "*NO_ART*";
		}
	}

	/**
	 * Gets the last played song from the staff.krui.fm playlist via its api.
	 * @return Artist and album name as a string array.
	 * Array format: { track name, artist name, album name }
	 */
	protected String[] getSongInfo() {
		String apiQuery = "http://staff.krui.fm/api/playlist/main/items.json?limit=1";
		JSONArray arr = JSONFunctions.getJSONArrayFromURL(apiQuery);
		try {
			JSONArray nestedArr = arr.getJSONArray(0);
			JSONObject obj = nestedArr.getJSONObject(0);
			JSONObject songObj = obj.getJSONObject("song");
			String[] info = { songObj.getString("name"), songObj.getString("artist"), songObj.getString("album") };
			return info;
		} catch (JSONException e) {
			Log.e(TAG, "Failed to get song info from JSON: ");
			e.printStackTrace();
			String[] empty = {};
			return empty;
		}

	}

	/**
	 * Downloads album art from passed URL using the last.fm api.
	 * @param url Web safe location of image as URL.
	 * @return Bitmap of the image requested
	 */
	protected Bitmap downloadAlbumArt(String url) {

		Bitmap bitmap;
		
		// Check if an album art URL was returned. If there's no location, there's no point in wasting resources trying to download nothing.
		if (artUrl.equals("*NO_ART*") == false) {
			
			// Download the image from URL
			try {
				HttpURLConnection connection;
				connection = (HttpURLConnection)new URL(url).openConnection();
				connection.connect();
				InputStream input = connection.getInputStream();
				bitmap = BitmapFactory.decodeStream(input);
				return bitmap;

			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL detected when downloading album art: ");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "Failed to process album art: ");
				e.printStackTrace();
			}
		}

		// If the download fails or image doesn't exist, display the default KRUI background image.
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.krui_background_logo);
		return bitmap;
	}

    /**
     * Writes downloaded album art to file storage
     * @param bitmap Album Art image as Bitmap
     */
    private void writeAlbumArtToFile(Bitmap bitmap) {
        File file = new File(context.getFilesDir(), ALBUM_ART_FILENAME);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            Log.v(TAG, "Album art successfully written to disk!");
            out.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "WARNING: Album art could not be saved to disk. FileNotFoundException.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "WARNING: Album art could not be saved to disk. IOException.");
            e.printStackTrace();
        }
    }

}
