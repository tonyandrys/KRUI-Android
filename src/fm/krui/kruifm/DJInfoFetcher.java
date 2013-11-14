/*
 * fm.krui.kruifm.DJInfoFetcher - DJInfoFetcher.java
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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * fm.krui.kruifm - DJInfoFetcher
 * Created by Tony Andrys on 07/14/{2013}.
 */
public class DJInfoFetcher extends AsyncTask<Void, Void, DJ> {

    private final String TAG = DJInfoFetcher.class.getName();

    private Activity activity;
    private DJInfoListener callback;
    private ProgressDialog pd;
    private final int RECORD_COUNT = 1;

    public DJInfoFetcher(Activity activity, DJInfoListener callback) {
        this.activity = activity;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {

        // Create ProgressDialog and display during network request
        String pdTitle = activity.getResources().getString(R.string.loading_dj_information);
        String pdMessage = activity.getResources().getString(R.string.please_wait);
        pd = new ProgressDialog(activity);
        pd.setTitle(pdTitle);
        pd.setMessage(pdMessage);
        pd.show();
    }

    @Override
    protected DJ doInBackground(Void... params) {
        Log.v(TAG, "Entering DJInfo doInBackground");
        // Get DJ information from the latest track update, so we only need to retrieve one record.
        String apiQuery = "http://staff.krui.fm/api/playlist/main/items.json?limit=" + Integer.toString(RECORD_COUNT);
        JSONArray arr = JSONFunctions.getJSONArrayFromURL(apiQuery);
        DJ dj = new DJ();
        try {
            // Hardcoded numbers are lame, but the staff.krui.fm api isn't the cleanest...
            JSONArray nestedArray = arr.getJSONArray(0);
            JSONObject o = nestedArray.getJSONObject(0);

            // Get the user JSON object
            JSONObject userObj = o.getJSONObject(Track.KEY_USER);

            // Add retrieved data to DJ object.
            dj.setFirstName(userObj.getString(DJ.KEY_FIRST_NAME));
            Log.v(TAG, "Got first name: " +userObj.getString(DJ.KEY_FIRST_NAME));
            dj.setLastName(userObj.getString(DJ.KEY_LAST_NAME));
            Log.v(TAG, "Got last name: " +userObj.getString(DJ.KEY_LAST_NAME));
            dj.setUrl(userObj.getString(DJ.KEY_URL));
            Log.v(TAG, "Got URL: " +userObj.getString(DJ.KEY_URL));
            dj.setBio(userObj.getString(DJ.KEY_BIO));
            Log.v(TAG, "Got bio: " +userObj.getString(DJ.KEY_BIO));
            dj.setTwitter(userObj.getString(DJ.KEY_TWITTER));
            Log.v(TAG, "Got twitter: " +userObj.getString(DJ.KEY_TWITTER));
            dj.setImageURL(userObj.getString(DJ.KEY_IMAGE));
            Log.v(TAG, "Got image URL: " +userObj.getString(DJ.KEY_IMAGE));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the DJ object.
        return dj;
    }

    @Override
    protected void onPostExecute(DJ result) {
        pd.dismiss();

        // Send the DJ object to the previous activity via callback method
        callback.onFinish(result);
    }

}
