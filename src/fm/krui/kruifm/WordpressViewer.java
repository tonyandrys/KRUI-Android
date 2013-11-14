/*
 * fm.krui.kruifm.WordpressViewer - WordpressViewer.java
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * fm.krui.kruifm - WordpressViewer
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Retrieves content from a wordpress page (url) and displays it as a listview.
 */
public class WordpressViewer extends ListFragment implements ArticleDownloadListener {

    String url;
    View rootView;
    String title;

    public WordpressViewer(String title, String url) {
        this.url = url;
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wordpress_layout, container, false);
        rootView = view;
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Download articles from URL
        showLoadingScreen(true);
        WordpressFetcher fetcher = new WordpressFetcher(url, this);
        fetcher.execute();
    }

    @Override
    public void onArticlesDownloaded(ArrayList<Article> downloadedList) {

        // Kill loading screen
        showLoadingScreen(false);
        WordpressAdapter adapter = new WordpressAdapter(getActivity(), downloadedList);

        // Bind adapter to this list to display its data.
        setListAdapter(adapter);
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);

    }

    @Override
    public void onListItemClick(ListView lv, View view, int position, long id) {

        // Get the article for this position
        Article article = (Article)lv.getAdapter().getItem(position);

        // Pass it to the viewer activity as an intent
        Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
        intent.putExtra(article.ARTICLE_INTENT, article);
        startActivity(intent);

    }

    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)rootView.findViewById(R.id.wordpress_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.INVISIBLE);
        }

    }

}
