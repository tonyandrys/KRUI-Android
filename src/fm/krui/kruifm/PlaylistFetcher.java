/*
 * fm.krui.kruifm.PlaylistFetcher - PlaylistFetcher.java
 *
 * (C) 2013 - Tony Andrys
 * http://www.tonyandrys.com
 *
 * Created: 11/14/2013
 *
 * ---
 *
 * This file is part of KRUI.FM.
 *
 * KRUI.FM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * KRUI.FM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KRUI.FM.  If not, see <http://www.gnu.org/licenses/>.
 */

package fm.krui.kruifm;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * fm.krui.kruifm - PlaylistFetcher
 * Created by Tony Andrys on 07/11/{2013}.
 */

/**
 * PlaylistFetcher
 * Returns an arbitrary number of Track objects from the KRUI playlist.
 * @return ArrayList of length songCount containing track information
 */

//TODO: Make the hashMap notification of listview SIGNIFICANTLY more elegant. Kind of pathetic right now.
public class PlaylistFetcher extends AsyncTask<Integer, Void, ArrayList<HashMap<String, Track>>> {

    private static final String TAG = PlaylistFetcher.class.getName();
    protected Activity activity;
    protected PlaylistListener listener;

    public PlaylistFetcher(Activity activity, PlaylistListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected ArrayList<HashMap<String, Track>> doInBackground(Integer... params) {
        Integer trackCount = params[0];
        Log.v(TAG, "Entered PlaylistFetcher...");
        Log.v(TAG, "Track count is " + Integer.toString(trackCount));

        // Get the requested number of tracks from the staff.krui.fm api
        String apiQuery = "http://staff.krui.fm/api/playlist/main/items.json?limit=" + Integer.toString(trackCount);
        JSONArray arr = JSONFunctions.getJSONArrayFromURL(apiQuery);

        // Create Track list
        String dateHolder = "0";
        ArrayList<HashMap<String, Track>> trackList = new ArrayList<HashMap<String, Track>>();
        try {
            for (int i=0; i<arr.length(); i++) {

                // Create map for this entry.
                HashMap<String, Track> trackMap = new HashMap<String, Track>();

                // Retrieve the song object from the JSON array
                JSONArray nestedArr = arr.getJSONArray(i);
                JSONObject obj = nestedArr.getJSONObject(0);
                JSONObject songObj = obj.getJSONObject(Track.KEY_SONG);
                JSONObject userObj = obj.getJSONObject(Track.KEY_USER);

                // Store user characteristics for this play in a DJ object.
                DJ user = new DJ(userObj.optString(DJ.KEY_FIRST_NAME, ""), userObj.optString(DJ.KEY_LAST_NAME, ""));
                user.setUrl(userObj.optString(DJ.KEY_URL));
                user.setBio(userObj.optString(DJ.KEY_BIO));
                user.setTwitter(userObj.optString(DJ.KEY_TWITTER));
                user.setImageURL(userObj.optString(DJ.KEY_IMAGE));

                // Convert datetime to human readable format
                String rawDateTime = songObj.optString(Track.KEY_DATETIME);
                String hrTime = Utils.convertTime(rawDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"), "kk:mm", TimeZone.getTimeZone("America/Chicago"));
                String date = Utils.convertTime(rawDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"), "MM/dd/yy&EEEE", TimeZone.getTimeZone("America/Chicago"));

                // Store song and DJ characteristics into a Track
                Track track = new Track();
                track.setTitle(songObj.optString(Track.KEY_NAME, ""));
                track.setArtist(songObj.optString(Track.KEY_ARTIST, ""));
                track.setAlbum(songObj.optString(Track.KEY_ALBUM, ""));
                track.setRequest(songObj.optBoolean(Track.KEY_REQUEST, false));
                track.setTime(hrTime);
                track.setDate(date);
                track.setPlayedBy(user); //TODO: To save memory, maybe a reference ID is better here?



                // Check if the date has changed since the last track. If so, add a spacer object to allow proper listView functionality.
                if (!track.getDate().equals(dateHolder)) {
                    Log.v(TAG, "Dummy track added at position " + i + "!");
                    Log.v(TAG, "dateHolder (" + dateHolder  + ") does not match current track date of " + track.getDate());

                    // Add a blank entry with a spacer flag to the list BEFORE adding the real track information.
                    HashMap<String, Track> dummyTrackMap = new HashMap<String, Track>();
                    dummyTrackMap.put("track", track);
                    dummyTrackMap.put("spacer", new Track());
                    trackList.add(dummyTrackMap);

                    // Update dateHolder to this date
                    dateHolder = track.getDate();
                    Log.v(TAG, "dateHolder is now " + track.getDate());
                }

                // Add this track to the map
                trackMap.put("track", track);

                //Then add the map to the track list.
                trackList.add(trackMap);
            }

            // When finished, return the completed list.
            return trackList;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to get song info from JSON! Tracklist is NOT fully compiled!");
            e.printStackTrace();
            return trackList;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, Track>> result) {
        Log.v(TAG, "onPostExecute called!");
        listener.onPlaylistFinish(result);

    }

    // If a URL is found, try and download the album art bitmap.
                /*if (!artUrl.equals("")) {

                    Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.noalbumart);

                    // Download the image from URL
                    try {
                        Log.v(TAG, "Downloading album art for " + track.getAlbum());
                        HttpURLConnection connection;
                        connection = (HttpURLConnection)new URL(artUrl).openConnection();
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(input);

                    } catch (MalformedURLException e) {
                        Log.e(TAG, "Malformed URL detected when downloading album art: " + artUrl);
                        e.printStackTrace();
                        // If no album art can be retrieved, display this stupid picture of a music note.
                        bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.noalbumart);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to process album art! ");
                        e.printStackTrace();
                        // If no album art can be retrieved, display this stupid picture of a music note.
                        bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.noalbumart);
                    }

                    // Apply the image to the track
                    track.setAlbumArt(bitmap);

                } else {
                    // If no URL is found, display default album art picture.
                    Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.noalbumart);
                    track.setAlbumArt(bitmap);
                }

                // Art URL Grabber
                                // Try to get album art URL for this track if there is a non-empty artist/album defined...
                if ((!track.getAlbum().equals("")) && (!track.getAlbum().equals("Album"))) {

                    String key  = activity.getString(R.string.lastfm_api_key);
                    Log.v(TAG, "Querying last.fm for art URL (Artist: " + Utils.webEncodeString(track.getArtist()) + " Album: " + Utils.webEncodeString(track.getAlbum())+ ").");
                    String lastFmQuery = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" + key + "&artist=" + Utils.webEncodeString(track.getArtist()) + "&album=" + Utils.webEncodeString(track.getAlbum()) + "&autocorrect=1&format=json";
                    JSONObject lastObj = JSONFunctions.getJSONObjectFromURL(lastFmQuery);
                    String artUrl; // Album Art URL stored here.
                    try {
                        JSONObject album = lastObj.getJSONObject("album");
                        JSONArray imageArray = album.getJSONArray("image");
                        // Art is distributed in small, medium, large, extralarge, and mega
                        JSONObject urlObj = imageArray.getJSONObject(4);
                        // If we make it this far, we have a valid URL, so store it in the track information.
                        artUrl = urlObj.getString("#text");
                        Log.v(TAG, "Got album art for "+ track.getAlbum() +"! URL: " + artUrl);
                        track.setArtUrl(urlObj.optString("#text", ""));
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to retrieve album art from JSON (Params: " + track.getArtist() + ", " + track.getAlbum() + ": ");
                        e.printStackTrace();
                        track.setArtUrl(""); // If we fail, set the URL an empty string.
                    }
                } else {
                    // If artist or album is undefined, set no url.
                    track.setArtUrl("");
                }*/
}
