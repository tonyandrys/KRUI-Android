/*
 * fm.krui.kruifm.TextFetcher - TextFetcher.java
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
 * Downloads text files from the internet and stores them locally.
 */
public class TextFetcher extends AsyncTask<Void, Void, Void> {

    final private String TAG = TextFetcher.class.getName();

    String[] urls; // URLs pointing to text files to download
    Activity activity;
    String[] filenames;
    TextListener callback;

    public TextFetcher(Activity activity, String[] urls, String[] filenames, TextListener listener) throws IOException {
        this.urls = urls;
        this.activity = activity;
        this.filenames = filenames;
        this.callback = listener;
    }

    @Override
    protected Void doInBackground(Void... Voids) {
        Log.v(TAG, "Filenames.length = " + filenames.length);
        for (int i=0; i<filenames.length; i++) {
            File file = new File(activity.getFilesDir(), filenames[i]);

            // Thanks to MrYanDao for this method!
            int count;
            try {
                URL url = new URL(urls[i]);
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
                Log.v(TAG, "Text downloaded from " + filenames[i]+ "!");
                Log.v(TAG, "Stored as: " + filenames[i]);
            } catch (Exception e) {
                Log.e(TAG, "Unable to download text file: " + e.getMessage());
            }
            Log.v(TAG, "For loop iterated!");
        }
        Log.v(TAG, "Returning null.");
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        Log.v(TAG, "Calling onTextDownloaded!");
        callback.onTextDownloaded();
    }
}
