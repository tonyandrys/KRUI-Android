package fm.krui.kruifm;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TrackUpdateHandler extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = TrackUpdateHandler.class.getName();

    /* Constants */
    final private int NO_UPDATE = 0;
    final private int UPDATE_REQUESTED = 1;

    /* Class members */
    TrackUpdateListener listener;
    Activity activity;
    ProgressBar progressBar;
    ImageView albumArtPane;
    ImageView albumArtLoadingPane;

	// Song info storage
	String[] trackInfo;
	String artUrl;
	Bitmap albumArt;
    boolean updateAlbumArt;
    String[] currentTrackInfo;


	public TrackUpdateHandler(TrackUpdateListener l, Activity a, RelativeLayout container, String[] currentTrackInfo, boolean updateAlbumArt) {
		this.listener = l;
		this.activity = a;
		this.progressBar = (ProgressBar)container.findViewById(R.id.album_art_progressbar);
        this.albumArtPane = (ImageView)container.findViewById(R.id.album_art_pane);
        this.albumArtLoadingPane = (ImageView)container.findViewById(R.id.album_art_loading_pane);
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

            // If either the song, artist, or album name is different than the current song playing, we have downloaded a new track and we need to update the UI.
            // If album art update is requested, get the location of the album art and download it.
            if (updateAlbumArt) {
                setLoadingIndicator(true);
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

            // Apply new info to views.
            Log.v(TAG, "Finished. Applying results to views.");

            if (updateAlbumArt) {
                ImageView artView = (ImageView) activity.findViewById(R.id.album_art_pane);
                artView.setImageBitmap(albumArt);
            }

            TextView songNameTextView = (TextView) activity.findViewById(R.id.song_name_textview);
            TextView artistTextView = (TextView) activity.findViewById(R.id.artist_name_textview);
            TextView albumNameTextView = (TextView) activity.findViewById(R.id.album_name_textview);
            songNameTextView.setText(trackInfo[0]);
            artistTextView.setText(trackInfo[1]);
            albumNameTextView.setText(trackInfo[2]);
            setLoadingIndicator(false);

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
		String key  = activity.getString(R.string.lastfm_api_key);
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
		bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.krui_background_logo);
		return bitmap;
	}

    /**
     * Enables and disables the loading indicator on the UI thread.
     * @param showLoadingIndicator True to enable, false to disable.
     */
    private void setLoadingIndicator(final boolean showLoadingIndicator) {

        Thread t = new Thread(){
            public void run(){
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (showLoadingIndicator) {
                            // Show loading indicators
                            progressBar.setVisibility(View.VISIBLE);
                            albumArtLoadingPane.setVisibility(View.VISIBLE);
                            albumArtPane.setVisibility(View.INVISIBLE);

                        } else {
                            // Hide progressBar and placeholder image and re-show album art pane.
                            progressBar.setVisibility(View.INVISIBLE);
                            albumArtLoadingPane.setVisibility(View.INVISIBLE);
                            albumArtPane.setVisibility(View.VISIBLE);

                        }
                    }
                });
            }
        };
        t.start();
    }



}
