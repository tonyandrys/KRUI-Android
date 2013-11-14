/*
 * fm.krui.kruifm.WordpressFetcher - WordpressFetcher.java
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
 * fm.krui.kruifm - WordpressFetcher
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Retrieves Wordpress articles via JSON API.
 */
public class WordpressFetcher extends AsyncTask<Void, Integer, ArrayList<Article>> {

    final private String TAG = WordpressFetcher.class.getName();

    // JSON Keys
    final public String KEY_CATEGORY = "category";
    final public String KEY_POSTS = "posts";
    final public String KEY_TITLE = "title_plain";
    final public String KEY_EXCERPT = "excerpt";
    final public String KEY_URL = "url";
    final public String KEY_AUTHOR = "author";
    final public String KEY_FIRST_NAME = "first_name";
    final public String KEY_LAST_NAME = "last_name";

    String url;
    ArticleDownloadListener listener;

    /**
     * Must be constructed with a URL to a JSON feed to pull articles from.
     * @param url JSON feed location
     */
    public WordpressFetcher(String url, ArticleDownloadListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Article> doInBackground(Void... params) {

        ArrayList<Article> articleList = new ArrayList<Article>();
        int postSize;

        // Get JSON Object from feed passed as URL.
        JSONObject o = JSONFunctions.getJSONObjectFromURL(url);

        try {
            JSONObject categoryObject = o.getJSONObject(KEY_CATEGORY);
            JSONArray a = o.getJSONArray(KEY_POSTS);

            // Extract sub objects and arrays
            for (int i=0; i<o.length(); i++) {
                Article article = new Article();
                JSONObject postObject = a.getJSONObject(i);
                JSONObject authorObject = postObject.getJSONObject(KEY_AUTHOR);
                String title = postObject.getString(KEY_TITLE);
                String excerpt = postObject.getString(KEY_EXCERPT);

                // Add the extracted information to this article object
                article.setTitle(title);
                article.setAuthorObj(authorObject.toString());
                article.setExcerpt(excerpt);
                article.setContentObj(postObject.toString());

                // Add this article to the article list
                articleList.add(article);
                publishProgress(i);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException caught when downloading article list!");
            e.printStackTrace();
        }

        return articleList;
    }

    @Override
    protected void onPostExecute(ArrayList<Article> list) {
        // execute listener callback.
        listener.onArticlesDownloaded(list);
    }

}
