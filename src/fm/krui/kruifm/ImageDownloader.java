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
