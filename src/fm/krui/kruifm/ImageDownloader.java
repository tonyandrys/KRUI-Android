/*
 * fm.krui.kruifm.ImageDownloader - ImageDownloader.java
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

/**
 * fm.krui.kruifm - ImageDownloader
 *
 * @author Tony Andrys
 *         Created: 08/14/2013
 *         (C) 2013 - Tony Andrys
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Asynchronous image grabber which retrieves an image from a URL as a bitmap. When the image is downloaded, it is
 * passed to the callback listener passed when constructing this object.
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    final private static String TAG = ImageDownloader.class.getName();
    private ImageListener listener;

    /**
     * @param listener ImageDownloader listener to be called on finish.
     */
    public ImageDownloader(ImageListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Bitmap doInBackground(String... params) {

        // Try to download the image at the passed URL.
        Bitmap bitmap = null;
        Log.v(TAG, "ImageDownloader task started!");
        Log.v(TAG, "Trying to download image at " + params[0]);

        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection)new URL(params[0]).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
            Log.v(TAG, "New image downloaded!");
            return bitmap;

        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL detected when downloading image. ");
            e.printStackTrace();

        } catch (IOException e) {
            Log.e(TAG, "Failed to process downloaded image. ");
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // Pass the retrieved image to the listener's callback method if an image was retrieved.
        if (bitmap != null) {
        listener.onImageDownloaded(bitmap);
        } else {
            Log.e(TAG, "WARNING: Bitmap is null, listener callback will NOT be executed.");
        }
    }
}
