package fm.krui.kruifm;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * fm.krui.kruifm - TextFetcher
 * @author Tony Andrys
 * @author MrYanDao (authored the logic behind downloading text from a server)
 * http://stackoverflow.com/users/1275347/mryandao
 */

/**
 * Downloads a text file from the internet and stores it locally.
 */
public class TextFetcher extends AsyncTask<Integer, Void, Void> {

    final private String TAG = TextFetcher.class.getName();

    String query; // URL pointing to text file to download
    Activity activity;
    String filename;
    TextListener callback;

    public TextFetcher(Activity activity, String url, String filename, TextListener listener) throws IOException {
        this.query = url;
        this.activity = activity;
        this.filename = filename;
        this.callback = listener;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        File file = new File(activity.getFilesDir(), filename);

        // Thanks to MrYanDao for this method!
        int count;
        try {
            URL url = new URL(query);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            InputStream is = url.openStream();
            FileOutputStream fos = new FileOutputStream(file);
            byte data[] = new byte[1024];
            long total = 0;
            int progress = 0;
            while ((count = is.read(data)) != -1) {
                total += count;
                int progress_temp = (int) total * 100 / lengthOfFile;
                        /*publishProgress("" + progress_temp); //only for asynctask
                        if (progress_temp % 10 == 0 && progress != progress_temp) {
                            progress = progress_temp;
                        }*/
                fos.write(data, 0, count);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            Log.e("ERROR DOWNLOADING",
                    "Unable to download" + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        Log.v(TAG, "Text downloaded!");
        callback.onTextDownloaded();
    }
}
