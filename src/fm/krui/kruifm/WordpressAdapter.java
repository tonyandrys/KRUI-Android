/*
 * fm.krui.kruifm.WordpressAdapter - WordpressAdapter.java
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
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * fm.krui.kruifm - WordpressAdapter
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */
public class WordpressAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<Article> articleList;

    public WordpressAdapter(Activity activity, ArrayList<Article> articleList) {
        this.activity = activity;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.articleList = articleList;
    }

    @Override
    public int getCount() {
        return articleList.size();
    }

    @Override
    public Object getItem(int position) {
        return articleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the article at this position
        Article article = articleList.get(position);

        ArticleViewHolder articleHolder = new ArticleViewHolder();
        View view;

        // If there is no convertView to use, we have to create one.
        if ((convertView == null) || (convertView.getTag() != articleHolder)) {
            convertView = inflater.inflate(R.layout.article_listrow, null);
            articleHolder = new ArticleViewHolder();
            articleHolder.articleTitleTextView = (TextView)convertView.findViewById(R.id.article_title_textview);
            articleHolder.articleExcerptTextView = (TextView)convertView.findViewById(R.id.article_exerpt_textview);

            convertView.setTag(articleHolder);
            view = convertView;
        }

        // If there IS a convertView, just use its content to save resources.
        else {
            // Get the holder so we can set the data
            articleHolder = (ArticleViewHolder)convertView.getTag();
            view = convertView;
        }

        // Set the contents of the article row.
        articleHolder.articleTitleTextView.setText(Html.fromHtml(article.getTitle()));
        articleHolder.articleExcerptTextView.setText(Html.fromHtml(article.getExcerpt()));

        return view;
    }

    /*
     * Holds location of views to avoid expensive findViewById operations.
     */

    static class ArticleViewHolder {
        TextView articleTitleTextView;
        TextView articleExcerptTextView;

    }
}
